package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FlightAction extends AAction {
    private final BooleanFlag setFly = new BooleanFlag(true, "setFlying");
    private final BooleanFlag startFly = new BooleanFlag(false, "startFly");

    protected FlightAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_FLIGHT_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ALLOW_NAME),
                MinigameMessageManager.getMgMessage(
                        setFly.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED),
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ENABLE_NAME),
                MinigameMessageManager.getMgMessage(
                        startFly.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED)
        );
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
        if (mgPlayer != null) {
            execute(mgPlayer);
        }
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);
        execute(mgPlayer);
    }

    private void execute(@NotNull MinigamePlayer player) {
        player.setCanFly(setFly.getFlag());
        if (setFly.getFlag()) {
            player.getPlayer().setFlying(startFly.getFlag());
        }
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        setFly.saveValue(config, path);
        startFly.saveValue(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        setFly.loadValue(config, path);
        startFly.loadValue(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, @NotNull Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        m.addItem(setFly.getMenuItem(Material.FEATHER, MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ALLOW_NAME));
        m.addItem(startFly.getMenuItem(Material.FEATHER, MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ENABLE_NAME,
                MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ENABLE_DESCRIPTION));
        m.displayMenu(mgPlayer);
        return true;
    }
}
