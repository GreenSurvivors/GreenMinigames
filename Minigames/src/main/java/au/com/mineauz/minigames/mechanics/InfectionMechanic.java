package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.gametypes.MultiplayerType;
import au.com.mineauz.minigames.managers.MinigamePlayerManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class InfectionMechanic extends AGameMechanic {
    private final @NotNull IntegerFlag infectedPercent = new IntegerFlag("infectedPercent", 18);
    private final @NotNull EnumFlag<TeamColor> infectedTeam = new EnumFlag<>("infectedTeam", TeamColor.RED);
    private final @NotNull EnumFlag<TeamColor> survivorTeam = new EnumFlag<>("survivorTeam", TeamColor.BLUE);

    //Unsaved Data
    private final List<MinigamePlayer> infected = new ArrayList<>();

    public InfectionMechanic(final @NotNull Minigames plugin, final @NotNull Key key, final @NotNull Minigame minigame) {
        super(plugin, key, minigame);
    }

    @Override
    public @NotNull EnumSet<@NotNull MinigameType> validTypes() {
        return EnumSet.of(MinigameType.MULTIPLAYER);
    }

    @Override
    public boolean checkCanStart(final @Nullable MinigamePlayer caller) {
        final TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);
        if (!minigame.isTeamGame() ||
            teamsModule.getTeams().size() != 2 ||
            !teamsModule.hasTeam(getInfectedTeam()) ||
            !teamsModule.hasTeam(getSurvivorTeam())) {
            if (caller != null) {
                MinigameMessageManager.sendMgMessage(caller, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOINFECTION);
            } else {
                Minigames.getPlugin().getComponentLogger().warn("The Infection Minigame \"" + minigame.getName() + "\"is not properly configured! Visit the wiki for help configuring an Infection Minigame.");
            }
            return false;
        }
        return true;
    }

    @Override
    public @NotNull List<@NotNull MinigamePlayer> balanceTeam(final @NotNull List<@NotNull MinigamePlayer> mgPlayers) {
        List<MinigamePlayer> result = new ArrayList<>();
        Collections.shuffle(mgPlayers);
        for (MinigamePlayer mgPlayer : mgPlayers) {
            TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);
            Team infectedTeam = teamsModule.getTeam(getInfectedTeam());
            Team survivorTeam = teamsModule.getTeam(getSurvivorTeam());
            Team team = mgPlayer.getTeam();
            double percent = ((Integer) getInfectedPercent()).doubleValue() / 100d;
            if (team == survivorTeam) {
                if (infectedTeam.getPlayers().size() < Math.ceil(mgPlayers.size() * percent) && infectedTeam.hasRoom()) {
                    MultiplayerType.switchTeam(minigame, mgPlayer, infectedTeam);
                    result.add(mgPlayer);
                    MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MiniMessage.miniMessage().deserialize(infectedTeam.getPlayerAssignMessage(),
                        Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(infectedTeam.getDisplayName(), infectedTeam.getTextColor()))));
                    MinigameMessageManager.sendMinigameMessage(minigame, MiniMessage.miniMessage().deserialize(infectedTeam.getJoinAnnounceMessage(),
                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(infectedTeam.getDisplayName(), infectedTeam.getTextColor()))),
                        MinigameMessageType.INFO, mgPlayer);
                }
            } else if (team == null) {
                if (infectedTeam.getPlayers().size() < Math.ceil(mgPlayers.size() * percent) && infectedTeam.hasRoom()) {
                    infectedTeam.addPlayer(mgPlayer);
                    result.add(mgPlayer);
                    MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MiniMessage.miniMessage().deserialize(infectedTeam.getPlayerAssignMessage(),
                        Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(infectedTeam.getDisplayName(), infectedTeam.getTextColor()))));
                    MinigameMessageManager.sendMinigameMessage(minigame, MiniMessage.miniMessage().deserialize(infectedTeam.getJoinAnnounceMessage(),
                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(infectedTeam.getDisplayName(), infectedTeam.getTextColor()))),
                        MinigameMessageType.INFO, mgPlayer);
                } else if (survivorTeam.hasRoom()) {
                    survivorTeam.addPlayer(mgPlayer);
                    result.add(mgPlayer);
                    MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MiniMessage.miniMessage().deserialize(survivorTeam.getPlayerAssignMessage(),
                        Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(survivorTeam.getDisplayName(), survivorTeam.getTextColor()))));
                    MinigameMessageManager.sendMinigameMessage(minigame, MiniMessage.miniMessage().deserialize(survivorTeam.getJoinAnnounceMessage(),
                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(survivorTeam.getDisplayName(), survivorTeam.getTextColor()))),
                        MinigameMessageType.INFO, mgPlayer);
                } else {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_FULL);
                    plugin.getPlayerManager().quitMinigame(mgPlayer, false);
                }
            }
        }
        return result;
    }

    @Override
    public @NotNull MenuItemPage displayMechanicSettings(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(6, MgMenuLangKey.MENU_INFECTED_NAME, previous.getIntendedViewer());
        menu.setItem(new MenuItemBack(previous), menu.getSize() - 9);

        menu.addItem(infectedPercent.getMenuItem(ItemType.ZOMBIE_HEAD, MgMenuLangKey.MENU_INFECTED_PERCENT_NAME,
            MgMenuLangKey.MENU_INFECTED_PERCENT_DESCRIPTION, 1, 99));

        final @Nullable TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);
        final @NotNull List<@NotNull TeamColor> teams = new ArrayList<>(teamsModule.getTeamColors().size() + 3);
        for (TeamColor teamColor : teamsModule.getTeamColors()) {
            if (teamColor != infectedTeam.getDefaultFlag() && teamColor != survivorTeam.getDefaultFlag()) {
                teams.add(teamColor);
            } // avoid adding defaults twice
        }
        // add defaults
        teams.add(infectedTeam.getDefaultFlag());
        teams.add(survivorTeam.getDefaultFlag());
        teams.add(TeamColor.NONE);
        menu.addItem(new MenuItemList<>(ItemType.PAPER, MgMenuLangKey.MENU_INFECTED_TEAM_INFECTED_NAME, getInfectedTeamCallback(), teams));
        menu.addItem(new MenuItemList<>(ItemType.PAPER, MgMenuLangKey.MENU_INFECTED_TEAM_SURVIVOR_NAME, getSurvivorTeamCallback(), teams));
        return new MenuItemPage(ItemType.SCULK_CATALYST, MgMenuLangKey.MENU_MINIGAME_MECHANIC_SETTINGS_NAME, menu);
    }

    @Override
    public void startMinigame(final @Nullable MinigamePlayer caller) {
    }

    @Override
    public void stopMinigame() {
    }

    @Override
    public void onJoinMinigame(final @NotNull MinigamePlayer player) {
    }

    @Override
    public void quitMinigame(final @NotNull MinigamePlayer player, boolean forced) {
        if (isInfectedPlayer(player)) {
            removeInfectedPlayer(player);
        }
    }

    @Override
    public void endMinigame(final @NotNull List<@NotNull MinigamePlayer> winners,
                            final @NotNull List<@NotNull MinigamePlayer> losers) {
        final @NotNull List<@NotNull MinigamePlayer> wins = new ArrayList<>(winners);
        for (MinigamePlayer mgPlayer : wins) {
            if (isInfectedPlayer(mgPlayer)) {
                winners.remove(mgPlayer);
                losers.add(mgPlayer);
                removeInfectedPlayer(mgPlayer);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void playerDeath(final @NotNull PlayerDeathEvent event) {
        final @NotNull MinigamePlayerManager playerManager = plugin.getPlayerManager();
        final @NotNull MinigamePlayer player = playerManager.getMinigamePlayer(event.getEntity());
        if (player.isInMinigame()) {
           // Minigame mgm = player.getMinigame();
            if (minigame.isTeamGame() && minigame.equals(player.getMinigame())) {
                final TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);

                Team survivorTeam = teamsModule.getTeam(getSurvivorTeam());
                Team infectedTeam = teamsModule.getTeam(getInfectedTeam());
                if (survivorTeam.getPlayers().contains(player)) {
                    if (infectedTeam.hasRoom()) {
                        MultiplayerType.switchTeam(minigame, player, infectedTeam);
                        addInfectedPlayer(player);
                        if (event.getEntity().getKiller() != null) {
                            MinigamePlayer killer = playerManager.getMinigamePlayer(event.getEntity().getKiller());
                            killer.addScore();
                            minigame.setScore(killer, killer.getScore());
                        }
                        player.resetScore();
                        minigame.setScore(player, player.getScore());

                        if (minigame.getLives() != player.getDeaths()) {
                            MinigameMessageManager.sendMinigameMessage(minigame, MiniMessage.miniMessage().deserialize(infectedTeam.getJoinAnnounceMessage(),
                                    Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), player.displayName()),
                                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(infectedTeam.getDisplayName(), infectedTeam.getTextColor()))),
                                MinigameMessageType.ERROR);
                        }
                        if (survivorTeam.getPlayers().isEmpty()) {
                            final @NotNull List<@NotNull MinigamePlayer> winners = new ArrayList<>(infectedTeam.getPlayers());
                            final @NotNull List<@NotNull MinigamePlayer> losers = new ArrayList<>();
                            playerManager.endMinigame(minigame, winners, losers);
                        }
                    } else {
                        playerManager.quitMinigame(player, false);
                    }
                } else {
                    if (event.getEntity().getKiller() != null) {
                        MinigamePlayer killer = playerManager.getMinigamePlayer(event.getEntity().getKiller());
                        killer.addScore();
                        minigame.setScore(killer, killer.getScore());
                    }
                }
            }
        }
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        infectedPercent.saveValue(config);
        infectedTeam.saveValue(config);
        survivorTeam.saveValue(config);
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        infectedPercent.loadValue(config);
        infectedTeam.loadValue(config);
        survivorTeam.loadValue(config);
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    protected @NotNull Callback<TeamColor> getInfectedTeamCallback() {
        return new Callback<>() {
            @Override
            public TeamColor getValue() {
                if (infectedTeam.getFlag() != null) {
                    if (infectedTeam.getFlag().equals(TeamColor.NONE)) {
                        return infectedTeam.getFlag();
                    } else if (infectedTeam.getFlag() == infectedTeam.getDefaultFlag() || infectedTeam.getFlag() == survivorTeam.getDefaultFlag() ||
                        TeamsModule.getMinigameModule(minigame).getTeamColors().contains(infectedTeam.getFlag())) {

                        return infectedTeam.getFlag();
                    } else {
                        return infectedTeam.getDefaultFlag();
                    }
                } else {
                    return infectedTeam.getDefaultFlag();
                }
            }

            @Override
            public void setValue(TeamColor value) {
                if (value == TeamColor.NONE) {
                    infectedTeam.setFlag(value);
                } else if (value == infectedTeam.getDefaultFlag() || value == survivorTeam.getDefaultFlag() ||
                    TeamsModule.getMinigameModule(minigame).getTeamColors().contains(value)) {
                    infectedTeam.setFlag(value);
                } else {
                    infectedTeam.setFlag(null);
                }
            }
        };
    }

    protected @NotNull Callback<TeamColor> getSurvivorTeamCallback() {
        return new Callback<>() {
            @Override
            public TeamColor getValue() {
                if (survivorTeam.getFlag() != null) {
                    if (survivorTeam.getFlag() == TeamColor.NONE) {
                        return survivorTeam.getFlag();
                    } else if (survivorTeam.getFlag() == infectedTeam.getDefaultFlag() || survivorTeam.getFlag() == survivorTeam.getDefaultFlag() ||
                        TeamsModule.getMinigameModule(minigame).getTeamColors().contains(survivorTeam.getFlag())) {

                        return survivorTeam.getFlag();
                    } else {
                        return survivorTeam.getDefaultFlag();
                    }
                } else {
                    return survivorTeam.getDefaultFlag();
                }
            }

            @Override
            public void setValue(final TeamColor value) {
                if (value == TeamColor.NONE) {
                    survivorTeam.setFlag(TeamColor.NONE);
                } else if (value == infectedTeam.getDefaultFlag() || value == survivorTeam.getDefaultFlag() ||
                    TeamsModule.getMinigameModule(minigame).getTeamColors().contains(value)) {

                    survivorTeam.setFlag(value);
                } else {
                    survivorTeam.setFlag(null);
                }
            }
        };
    }

    public int getInfectedPercent() {
        return infectedPercent.getFlag();
    }

    public void setInfectedPercent(final int amount) {
        infectedPercent.setFlag(amount);
    }

    public @NotNull TeamColor getInfectedTeam() {
        if (infectedTeam.getFlag() != null && infectedTeam.getFlag() != TeamColor.NONE) {
            TeamColor teamColor = infectedTeam.getFlag();
            if (teamColor == infectedTeam.getDefaultFlag() || teamColor == survivorTeam.getDefaultFlag() ||
                TeamsModule.getMinigameModule(minigame).getTeamColors().contains(teamColor)) {

                return teamColor;
            } else {
                return TeamColor.NONE;
            }
        } else {
            return TeamColor.NONE;
        }
    }

    public void setInfectedTeam(final @NotNull TeamColor teamColor) {
        if (teamColor == infectedTeam.getDefaultFlag() || teamColor == survivorTeam.getDefaultFlag() ||
            TeamsModule.getMinigameModule(minigame).getTeamColors().contains(teamColor)) {

            this.infectedTeam.setFlag(teamColor);
        } else
            this.infectedTeam.setFlag(TeamColor.NONE);
    }

    public TeamColor getDefaultInfectedTeam() {
        return infectedTeam.getDefaultFlag();
    }

    public @Nullable TeamColor getSurvivorTeam() {
        if (survivorTeam.getFlag() != null) {
            TeamColor teamColor = survivorTeam.getFlag();
            if (teamColor == infectedTeam.getDefaultFlag() || teamColor == survivorTeam.getDefaultFlag() ||
                TeamsModule.getMinigameModule(minigame).getTeamColors().contains(teamColor)) {

                return teamColor;
            } else
                return null;
        } else
            return null;
    }

    public boolean setSurvivorTeam(final @NotNull TeamColor survivorTeamColor) {
        final @NotNull TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);

        if (survivorTeamColor == TeamColor.NONE ||
            survivorTeamColor == infectedTeam.getDefaultFlag() || survivorTeamColor == survivorTeam.getDefaultFlag() ||
            (teamsModule != null && teamsModule.getTeamColors().contains(survivorTeamColor))) {

            this.survivorTeam.setFlag(survivorTeamColor);

            return true;
        } else {
            this.survivorTeam.setFlag(null);
            return false;
        }
    }

    public @NotNull TeamColor getDefaultSurvivorTeam() {
        return survivorTeam.getDefaultFlag();
    }

    public void addInfectedPlayer(final @NotNull MinigamePlayer mgPlayer) {
        infected.add(mgPlayer);
    }

    public void removeInfectedPlayer(final @NotNull MinigamePlayer mgPlayer) {
        infected.remove(mgPlayer);
    }

    public boolean isInfectedPlayer(final @Nullable MinigamePlayer mgPlayer) {
        return infected.contains(mgPlayer);
    }

    public void clearInfectedPlayers() {
        infected.clear();
    }
}
