package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.events.EndedMinigameEvent;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.script.ScriptObject;
import au.com.mineauz.minigamesregions.*;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.triggers.MgRegTrigger;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This Action trips {@link MgRegTrigger#REMOTE_TIMED} in a region or a node Applicable to nodes Uses the
 * {@link org.bukkit.scheduler.BukkitScheduler} scheduler for threading.
 *
 * @author <a href="https://github.com/Turidus/Minigames">Turidus</a>
 */
public class TimedTriggerAction extends AAction implements Listener {
    private final StringFlag toTrigger = new StringFlag("toTrigger", "None");
    private final BooleanFlag isRegion = new BooleanFlag("isRegion", false);
    private final TimeFlag delay = new TimeFlag("delay", 20L);
    /*
     * The AAction Object is created once per minigame,
     * but at the time of creation we don't know which minigame we belong to yet.
     * So this Map contains all tasks of all minigames
     */
    private static final @NotNull Map<@NotNull Minigame, @NotNull Collection<BukkitTask>> globalTasks = new HashMap<>();

    protected TimedTriggerAction(@NotNull String name) {
        super(name);

        Bukkit.getPluginManager().registerEvents(this, Main.getPlugin());
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TIMEDTRIGGER_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.REMOTE;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TIMEDTRIGGER_NAME_NAME),
                Component.text(toTrigger.getFlag()),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TIMEDTRIGGER_ISREGION_NAME),
                MinigameMessageManager.getMgMessage(isRegion.getFlag() ? MgMiscLangKey.BOOL_TRUE : MgMiscLangKey.BOOL_FALSE),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TIMEDTRIGGER_DELAY_NAME),
                MinigameUtils.convertTime(Duration.ofSeconds(delay.getFlag() / 20)));
    }

    @Override
    public boolean useInRegions() {
        return true;
    }

    @Override
    public boolean useInNodes() {
        return true;
    }

    @Override
    public void executeRegionAction(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        execute(mgPlayer, region);
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        execute(mgPlayer, node);
    }

    private void execute(@Nullable MinigamePlayer player, @NotNull ScriptObject obj) {
        debug(player, obj);
        if (player == null || !player.isInMinigame()) {
            return;
        }
        Minigame mg = player.getMinigame();
        if (mg == null) {
            return;
        }
        RegionModule rMod = RegionModule.getMinigameModule(mg);
        if ((isRegion.getFlag() && !rMod.hasRegion(toTrigger.getFlag())
                || (!isRegion.getFlag() && !rMod.hasNode(toTrigger.getFlag())))) {
            return;
        }
        final ExecutableScriptObject toExecute = isRegion.getFlag() ? rMod.getRegion(toTrigger.getFlag()) : rMod.getNode(toTrigger.getFlag());
        final TaskHolder taskHolder = new TaskHolder();

        taskHolder.task = Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                toExecute.execute(MgRegTrigger.REMOTE_TIMED, player);

                globalTasks.remove(taskHolder.task);
        }, delay.getFlag());

        globalTasks.computeIfAbsent(mg, ignored -> new ArrayList<>()).add(taskHolder.task);
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        toTrigger.saveValue(config, path);
        isRegion.saveValue(config, path);
        delay.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        toTrigger.loadValue(config, path);
        isRegion.loadValue(config, path);
        delay.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, @NotNull Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        m.addItem(toTrigger.getMenuItem(Material.ENDER_EYE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TIMEDTRIGGER_NAME_NAME)));
        m.addItem(isRegion.getMenuItem(Material.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TIMEDTRIGGER_ISREGION_NAME)));
        m.addItem(delay.getMenuItem(Material.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TIMEDTRIGGER_DELAY_NAME), 0L, null));
        m.displayMenu(mgPlayer);
        return true;
    }

    @EventHandler
    protected void onGameEnd (final @NotNull EndedMinigameEvent event) {
        final @Nullable Collection<BukkitTask> tasks = globalTasks.remove(event.getMinigame());

        if (tasks != null) {
            for (BukkitTask task : tasks) {
                if (!task.isCancelled()) {
                    task.cancel();
                }
            }
        }
    }

    // Java being java and being too strict in lambdas
    protected static class TaskHolder {
        BukkitTask task = null;
    }
}
