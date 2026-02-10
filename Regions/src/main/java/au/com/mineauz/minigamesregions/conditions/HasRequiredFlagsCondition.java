package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class HasRequiredFlagsCondition extends ACondition { // the whole singleplayer flag system is unused.

    protected HasRequiredFlagsCondition(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_HASREQUIREDFLAGS_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.PLAYER;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of();
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
    public boolean checkRegionCondition(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return false;
        return true;//Minigames.getPlugin().getPlayerManager().checkRequiredFlags(mgPlayer, mgPlayer.getMinigame()).isEmpty();
    }

    @Override
    public boolean checkNodeCondition(final @Nullable MinigamePlayer mgPlayer, final @NotNull Node node) {
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return false;
        return true; //Minigames.getPlugin().getPlayerManager().checkRequiredFlags(mgPlayer, mgPlayer.getMinigame()).isEmpty();
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        loadInvert(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, getDisplayName(), prev.getIntendedViewer());
        addInvertMenuItem(menu);
        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);
        menu.displayMenu();
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }
}
