package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemList;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MatchTeamCondition extends ACondition {
    private final @NotNull EnumFlag<@NotNull TeamColor> teamColor = new EnumFlag<>("team", TeamColor.RED);

    protected MatchTeamCondition(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_MATCHTEAM_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.TEAM;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(RegionMessageManager.getMessage(RegionLangKey.MENU_TEAM_NAME), teamColor.getFlag().getCompName());
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
    public boolean checkNodeCondition(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        return mgPlayer.getTeam() != null && mgPlayer.getTeam().getColor() == teamColor.getFlag();
    }

    @Override
    public boolean checkRegionCondition(final @Nullable MinigamePlayer mgPlayer, final @NotNull Region region) {
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return false;
        return mgPlayer.getTeam() != null && mgPlayer.getTeam().getColor() == teamColor.getFlag();
    }

    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        teamColor.saveValue(config);
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
        teamColor.loadValue(config);
        loadInvert(config);
    }

    @Override
    public boolean displayMenu(final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, getDisplayName(), prev.getIntendedViewer());
        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);

        final @NotNull List<@NotNull TeamColor> teams = new ArrayList<>(TeamColor.validColors());

        menu.addItem(new MenuItemList<>(getTeamDisplayItemType(), RegionMessageManager.getMessage(RegionLangKey.MENU_TEAM_NAME), new Callback<TeamColor>() { // don't know why but for some reason the compiler doesn't like when I remove the redundant Teamcolor from the callback. Please let it in there for now!
            @Override
            public TeamColor getValue() {
                return teamColor.getFlag();
            }

            @Override
            public void setValue(TeamColor value) {
                teamColor.setFlag(value);
            }
        }, teams) {
            @Override
            public @NotNull ItemStack getDisplayItem() {
                ItemStack stack = super.getDisplayItem();
                return stack.withType(getTeamDisplayItemType().asMaterial());
            }
        });

        addInvertMenuItem(menu);
        menu.displayMenu();
        return true;
    }

    private @NotNull ItemType getTeamDisplayItemType() {
        return teamColor.getFlagOrDefault().getDisplayType();
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }
}
