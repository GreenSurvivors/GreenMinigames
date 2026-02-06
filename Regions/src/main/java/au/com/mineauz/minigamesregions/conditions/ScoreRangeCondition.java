package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemList;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoreRangeCondition extends ACondition {
    private final IntegerFlag min = new IntegerFlag("min", 5);
    private final IntegerFlag max = new IntegerFlag("max", 10);
    private final EnumFlag<TeamColor> teamColor = new EnumFlag<>("team", TeamColor.RED);
    // note: autodetect team being default here is part of migrating old behavior aka data fixer upper
    private  final EnumFlag<ScoreHolderType> scoreHolder = new EnumFlag<>("scoreHolder", ScoreHolderType.AUTODETECT_TEAM);

    protected ScoreRangeCondition(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_SCORERANGE_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.MINIGAME;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
            RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_SCORERANGE_NAME),
            RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_FORMAT,
                Placeholder.unparsed(MinigamePlaceHolderKey.MIN.getKey(), String.valueOf(min.getFlag())),
                Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), String.valueOf(max.getFlag()))),
            RegionMessageManager.getMessage(RegionLangKey.MENU_TEAM_NAME), teamColor.getFlag().getCompName());
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

    private boolean checkCondition(final @Nullable MinigamePlayer player) {
        if (player == null || !player.isInMinigame()) {
            return false;
        }

        if (scoreHolder.getFlag() == ScoreHolderType.PLAYER) {
            return player.getScore() >= min.getFlag() && player.getScore() <= max.getFlag();
        } else {
            if (player.getMinigame().isTeamGame()) {
                @Nullable Team team = null;

                if (scoreHolder.getFlag() == ScoreHolderType.AUTODETECT_TEAM ) {
                    team = player.getTeam();
                }
                if (team == null) {
                    TeamsModule tm = TeamsModule.getMinigameModule(player.getMinigame());
                    team = tm.getTeam(teamColor.getFlag());
                }

                if (team != null) {
                    return team.getScore() >= min.getFlag() && team.getScore() <= max.getFlag();
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    @Override
    public void saveArguments(@NotNull CommentedConfigurationNode config) throws SerializationException {
        min.saveValue(config);
        max.saveValue(config);
        scoreHolder.saveValue(config);
        teamColor.saveValue(config);
        saveInvertedStatus(config);
    }

    @Override
    public void loadArguments(@NotNull CommentedConfigurationNode config) {
        min.loadValue(config);
        max.loadValue(config);
        scoreHolder.loadValue(config);
        teamColor.loadValue(config);

        /*
         DATA FIXER UPPER
         note: this is a slight change in behavior:
         until now, if the player was in no team and this setting was never touched, this condition would return false.
         however, now it defaults to the red team.
         Should be edge cases however, if a player was playing a team game with the red team but is not part of a team themselfs
        */
        if (teamColor.getFlag() == TeamColor.NONE) {
            teamColor.setFlag(TeamColor.RED);
        }

        loadInvert(config);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer player, @NotNull Menu prev) {
        Menu menu = new Menu(3, getDisplayName(), player);
        menu.addItem(min.getMenuItem(ItemType.STONE_SLAB,
            RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_MIN_NAME), 0, null));
        menu.addItem(max.getMenuItem(ItemType.STONE,
            RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_MAX_NAME), 0, null));
        menu.addItem(scoreHolder.getMenuItem(ItemType.PUFFERFISH_BUCKET,
            RegionMessageManager.getMessage(RegionLangKey.MENU_SCORE_HOLDER_NAME)));
        List<TeamColor> teams = new ArrayList<>(TeamColor.validColors());

        // todo cycle through color material
        menu.addItem(new MenuItemList<>(getTeamMaterial(), RegionMessageManager.getMessage(RegionLangKey.MENU_TEAM_NAME), new Callback<>() {
            @Override
            public TeamColor getValue() {
                return teamColor.getFlag();
            }

            @Override
            public void setValue(TeamColor value) {
                teamColor.setFlag(value);
            }
        }, teams));

        menu.addItem(new MenuItemBack(prev), menu.getSize() - 9);
        addInvertMenuItem(menu);

        menu.displayMenu(player);
        return true;
    }

    private @NotNull ItemType getTeamMaterial() {
        return teamColor.getFlag().getDisplayType();
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }

    private enum ScoreHolderType {
        TEAM,
        AUTODETECT_TEAM,
        PLAYER
    }
}
