package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;

/**
 * This Action trips {@link MgRegTrigger#REMOTE_TIMED} in a region or a node Applicable to nodes Uses the
 * {@link org.bukkit.scheduler.BukkitScheduler} scheduler for threading.
 *
 * @author <a href="https://github.com/Turidus/Minigames">Turidus</a>
 */
public class TimedTriggerAction extends AAction {
    private final StringFlag toTrigger = new StringFlag("None", "toTrigger");
    private final BooleanFlag isRegion = new BooleanFlag(false, "isRegion");
    private final TimeFlag delay = new TimeFlag(20L, "delay");

    protected TimedTriggerAction(@NotNull String name) {
        super(name);
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
                MinigameMessageManager.getMgMessage(isRegion.getFlag() ? MinigameLangKey.BOOL_TRUE : MinigameLangKey.BOOL_FALSE),
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
        ExecutableScriptObject toExecute = isRegion.getFlag() ? rMod.getRegion(toTrigger.getFlag()) : rMod.getNode(toTrigger.getFlag());
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> toExecute.execute(MgRegTrigger.REMOTE_TIMED, player), delay.getFlag());
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
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        m.addItem(toTrigger.getMenuItem(Material.ENDER_EYE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TIMEDTRIGGER_NAME_NAME)));
        m.addItem(isRegion.getMenuItem(Material.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TIMEDTRIGGER_ISREGION_NAME)));
        m.addItem(delay.getMenuItem(Material.ENDER_PEARL, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_TIMEDTRIGGER_DELAY_NAME), 0L, null));
        m.displayMenu(mgPlayer);
        return true;
    }
}
