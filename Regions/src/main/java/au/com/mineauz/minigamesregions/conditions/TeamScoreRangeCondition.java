package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemList;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TeamScoreRangeCondition extends ACondition {
    private final IntegerFlag min = new IntegerFlag(5, "min");
    private final IntegerFlag max = new IntegerFlag(10, "max");
    private final EnumFlag<TeamColor> teamColor = new EnumFlag<>(TeamColor.NONE, "team");

    protected TeamScoreRangeCondition(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_TEAMSCORERANGE_NAME);
    }

    @Override
    public @NotNull IConditionCategory getCategory() {
        return RegionConditionCategories.TEAM;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITION_TEAMSCORERANGE_NAME),
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

    private boolean checkCondition(@Nullable MinigamePlayer player) {
        if (player == null || !player.isInMinigame()) {
            return false;
        }

        Team team;
        if (player.getTeam() != null && this.teamColor.getFlag() == TeamColor.NONE) {
            team = player.getTeam();
        } else if (this.teamColor.getFlag() != TeamColor.NONE) {
            TeamsModule tm = TeamsModule.getMinigameModule(player.getMinigame());
            team = tm.getTeam(this.teamColor.getFlag());
        } else {
            team = null;
        }

        if (team != null) {
            return team.getScore() >= min.getFlag() && team.getScore() <= max.getFlag();
        } else {
            return false;
        }
    }

    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
        min.saveValue(config, path);
        max.saveValue(config, path);
        teamColor.saveValue(config, path);
        saveInvert(config, path);
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
        min.loadValue(config, path);
        max.loadValue(config, path);
        teamColor.loadValue(config, path);
        loadInvert(config, path);
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer player, @NotNull Menu prev) {
        Menu m = new Menu(3, getDisplayName(), player);
        m.addItem(min.getMenuItem(Material.STONE_SLAB,
                RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_MIN_NAME), 0, null));
        m.addItem(max.getMenuItem(Material.STONE,
                RegionMessageManager.getMessage(RegionLangKey.MENU_RANGE_MAX_NAME), 0, null));
        List<TeamColor> teams = new ArrayList<>(TeamColor.validColors());

        m.addItem(new MenuItemList<>(getTeamMaterial(), RegionMessageManager.getMessage(RegionLangKey.MENU_TEAM_NAME), new Callback<>() {
            @Override
            public TeamColor getValue() {
                return teamColor.getFlag();
            }

            @Override
            public void setValue(TeamColor value) {
                teamColor.setFlag(value);
            }
        }, teams));
        m.addItem(new MenuItemBack(prev), m.getSize() - 9);
        addInvertMenuItem(m);
        m.displayMenu(player);
        return true;
    }

    private @NotNull Material getTeamMaterial() {
        return teamColor.getFlag().getDisplaMaterial();
    }

    @Override
    public boolean playerNeeded() {
        return true;
    }
}
