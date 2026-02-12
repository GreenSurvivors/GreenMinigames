package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Map;

public class ContainsOneTeamCondition extends ACondition {

    protected ContainsOneTeamCondition(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return MessageManager.getMessage(RegionLangKey.MENU_CONDITION_CONTAINSONETEAM_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.TEAM;
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
        return false;
    }

    @Override
    public boolean checkNodeCondition(final @Nullable MinigamePlayer mgPlayer, final @NotNull Node node) {
        return false;
    }

    @Override
    public boolean checkRegionCondition(final @NotNull MinigamePlayer mgPlayer, final @NotNull Region region) {
        boolean ret = true;
        final @Nullable Team last = mgPlayer.getTeam();
        if (last == null) return true;
        for (MinigamePlayer p : region.getPlayers()) {
            if (last != p.getTeam()) {
                ret = false;
                break;
            }
        }
        return ret;
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
        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);
        addInvertMenuItem(menu);
        menu.displayMenu();
        return true;
    }

    @Override
    public boolean playerNeeded() {
        return false;
    }
}
