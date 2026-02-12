package au.com.mineauz.minigames.managers;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.MultiplayerBets;
import au.com.mineauz.minigames.commands.QuitCommand;
import au.com.mineauz.minigames.events.*;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.mechanics.AGameMechanic;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.MinigameState;
import au.com.mineauz.minigames.minigame.modules.GameOverModule;
import au.com.mineauz.minigames.minigame.modules.LobbySettingsModule;
import au.com.mineauz.minigames.minigame.modules.ResourcePackModule;
import au.com.mineauz.minigames.minigame.modules.WeatherTimeModule;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.ResourcePack;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import au.com.mineauz.minigames.recorder.RegenRecorder;
import au.com.mineauz.minigames.sounds.MGSounds;
import au.com.mineauz.minigames.sounds.PlayMGSound;
import au.com.mineauz.minigames.stats.DynamicMinigameStat;
import au.com.mineauz.minigames.stats.MinigameStatistics;
import au.com.mineauz.minigames.stats.StoredGameStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

/**
 * Manager Class of all players playing Minigames.
 **/
public class MinigamePlayerManager {
    private final @NotNull Minigames plugin;
    private final @NotNull Map<@NotNull UUID, @NotNull MinigamePlayer> minigamePlayers = new HashMap<>();
    private final @NotNull List<@NotNull MinigamePlayer> applyingPack = new ArrayList<>();
    private boolean partyMode = false;
    private @NotNull List<@NotNull String> deniedCommands = new ArrayList<>();

    public MinigamePlayerManager(final @NotNull Minigames plugin) {
        this.plugin = plugin;
    }

    public @NotNull List<@NotNull MinigamePlayer> getApplyingPack() {
        return applyingPack;
    }

    public void needsResourcePack(@NotNull MinigamePlayer mgPlayer) {
        applyingPack.add(mgPlayer);
    }

    public void joinMinigame(final @NotNull Minigame minigame, final @NotNull MinigamePlayer mgPlayer,
                             final boolean isBetting, final double betAmount) {
        MinigameType type = minigame.getType();
        JoinMinigameEvent event = new JoinMinigameEvent(mgPlayer, minigame);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            plugin.getComponentLogger().info("Join Event was cancelled: " + event);
            return;
        }
        final @NotNull MinigameManager minigameManager = plugin.getMinigameManager();
        if (!minigameManager.minigameStartStateCheck(minigame, mgPlayer)) return;
        //Do betting stuff
        if (isBetting && !handleMoneyBet(minigame, mgPlayer, betAmount)) {
            return;
        }
        //Try to apply ressource pack
        ResourcePack pack = getResourcePack(minigame);
        if (pack != null && pack.isValid()) {
            if (mgPlayer.applyResourcePack(pack)) {
                MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_RESSOURCEPACK_APPLY);
            }
        }
        //Check if Minigame is full
        if (minigame.isGameFull()) {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_FULL);
            return;
        }
        //Check if Minigame has a lobby and teleport them there
        if (!minigameManager.teleportPlayerOnJoin(minigame, mgPlayer)) {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOLOBY);
            return;
        }
        //Give them the game type name
        if (minigame.getGameTypeName() == null) {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.SUCCESS, MgMiscLangKey.PLAYER_JOIN_PLAYERINFO,
                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getType().getName()));
        } else {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.SUCCESS, MgMiscLangKey.PLAYER_JOIN_PLAYERINFO,
                Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getGameTypeName()));
        }

        //Give them the objective
        if (minigame.getObjective() != null) {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.NONE, MgCommandLangKey.COMMAND_DIVIDER_LARGE);
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_JOIN_OBJECTIVE,
                Placeholder.component(MinigamePlaceHolderKey.OBJECTIVE.getKey(), minigame.getObjective()));
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.NONE, MgCommandLangKey.COMMAND_DIVIDER_LARGE);
        }
        //Prepare regeneration region for rollback.
        minigameManager.addRegenDataToRecorder(minigame);
        //Standardize player
        mgPlayer.storePlayerData();
        mgPlayer.setMinigame(minigame);
        minigame.addPlayer(mgPlayer);
        WeatherTimeModule mod = WeatherTimeModule.getMinigameModule(minigame);
        if (mod != null) {
            mod.applyCustomTime(mgPlayer);
            mod.applyCustomWeather(mgPlayer);
        }
        final Player player = mgPlayer.getPlayer();
        mgPlayer.setCheckpoint(mgPlayer.getSafeLocation());
        player.setFallDistance(0);
        player.setWalkSpeed(0.2f);
        mgPlayer.setStartTime(Calendar.getInstance().getTimeInMillis());
        mgPlayer.setGamemode(minigame.getDefaultGamemode());
        player.setAllowFlight(false);
        player.clearActivePotionEffects();
        //Hide Spectators
        for (MinigamePlayer pl : minigame.getSpectators()) {
            player.hidePlayer(plugin, pl.getPlayer());
        }

        if (minigame.getPlayers().size() == 1) {
            //Register regen recorder events
            if (minigame.hasRegenArea())
                Bukkit.getServer().getPluginManager().registerEvents(new RegenRecorder(minigame), plugin);
            if (mod != null) mod.startTimeLoop();
        }
        //Call Type specific join
        minigameManager.minigameType(type).joinMinigame(mgPlayer, minigame);

        //Call Mechanic specific join
        minigame.getMechanic().onJoinMinigame(mgPlayer);

        //Send other players the join message.
        MessageManager.sendMinigameMessage(minigame, MessageManager.getMessage(
                MgMiscLangKey.PLAYER_JOIN_PLAYERMSG,
                Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())),
            MinigameMessageType.INFO);
        mgPlayer.updateInventory();

        if (minigame.canDisplayScoreboard()) {
            player.setScoreboard(minigame.getScoreboard());
            minigame.setScore(mgPlayer, 1);
            minigame.setScore(mgPlayer, 0);
        }
        if (minigame.getState() == MinigameState.STARTING && minigame.canLateJoin()) {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_LATEJOINWAIT,
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(),
                    MinigameUtils.convertTime(Duration.ofSeconds(minigame.getMultiplayerTimer().getStartWaitTimeLeft()))));
        }
    }

    /**
     * @param minigame  the minigame to bet on
     * @param mgPlayer  the player who was betting
     * @param betAmount the amount in economy money. might be 0, if the player was betting an item
     * @return true if the player could successfully bet
     */
    private boolean handleMoneyBet(final @NotNull Minigame minigame, final @NotNull MinigamePlayer mgPlayer, final double betAmount) {
        final Player player = mgPlayer.getPlayer();
        final @NotNull ItemStack itemInMainHand = player.getInventory().getItemInMainHand().clone();
        if (minigame.getMultiplayerBets() == null && (itemInMainHand.isEmpty() || betAmount != 0)) {
            minigame.setMultiplayerBets(new MultiplayerBets());
        }

        final @NotNull MultiplayerBets mpBets = minigame.getMultiplayerBets();

        if (mpBets != null) {
            if (!mpBets.hasAlreadyBet(mgPlayer)) {
                //has the player not already bet and are they the highest better?
                if (mpBets.isHighestBetter(betAmount, itemInMainHand)) {
                    if (betAmount >= 0) {
                        if (plugin.getEconomy().getBalance(player) >= betAmount) {
                            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_BET_PLAYERMSG);

                            mpBets.addBet(mgPlayer, betAmount);
                            plugin.getEconomy().withdrawPlayer(player, betAmount);

                            return true;
                        } else {
                            //not enough money
                            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_BET_NOTENOUGHMONEY);
                            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_BET_NOTENOUGHMONEYINFO,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MONEY.getKey(), plugin.getEconomy().format(minigame.getMultiplayerBets().getHighestMoneyBet())));
                        }
                    }

                    if (itemInMainHand.isEmpty()) {
                        MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_BET_PLAYERMSG);
                        player.getInventory().remove(itemInMainHand);

                        mpBets.addBet(mgPlayer, itemInMainHand);

                        return true;
                    } else {
                        //no item to bet, and betAmount == 0
                        MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_BET_PLAYERNOBET);
                        return false; //maybe? or better true in this case?
                    }
                } else {
                    if (mpBets.getHighestMoneyBet() > 0) {
                        MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_BET_INCORRECTMONEYAMOUNTINFO,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MONEY.getKey(), plugin.getEconomy().format(mpBets.getHighestMoneyBet())));
                    }
                    //todo connect both messages with an "or"
                    if (mpBets.getHighestItemBet().isEmpty()) {

                        MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_BET_INCORRECTITEMAMOUNTINFO,
                            Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(mpBets.getHighestItemBet().getAmount())),
                            Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(),
                                Component.translatable(mpBets.getHighestItemBet().getType().translationKey())));
                    }
                    return false;
                }
            } else { //todo figure out why one is only allowed to bet once
                //already bet once.
                //todo feedback
                return false;
            }
        } else {
            // no bets where made already, no amount and no item in hand
            //todo feedback
            return false;
        }
    }

    public void spectateMinigame(final @NotNull Minigame minigame, final @NotNull MinigamePlayer mgPlayerSpectating) {
        final @NotNull SpectateMinigameEvent event = new SpectateMinigameEvent(mgPlayerSpectating, minigame);

        if (event.callEvent()) {
            boolean tpd;
            if (minigame.getSpectatorLocation() != null) {
                tpd = mgPlayerSpectating.teleport(minigame.getSpectatorLocation());
            } else {
                MessageManager.sendMessage(mgPlayerSpectating, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOSPECTATELOC);
                return;
            }
            if (!tpd) {
                MessageManager.sendMessage(mgPlayerSpectating, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTELEPORTALLOWED);
                return;
            }
            mgPlayerSpectating.storePlayerData();
            mgPlayerSpectating.setMinigame(minigame);
            mgPlayerSpectating.setGamemode(GameMode.ADVENTURE);

            minigame.addSpectator(mgPlayerSpectating);

            final Player playerSpectating = mgPlayerSpectating.getPlayer();
            if (minigame.canSpectateFly()) {
                playerSpectating.setAllowFlight(true);
            }
            for (final @NotNull MinigamePlayer mgPlayer : minigame.getPlayers()) {
                mgPlayer.getPlayer().hidePlayer(plugin, playerSpectating);
            }

            if (minigame.canDisplayScoreboard()) {
                playerSpectating.setScoreboard(minigame.getScoreboard());
            }

            playerSpectating.clearActivePotionEffects();

            MessageManager.sendMessage(mgPlayerSpectating, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SPECTATE_JOIN_PLAYERMSG,
                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));

            MessageManager.sendMessage(mgPlayerSpectating, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SPECTATE_JOIN_PLAYERHELP,
                Placeholder.component(MinigamePlaceHolderKey.COMMAND.getKey(),
                    Component.text("\"").append(new QuitCommand().getUsage()).append(Component.text("\""))));
            MessageManager.sendMinigameMessage(minigame, MessageManager.getMessage(MgMiscLangKey.PLAYER_SPECTATE_JOIN_MINIGAMEMSG,
                    Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayerSpectating.displayName()),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())),
                MinigameMessageType.INFO, mgPlayerSpectating);
        }
    }

    public void startMPMinigame(@NotNull Minigame minigame) {
        startMPMinigame(minigame, LobbySettingsModule.getMinigameModule(minigame).isTeleportOnStart());
    }

    public void startMPMinigame(@NotNull Minigame minigame, boolean teleport) {
        List<MinigamePlayer> players = new ArrayList<>(minigame.getPlayers());
        for (MinigamePlayer mgPlayer : players) {
            if (minigame.getMaxScore() != 0) {
                MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_SCORETOWIN,
                    Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), String.valueOf(minigame.getMaxScorePerPlayer())));
            }

            if (minigame.isAllowedFlight()) {
                mgPlayer.setCanFly(true);
                if (minigame.isFlightEnabled())
                    mgPlayer.getPlayer().setFlying(true);
            } else {
                mgPlayer.setCanFly(false);
            }

            mgPlayer.getLoadout().equipLoadout(mgPlayer);

            if (!minigame.isTeamGame()) {
                if (minigame.getLives() > 0) {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_LIVES_LIVESLEFT,
                        Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(minigame.getLives())));
                }
                mgPlayer.setStartTime(Calendar.getInstance().getTimeInMillis());
                if (!minigame.isPlayersAtStart()) {
                    if (teleport) {
                        teleportToStart(minigame);
                    }
                }
            } else {
                List<MinigamePlayer> moved = balanceGame(minigame);
                if (moved != null && !moved.isEmpty()) {
                    getStartLocations(minigame.getPlayers(), minigame);
                }
                if (!minigame.isPlayersAtStart()) {
                    if (teleport) {
                        teleportToStart(minigame);
                    }
                }

                PlayMGSound.playSound(mgPlayer, MGSounds.GAME_START.getSound());
            }
        }


        Bukkit.getServer().getPluginManager().callEvent(new StartMinigameEvent(players, minigame, teleport));
        minigame.setState(MinigameState.STARTED);
    }

    public @Nullable List<@NotNull MinigamePlayer> balanceGame(final @NotNull Minigame game) {
        if (game.isTeamGame()) {
            final @Nullable AGameMechanic mech = game.getMechanic();
            if (mech != null) {
                List<MinigamePlayer> players = new ArrayList<>(game.getPlayers());
                return mech.balanceTeam(players);
            }
        }

        return null;
    }

    public void teleportToStart(final @NotNull Minigame minigame) {
        List<MinigamePlayer> findStart = new ArrayList<>();
        for (MinigamePlayer mgPlayer : minigame.getPlayers()) {
            if (mgPlayer.getStartPos() == null) {
                findStart.add(mgPlayer);
            }
        }
        if (!findStart.isEmpty()) {
            getStartLocations(findStart, minigame);
        }

        for (final @NotNull MinigamePlayer mgPlayer : minigame.getPlayers()) {
            mgPlayer.teleport(mgPlayer.getStartPos());
        }
        minigame.setPlayersAtStart(true);
    }

    public @Nullable ResourcePack getResourcePack(final @NotNull Minigame game) {
        ResourcePackModule module = ResourcePackModule.getMinigameModule(game);
        if (module != null && module.isEnabled()) {
            ResourcePack pack = plugin.getResourcePackManager().getResourcePack(module.getResourcePackName());
            if (pack != null && pack.isValid()) {
                return pack;
            } else {
                return null;
            }
        }
        return null;
    }

    public void clearResourcePack(final @NotNull Minigame game) {
        ResourcePack pack = plugin.getResourcePackManager().getResourcePack(
            MessageManager.getStrippedMessage(MgMiscLangKey.MINIGAME_RESSOURCEPACK_EMPTY_NAME)); //todo ressource pack manager - allow multiple!
        if (pack != null && pack.isValid()) {
            for (MinigamePlayer mgPlayer : game.getPlayers()) {
                mgPlayer.applyResourcePack(pack);
            }
        }
    }

    public void getStartLocations(final @NotNull List<@NotNull MinigamePlayer> players, final @NotNull Minigame game) {
        MessageManager.sendMinigameMessage(game, MessageManager.getMessage(MgMiscLangKey.MINIGAME_STARTRANDOMIZED), MinigameMessageType.INFO, (List<MinigamePlayer>) null);
        Collections.shuffle(players);
        int pos = 0;
        Map<Team, Integer> tpos = new HashMap<>();
        for (Team t : TeamsModule.getMinigameModule(game).getTeams()) {
            tpos.put(t, 0);
        }
        if (game.isRandomizeStart()) {
            if (game.isTeamGame()) {
                MessageManager.debugMessage("Setting Starts for Team game");
                TeamsModule mod = TeamsModule.getMinigameModule(game);
                if (mod.hasTeamStartLocations()) {
                    for (Team team : mod.getTeams()) {
                        MessageManager.debugMessage("Team" + team.getDisplayName() + " is randomized");
                        Collections.shuffle(team.getStartLocations());
                    }
                } else {
                    MessageManager.debugMessage("Team game using global starts randomized");
                    Collections.shuffle(game.getStartLocations());
                }
            } else {
                MessageManager.debugMessage("Setting Starts for MP game randomized");
                Collections.shuffle(game.getStartLocations());
            }
        } else {
            if (game.isTeamGame()) {
                MessageManager.debugMessage("Setting Starts for Team game");
            } else {
                MessageManager.debugMessage("MP game using global starts");
            }
        }
        for (MinigamePlayer mgPlayer : players) {
            SafeFullLocation result = null;
            if (!game.isTeamGame()) {
                if (pos < game.getStartLocations().size()) {
                    mgPlayer.setStartTime(Calendar.getInstance().getTimeInMillis());
                    result = game.getStartLocations().get(pos);
                } else {
                    MessageManager.debugMessage("StartLocations filled - recycling from start");
                    if (!game.getStartLocations().isEmpty()) {
                        pos = 0;
                        result = game.getStartLocations().get(pos);
                    }
                }
            } else {
                Team team = mgPlayer.getTeam();
                if (team != null) {
                    if (TeamsModule.getMinigameModule(game).hasTeamStartLocations()) {
                        if (tpos.get(team) >= team.getStartLocations().size()) {
                            MessageManager.debugMessage("Team Starts for " + team.getDisplayName() + " filled - recylcing from start");
                            tpos.put(team, 0);
                        }
                        result = team.getStartLocations().get(tpos.get(team));
                        tpos.put(team, tpos.get(team) + 1);
                    } else {
                        if (pos < game.getStartLocations().size()) {
                            result = game.getStartLocations().get(pos);
                        } else {
                            MessageManager.debugMessage("StartLocations filled - recycling from start");
                            pos = 0;
                            if (!game.getStartLocations().isEmpty()) {
                                result = game.getStartLocations().get(pos);
                            }
                        }
                    }
                } else {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTEAMASSIGNED);
                }
            }

            if (result == null) {
                MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_INCORRECTSTART);
                quitMinigame(mgPlayer, false);
            } else {
                mgPlayer.setStartPos(result);
                mgPlayer.setCheckpoint(result);
                pos++;
            }
        }
    }

    public void revertToCheckpoint(final @NotNull MinigamePlayer mgPlayer) {
        RevertCheckpointEvent event = new RevertCheckpointEvent(mgPlayer);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            mgPlayer.teleport(mgPlayer.getCheckpoint());
            mgPlayer.addRevert();
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_CHECKPOINT_REVERT);

            // Reset the player's health and extinguish flames when they revert
            final Player player = mgPlayer.getPlayer();
            if (player != null) {
                player.setFireTicks(0);
                AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
                if (maxHealth != null) {
                    player.setHealth(maxHealth.getValue());
                }
                player.setFoodLevel(20);
                player.setSaturation(20f);
                player.setRemainingAir(player.getMaximumAir());
            }
        }
    }

    public void quitMinigame(final @NotNull MinigamePlayer mgPlayerQuit, final boolean forced) {
        final @NotNull Minigame minigame = mgPlayerQuit.getMinigame();
        final boolean isWinner = GameOverModule.getMinigameModule(minigame).getWinners().contains(mgPlayerQuit);

        QuitMinigameEvent event = new QuitMinigameEvent(mgPlayerQuit, minigame, forced, isWinner);
        if (event.callEvent()) {
            final Player player = mgPlayerQuit.getPlayer();
            if (minigame.isSpectator(mgPlayerQuit)) {
                if (player.getVehicle() != null) {
                    Vehicle vehicle = (Vehicle) player.getVehicle();
                    vehicle.eject();
                }
                player.setFallDistance(0);
                player.setNoDamageTicks(60);
                player.clearActivePotionEffects();
                Bukkit.getScheduler().runTask(plugin, () -> player.setFireTicks(0));

                player.closeInventory();
                if (mgPlayerQuit.isLiving()) {
                    mgPlayerQuit.restorePlayerData();
                }

                final @Nullable SafeFullLocation loc;
                if (minigame.getEndLocation() != null) {
                    loc = minigame.getEndLocation();
                } else {
                    loc = minigame.getQuitLocation();
                }

                if (loc != null) {
                    mgPlayerQuit.teleport(loc);
                } else {
                    plugin.getComponentLogger().warn("Minigame " + minigame.getName() + " has no end location set! (Player: " + mgPlayerQuit.getName() + ")");
                }

                mgPlayerQuit.setStartPos(null);
                mgPlayerQuit.removeMinigame();
                minigame.removeSpectator(mgPlayerQuit);

                for (final @NotNull MinigamePlayer mgPlayer : minigame.getPlayers()) {
                    mgPlayer.getPlayer().showPlayer(plugin, player);
                }

                MessageManager.sendMessage(mgPlayerQuit, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_SPECTATE_QUIT_PLAYERMSG,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                MessageManager.sendMinigameMessage(minigame, MessageManager.getMessage(MgMiscLangKey.PLAYER_SPECTATE_QUIT_MINIGAMEMSG,
                    Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayerQuit.displayName()),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())), MinigameMessageType.ERROR, mgPlayerQuit);
            } else {
                if (mgPlayerQuit.getEndTime() == 0) {
                    mgPlayerQuit.setEndTime(System.currentTimeMillis());
                }

                if (isWinner) {
                    GameOverModule.getMinigameModule(minigame).getWinners().remove(mgPlayerQuit);

                    if (minigame.getShowCompletionTime()) {
                        mgPlayerQuit.setCompleteTime(mgPlayerQuit.getEndTime() - mgPlayerQuit.getStartTime() + mgPlayerQuit.getStoredTime());
                    }

                } else {
                    GameOverModule.getMinigameModule(minigame).getLosers().remove(mgPlayerQuit);
                }

                if (!isWinner) {
                    if (!minigame.canSaveCheckpoint() && minigame.isEnabled()) {
                        StoredGameStats saveData = new StoredGameStats(minigame, mgPlayerQuit);
                        saveData.addStat(MinigameStatistics.Attempts, 1);

                        for (DynamicMinigameStat stat : MinigameStatistics.getDynamicStats()) {
                            if (stat.doesApply(minigame, mgPlayerQuit, false)) {
                                saveData.addStat(stat, stat.getValue(minigame, mgPlayerQuit, false));
                            }
                        }

                        saveData.applySettings(minigame.getStatSettings(saveData));

                        plugin.queueStatSave(saveData, false);
                    }
                }

                //Call Types quit.
                plugin.getMinigameManager().minigameType(minigame.getType()).quitMinigame(mgPlayerQuit, minigame, forced);

                //Call Mechanic quit.
                minigame.getMechanic().quitMinigame(mgPlayerQuit, forced);

                //Prepare player for quit
                if (player.getVehicle() != null) {
                   player.getVehicle().eject();
                }
                player.closeInventory();
                if (mgPlayerQuit.getLoadout() != null) {
                    mgPlayerQuit.getLoadout().removeLoadout(mgPlayerQuit);
                }
                mgPlayerQuit.removeMinigame();
                minigame.removePlayer(mgPlayerQuit);
                player.clearActivePotionEffects();

                player.setFallDistance(0);
                player.setNoDamageTicks(60);
                final MinigamePlayer fplayer = mgPlayerQuit;
                Bukkit.getScheduler().runTaskLater(plugin, () -> fplayer.getPlayer().setFireTicks(0), 0L);
                mgPlayerQuit.resetAllStats();
                mgPlayerQuit.setStartPos(null);
                if (mgPlayerQuit.isLiving()) {
                    mgPlayerQuit.restorePlayerData();
                    final @Nullable SafeFullLocation loc;
                    if (!isWinner) {
                        if (minigame.getQuitLocation() != null) {
                            loc = minigame.getQuitLocation();
                        } else {
                            loc = minigame.getEndLocation();
                        }
                    } else {
                        if (minigame.getEndLocation() != null) {
                            loc = minigame.getEndLocation();
                        } else {
                            loc = minigame.getQuitLocation();
                        }
                    }
                    if (loc != null) {
                        mgPlayerQuit.teleport(loc);
                    } else {
                        plugin.getComponentLogger().warn("Minigame " + minigame.getName() + " has no end location set! (Player: " + mgPlayerQuit.getName() + ")");
                    }
                } else {
                    if (!isWinner) {
                        mgPlayerQuit.setQuitPos(minigame.getQuitLocation());
                    } else {
                        mgPlayerQuit.setQuitPos(minigame.getEndLocation());
                    }
                    mgPlayerQuit.setRequiredQuit(true);
                }
                mgPlayerQuit.setStartPos(null);

                //Reward Player
                if (isWinner) {
                    mgPlayerQuit.claimTempRewardItems();
                }
                mgPlayerQuit.claimRewards();

                //Reset Minigame
                if (minigame.getPlayers().isEmpty()) {
                    //call event about this minigame has come to an end (and therefor is past an optional end phase)
                    Bukkit.getServer().getPluginManager().callEvent(new EndedMinigameEvent(minigame));

                    if (minigame.getMinigameTimer() != null) {
                        minigame.getMinigameTimer().stopTimer();
                        minigame.setMinigameTimer(null);
                    }

                    if (minigame.getFloorDegenerator() != null) {
                        minigame.getFloorDegenerator().stopDegenerator();
                    }

                    minigame.setState(MinigameState.IDLE);
                    minigame.setPlayersAtStart(false);

                    if (minigame.getRecorderData().hasData()) {
                        minigame.getRecorderData().restoreBlocks();
                        minigame.getRecorderData().restoreEntities();
                        minigame.getRecorderData().setCreatedRegenBlocks(false);
                    }

                    if (minigame.getMultiplayerTimer() != null) {
                        minigame.getMultiplayerTimer().pauseTimer();
                        minigame.getMultiplayerTimer().removeTimer();
                        minigame.setMultiplayerTimer(null);
                    }

                    if (minigame.getMultiplayerBets() != null) {
                        minigame.setMultiplayerBets(null);
                    }

                    plugin.getMinigameManager().clearClaimedScore(minigame);

                    WeatherTimeModule mod = WeatherTimeModule.getMinigameModule(minigame);
                    if (mod != null) {
                        mod.stopTimeLoop();
                    }

                    GameOverModule.getMinigameModule(minigame).stopEndGameTimer();

                    for (Team team : TeamsModule.getMinigameModule(minigame).getTeams()) {
                        team.setScore(0);
                    }
                }

                minigame.getScoreboard().resetScores(player);

                for (final @NotNull MinigamePlayer mgSpectator : minigame.getSpectators()) {
                    player.showPlayer(plugin, mgSpectator.getPlayer());
                }

                if (minigame.getPlayers().isEmpty() && !minigame.isRegenerating()) {
                    HandlerList.unregisterAll(minigame.getRecorderData());
                }

                //Send out messages
                if (!forced) {
                    MessageManager.sendMinigameMessage(minigame, MessageManager.getMessage(MgMiscLangKey.PLAYER_QUIT_PLAYERMSG,
                        Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayerQuit.displayName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())), MinigameMessageType.ERROR, mgPlayerQuit);
                }
                plugin.getLogger().info(mgPlayerQuit.getName() + " quit " + minigame);
                mgPlayerQuit.updateInventory();
            }
            if (ResourcePackModule.getMinigameModule(minigame).isEnabled()) {
                if (mgPlayerQuit.applyResourcePack(plugin.getResourcePackManager().getResourcePack("empty"))) {
                    plugin.getComponentLogger().warn("Could not apply empty resource pack to " + player.getName());
                } else {
                    MessageManager.sendMessage(mgPlayerQuit, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_RESSOURCEPACK_REMOVE);
                }
            }
            if (player.getGameMode() != GameMode.CREATIVE)
                mgPlayerQuit.setCanFly(false);

            if (!forced) {
                minigame.getScoreboardData().reload();
            }
        }
    }

    /// if the player is currently in a minigame, it will end with the player as a winner.
    public void winMinigame(final @NotNull MinigamePlayer mgPlayer) {
        if (mgPlayer.isInMinigame()) {
            final @NotNull List<@NotNull MinigamePlayer> winner = List.of(mgPlayer);
            final @NotNull List<@NotNull MinigamePlayer> losers = new ArrayList<>();
            endMinigame(mgPlayer.getMinigame(), winner, losers);
        }
    }

    public void endMinigame(final @NotNull Minigame minigame,
                            @NotNull List<@NotNull MinigamePlayer> winners,
                            @NotNull List<@NotNull MinigamePlayer> losers) {
        //When the minigame ends, the flag for recognizing the start teleportation needs to be resetted
        minigame.setPlayersAtStart(false);
        final @NotNull EndPhaseMinigameEvent event = new EndPhaseMinigameEvent(winners, losers, minigame);

        if (event.callEvent()) {
            winners = event.getWinners();
            losers = event.getLosers();
            //Call Mechanics End
            minigame.getMechanic().endMinigame(winners, losers);

            //Prepare split bet rewards
            double bets = 0;
            Set<ItemStack> betItems = new HashSet<>();
            if (minigame.getMultiplayerBets() != null && !winners.isEmpty()) {
                if (minigame.getMultiplayerBets().hasMoneyBets()) {
                    bets = Math.round(minigame.getMultiplayerBets().claimMoneyBets() / (double) winners.size());
                }

                //todo this  multiplies items, if the rest is over 0.5 and deletes items, if the rest is under it, but not 0
                // for items that are in the division rest me might want to give them to random winners.
                if (minigame.getMultiplayerBets().hasItemBets()) {
                    betItems = minigame.getMultiplayerBets().claimItemBets();

                    final List<MinigamePlayer> finalWinners = winners;
                    betItems.forEach(item -> item.setAmount((int) Math.round(item.getAmount() / (double) finalWinners.size())));
                }

                minigame.setMultiplayerBets(null);
            }

            //Broadcast Message
            broadcastEndGame(winners, minigame);

            GameOverModule gom = GameOverModule.getMinigameModule(minigame);
            boolean usedTimer = false;

            gom.setWinners(winners);
            gom.setLosers(losers);

            if (gom.getTimer() > 0 && minigame.getType() == MinigameType.MULTIPLAYER) {
                gom.startEndGameTimer();
                usedTimer = true;
            }

            for (MinigamePlayer player : losers) {
                player.setEndTime(System.currentTimeMillis());
                if (!usedTimer)
                    quitMinigame(player, true);
                PlayMGSound.playSound(player, MGSounds.LOSE.getSound());
            }

            if (minigame.getEndLocation() == null) {
                plugin.getComponentLogger().warn("The Minigame \"" + minigame.getName() + "\" has no end position!");
            }

            for (MinigamePlayer mgWinner : winners) {
                mgWinner.setEndTime(System.currentTimeMillis());

                StoredGameStats saveData = new StoredGameStats(minigame, mgWinner);
                saveData.addStat(MinigameStatistics.Attempts, 1);
                saveData.addStat(MinigameStatistics.Wins, 1);

                saveData.addStat(MinigameStatistics.Kills, mgWinner.getKills());
                saveData.addStat(MinigameStatistics.Deaths, mgWinner.getDeaths());
                saveData.addStat(MinigameStatistics.Score, mgWinner.getScore());
                saveData.addStat(MinigameStatistics.Reverts, mgWinner.getReverts());
                saveData.addStat(MinigameStatistics.CompletionTime, mgWinner.getEndTime() - mgWinner.getStartTime() + mgWinner.getStoredTime());

                if (minigame.getShowCompletionTime()) {
                    MessageManager.sendMessage(mgWinner, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_COMPLETIONTIME,
                        Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(),
                            MinigameUtils.convertTime(Duration.ofMillis(((winners.getFirst().getEndTime() - winners.getFirst().getStartTime() + winners.getFirst().getStoredTime()))))));
                }

                for (DynamicMinigameStat stat : MinigameStatistics.getDynamicStats()) {
                    if (stat.doesApply(minigame, mgWinner, true)) {
                        saveData.addStat(stat, stat.getValue(minigame, mgWinner, true));
                    }
                }

                saveData.applySettings(minigame.getStatSettings(saveData));

                if (!usedTimer)
                    quitMinigame(mgWinner, true);

                //Group money bets
                final @Nullable Player player = mgWinner.getPlayer();
                if (player != null && bets != 0) {
                    plugin.getEconomy().depositPlayer(player, bets);
                    MessageManager.sendMessage(mgWinner, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_BET_WINMONEY,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MONEY.getKey(), plugin.getEconomy().format(bets)));
                }

                // Record player completion and give rewards
                if (minigame.isEnabled()) {
                    plugin.queueStatSave(saveData, true);
                } else {
                    MessageManager.debugMessage("Skipping SQL data save for " + saveData + "; minigame is disabled");
                }

                //Item Bets (for non groups)
                if (player != null && minigame.getMultiplayerBets() != null) {
                    if (minigame.getMultiplayerBets().hasItemBets()) {
                        if (mgWinner.isInMinigame()) {
                            for (ItemStack i : betItems) {
                                mgWinner.addTempRewardItem(i);
                            }
                        } else {
                            player.give(betItems);
                        }
                        minigame.setMultiplayerBets(null);
                    }
                }

                PlayMGSound.playSound(mgWinner, MGSounds.WIN.getSound());
            }

            if (!usedTimer) {
                gom.clearLosers();
                gom.clearWinners();
            }

            plugin.getMinigameManager().clearClaimedScore(minigame);

            //Call Types End.
            plugin.getMinigameManager().minigameType(minigame.getType()).endMinigame(winners, losers, minigame);
            minigame.getScoreboardData().reload();
        }
    }

    public void broadcastEndGame(@NotNull List<@NotNull MinigamePlayer> winners, @NotNull Minigame minigame) { // todo to much hardcoded here
        if (plugin.getConfig().getBoolean("broadcastCompletion") && minigame.isEnabled()) { // todo rename to broadcastGameEnd
            TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);
            if (minigame.isTeamGame() && teamsModule != null) {
                if (!winners.isEmpty() || teamsModule.getDefaultWinner() != null) {
                    Team team;
                    if (!winners.isEmpty()) {
                        team = winners.getFirst().getTeam();
                    } else {
                        team = teamsModule.getTeam(teamsModule.getDefaultWinner());
                    }
                    Component score = Component.empty();
                    List<Team> teams = teamsModule.getTeams();
                    for (Team t : teams) {
                        score = score.append(Component.text(t.getColor().name(), t.getTextColor()).append(Component.text(t.getScore())));

                        if (t != teams.getLast()) {
                            score = score.append(Component.text(" : "));
                        }
                    }
                    Component nscore = Component.text(", ").append(MessageManager.getMessage(MgMiscLangKey.PLAYER_END_TEAM_SCORE,
                        Placeholder.component(MinigamePlaceHolderKey.SCORE.getKey(), score)));
                    if (team.getScore() > 0) {
                        MessageManager.broadcastServer(MessageManager.getMessage(MgMiscLangKey.PLAYER_END_TEAM_WIN,
                            Placeholder.unparsed(MinigamePlaceHolderKey.TEAM.getKey(),
                                "<" + team.getTextColor().asHexString() + ">" + team.getDisplayName() + "</" + team.getTextColor().asHexString() + ">"),
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                            Placeholder.component(MinigamePlaceHolderKey.SCORE.getKey(), nscore)
                        ), minigame, MinigameMessageType.WIN);
                    } else {
                        MessageManager.broadcastServer(MessageManager.getMessage(MgMiscLangKey.PLAYER_END_TEAM_WIN,
                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(team.getDisplayName(), team.getTextColor())),
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())), minigame, MinigameMessageType.WIN);
                    }
                } else {
                    MessageManager.broadcastServer(MessageManager.getMessage(MgMiscLangKey.PLAYER_END_BROADCAST_NOBODY,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())), minigame, MinigameMessageType.WIN);
                }
            } else {
                if (winners.size() == 1) {
                    Component score = Component.empty();
                    MinigamePlayer winner = winners.getFirst();
                    if (winner.getScore() != 0) {
                        score = MessageManager.getMessage(MgMiscLangKey.PLAYER_END_TEAM_SCORE,
                            Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), String.valueOf(winner.getScore())));
                    }

                    MessageManager.broadcastServer(MessageManager.getMessage(MgMiscLangKey.PLAYER_END_BROADCAST_WIN,
                        Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), winner.displayName()),
                        Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()),
                        Placeholder.component(MinigamePlaceHolderKey.SCORE.getKey(), score)), minigame, MinigameMessageType.WIN);
                } else if (winners.size() > 1) {
                    TextComponent.Builder winCompBuilder = Component.text();
                    winners.sort(Comparator.comparingInt(MinigamePlayer::getScore));

                    for (MinigamePlayer pl : winners) {
                        if (winners.indexOf(pl) < 2) {
                            winCompBuilder.append(pl.displayName());
                            if (winners.indexOf(pl) + 2 >= winners.size()) {
                                winCompBuilder.appendSpace().append(MessageManager.getMessage(MgMiscLangKey.AND)).appendSpace();
                            } else {
                                winCompBuilder.append(Component.text(", "));
                            }
                        } else {
                            winCompBuilder.append(Component.text(String.valueOf(winners.size() - 3))).
                                appendSpace().append(MessageManager.getMessage(MgMiscLangKey.PLAYER_END_BROADCAST_OTHERS));
                        }
                    }
                    MessageManager.broadcastServer(
                        MessageManager.getMessage(MgMiscLangKey.PLAYER_END_BROADCAST_WIN,
                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), winCompBuilder.build()),
                            Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()),
                            Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), "")),
                        minigame, MinigameMessageType.WIN);
                } else {
                    MessageManager.broadcastServer(MessageManager.getMessage(MgMiscLangKey.PLAYER_END_BROADCAST_NOBODY,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())), minigame, MinigameMessageType.ERROR);
                }
            }
        }
    }

    @Deprecated
    public @NotNull List<@NotNull Player> playersInMinigame() {
        List<Player> players = new ArrayList<>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (hasMinigamePlayer(player.getUniqueId())) {
                players.add(player);
            }
        }
        return players;
    }

    public void addMinigamePlayer(final @NotNull Player player) {
        minigamePlayers.put(player.getUniqueId(), new MinigamePlayer(player));
    }

    public void removeMinigamePlayer(final @NotNull Player player) {
        minigamePlayers.remove(player.getUniqueId());
    }

    /**
     * @return null, if the given player was null, the respecting MinigamePlayer object otherwise
     */
    @Contract("null -> null; !null -> !null")
    public @Nullable MinigamePlayer getMinigamePlayer(final @Nullable Player player) {
        if (player == null) {
            return null;
        }

        return minigamePlayers.computeIfAbsent(player.getUniqueId(), __ -> new MinigamePlayer(player));
    }

    public @Nullable MinigamePlayer getMinigamePlayer(final @NotNull UUID uuid) {
        final @Nullable MinigamePlayer mgPlayer = minigamePlayers.get(uuid);

        if (mgPlayer != null) {
            return mgPlayer;
        }

        return getMinigamePlayer(Bukkit.getPlayer(uuid));
    }


    /**
     * @see #getMinigamePlayer(UUID)
     */
    public @Nullable MinigamePlayer getMinigamePlayer(final @NotNull String playerName) {
        return getMinigamePlayer(plugin.getServer().getPlayer(playerName));
    }

    public @NotNull Collection<MinigamePlayer> getAllMinigamePlayers() {
        return minigamePlayers.values();
    }

    /**
     * @see #hasMinigamePlayer(UUID)
     */
    public boolean hasMinigamePlayer(@NotNull Player player) {
        return hasMinigamePlayer(player.getUniqueId());
    }

    public boolean hasMinigamePlayer(@NotNull UUID uuid) {
        return minigamePlayers.containsKey(uuid);
    }

// the whole singleplayer flag system is unused.
//    public @NotNull List<@NotNull String> checkRequiredFlags(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame) {
//        List<String> checkpoints = new ArrayList<>(minigame.getSinglePlayerFlags());
//        List<String> pchecks = mgPlayer.getSinglePlayerFlags();
//
//        if (!pchecks.isEmpty()) {
//            checkpoints.removeAll(pchecks);
//        }
//
//        return checkpoints;
//    }

    public boolean onPartyMode() {
        return partyMode;
    }

    public void setPartyMode(boolean mode) {
        partyMode = mode;
    }

    public void partyMode(final @NotNull MinigamePlayer mgPlayer) {
        if (onPartyMode()) {
            final @NotNull Location loc = mgPlayer.getLocation();
            loc.getWorld().spawn(loc, Firework.class, firework -> {
                final @NotNull FireworkMeta fwm = firework.getFireworkMeta();

               final @NotNull Random chance = new Random();
               @NotNull Type type = Type.BALL_LARGE;
               if (chance.nextInt(100) < 50) {
                   type = Type.BALL;
               }

               final @NotNull Color col = Color.fromRGB(chance.nextInt(255), chance.nextInt(255), chance.nextInt(255));

               final @NotNull FireworkEffect effect = FireworkEffect.builder().with(type).withColor(col).flicker(chance.nextBoolean()).trail(chance.nextBoolean()).build();
               fwm.addEffect(effect);
               fwm.setPower(1);
               firework.setFireworkMeta(fwm);
            });
        }
    }

    public void partyMode(final @NotNull MinigamePlayer player, final int amount, final long delay) {
        if (!onPartyMode()) {
            return;
        }
        partyMode(player);
        if (amount == 1) return;
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> partyMode(player, amount - 1, delay), delay);
    }

    public @NotNull List<@NotNull String> getDeniedCommands() {
        return deniedCommands;
    }

    public void setDeniedCommands(@NotNull List<@NotNull String> deniedCommands) {
        this.deniedCommands = deniedCommands;
    }

    public void addDeniedCommand(@NotNull String command) {
        deniedCommands.add(command);
    }

    public void removeDeniedCommand(@NotNull String command) {
        deniedCommands.remove(command);
    }

    public void saveDeniedCommands() {
        plugin.getConfig().set("disabledCommands", deniedCommands);
        plugin.saveConfig();
    }

    public void loadDeniedCommands() {
        setDeniedCommands(plugin.getConfig().getStringList("disabledCommands"));
    }
}
