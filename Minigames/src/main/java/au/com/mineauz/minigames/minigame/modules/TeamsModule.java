package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.TeamFlag;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.TeamColor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;
import java.util.stream.Collectors;

public class TeamsModule extends AMinigameModule {
    private final @NotNull Map<@NotNull TeamColor, @NotNull TeamFlag> teams = new HashMap<>();
    private final @NotNull EnumFlag<@NotNull TeamColor> defaultWinner = new EnumFlag<>("defaultwinner", TeamColor.NONE);

    public TeamsModule(final @NotNull Minigame mgm, final @NotNull Key key) {
        super(mgm, key);
    }

    public static @Nullable TeamsModule getMinigameModule(@NotNull Minigame mgm) {
        return ((TeamsModule) mgm.getModule(MgModules.TEAMS.getKey()));
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        final @NotNull CommentedConfigurationNode teamsNode = config.node("teams");
        for (final @NotNull TeamFlag teamFlag : teams.values()) {
            teamFlag.saveValue(teamsNode);
        }

        defaultWinner.saveValue(config);
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) {
        // this does not have a dataFixerUpper
        if (config.hasChild("startposred") || config.hasChild( "startposblue")) {
            Minigames.getPlugin().getLogger().warning(config.path() + " contains unsupported configurations: startpos*");
        }

        final @NotNull CommentedConfigurationNode teamsNode = config.node("teams");
        if (!teamsNode.virtual() && !teamsNode.isNull()) {
            final @NotNull Scoreboard scoreboard = getMinigame().getScoreboard();

            for (final @NotNull CommentedConfigurationNode teamNode : teamsNode.childrenList()) {
                try {
                    TeamFlag tf = new TeamFlag(teamNode.key().toString(), null, getMinigame());
                    tf.loadValue(teamNode);

                    teams.put(tf.getFlag().getColor(), tf);
                    String sbTeam = tf.getFlag().getColor().toString().toLowerCase();
                    org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.registerNewTeam(sbTeam);
                    scoreboardTeam.setAllowFriendlyFire(tf.getFlag().isFriendlyFireEnabled());
                    scoreboardTeam.setCanSeeFriendlyInvisibles(tf.getFlag().canSeeFriendlyInvisibles());
                    scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.NAME_TAG_VISIBILITY, tf.getFlag().getNameTagVisibility());
                    scoreboardTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, tf.getFlag().getCollisionRule());
                    scoreboardTeam.color(tf.getFlag().getTextColor());
                } catch (final @NotNull SerializationException e) {
                    Minigames.getPlugin().getComponentLogger().error("Couldn't load team " + teamNode.path(), e);
                }
            }
        }

        defaultWinner.loadValue(config);
    }

    public @Nullable Team getTeam(@NotNull TeamColor color) {
        return teams.get(color).getFlag();
    }

    public @NotNull List<@NotNull Team> getTeams() {
        return teams.values().stream().map(TeamFlag::getFlag).collect(Collectors.toCollection(ArrayList::new));
    }

    public @NotNull List<@NotNull TeamColor> getTeamColors() {
        return new ArrayList<>(teams.keySet());
    }

    /**
     * For constructing a script reference
     */
    public @NotNull Map<@NotNull String, @NotNull Team> getTeamsNameMap() {
        Map<String, Team> result = new HashMap<>(teams.size());

        for (TeamFlag teamFlag : teams.values()) {
            result.put(teamFlag.getFlag().getColor().name().toLowerCase(), teamFlag.getFlag());
        }

        return result;
    }

    /**
     * Adds or returns the existing team of TeamColor
     *
     * @param color {@link TeamColor}
     * @return {@link Team}
     */
    public @NotNull Team addTeam(@NotNull TeamColor color) {
        return addTeam(color, (String) null);
    }

    /**
     * Adds a new team with the color unless one already exists in which case this returns the
     * existing team
     *
     * @param color {@link TeamColor}
     * @param name  Team name
     * @return Team
     */
    public @NotNull Team addTeam(@NotNull TeamColor color, @Nullable String name) {
        if (!hasTeam(color)) {
            final TeamFlag teamFlag = new TeamFlag(color, new Team(color, getMinigame()), getMinigame());
            teams.put(color, teamFlag);
            String teamNameString = color.getUserFriendlyName().toLowerCase();
            @NotNull org.bukkit.scoreboard.Team bukkitTeam = getMinigame().getScoreboard().registerNewTeam(teamNameString);
            bukkitTeam.setAllowFriendlyFire(teamFlag.getFlag().isFriendlyFireEnabled());
            bukkitTeam.setCanSeeFriendlyInvisibles(teamFlag.getFlag().canSeeFriendlyInvisibles());
            bukkitTeam.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, teamFlag.getFlag().getCollisionRule());
            bukkitTeam.color(color.getColor());
            if (name != null && !name.isEmpty()) {
                bukkitTeam.displayName(MiniMessage.miniMessage().deserialize(name).colorIfAbsent(color.getColor()));
                bukkitTeam.displayName(Component.text(name, color.getColor()));
            }
        }
        return teams.get(color).getFlag();
    }

    /**
     * Adds a team with the new color -and removes any other team with that color name from the scoreboard
     *
     * @param color {@link TeamColor}  the TeamColor to set
     * @param team  The new Team
     */
    public void addTeam(@NotNull TeamColor color, @NotNull Team team) {
        teams.put(color, new TeamFlag(color, team, getMinigame()));
        String sbTeam = color.getUserFriendlyName().toLowerCase();
        Scoreboard scoreboard = getMinigame().getScoreboard();
        org.bukkit.scoreboard.Team bukkitTeam = scoreboard.getTeam(sbTeam);
        if (bukkitTeam != null) {
            bukkitTeam.unregister();
        }
        bukkitTeam = getMinigame().getScoreboard().registerNewTeam(sbTeam);
        bukkitTeam.setAllowFriendlyFire(false);
        bukkitTeam.setCanSeeFriendlyInvisibles(true);
        bukkitTeam.displayName(MiniMessage.miniMessage().deserialize(team.getDisplayName()).colorIfAbsent(color.getColor()));
        bukkitTeam.color(color.getColor());
    }

    /**
     * True of {@link TeamColor} exists as a team
     *
     * @param color {@link TeamColor}
     * @return boolean
     */
    public boolean hasTeam(@NotNull TeamColor color) {
        return teams.containsKey(color);
    }

    /**
     * Removes a team from the module and the scoreboard
     *
     * @param color {@link TeamColor}
     */
    public void removeTeam(@NotNull TeamColor color) {
        if (hasTeam(color)) {
            teams.remove(color);
            org.bukkit.scoreboard.Team bukkitTeam =
                    getMinigame().getScoreboard().getTeam(color.getUserFriendlyName().toLowerCase());
            if (bukkitTeam != null) {
                bukkitTeam.unregister();
            }
        }
    }

    public boolean hasTeamStartLocations() {
        for (TeamFlag teamFlag : teams.values()) {
            if (!teamFlag.getFlag().hasStartLocations()) {
                return false;
            }
        }
        return true;
    }

    public @NotNull Callback<TeamColor> getDefaultWinnerCallback() {
        return new Callback<>() {

            @Override
            public @NotNull TeamColor getValue() {
                if (!teams.containsKey(defaultWinner.getFlag())) {
                    return TeamColor.NONE;
                }

                return defaultWinner.getFlag();
            }

            @Override
            public void setValue(@NotNull TeamColor value) {
                defaultWinner.setFlag(value);
            }
        };
    }

    public @Nullable TeamColor getDefaultWinner() {
        if (defaultWinner.getFlag() != TeamColor.NONE) {
            TeamColor team = defaultWinner.getFlag();
            if (!teams.containsKey(team)) {
                return null;
            } else {
                return team;
            }
        }
        return null;
    }

    public void setDefaultWinner(@NotNull TeamColor defaultWinner) {
        this.defaultWinner.setFlag(defaultWinner);
    }

    public void clearTeams() {
        teams.clear();
        defaultWinner.setFlag(TeamColor.NONE);
    }

    @Override
    public @Nullable SequencedCollection<@NotNull TypeDependentDisplayData> addEditMenuOptions(@NotNull Menu previousMenu) {
        Menu menu = new Menu(6, MgMenuLangKey.MENU_TEAM_NAME, previousMenu.getViewer());
        menu.setPreviousPage(previousMenu);
        List<MenuItem> menuItems = new ArrayList<>();

        List<TeamColor> teamColors = new ArrayList<>(teams.keySet());
        teamColors.add(TeamColor.NONE);
        menuItems.add(new MenuItemList<>(ItemType.PAPER, MgMenuLangKey.MENU_TEAM_DEFAULTWIN_NAME,
                getDefaultWinnerCallback(), teamColors));

        menuItems.add(new MenuItemNewLine());

        for (TeamFlag teamFlag : this.teams.values()) {
            menuItems.add(new MenuItemTeam(teamFlag.getFlag().getColoredDisplayName(), teamFlag.getFlag()));
        }

        menu.addItem(new MenuItemAddTeam(MgMenuLangKey.MENU_TEAMADD_NAME, this), menu.getSize() - 1);

        menu.addItems(menuItems);

        menu.addItem(new MenuItemBack(previousMenu), menu.getSize() - 9);

        MenuItemPage teamOptionsMenuPage = new MenuItemPage(ItemType.CHEST, MgMenuLangKey.MENU_TEAM_OPTIONS_NAME, menu);
        previousMenu.addItem(teamOptionsMenuPage);

        return null;
    }
}
