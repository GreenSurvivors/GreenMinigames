package au.com.mineauz.minigames.gametypes;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.MultiplayerTimer;
import au.com.mineauz.minigames.events.TimerExpireEvent;
import au.com.mineauz.minigames.managers.MinigamePlayerManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.MinigameState;
import au.com.mineauz.minigames.minigame.modules.LobbySettingsModule;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiplayerType extends MinigameTypeBase {
    private static final Minigames plugin = Minigames.getPlugin();
    private static final int secondsUntilLateJoin = 5; // todo configurable
    private final MinigamePlayerManager pdata = plugin.getPlayerManager();

    public MultiplayerType() {
        setType(MinigameType.MULTIPLAYER);
    }

    public static void switchTeam(final @NotNull Minigame minigame, final @NotNull MinigamePlayer mgPlayer,
                                  final @NotNull Team newTeam) {
        if (mgPlayer.isInMinigame() && minigame.equals(mgPlayer.getMinigame())) { // sanitycheck
            if (mgPlayer.getTeam() != null) {
                mgPlayer.removeTeam();
            }

            newTeam.addPlayer(mgPlayer);
        }
    }

    @Override
    public boolean cannotStart(final @NotNull Minigame minigame, final @NotNull MinigamePlayer mgPlayer) {
        if (minigame.getPlayers().size() < minigame.getMaxPlayers()) {
            if (minigame.getLobbyLocation() != null) {
                return false;
            } else {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOLOBY);
            }
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_FULL);
        }

        return true;
    }

    @Override
    public boolean teleportOnJoin(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame) {
        final @Nullable SafeFullLocation location = minigame.getLobbyLocation();
        boolean result = false;
        if (location == null) {
            plugin.getLogger().warning("Game has no lobby set and it was expected:" + minigame.getName());
        } else {
            result = mgPlayer.teleport(location);
            final Player player = mgPlayer.getPlayer();
            if (plugin.getConfig().getBoolean("warnings") && player.getWorld() != location.getWorld() &&
                player.hasPermission("minigame.set.lobby")) { //todo permission manager

                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.WARNING, MgMiscLangKey.MINIGAME_WARNING_TELEPORT_ACROSS_WORLDS);
            }
        }
        return result;
    }

    @Override
    public boolean joinMinigame(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame) {
        if (!LobbySettingsModule.getMinigameModule(minigame).canInteractPlayerWait()) mgPlayer.setCanInteract(false);
        if (!LobbySettingsModule.getMinigameModule(minigame).canMoveOnPlayerWait()) mgPlayer.setFrozen(true);
        if (!minigame.isWaitingForPlayers() && !minigame.hasStarted()) {
            if (minigame.getMultiplayerTimer() == null && minigame.getPlayers().size() == minigame.getMinPlayers()) {
                minigame.setMultiplayerTimer(new MultiplayerTimer(minigame));
                minigame.getMultiplayerTimer().startTimer();

                if (minigame.getPlayers().size() == minigame.getMaxPlayers()) {
                    minigame.getMultiplayerTimer().setCurrentLobbyWaitTime(0);
                    MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_SKIPWAITTIME));
                }
            } else if (minigame.getMultiplayerTimer() != null && minigame.getPlayers().size() == minigame.getMaxPlayers()) {
                minigame.getMultiplayerTimer().setCurrentLobbyWaitTime(0);
                MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_SKIPWAITTIME));
            } else if (minigame.getMultiplayerTimer() == null) {
                final int neededPlayers = minigame.getMinPlayers() - minigame.getPlayers().size();
                MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_WAITINGFORPLAYERS,
                    Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(neededPlayers))));
            }
        } else if (minigame.hasStarted()) {
            mgPlayer.setLateJoining(true);
            MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_LATEJOIN,
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(secondsUntilLateJoin)))));
            if (minigame.isTeamGame()) {
                Team teamToJoin = null;
                final TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);
                for (final @NotNull Team team : teamsModule.getTeams()) {
                    if (teamToJoin == null || team.getPlayers().size() < teamToJoin.getPlayers().size()) {
                        teamToJoin = team;
                    }
                }

                teamToJoin.addPlayer(mgPlayer);
                MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MiniMessage.miniMessage().deserialize(teamToJoin.getPlayerAssignMessage(),
                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(teamToJoin.getDisplayName(), teamToJoin.getTextColor()))));

                final Team fteam = teamToJoin;
                mgPlayer.setLateJoinTimer(Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (mgPlayer.isInMinigame()) {
                        final @NotNull List<SafeFullLocation> locs = new ArrayList<>();
                        if (teamsModule.hasTeamStartLocations()) {
                            locs.addAll(fteam.getStartLocations());
                        } else {
                            locs.addAll(minigame.getStartLocations());
                        }
                        Collections.shuffle(locs);
                        mgPlayer.teleport(locs.getFirst());
                        mgPlayer.getLoadout().equipLoadout(mgPlayer);
                        mgPlayer.setLateJoining(false);
                        mgPlayer.setFrozen(false);
                        mgPlayer.setCanInteract(true);
                        mgPlayer.setLateJoinTimer(-1);
                    }
                }, secondsUntilLateJoin * 20));
            } else {
                mgPlayer.setLateJoinTimer(Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    if (mgPlayer.isInMinigame()) {
                        final @NotNull List<@NotNull SafeFullLocation> locs = new ArrayList<>(minigame.getStartLocations());
                        Collections.shuffle(locs);
                        mgPlayer.teleport(locs.getFirst());
                        mgPlayer.getLoadout().equipLoadout(mgPlayer);
                        mgPlayer.setLateJoining(false);
                        mgPlayer.setFrozen(false);
                        mgPlayer.setCanInteract(true);
                        mgPlayer.setLateJoinTimer(-1);
                    }
                }, secondsUntilLateJoin * 20));
            }
            mgPlayer.getPlayer().setScoreboard(minigame.getScoreboard());
            minigame.setScore(mgPlayer, 1);
            minigame.setScore(mgPlayer, 0);
        }
        return true;
    }

    @Override
    public void quitMinigame(final @NotNull MinigamePlayer mgPlayer, final @NotNull Minigame minigame, final boolean forced) {
        int teamsWithPlayers = 0;

        final Player player = mgPlayer.getPlayer();
        if (minigame.isTeamGame()) {
            final TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);
            mgPlayer.removeTeam();
            for (final @NotNull Team team : teamsModule.getTeams()) {
                if (!team.getPlayers().isEmpty())
                    teamsWithPlayers++;
            }

            if (minigame.getMultiplayerBets() != null && minigame.isWaitingForPlayers() && !forced) {
                if (minigame.getMultiplayerBets().getPlayersMoneyBet(mgPlayer) != null) {
                    plugin.getEconomy().depositPlayer(player.getPlayer(), minigame.getMultiplayerBets().getPlayersMoneyBet(mgPlayer));
                }
                minigame.getMultiplayerBets().removePlayersBet(mgPlayer);
            }
        } else {
            if (minigame.getMultiplayerBets() != null && (minigame.getMultiplayerTimer() == null || minigame.getMultiplayerTimer().getPlayerWaitTimeLeft() != 0)) {
                if (minigame.getMultiplayerBets().getPlayersItemBet(mgPlayer) != null) {
                    final ItemStack item = minigame.getMultiplayerBets().getPlayersItemBet(mgPlayer).clone();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.getInventory().addItem(item));
                } else if (minigame.getMultiplayerBets().getPlayersMoneyBet(mgPlayer) != null) {
                    plugin.getEconomy().depositPlayer(player.getPlayer(), minigame.getMultiplayerBets().getPlayersMoneyBet(mgPlayer));
                }
                minigame.getMultiplayerBets().removePlayersBet(mgPlayer);
            }
        }

        if (minigame.isTeamGame() && minigame.getPlayers().size() > 1 &&
            teamsWithPlayers == 1 && minigame.hasStarted() && !forced) {
            final TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);

            if (teamsModule.getTeams().size() != 1) {
                Team winnerTeam = null;
                for (final @NotNull Team team : teamsModule.getTeams()) {
                    if (!team.getPlayers().isEmpty()) {
                        winnerTeam = team;
                        break;
                    }
                }
                final @NotNull List<@NotNull MinigamePlayer> winners = new ArrayList<>(winnerTeam.getPlayers());
                final @NotNull List<@NotNull MinigamePlayer> losers = new ArrayList<>();
                plugin.getPlayerManager().endMinigame(minigame, winners, losers);

                if (minigame.getMultiplayerBets() != null) {
                    minigame.setMultiplayerBets(null);
                }
            }
        } else if (minigame.getPlayers().size() == 2 && minigame.hasStarted() && !forced) {
            final @NotNull List<@NotNull MinigamePlayer> winners = new ArrayList<>(minigame.getPlayers());
            winners.remove(mgPlayer);
            final @NotNull List<@NotNull MinigamePlayer> losers = new ArrayList<>();
            plugin.getPlayerManager().endMinigame(minigame, winners, losers);

            if (minigame.getMultiplayerBets() != null) {
                minigame.setMultiplayerBets(null);
            }
        } else if (minigame.getPlayers().size() - 1 < minigame.getMinPlayers() &&
            minigame.getMultiplayerTimer() != null &&
            minigame.getMultiplayerTimer().getStartWaitTimeLeft() != 0 &&
            (minigame.getState() == MinigameState.STARTING || minigame.getState() == MinigameState.WAITING)) {

            minigame.getMultiplayerTimer().setCurrentLobbyWaitTime(Minigames.getPlugin().getConfig().getInt("multiplayer.waitforplayers"));
            minigame.getMultiplayerTimer().pauseTimer();
            minigame.getMultiplayerTimer().removeTimer();
            minigame.setMultiplayerTimer(null);
            minigame.setState(MinigameState.IDLE);
            MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_WAITINGFORPLAYERS,
                Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(1))));
        }
    }

    @Override
    public void endMinigame(final @NotNull List<@NotNull MinigamePlayer> winners, final @NotNull List<@NotNull MinigamePlayer> losers,
                            final @NotNull Minigame minigame) {
        if (minigame.isTeamGame()) {
            final TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);

            for (final @NotNull MinigamePlayer player : winners) {
                player.removeTeam();
            }
            for (final @NotNull MinigamePlayer player : losers) {
                player.removeTeam();
            }
            for (final @NotNull Team team : teamsModule.getTeams()) {
                team.resetScore();
            }
        }

        if (minigame.getMultiplayerTimer() == null) {
            return;
        }
        minigame.getMultiplayerTimer().setStartWaitTime(0);
        minigame.setMultiplayerTimer(null);
    }

    public void endMinigameFindWinner(final @NotNull Minigame minigame) {
        if (minigame.isTeamGame()) {
            final TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);
            if (teamsModule.getDefaultWinner() != null) { //default winner
                List<MinigamePlayer> defaultWinners;
                List<MinigamePlayer> defaultLosers;

                //if we have the default winner team on the field, make them winners
                if (teamsModule.hasTeam(teamsModule.getDefaultWinner())) {
                    defaultWinners = new ArrayList<>(teamsModule.getTeam(teamsModule.getDefaultWinner()).getPlayers().size());
                    defaultLosers = new ArrayList<>(minigame.getPlayers().size() - teamsModule.getTeam(teamsModule.getDefaultWinner()).getPlayers().size());

                    defaultWinners.addAll(teamsModule.getTeam(teamsModule.getDefaultWinner()).getPlayers());
                } else { // no one wins
                    defaultWinners = new ArrayList<>();
                    defaultLosers = new ArrayList<>(minigame.getPlayers().size());
                }

                //make all losers, that are not in default winners team
                for (final @NotNull Team team : teamsModule.getTeams()) {
                    if (team.getColor() != teamsModule.getDefaultWinner())
                        defaultLosers.addAll(team.getPlayers());
                }

                pdata.endMinigame(minigame, defaultWinners, defaultLosers);

            } else { // no default winner
                final @NotNull List<@NotNull Team> drawTeams = new ArrayList<>();
                Team winner = null;

                for (final @NotNull Team team : teamsModule.getTeams()) {
                    //make the next team winner, if they have the highest score
                    if (winner == null || (team.getScore() > winner.getScore() &&
                        (drawTeams.isEmpty() || team.getScore() > drawTeams.getFirst().getScore()))) {

                        winner = team;

                        //make the next team draw with the last winner, if their scores match
                    } else if (team.getScore() == winner.getScore()) {
                        //clear lower draw teams
                        if (!drawTeams.isEmpty()) {
                            drawTeams.clear();
                        }

                        drawTeams.add(winner);
                        drawTeams.add(team);

                        //the last winner draws
                        winner = null;

                    } else if (!drawTeams.isEmpty() && drawTeams.getFirst().getScore() == team.getScore()) {
                        //new team also draws
                        drawTeams.add(team);
                    }
                }

                //if we have a winner, all the other ones are losers
                if (winner != null) {
                    final @NotNull List<@NotNull MinigamePlayer> winners = new ArrayList<>(winner.getPlayers());
                    final @NotNull List<@NotNull MinigamePlayer> losers = new ArrayList<>(minigame.getPlayers().size() - winner.getPlayers().size());

                    //gather losers
                    for (final @NotNull Team team : teamsModule.getTeams()) {
                        if (team != winner)
                            losers.addAll(team.getPlayers());
                    }

                    pdata.endMinigame(minigame, winners, losers);
                } else { //no winner
                    final @NotNull List<@NotNull MinigamePlayer> players = new ArrayList<>(minigame.getPlayers());

                    if (plugin.getConfig().getBoolean("multiplayer.broadcastwin")) {
                        if (drawTeams.size() == 2) {
                            MinigameMessageManager.broadcastServer(MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_TEAM_TIE,
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(drawTeams.get(0).getDisplayName(), drawTeams.get(0).getTextColor())),
                                Placeholder.component(MinigamePlaceHolderKey.OTHER_TEAM.getKey(), Component.text(drawTeams.get(1).getDisplayName(), drawTeams.get(1).getTextColor())),
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())
                            ), minigame, MinigameMessageType.TIE);
                        } else {
                            MinigameMessageManager.broadcastServer(MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_TEAM_TIECOUNT,
                                Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(drawTeams.size())),
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())
                            ), minigame, MinigameMessageType.TIE);
                        }

                        //build score message
                        final @NotNull TextComponent.Builder scores = Component.text();
                        final @NotNull List<@NotNull Team> teams = teamsModule.getTeams();

                        for (int i = 0; i < teams.size(); ) {
                            scores.append(Component.text(teams.get(i).getColor().name())).append(Component.text(teams.get(i).getScore()));

                            if (++i < teams.size()) {
                                scores.append(Component.text(" : ", NamedTextColor.WHITE));
                            }
                        }

                        MinigameMessageManager.broadcastServer(MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_INFO_SCORE,
                                Placeholder.component(MinigamePlaceHolderKey.SCORE.getKey(), scores)),
                            minigame, MinigameMessageType.INFO);
                    } else { // don't broadcastServer win
                        if (drawTeams.size() == 2) {
                            MinigameMessageManager.sendMinigameMessage(minigame,
                                MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_TEAM_TIE,
                                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(drawTeams.get(0).getDisplayName(), drawTeams.get(0).getTextColor())),
                                    Placeholder.component(MinigamePlaceHolderKey.OTHER_TEAM.getKey(), Component.text(drawTeams.get(1).getDisplayName(), drawTeams.get(1).getTextColor())),
                                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())),
                                MinigameMessageType.TIE);
                        } else {
                            MinigameMessageManager.sendMinigameMessage(minigame,
                                MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_TEAM_TIECOUNT,
                                    Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(drawTeams.size())),
                                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())),
                                MinigameMessageType.TIE);
                        }

                        for (final @NotNull MinigamePlayer mgPlayer : players) {
                            pdata.quitMinigame(mgPlayer, true);

                            //build score message
                            final @NotNull TextComponent.Builder scores = Component.text();
                            final @NotNull List<@NotNull Team> teams = teamsModule.getTeams();

                            for (int i = 0; i < teams.size(); ) {
                                scores.append(Component.text(teams.get(i).getColor().name())).append(Component.text(teams.get(i).getScore()));

                                if (++i < teams.size()) {
                                    scores.append(Component.text(" : ", NamedTextColor.WHITE));
                                }
                            }

                            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_INFO_SCORE,
                                Placeholder.component(MinigamePlaceHolderKey.SCORE.getKey(), scores));
                        }
                    }

                    //reset scores
                    for (final @NotNull Team team : teamsModule.getTeams()) {
                        team.resetScore();
                    }

                    //todo figure out why the resetting happens here, but not in cases someone wins or in cases this isn't a multiplayer game
                    //reset timers
                    if (minigame.getMinigameTimer() != null) {
                        minigame.getMinigameTimer().stopTimer();
                        minigame.setMinigameTimer(null);
                    }

                    if (minigame.getMultiplayerTimer() != null) {
                        minigame.getMultiplayerTimer().setStartWaitTime(0);
                        minigame.getMultiplayerTimer().pauseTimer();
                        minigame.getMultiplayerTimer().removeTimer();
                        minigame.setMultiplayerTimer(null);
                    }

                    // reset floor degenerators
                    if (minigame.getFloorDegenerator() != null && minigame.getPlayers().isEmpty()) {
                        minigame.getFloorDegenerator().stopDegenerator();
                    }

                    //if no one wins the game, no one wins the bets
                    if (minigame.getMultiplayerBets() != null && minigame.getPlayers().isEmpty()) {
                        minigame.setMultiplayerBets(null);
                    }
                }
            }
        } else { //no team minigame
            MinigamePlayer winningPlayer = null;
            int winingScore = 0;

            for (final @NotNull MinigamePlayer mgPlayer : minigame.getPlayers()) {
                if (mgPlayer.getScore() > 0) {
                    if (mgPlayer.getScore() > winingScore) {
                        winningPlayer = mgPlayer;
                        winingScore = mgPlayer.getScore();

                    } else if (mgPlayer.getScore() == winingScore) {
                        if (winningPlayer != null && mgPlayer.getDeaths() < winningPlayer.getDeaths()) {
                            winningPlayer = mgPlayer;

                        } else if (winningPlayer == null) {
                            winningPlayer = mgPlayer;
                        }
                    }
                }
            }

            final @NotNull List<@NotNull MinigamePlayer> losers = new ArrayList<>(minigame.getPlayers());
            final @NotNull List<@NotNull MinigamePlayer> winners = new ArrayList<>();

            if (winningPlayer != null) {
                losers.remove(winningPlayer);
                winners.add(winningPlayer);
            }

            pdata.endMinigame(minigame, winners, losers);
        }
    }

    /*----------------*/
    /*-----EVENTS-----*/
    /*----------------*/

    @EventHandler(priority = EventPriority.HIGHEST)
    private void playerRespawn(final @NotNull PlayerRespawnEvent event) {
        final @NotNull MinigamePlayer mgPlayer = pdata.getMinigamePlayer(event.getPlayer());
        if (mgPlayer.isInMinigame() && mgPlayer.getMinigame().getType() == MinigameType.MULTIPLAYER) {
            final Minigame minigame = mgPlayer.getMinigame();
            final @Nullable SafeFullLocation respawnPos;
            if (mgPlayer.getMinigame().isTeamGame()) {
                final @NotNull Team team = mgPlayer.getTeam();
                if (minigame.hasStarted() && !mgPlayer.isJoiningLate()) {
                    if (minigame.isAllowedMPCheckpoints() && mgPlayer.hasCheckpoint()) {
                        respawnPos = mgPlayer.getCheckpoint();
                    } else {
                        final @NotNull List<@NotNull SafeFullLocation> startLocations = new ArrayList<>();
                        if (TeamsModule.getMinigameModule(minigame).hasTeamStartLocations()) {
                            startLocations.addAll(team.getStartLocations());
                            mgPlayer.getLoadout().equipLoadout(mgPlayer);
                        } else {
                            startLocations.addAll(minigame.getStartLocations());
                        }
                        Collections.shuffle(startLocations);
                        respawnPos = startLocations.getFirst();
                    }
                    mgPlayer.getLoadout().equipLoadout(mgPlayer);
                } else {
                    respawnPos = minigame.getLobbyLocation();
                }
            } else {
                if (minigame.hasStarted() && !mgPlayer.isJoiningLate()) {
                    if (minigame.isAllowedMPCheckpoints() && mgPlayer.hasCheckpoint()) {
                        respawnPos = mgPlayer.getCheckpoint();
                    } else {
                        final @NotNull List<@NotNull SafeFullLocation> startLocations = new ArrayList<>(minigame.getStartLocations());
                        Collections.shuffle(startLocations);
                        respawnPos = startLocations.getFirst();
                    }

                    mgPlayer.getLoadout().equipLoadout(mgPlayer);
                } else {
                    respawnPos = minigame.getLobbyLocation();
                }
            }

            if (respawnPos != null) {
                event.setRespawnLocation(respawnPos.toLocation());
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> mgPlayer.getPlayer().setNoDamageTicks(60));
        }
    }

    @EventHandler
    private void timerExpire(final @NotNull TimerExpireEvent event) {
        final @NotNull Minigame minigame = event.getMinigame();
        if (minigame.getType() == MinigameType.MULTIPLAYER && event.getMinigame().getState() == MinigameState.STARTED) {
            endMinigameFindWinner(minigame);
        }
    }
}
