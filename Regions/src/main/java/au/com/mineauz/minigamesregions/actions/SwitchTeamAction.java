package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.StringFlag;
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
import au.com.mineauz.minigamesregions.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import org.apache.commons.text.WordUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SwitchTeamAction extends AAction {
    private final StringFlag teamTo = new StringFlag("ALL", "To");
    private final StringFlag teamFrom = new StringFlag("ALL", "From");

    protected SwitchTeamAction(@NotNull String name) {
        super(name);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SWITCHTEAM_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.TEAM;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SWITCHTEAM_FROM_NAME), Component.text(teamFrom.getFlag()),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SWITCHTEAM_TO_NAME), Component.text(teamTo.getFlag()));
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
        executeAction(mgPlayer);
    }

    @Override
    public void executeNodeAction(@NotNull MinigamePlayer mgPlayer, @NotNull Node node) {
        executeAction(mgPlayer);
    }

    private void executeAction(@Nullable MinigamePlayer mgPlayer) {
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return;
        if (teamFrom.getFlag().equals("NONE")) return;
        if (!teamFrom.getFlag().equals("ALL") || !teamFrom.getFlag().equals(mgPlayer.getTeam().getColor().toString()))
            return;
        if (teamTo.getFlag().equals("ALL")) {
            List<Team> teams = TeamsModule.getMinigameModule(mgPlayer.getMinigame()).getTeams();
            Collections.shuffle(teams);
            for (Team t : teams) {
                if (t != mgPlayer.getTeam()) {
                    mgPlayer.setTeam(t);
                    return;
                }
            }

        } else {
            if (teamTo.getFlag().equals("NONE")) {
                mgPlayer.setTeam(null);
            }
        }
        for (Team t : TeamsModule.getMinigameModule(mgPlayer.getMinigame()).getTeams()) {
            if (t.getColor().toString().equals(teamTo.getFlag())) {
                mgPlayer.setTeam(t);
            }
        }
    }


    @Override
    public void saveArguments(@NotNull FileConfiguration config, @NotNull String path) {
    }

    @Override
    public void loadArguments(@NotNull FileConfiguration config, @NotNull String path) {
    }

    @Override
    public boolean displayMenu(@NotNull MinigamePlayer mgPlayer, Menu prev) {
        Menu menu = new Menu(3, getDisplayname(), mgPlayer);
        menu.addItem(new MenuItemBack(prev), menu.getSize() - 9);
        List<String> teams = new ArrayList<>(TeamColor.colorNames());
        teams.add("All"); //todo ?
        menu.addItem(new MenuItemList<>(Material.PAPER, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SWITCHTEAM_FROM_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_SWITCHTEAM_FROM_DESCRIPTION), new Callback<>() {

            @Override
            public String getValue() {
                return WordUtils.capitalizeFully(teamFrom.getFlag());
            }

            @Override
            public void setValue(String value) {
                teamFrom.setFlag(value.toUpperCase());
            }


        }, teams));
        menu.addItem(new MenuItemList<>(Material.PAPER, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTION_SWITCHTEAM_TO_NAME),
                RegionMessageManager.getMessageList(RegionLangKey.MENU_ACTION_SWITCHTEAM_TO_DESCRIPTION), new Callback<>() {

            @Override
            public String getValue() {
                return WordUtils.capitalizeFully(teamTo.getFlag());
            }

            @Override
            public void setValue(String value) {
                teamTo.setFlag(value.toUpperCase());
            }
        }, teams));
        menu.displayMenu(mgPlayer);
        return true;
    }
}
