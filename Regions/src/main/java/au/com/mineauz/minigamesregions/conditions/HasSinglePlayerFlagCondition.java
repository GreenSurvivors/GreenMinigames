package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class HasSinglePlayerFlagCondition extends ACondition { // the whole singleplayer flag system is unused.
    private final StringFlag flagName = new StringFlag("flag", "flag");

    protected HasSinglePlayerFlagCondition(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_HASSINGLEPLAYERFLAG_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_HASSINGLEPLAYERFLAG_FLAG_NAME), Component.text(flagName.getFlag()));
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
    public boolean checkRegionCondition(@Nullable MinigamePlayer mgPlayer, @NotNull Region region) {
        return checkCondition(mgPlayer);
    }

    @Override
    public boolean checkNodeCondition(@Nullable MinigamePlayer mgPlayer, @NotNull Node node) {
        return checkCondition(mgPlayer);
    }

    private boolean checkCondition(@Nullable MinigamePlayer player) {
        if (player == null) {
            return false;
        }
        return true;//player.hasFlag(flagName.getFlag());
    }

    @Override
    public void saveArguments(@NotNull CommentedConfigurationNode config) throws SerializationException {
        flagName.saveValue(config);
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(@NotNull CommentedConfigurationNode config) {
        flagName.loadValue(config);
        loadInvert(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, getDisplayName(), prev.getIntendedViewer());
        menu.addItem(new MenuItemBack(prev), menu.getSize() - 9);
        menu.addItem(flagName.getMenuItem(ItemType.NAME_TAG, RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_HASSINGLEPLAYERFLAG_FLAG_NAME)));
        addInvertMenuItem(menu);
        menu.displayMenu();
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }
}
