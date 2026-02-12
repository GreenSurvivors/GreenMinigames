package au.com.mineauz.minigames.minigame.modules.team;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.ScoreHolder;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import au.com.mineauz.minigames.script.ScriptCollection;
import au.com.mineauz.minigames.script.ScriptObject;
import au.com.mineauz.minigames.script.ScriptReference;
import au.com.mineauz.minigames.script.ScriptValue;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.commons.text.WordUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Team implements ScriptObject, ScoreHolder {
    private final @NotNull Minigames plugin = Minigames.getPlugin();
    private final @NotNull IntegerFlag maxPlayers = new IntegerFlag("maxPlayers", 0);
    private final @NotNull List<SafeFullLocation> startLocations = new ArrayList<>();
    private final @NotNull StringFlag playerAssignMsg = new StringFlag("assignMsg", MessageManager.getRawMessage(MgMiscLangKey.PLAYER_TEAM_ASSIGN_JOINTEAM));
    private final @NotNull StringFlag joinAnnounceMsg = new StringFlag("gameAssignMsg", MessageManager.getRawMessage(MgMiscLangKey.PLAYER_TEAM_ASSIGN_JOINANNOUNCE));
    private final @NotNull StringFlag playerAutobalanceMsg = new StringFlag("autobalanceMsg", MessageManager.getRawMessage(MgMiscLangKey.PLAYER_TEAM_AUTOBALANCE_PLYMSG));
    private final @NotNull StringFlag gameAutobalanceMsg = new StringFlag("gameAutobalanceMsg", MessageManager.getRawMessage(MgMiscLangKey.PLAYER_TEAM_AUTOBALANCE_MINIGAMEMSG));
    private final @NotNull EnumFlag<OptionStatus> nametagVisibility = new EnumFlag<>("nametagVisibility", OptionStatus.ALWAYS);
    private final @NotNull EnumFlag<OptionStatus> collisionRule = new EnumFlag<>("collision", OptionStatus.ALWAYS);
    private final EnumFlag<OptionStatus> showDeathMessage = new EnumFlag<>("deathMessage", OptionStatus.ALWAYS);  // todo does this need a helper like the visibility?
    private final @NotNull BooleanFlag friendlyFire = new BooleanFlag("friendlyFire", false);
    private final @NotNull BooleanFlag seeFriendlyInvisibles = new BooleanFlag("seeFriendlyInvisibles", true);
    private final @NotNull BooleanFlag autoBalance = new BooleanFlag("autoBalance", true);
    private final @NotNull List<@NotNull MinigamePlayer> players = new ArrayList<>();
    private final @NotNull Minigame mgm;
    private final @NotNull String scoreboardName;
    private @NotNull String displayName;
    private @NotNull TeamColor color;
    private int score = 0;

    /**
     * Creates a team for the use in a specific Minigame
     *
     * @param color    - The unique team color to identify the team by.
     * @param minigame - The Minigame this team is assigned to.
     */
    public Team(@NotNull TeamColor color, @NotNull Minigame minigame) {
        this.color = color;
        displayName = WordUtils.capitalizeFully(color.getUserFriendlyName()) + " Team";
        scoreboardName = color.getUserFriendlyName().toLowerCase();
        mgm = minigame;
    }

    /**
     * Gets the teams Minigame
     *
     * @return The Minigame this team is assigned to.
     */
    public @NotNull Minigame getMinigame() {
        return mgm;
    }

    /**
     * Changes the color of the team for the Minigame its assigned to.
     *
     * @param color - The color to change this team to.
     * @return true if the Minigame doesn't have the team color already available, fails if it already has that team.
     */
    public boolean setColor(@NotNull TeamColor color) {
        if (!TeamsModule.getMinigameModule(mgm).hasTeam(color)) {
            if (displayName.toLowerCase().equals(this.color.getUserFriendlyName().toLowerCase() + " team"))
                displayName = WordUtils.capitalizeFully(color.getUserFriendlyName()) + " Team";
            TeamsModule.getMinigameModule(mgm).removeTeam(this.color);
            this.color = color;
            TeamsModule.getMinigameModule(mgm).addTeam(color, this);

            return true;
        }
        return false;
    }

    /**
     * Gets the teams color.
     *
     * @return The teams color.
     */
    public @NotNull TeamColor getColor() {
        return color;
    }

    /**
     * Gets the teams TextColor alternative.
     *
     * @return The TextColor
     */
    public @NotNull NamedTextColor getTextColor() {
        return color.getColor();
    } //todo

    /**
     * Gets the teams display name. If none is set, it will return the teams color followed by "Team".
     *
     * @return The display name or the teams color followed by "Team"
     */
    public @NotNull String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name for this team. If the name is longer than 32 characters,
     * it'll be trimmed to that length (Minecraft limitation).
     *
     * @param name - The name to change the team to.
     */
    public void setDisplayName(@NotNull String name) {
        if (name.length() > 32)
            name = name.substring(0, 31);
        displayName = name;
    }

    /**
     * Gets the display name prefixed with its color. If none is set,
     * it will return the teams color followed by "Team".
     *
     * @return The colored display name or the team color followed by "Team"
     */
    public @NotNull Component getColoredDisplayName() {
        return Component.text(getDisplayName(), getTextColor());
    }

    public int getMaxPlayers() {
        return maxPlayers.getFlag();
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers.setFlag(maxPlayers);
    }

    public boolean hasRoom() {
        return maxPlayers.getFlag() == 0 || players.size() < maxPlayers.getFlag();
    }


    /**
     * Gets the teams current score
     *
     * @return The score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the teams score to a specific value.
     *
     * @param amount The score amount to set for the team.
     */
    public void setScore(int amount) {
        score = amount;
        Objective obj = mgm.getScoreboard().getObjective(mgm.getName());
        if (obj != null) {
            obj.getScore(getDisplayName()).setScore(score);
        }
    }

    /**
     * Adds 1 point to the team.
     */
    public int addScore() {
        return addScore(1);
    }

    /**
     * Adds a specific amount to the teams score.
     *
     * @param amount - The amount of points to add to the team
     * @return The new score for the team.
     */
    public int addScore(int amount) {
        score += amount;
        Objective obj = mgm.getScoreboard().getObjective(mgm.getName());
        if (obj != null) {
            obj.getScore(getDisplayName()).setScore(score);
        }
        return score;
    }

    /**
     * Sets the teams score back to 0.
     */
    public void resetScore() {
        score = 0;
        mgm.getScoreboard().resetScores(getDisplayName());
    }

    /**
     * Gets a list of all the players assigned to this team.
     *
     * @return A list of all players assigned to the team.
     */
    public @NotNull List<@NotNull MinigamePlayer> getPlayers() {
        return players;
    }

    /**
     * Adds a player to the team.
     *
     * @param mgPlayer - The player to add.
     */
    public void addPlayer(final @NotNull MinigamePlayer mgPlayer) {
        final @Nullable Player player = mgPlayer.getPlayer();
        if (player == null) {
            return;
        }
        players.add(mgPlayer);
        mgPlayer.setTeam(this);
        player.setScoreboard(mgm.getScoreboard());
        org.bukkit.scoreboard.Team team = mgm.getScoreboard().getTeam(scoreboardName);
        if (team != null) {
            team.addPlayer(player);
        }
    }

    /**
     * Removes a player from the team.
     *
     * @param mgPlayer - The player to remove.
     */
    public void removePlayer(final @NotNull MinigamePlayer mgPlayer) {
        players.remove(mgPlayer);
        final Scoreboard board = mgm.getScoreboard();
        final org.bukkit.scoreboard.Team team = board.getTeam(scoreboardName);
        final @NotNull OfflinePlayer offlinePlayer = mgPlayer.getOfflinePlayer();
        if (team != null) {
            team.removePlayer(offlinePlayer);
        }
        if (offlinePlayer.isOnline()) {
            offlinePlayer.getPlayer().setScoreboard(plugin.getServer().getScoreboardManager().getMainScoreboard());
        }
    }

    /**
     * Adds a starting location for the team to spawn at.
     *
     * @param loc - The location to add to the team.
     */
    public void addStartLocation(final @NotNull SafeFullLocation loc) {
        startLocations.add(loc);
    }

    /**
     * Replaces a starting location already assigned on the team.
     *
     * @param loc    - The new location
     * @param number - The number id of the original starting location (Ranging from 1 to the amount of start points [Not 0])
     */
    public void addStartLocation(final @NotNull SafeFullLocation loc, final int number) {
        if (startLocations.size() >= number) {
            startLocations.set(number - 1, loc);
        } else {
            startLocations.add(loc);
        }
    }

    /**
     * Gets all the starting locations for this team.
     *
     * @return The teams starting locations.
     */
    public @NotNull List<@NotNull SafeFullLocation> getStartLocations() {
        return startLocations;
    }

    /**
     * Gets whether the team has start locations
     *
     * @return true if the team has start locations
     */
    public boolean hasStartLocations() {
        return !startLocations.isEmpty();
    }

    /**
     * Removes a specific start location from this team.
     *
     * @param locNumber - The id of the starting location.
     * @return true if removal was successful.
     */
    public boolean removeStartLocation(int locNumber) {
        if (startLocations.size() > locNumber) {
            startLocations.remove(locNumber);
            return true;
        }
        return false;
    }

    public String getPlayerAssignMessage() {
        return playerAssignMsg.getFlag();
    }

    public @NotNull StringFlag getPlayerAssignMessageFlag() {
        return playerAssignMsg;
    }

    public void setPlayerAssignMsg(@NotNull String msg) {
        playerAssignMsg.setFlag(msg);
    }

    public @NotNull String getJoinAnnounceMessage() {
        return joinAnnounceMsg.getFlag();
    }

    public void setGameAssignMessage(@NotNull String msg) {
        joinAnnounceMsg.setFlag(msg);
    }

    public @NotNull String getAutobalanceMessage() {
        return playerAutobalanceMsg.getFlag();
    }

    public void setAutobalanceMessage(@NotNull String msg) {
        playerAutobalanceMsg.setFlag(msg);
    }

    public @NotNull String getGameAutobalanceMessage() {
        return gameAutobalanceMsg.getFlag();
    }

    public void setGameAutobalanceMessage(@NotNull String msg) {
        gameAutobalanceMsg.setFlag(msg);
    }

    public @NotNull StringFlag getAutoBalanceMsgFlag() {
        return playerAutobalanceMsg;
    }

    public @NotNull StringFlag getGameAutoBalanceMsgFlag() {
        return gameAutobalanceMsg;
    }

    public @NotNull OptionStatus getNameTagVisibility() {
        return nametagVisibility.getFlagOrDefault();
    }

    public @NotNull OptionStatus getCollisionRule() {
        return collisionRule.getFlagOrDefault();
    }

    public boolean isFriendlyFireEnabled() {
        return friendlyFire.getFlagOrDefault();
    }

    public void setFriendlyFire(boolean isEnabled) {
         friendlyFire.setFlag(isEnabled);

        org.bukkit.scoreboard.Team bukkitTeam = mgm.getScoreboard().getTeam(color.getUserFriendlyName().toLowerCase());
        if (bukkitTeam != null) {
            bukkitTeam.setAllowFriendlyFire(isEnabled);
        } else {
            plugin.getComponentLogger().warn("No team for set friendly fire call");
        }
    }

    public void setNameTagVisibility(@NotNull OptionStatus vis) {
        nametagVisibility.setFlag(vis);
        org.bukkit.scoreboard.Team bukkitTeam = mgm.getScoreboard().getTeam(color.getUserFriendlyName().toLowerCase());
        if (bukkitTeam != null) {
            bukkitTeam.setOption(Option.NAME_TAG_VISIBILITY, vis);
        } else {
            plugin.getComponentLogger().warn("No team set for visibility call");
        }
    }

    public void setCollisionRule(@NotNull OptionStatus col) {
        nametagVisibility.setFlag(col);
        org.bukkit.scoreboard.Team bukkitTeam = mgm.getScoreboard().getTeam(color.getUserFriendlyName().toLowerCase());
        if (bukkitTeam != null) {
            bukkitTeam.setOption(Option.COLLISION_RULE, col);
        } else {
            plugin.getComponentLogger().warn("No team set for collision rule call");
        }
    }

    public @NotNull Callback<@NotNull VisibilityMapper> getNameTagVisibilityCallback() {
        return new Callback<>() {

            @Override
            public @NotNull VisibilityMapper getValue() {
                return VisibilityMapper.getMapping(getNameTagVisibility());
            }

            @Override
            public void setValue(@NotNull VisibilityMapper value) {
                setNameTagVisibility(value.getStatus());
            }
        };
    }

    public @NotNull Callback<@NotNull CollisionRuleMapper> getCollisionRuleCallback() {
        return new Callback<>() {

            @Override
            public @NotNull CollisionRuleMapper getValue() {
                return CollisionRuleMapper.getMapping(getCollisionRule());
            }

            @Override
            public void setValue(@NotNull CollisionRuleMapper value) {
                setCollisionRule(value.getStatus());
            }
        };
    }

    @NotNull
    public Callback<Boolean> getFriedndlyFireCallback() {
        return new Callback<>() {

            @NotNull
            @Override
            public Boolean getValue() {
                return isFriendlyFireEnabled();
            }

            @Override
            public void setValue(Boolean value) {
                setFriendlyFire(value);
            }
        };
    }

    public OptionStatus showDeathMessageToWhom() {
        return showDeathMessage.getFlag();
    }

    public void setToWhomShowDeathMessage(OptionStatus death) {
        showDeathMessage.setFlag(death);
    }

    public Callback<String> getWhohmtoShowDeathmessageCallback() {
        return new Callback<>() {

            @Override
            public String getValue() {
                return showDeathMessageToWhom().toString();
            }

            @Override
            public void setValue(String value) {
                setToWhomShowDeathMessage(OptionStatus.valueOf(value));
            }

        };
    }

    public @NotNull Callback<@NotNull Boolean> getSeeFriendlyInvisiblesCallback() {
        return new Callback<>() {
            @Override
            public @NotNull Boolean getValue() {
                return canSeeFriendlyInvisibles();
            }

            @Override
            public void setValue(@NotNull Boolean value) {
                setCanSeeFriendlyInvisibles(value);
            }
        };
    }

    public void setCanSeeFriendlyInvisibles(boolean seeFriendlyInvisibles) {
        this.seeFriendlyInvisibles.setFlag(seeFriendlyInvisibles);

        org.bukkit.scoreboard.Team bukkitTeam = mgm.getScoreboard().getTeam(color.getUserFriendlyName().toLowerCase());
        if (bukkitTeam != null) {
            bukkitTeam.setCanSeeFriendlyInvisibles(seeFriendlyInvisibles);
        } else {
            plugin.getComponentLogger().warn("No team for set see friendly invisibles call");
        }
    }

    @NotNull
    public Callback<Boolean> getAutoBalanceCallBack() {
        return new Callback<>() {

            @Override
            public @NotNull Boolean getValue() {
                return shouldAutoBalance();
            }

            @Override
            public void setValue(@NotNull Boolean value) {
                setAutoBalance(value);
            }
        };
    }

    public boolean canSeeFriendlyInvisibles() {
        return seeFriendlyInvisibles.getFlag();
    }

    public boolean shouldAutoBalance() {
        return autoBalance.getFlag();
    }

    public void setAutoBalance(Boolean flag) {
        autoBalance.setFlag(flag);
    }

    @Override
    public @Nullable ScriptReference resolveReference(@NotNull String name) {
        if (name.equalsIgnoreCase("colorname")) {
            return ScriptValue.of(getColor().name());
        } else if (name.equalsIgnoreCase("color")) {
            return ScriptValue.of(getTextColor().toString());
        } else if (name.equalsIgnoreCase("name")) {
            return ScriptValue.of(getDisplayName());
        } else if (name.equalsIgnoreCase("score")) {
            return ScriptValue.of(score);
        } else if (name.equalsIgnoreCase("players")) {
            return ScriptCollection.of(players);
        } else if (name.equalsIgnoreCase("minigame")) {
            return mgm;
        }

        return null;
    }

    @Override
    public @NotNull Set<@NotNull String> getReferenceKeys() {
        return Set.of("colorname", "color", "name", "score", "players", "minigame");
    }

    @Override
    public @NotNull String getAsString() {
        return getColor().name();
    }

    public void load(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        setDisplayName(config.node("displayName").getString());
        config.node("startpos").getList(TypeToken.get(SafeFullLocation.class), ArrayList::new).forEach(this::addStartLocation);

        maxPlayers.loadValue(config);
        playerAssignMsg.loadValue(config);
        joinAnnounceMsg.loadValue(config);
        gameAutobalanceMsg.loadValue(config);
        playerAutobalanceMsg.loadValue(config);
        nametagVisibility.loadValue(config);
        collisionRule.loadValue(config);
        friendlyFire.loadValue(config);
        seeFriendlyInvisibles.loadValue(config);
        showDeathMessage.loadValue(config);
        autoBalance.loadValue(config);

        //dataFixerUpper
        playerAssignMsg.setFlag(playerAssignMsg.getFlag().replaceFirst("%s", "<team>"));
        joinAnnounceMsg.setFlag(joinAnnounceMsg.getFlag().replaceFirst("%s", "<player>").replaceFirst("%s", "<team>"));
        playerAutobalanceMsg.setFlag(playerAutobalanceMsg.getFlag().replaceFirst("%s", "<team>"));
        gameAutobalanceMsg.setFlag(gameAutobalanceMsg.getFlag().replaceFirst("%s", "<player>").replaceFirst("%s", "<team>"));
    }

    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.node("displayName").set(getDisplayName());

        if (!getStartLocations().isEmpty()) {
           config.node("startpos").setList(TypeToken.get(SafeFullLocation.class), getStartLocations());
        }

        maxPlayers.saveValue(config);
        playerAssignMsg.saveValue(config);
        joinAnnounceMsg.saveValue(config);
        gameAutobalanceMsg.saveValue(config);
        playerAutobalanceMsg.saveValue(config);
        nametagVisibility.saveValue(config);
        collisionRule.saveValue(config);
        friendlyFire.loadValue(config);
        seeFriendlyInvisibles.loadValue(config);
        showDeathMessage.loadValue(config);
        autoBalance.saveValue(config);
    }


    /**
     * I have no Idea, why whoever fucked the naming in Bukkit up, but it is pretty bad
     */
    public enum CollisionRuleMapper {
        /**
         * Apply this option to everyone.
         */
        ALWAYS(OptionStatus.ALWAYS, MgMenuLangKey.MENU_TEAM_COLLIDE_ALLWAYS),
        /**
         * Never apply this option.
         */
        NEVER(OptionStatus.NEVER, MgMenuLangKey.MENU_TEAM_COLLIDE_NEVER),
        /**
         * Apply this option only for opposing teams.
         */
        COLLIDE_WITH_OTHER_TEAMS(OptionStatus.FOR_OTHER_TEAMS, MgMenuLangKey.MENU_TEAM_COLLIDE_WITH_OTHER_TEAMS),
        /**
         * Apply this option for only team members.
         */
        COLLIDE_WITH_OWN_TEAM(OptionStatus.FOR_OWN_TEAM, MgMenuLangKey.MENU_TEAM_COLLIDE_WITH_OWN_TEAM);

        private final @NotNull OptionStatus status;
        private final @NotNull String name;

        CollisionRuleMapper(@NotNull OptionStatus status, @NotNull MinigameLangKey langKey) {
            this.status = status;
            this.name = MessageManager.getRawMessage(langKey);
        }

        static @NotNull CollisionRuleMapper getMapping(@NotNull OptionStatus status) {
            for (CollisionRuleMapper mapping : CollisionRuleMapper.values()) {
                if (status == mapping.status) {
                    return mapping;
                }
            }

            // fallback should never get used unless Mojang decides to add another visibility
            return ALWAYS;
        }

        @NotNull OptionStatus getStatus() {
            return status;
        }

        @Override
        public @NotNull String toString() {
            return name;
        }
    }

    /**
     * I have no Idea, why whoever fucked the naming in Bukkit up, but it is pretty bad
     */
    public enum VisibilityMapper {
        /**
         * Apply this option to everyone.
         */
        ALWAYS(OptionStatus.ALWAYS, MgMenuLangKey.MENU_TEAM_NAMEVISIBILITY_ALWAYSVISIBLE),
        /**
         * Never apply this option.
         */
        NEVER(OptionStatus.NEVER, MgMenuLangKey.MENU_TEAM_NAMEVISIBILITY_NEVERVISIBLE),
        /**
         * Apply this option only for opposing teams.
         */
        HIDE_FOR_OTHER_TEAMS(OptionStatus.FOR_OTHER_TEAMS, MgMenuLangKey.MENU_TEAM_NAMEVISIBILITY_HIDEOTHERTEAM),
        /**
         * Apply this option for only team members.
         */
        HIDE_FOR_OWN_TEAM(OptionStatus.FOR_OWN_TEAM, MgMenuLangKey.MENU_TEAM_NAMEVISIBILITY_HIDEOWNTEAM);

        private final @NotNull OptionStatus status;
        private final @NotNull String name;

        VisibilityMapper(@NotNull OptionStatus status, @NotNull MinigameLangKey langKey) {
            this.status = status;
            this.name = MessageManager.getRawMessage(langKey);
        }

        static @NotNull VisibilityMapper getMapping(@NotNull OptionStatus status) {
            for (VisibilityMapper mapping : VisibilityMapper.values()) {
                if (status == mapping.status) {
                    return mapping;
                }
            }

            // fallback should never get used unless Mojang decides to add another visibility
            return ALWAYS;
        }

        @NotNull OptionStatus getStatus() {
            return status;
        }

        @Override
        public @NotNull String toString() {
            return name;
        }
    }
}
