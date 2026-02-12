package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
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
        return MessageManager.getMessage(RegionLangKey.MENU_ACTION_FLIGHT_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(MessageManager.getMessage(MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ALLOW_NAME),
                MessageManager.getMessage(
                        setFly.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED),
                MessageManager.getMessage(MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ENABLE_NAME),
                MessageManager.getMessage(
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
    public void executeRegionAction(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        debug(mgPlayer, region);
        if (mgPlayer != null) {
            execute(mgPlayer);
        }
    }

    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        debug(mgPlayer, node);
        execute(mgPlayer);
    }

    private void execute(final @NotNull MinigamePlayer mgPlayer) {
        mgPlayer.setCanFly(setFly.getFlag());
        if (setFly.getFlag()) {
            mgPlayer.getPlayer().setFlying(startFly.getFlag());
        }
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        setFly.saveValue(config);
        startFly.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        setFly.loadValue(config);
        startFly.loadValue(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), previous.getIntendedViewer());
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);
        menu.addItem(setFly.getMenuItem(ItemType.FEATHER, MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ALLOW_NAME));
        menu.addItem(startFly.getMenuItem(ItemType.FEATHER, MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ENABLE_NAME,
                MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ENABLE_DESCRIPTION));
        menu.displayMenu();
        return true;
    }
}
