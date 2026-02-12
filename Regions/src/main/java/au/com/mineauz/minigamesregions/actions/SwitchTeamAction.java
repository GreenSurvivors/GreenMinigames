package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.apache.commons.text.WordUtils;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.*;
import java.util.stream.Collectors;

public class SwitchTeamAction extends AAction {
    private final StringFlag teamFrom = new StringFlag("From", "ALL");
    private final StringFlag teamTo = new StringFlag("To", "ALL");

    protected SwitchTeamAction(final @NotNull Key key) {
        super(key);
    }

    @Override
    public @NotNull Component getDisplayname() {
        return MessageManager.getMessage(RegionLangKey.MENU_ACTION_SWITCHTEAM_NAME);
    }

    @Override
    public @NotNull IActionCategory getCategory() {
        return RegionActionCategories.TEAM;
    }

    @Override
    public @NotNull Map<@NotNull Component, @Nullable Component> describe() {
        return Map.of(
                MessageManager.getMessage(RegionLangKey.MENU_ACTION_SWITCHTEAM_FROM_NAME), Component.text(teamFrom.getFlag()),
                MessageManager.getMessage(RegionLangKey.MENU_ACTION_SWITCHTEAM_TO_NAME), Component.text(teamTo.getFlag()));
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
        executeAction(mgPlayer);
    }

    @Override
    public void executeNodeAction(final @NotNull MinigamePlayer mgPlayer, final @NotNull Node node) {
        executeAction(mgPlayer);
    }

    private void executeAction(final @Nullable MinigamePlayer mgPlayer) {
        if (mgPlayer == null || !mgPlayer.isInMinigame()) return;
        if (teamFrom.getFlag().equals("NONE")) return;
        if (!teamFrom.getFlag().equals("ALL") || !teamFrom.getFlag().equals(mgPlayer.getTeam().getColor().toString()))
            return;
        final TeamsModule teamsModule = TeamsModule.getMinigameModule(mgPlayer.getMinigame());
        final @NotNull List<@NotNull Team> teams = teamsModule.getTeams();
        if (teamTo.getFlag().equals("ALL")) {
            Collections.shuffle(teams);
            for (final @NotNull Team team : teams) {
                if (team != mgPlayer.getTeam()) {
                    mgPlayer.setTeam(team);
                    return;
                }
            }

        } else {
            if (teamTo.getFlag().equals("NONE")) {
                mgPlayer.setTeam(null);
            }
        }
        for (final @NotNull Team team : teams) {
            if (team.getColor().toString().equals(teamTo.getFlag())) {
                mgPlayer.setTeam(team);
            }
        }
    }


    @Override
    public void saveArguments(final @NotNull CommentedConfigurationNode config) {
    }

    @Override
    public void loadArguments(final @NotNull CommentedConfigurationNode config) {
    }

    @Override
    public boolean displayMenu(final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, getDisplayname(), prev.getIntendedViewer());
        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);

        final @NotNull List<@NotNull String> teams = Arrays.stream(TeamColor.values()).map(TeamColor::getUserFriendlyName).collect(Collectors.toCollection(ArrayList::new));
        teams.add("All"); //todo ?
        menu.addItem(new MenuItemList<>(ItemType.PAPER, MessageManager.getMessage(RegionLangKey.MENU_ACTION_SWITCHTEAM_FROM_NAME),
                MessageManager.getMessageList(RegionLangKey.MENU_ACTION_SWITCHTEAM_FROM_DESCRIPTION), new Callback<>() {

            @Override
            public @NotNull String getValue() {
                return WordUtils.capitalizeFully(teamFrom.getFlag()).replaceAll("_", " ");
            }

            @Override
            public void setValue(@NotNull String value) {
                teamFrom.setFlag(value.toUpperCase().replaceAll(" ", "_"));
            }
        }, teams));

        menu.addItem(new MenuItemList<>(ItemType.PAPER, MessageManager.getMessage(RegionLangKey.MENU_ACTION_SWITCHTEAM_TO_NAME),
                MessageManager.getMessageList(RegionLangKey.MENU_ACTION_SWITCHTEAM_TO_DESCRIPTION), new Callback<>() {

            @Override
            public @Nullable String getValue() {
                return WordUtils.capitalizeFully(teamTo.getFlag());
            }

            @Override
            public void setValue(@NotNull String value) {
                teamTo.setFlag(value.toUpperCase());
            }
        }, teams));
        menu.displayMenu();
        return true;
    }
}
