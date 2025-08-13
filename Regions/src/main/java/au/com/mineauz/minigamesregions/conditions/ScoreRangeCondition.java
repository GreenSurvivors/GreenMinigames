package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.Region;
import org.apache.commons.text.WordUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ScoreRangeCondition extends ACondition {
    private final IntegerFlag min = new IntegerFlag(5, "min");
    private final IntegerFlag max = new IntegerFlag(10, "max");
    private final StringFlag team = new StringFlag(TeamColor.RED.name(), "team");
    // note: autodetect team being default here is part of migrating old behavior aka DataFixerUpper
    private  final EnumFlag<ScoreHolderType> scoreHolder = new EnumFlag<>(ScoreHolderType.AUTODETECT_TEAM, "scoreHolder");

    @Override
    public String getName() {
        return "SCORE_RANGE";
    }

    @Override
    public String getCategory() {
        return "Minigame Conditions";
    }

    @Override
    public void describe(Map<String, Object> out) {
        out.put("Score", min.getFlag() + " - " + max.getFlag());
        out.put("Team", team.getFlag());
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
    public boolean checkRegionCondition(MinigamePlayer player, Region region) {
        return checkCondition(player);
    }

    @Override
    public boolean checkNodeCondition(MinigamePlayer player, Node node) {
        return checkCondition(player);
    }

    private boolean checkCondition(final @Nullable MinigamePlayer player) {
        if (player == null || !player.isInMinigame()) {
            return false;
        }

        if (Objects.requireNonNull(scoreHolder.getFlag()) == ScoreHolderType.PLAYER) {
            return player.getScore() >= min.getFlag() && player.getScore() <= max.getFlag();
        } else {
            if (player.getMinigame().isTeamGame()) {
                @Nullable Team team = null;

                if (scoreHolder.getFlag() == ScoreHolderType.AUTODETECT_TEAM ) {
                    team = player.getTeam();
                }
                if (team != null) {
                    TeamsModule tm = TeamsModule.getMinigameModule(player.getMinigame());
                    team = tm.getTeam(TeamColor.valueOf(this.team.getFlag()));
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
    public void saveArguments(FileConfiguration config, String path) {
        min.saveValue(path, config);
        max.saveValue(path, config);
        scoreHolder.saveValue(path, config);
        team.saveValue(path, config);
        saveInvert(config, path);
    }

    @Override
    public void loadArguments(FileConfiguration config, String path) {
        min.loadValue(path, config);
        max.loadValue(path, config);
        scoreHolder.loadValue(path, config);
        team.loadValue(path, config);

        // DATA FIXER UPPER
        if (team.getFlag().equalsIgnoreCase("NONE")) {
            team.setFlag(TeamColor.RED.name());
        }

        loadInvert(config, path);
    }

    @Override
    public boolean displayMenu(MinigamePlayer player, Menu prev) {
        Menu menu = new Menu(3, "Score Range", player);
        menu.addItem(min.getMenuItem("Minimum Score", Material.STONE_SLAB, 0, null));
        menu.addItem(max.getMenuItem("Maximum Score", Material.STONE, 0, null));
        menu.addItem(scoreHolder.getMenuItem("Score Holder", Material.PUFFERFISH_BUCKET));

        List<String> teams = new ArrayList<>();
        for (TeamColor t : TeamColor.values()) {
            teams.add(WordUtils.capitalize(t.toString().replace("_", " ")));
        }
        menu.addItem(new MenuItemList("Team Color", Material.WHITE_WOOL, new Callback<>() {
            @Override
            public String getValue() {
                return WordUtils.capitalize(team.getFlag().replace("_", " "));
            }

            @Override
            public void setValue(String value) {
                team.setFlag(value.toUpperCase().replace(" ", "_"));
            }
        }, teams));

        menu.addItem(new MenuItemPage("Back", MenuUtility.getBackMaterial(), prev), menu.getSize() - 9);

        addInvertMenuItem(menu);
        menu.displayMenu(player);
        return true;
    }

    @Override
    public boolean onPlayerApplicable() {
        return true;
    }

    private enum ScoreHolderType {
        TEAM,
        AUTODETECT_TEAM,
        PLAYER
    }
}
