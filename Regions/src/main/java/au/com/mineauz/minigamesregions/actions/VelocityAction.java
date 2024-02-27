package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.FloatFlag;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Main;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class VelocityAction extends AAction {
    private final FloatFlag x = new FloatFlag(0f, "xv");
    private final FloatFlag y = new FloatFlag(5f, "yv");
    private final FloatFlag z = new FloatFlag(0f, "zv");

    protected VelocityAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_VELOCITY_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_VELOCITY_NAME),
                MinigameMessageManager.getMgMessage(MinigameLangKey.POSITION,
                        Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_X.getKey(), String.valueOf(x.getFlag())),
                        Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_Y.getKey(), String.valueOf(y.getFlag())),
                        Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_Z.getKey(), String.valueOf(z.getFlag()))));
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
        debug(mgPlayer, region);
        execute(mgPlayer);
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);
        execute(mgPlayer);
    }

    private void execute(final MinigamePlayer player) {
        if (player == null) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> player.getPlayer().setVelocity(new Vector(x.getFlag(), y.getFlag(), z.getFlag())));
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        x.saveValue(config, path);
        y.saveValue(config, path);
        z.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        x.loadValue(config, path);
        y.loadValue(config, path);
        z.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        m.addItem(x.getMenuItem(Material.STONE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_VELOCITY_X_NAME), 0.5d, 1d, null, null));
        m.addItem(y.getMenuItem(Material.STONE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_VELOCITY_Y_NAME), 0.5d, 1d, null, null));
        m.addItem(z.getMenuItem(Material.STONE, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_VELOCITY_Z_NAME), 0.5d, 1d, null, null));
        m.displayMenu(mgPlayer);
        return true;
    }
}
