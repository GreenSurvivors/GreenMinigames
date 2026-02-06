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

public class SetEnabledAction extends AAction {
    private final BooleanFlag state = new BooleanFlag("state", false);

    protected SetEnabledAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SETENABLED_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.REGION_NODE;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_MINIGAME_ENABLED_NAME),
                MinigameMessageManager.getMgMessage(state.getFlag() ? MgCommandLangKey.COMMAND_STATE_ENABLED : MgCommandLangKey.COMMAND_STATE_DISABLED));
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
        region.setEnabled(state.getFlag());
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        debug(mgPlayer, node);
        node.setEnabled(state.getFlag());
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        state.saveValue(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        state.loadValue(config);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, @NotNull Menu previous) {
        Menu m = new Menu(3, getDisplayname(), mgPlayer);
        m.addItem(new MenuItemBack(previous), m.getSize() - 9);
        m.addItem(state.getMenuItem(ItemType.ENDER_PEARL, MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_MINIGAME_ENABLED_NAME)));
        m.displayMenu(mgPlayer);
        return true;
    }
}
