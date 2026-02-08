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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class FlightAction extends AAction {
    private final BooleanFlag setFly = new BooleanFlag("setFlying", true);
    private final BooleanFlag startFly = new BooleanFlag("startFly", false);

    protected FlightAction(final @NotNull Key key) {
        super(key);
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
    public void saveArguments(@NotNull CommentedConfigurationNode config) throws SerializationException {
        setFly.saveValue(config);
        startFly.saveValue(config);
    }

    @Override
    public void loadArguments(@NotNull CommentedConfigurationNode config) {
        setFly.loadValue(config);
        startFly.loadValue(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(setFly.getMenuItem(ItemType.FEATHER, MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ALLOW_NAME));
        menu.addItem(startFly.getMenuItem(ItemType.FEATHER, MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ENABLE_NAME,
                MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ENABLE_DESCRIPTION));
        menu.displayMenu();
        return true;
    }
}
