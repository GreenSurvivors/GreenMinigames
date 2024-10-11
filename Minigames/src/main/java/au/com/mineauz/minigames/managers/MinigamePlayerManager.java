package au.com.mineauz.minigames.managers;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.MultiplayerBets;
import au.com.mineauz.minigames.commands.QuitCommand;
import au.com.mineauz.minigames.events.*;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.mechanics.GameMechanicBase;
import au.com.mineauz.minigames.mechanics.GameMechanics;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.MinigameState;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.modules.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.ResourcePack;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

/**
 * Manager Class of all players playing Minigames.
 **/
public class MinigamePlayerManager {
    private static final @NotNull Minigames plugin = Minigames.getPlugin();
    private final @NotNull Map<@NotNull UUID, @NotNull MinigamePlayer> minigamePlayers = new HashMap<>();
    private final @NotNull List<@NotNull MinigamePlayer> applyingPack = new ArrayList<>();
    private final @NotNull MinigameManager mgManager = plugin.getMinigameManager();
    private boolean partyMode = false;
    private @NotNull List<@NotNull String> deniedCommands = new ArrayList<>();

    public MinigamePlayerManager() {
    }

    public @NotNull List<@NotNull MinigamePlayer> getApplyingPack() {
        return applyingPack;
    }

    public void needsResourcePack(@NotNull MinigamePlayer mgPlayer) {
        applyingPack.add(mgPlayer);
    }

    public void joinMinigame(@NotNull Minigame minigame, @NotNull MinigamePlayer mgPlayer, boolean isBetting, double betAmount) {
        MinigameType type = minigame.getType();
        JoinMinigameEvent event = new JoinMinigameEvent(mgPlayer, minigame);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            Minigames.getCmpnntLogger().info("Join Event was cancelled: " + event);
            return;
        }
        if (!mgManager.minigameStartStateCheck(minigame, mgPlayer)) return;
        //Do betting stuff
        if (isBetting && !handleMoneyBet(minigame, mgPlayer, betAmount)) {
            return;
        }
        //Try to apply ressource pack
        ResourcePack pack = getResourcePack(minigame);
        if (pack != null && pack.isValid()) {
            if (mgPlayer.applyResourcePack(pack)) {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_RESSOURCEPACK_APPLY);
            }
        }
        //Check if Minigame is full
        if (minigame.isGameFull()) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_FULL);
            return;
        }
        //Check if Minigame has a lobby and teleport them there
        if (!mgManager.teleportPlayerOnJoin(minigame, mgPlayer)) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOLOBY);
            return;
        }
        //Give them the game type name
        if (minigame.getGameTypeName() == null) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.SUCCESS, MgMiscLangKey.PLAYER_JOIN_PLAYERINFO,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getType().getName()));
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.SUCCESS, MgMiscLangKey.PLAYER_JOIN_PLAYERINFO,
                    Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getGameTypeName()));
        }

        //Give them the objective
        if (minigame.getObjective() != null) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.NONE, MgCommandLangKey.COMMAND_DIVIDER_LARGE);
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_JOIN_OBJECTIVE,
                    Placeholder.component(MinigamePlaceHolderKey.OBJECTIVE.getKey(), minigame.getObjective()));
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.NONE, MgCommandLangKey.COMMAND_DIVIDER_LARGE);
        }
        //Prepare regeneration region for rollback.
        mgManager.addRegenDataToRecorder(minigame);
        //Standardize player
        mgPlayer.storePlayerData();
        mgPlayer.setMinigame(minigame);
        minigame.addPlayer(mgPlayer);
        WeatherTimeModule mod = WeatherTimeModule.getMinigameModule(minigame);
        if (mod != null) {
            mod.applyCustomTime(mgPlayer);
            mod.applyCustomWeather(mgPlayer);
        }
        mgPlayer.setCheckpoint(mgPlayer.getPlayer().getLocation());
        mgPlayer.getPlayer().setFallDistance(0);
        mgPlayer.getPlayer().setWalkSpeed(0.2f);
        mgPlayer.setStartTime(Calendar.getInstance().getTimeInMillis());
        mgPlayer.setGamemode(minigame.getDefaultGamemode());
        mgPlayer.getPlayer().setAllowFlight(false);
        for (PotionEffect potion : mgPlayer.getPlayer().getActivePotionEffects()) {
            mgPlayer.getPlayer().removePotionEffect(potion.getType());
        }
        //Hide Spectators
        for (MinigamePlayer pl : minigame.getSpectators()) {
            mgPlayer.getPlayer().hidePlayer(plugin, pl.getPlayer());
        }

        if (minigame.getPlayers().size() == 1) {
            //Register regen recorder events
            if (minigame.hasRegenArea())
                Bukkit.getServer().getPluginManager().registerEvents(new RegenRecorder(minigame), plugin);
            if (mod != null) mod.startTimeLoop();
        }
        //Call Type specific join
        mgManager.minigameType(type).joinMinigame(mgPlayer, minigame);

        //Call Mechanic specific join
        minigame.getMechanic().onJoinMinigame(minigame, mgPlayer);

        //Send other players the join message.
        MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(
                        MgMiscLangKey.PLAYER_JOIN_PLAYERMSG,
                        Placeholder.unparsed(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())),
                MinigameMessageType.INFO);
        mgPlayer.updateInventory();

        if (minigame.canDisplayScoreboard()) {
            mgPlayer.getPlayer().setScoreboard(minigame.getScoreboard());
            minigame.setScore(mgPlayer, 1);
            minigame.setScore(mgPlayer, 0);
        }
        if (minigame.getState() == MinigameState.STARTING && minigame.canLateJoin()) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_LATEJOINWAIT,
                    Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(),
                            MinigameUtils.convertTime(Duration.ofSeconds(minigame.getMpTimer().getStartWaitTimeLeft()))));
        }
    }

    /**
     * @param minigame  the minigame to bet on
     * @param mgPlayer  the player who was betting
     * @param betAmount the amount in economy money. might be 0, if the player was betting an item
     * @return true if the player could successfully bet
     */
    private boolean handleMoneyBet(@NotNull Minigame minigame, @NotNull MinigamePlayer mgPlayer, double betAmount) {
        if (minigame.getMpBets() == null && (mgPlayer.getPlayer().getInventory().getItemInMainHand().getType() != Material.AIR || betAmount != 0)) {
            minigame.setMpBets(new MultiplayerBets());
        }

        MultiplayerBets mpBets = minigame.getMpBets();
        ItemStack item = mgPlayer.getPlayer().getInventory().getItemInMainHand().clone();

        if (mpBets != null) {
            if (!mpBets.hasAlreadyBet(mgPlayer)) {
                //has the player not already bet and are they the highest better?
                if (mpBets.isHighestBetter(betAmount, item)) {
                    if (betAmount >= 0) {
                        if (plugin.getEconomy().getBalance(mgPlayer.getPlayer().getPlayer()) >= betAmount) {
                            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_BET_PLAYERMSG);

                            mpBets.addBet(mgPlayer, betAmount);
                            plugin.getEconomy().withdrawPlayer(mgPlayer.getPlayer().getPlayer(), betAmount);

                            return true;
                        } else {
                            //not enough money
                            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_BET_NOTENOUGHMONEY);
                            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_BET_NOTENOUGHMONEYINFO,
                                    Placeholder.unparsed(MinigamePlaceHolderKey.MONEY.getKey(), Minigames.getPlugin().getEconomy().format(minigame.getMpBets().getHighestMoneyBet())));
                        }
                    }

                    if (item.getType() != Material.AIR) {
                        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_BET_PLAYERMSG);
                        mgPlayer.getPlayer().getInventory().remove(item);

                        mpBets.addBet(mgPlayer, item);

                        return true;
                    } else {
                        //no item to bet, and betAmount == 0
                        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_BET_PLAYERNOBET);
                        return false; //maybe? or better true in this case?
                    }
                } else {
                    if (mpBets.getHighestMoneyBet() > 0) {
                        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_BET_INCORRECTMONEYAMOUNTINFO,
                                Placeholder.unparsed(MinigamePlaceHolderKey.MONEY.getKey(), Minigames.getPlugin().getEconomy().format(mpBets.getHighestMoneyBet())));
                    }
                    //todo connect both messages with an "or"
                    if (mpBets.getHighestItemBet().getType() != Material.AIR) {

                        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_BET_INCORRECTITEMAMOUNTINFO,
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

    public void spectateMinigame(@NotNull Minigame minigame, @NotNull MinigamePlayer mgPlayer) {
        SpectateMinigameEvent event = new SpectateMinigameEvent(mgPlayer, minigame);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            boolean tpd;
            if (minigame.getSpectatorLocation() != null) {
                tpd = mgPlayer.teleport(minigame.getSpectatorLocation());
            } else {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOSPECTATELOC);
                return;
            }
            if (!tpd) {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTELEPORTALLOWED);
                return;
            }
            mgPlayer.storePlayerData();
            mgPlayer.setMinigame(minigame);
            mgPlayer.setGamemode(GameMode.ADVENTURE);

            minigame.addSpectator(mgPlayer);

            if (minigame.canSpectateFly()) {
                mgPlayer.getPlayer().setAllowFlight(true);
            }
            for (MinigamePlayer pl : minigame.getPlayers()) {
                pl.getPlayer().hidePlayer(plugin, mgPlayer.getPlayer());
            }

            if (minigame.canDisplayScoreboard()) {
                mgPlayer.getPlayer().setScoreboard(minigame.getScoreboard());
            }

            for (PotionEffect potion : mgPlayer.getPlayer().getActivePotionEffects()) {
                mgPlayer.getPlayer().removePotionEffect(potion.getType());
            }
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SPECTATE_JOIN_PLAYERMSG,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));

            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SPECTATE_JOIN_PLAYERHELP,
                    Placeholder.component(MinigamePlaceHolderKey.COMMAND.getKey(),
                            Component.text("\"").append(new QuitCommand().getUsage()).append(Component.text("\""))));
            MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_SPECTATE_JOIN_MINIGAMEMSG,
                            Placeholder.unparsed(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.getName()),
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())),
                    MinigameMessageType.INFO, mgPlayer);
        }
    }

    public void startMPMinigame(@NotNull Minigame minigame) {
        startMPMinigame(minigame, LobbySettingsModule.getMinigameModule(minigame).isTeleportOnStart());
    }

    public void startMPMinigame(@NotNull Minigame minigame, boolean teleport) {
        List<MinigamePlayer> players = new ArrayList<>(minigame.getPlayers());
        for (MinigamePlayer mgPlayer : players) {
            if (minigame.getMaxScore() != 0) {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_SCORETOWIN,
                        Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), String.valueOf(minigame.getMaxScorePerPlayer())));
            }
            if (minigame.isAllowedFlight()) {
                mgPlayer.setCanFly(true);
            }
            if (minigame.isFlightEnabled() && mgPlayer.canFly()) {
                mgPlayer.getPlayer().setFlying(true);
            }
            mgPlayer.getLoadout().equipLoadout(mgPlayer);

            if (!minigame.isTeamGame()) {
                if (minigame.getLives() > 0) {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_LIVES_LIVESLEFT,
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

    public @Nullable List<@NotNull MinigamePlayer> balanceGame(@NotNull Minigame game) {
        List<MinigamePlayer> result = null;
        if (game.isTeamGame()) {
            GameMechanicBase mech = GameMechanics.getGameMechanic(game.getMechanicName());
            if (mech != null) {
                List<MinigamePlayer> players = new ArrayList<>(game.getPlayers());
                result = mech.balanceTeam(players, game);
            }
        }
        return result;
    }

    public void teleportToStart(@NotNull Minigame minigame) {
        List<MinigamePlayer> findStart = new ArrayList<>();
        for (MinigamePlayer mgPlayer : minigame.getPlayers()) {
            if (mgPlayer.getStartPos() == null) {
                findStart.add(mgPlayer);
            }
        }
        if (!findStart.isEmpty()) {
            getStartLocations(findStart, minigame);
        }

        for (MinigamePlayer mgPlayer : minigame.getPlayers()) {
            mgPlayer.teleport(mgPlayer.getStartPos());
        }
        minigame.setPlayersAtStart(true);
    }

    public @Nullable ResourcePack getResourcePack(@NotNull Minigame game) {
        ResourcePackModule module = ResourcePackModule.getMinigameModule(game);
        if (module != null && module.isEnabled()) {
            ResourcePack pack = plugin.getResourceManager().getResourcePack(module.getName());
            if (pack != null && pack.isValid()) {
                return pack;
            } else {
                return null;
            }
        }
        return null;
    }

    public void clearResourcePack(@NotNull Minigame game) {
        ResourcePack pack = plugin.getResourceManager().getResourcePack(
                MinigameMessageManager.getStrippedMgMessage(MgMiscLangKey.MINIGAME_RESSOURCEPACK_EMPTY_NAME)); //todo ressource pack manager - allow multiple!
        if (pack != null && pack.isValid()) {
            for (MinigamePlayer mgPlayer : game.getPlayers()) {
                mgPlayer.applyResourcePack(pack);
            }
        }
    }

    public void getStartLocations(@NotNull List<@NotNull MinigamePlayer> players, @NotNull Minigame game) {
        MinigameMessageManager.sendMinigameMessage(game, MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_STARTRANDOMIZED), MinigameMessageType.INFO, (List<MinigamePlayer>) null);
        Collections.shuffle(players);
        int pos = 0;
        Map<Team, Integer> tpos = new HashMap<>();
        for (Team t : TeamsModule.getMinigameModule(game).getTeams()) {
            tpos.put(t, 0);
        }
        if (game.isRandomizeStart()) {
            if (game.isTeamGame()) {
                MinigameMessageManager.debugMessage("Setting Starts for Team game");
                TeamsModule mod = TeamsModule.getMinigameModule(game);
                if (mod.hasTeamStartLocations()) {
                    for (Team team : mod.getTeams()) {
                        MinigameMessageManager.debugMessage("Team" + team.getDisplayName() + " is randomized");
                        Collections.shuffle(team.getStartLocations());
                    }
                } else {
                    MinigameMessageManager.debugMessage("Team game using global starts randomized");
                    Collections.shuffle(game.getStartLocations());
                }
            } else {
                MinigameMessageManager.debugMessage("Setting Starts for MP game randomized");
                Collections.shuffle(game.getStartLocations());
            }
        } else {
            if (game.isTeamGame()) {
                MinigameMessageManager.debugMessage("Setting Starts for Team game");
            } else {
                MinigameMessageManager.debugMessage("MP game using global starts");
            }
        }
        for (MinigamePlayer mgPlayer : players) {
            Location result = null;
            if (!game.isTeamGame()) {
                if (pos < game.getStartLocations().size()) {
                    mgPlayer.setStartTime(Calendar.getInstance().getTimeInMillis());
                    result = game.getStartLocations().get(pos);
                } else {
                    MinigameMessageManager.debugMessage("StartLocations filled - recycling from start");
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
                            MinigameMessageManager.debugMessage("Team Starts for " + team.getDisplayName() + " filled - recylcing from start");
                            tpos.put(team, 0);
                        }
                        result = team.getStartLocations().get(tpos.get(team));
                        tpos.put(team, tpos.get(team) + 1);
                    } else {
                        if (pos < game.getStartLocations().size()) {
                            result = game.getStartLocations().get(pos);
                        } else {
                            MinigameMessageManager.debugMessage("StartLocations filled - recycling from start");
                            pos = 0;
                            if (!game.getStartLocations().isEmpty()) {
                                result = game.getStartLocations().get(pos);
                            }
                        }
                    }
                } else {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTEAMASSIGNED);
                }
            }

            if (result == null) {
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_INCORRECTSTART);
                quitMinigame(mgPlayer, false);
            } else {
                mgPlayer.setStartPos(result);
                mgPlayer.setCheckpoint(result);
                pos++;
            }
        }
    }

    public void revertToCheckpoint(@NotNull MinigamePlayer mgPlayer) {
        RevertCheckpointEvent event = new RevertCheckpointEvent(mgPlayer);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            mgPlayer.teleport(mgPlayer.getCheckpoint());
            mgPlayer.addRevert();
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_CHECKPOINT_REVERT);

            // Reset the player's health and extinguish flames when they revert
            Player player = mgPlayer.getPlayer();
            if (player.isOnline()) {
                player.setFireTicks(0);
                AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                if (maxHealth != null) {
                    player.setHealth(maxHealth.getValue());
                }
                player.setFoodLevel(20);
                player.setSaturation(20f);
                player.setRemainingAir(player.getMaximumAir());
            }
        }
    }

    public void quitMinigame(@NotNull MinigamePlayer mgPlayer, boolean forced) {
        Minigame minigame = mgPlayer.getMinigame();
        boolean isWinner = GameOverModule.getMinigameModule(minigame).getWinners().contains(mgPlayer);

        QuitMinigameEvent event = new QuitMinigameEvent(mgPlayer, minigame, forced, isWinner);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            if (minigame.isSpectator(mgPlayer)) {
                if (mgPlayer.getPlayer().getVehicle() != null) {
                    Vehicle vehicle = (Vehicle) mgPlayer.getPlayer().getVehicle();
                    vehicle.eject();
                }
                mgPlayer.getPlayer().setFallDistance(0);
                mgPlayer.getPlayer().setNoDamageTicks(60);
                final Player fplayer = mgPlayer.getPlayer();
                for (PotionEffect potion : mgPlayer.getPlayer().getActivePotionEffects()) {
                    mgPlayer.getPlayer().removePotionEffect(potion.getType());
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> fplayer.setFireTicks(0));

                mgPlayer.getPlayer().closeInventory();
                if (mgPlayer.isLiving()) {
                    mgPlayer.restorePlayerData();
                }

                Location loc;
                if (minigame.getEndLocation() != null) {
                    loc = minigame.getEndLocation();
                } else {
                    loc = minigame.getQuitLocation();
                }

                if (loc != null) {
                    mgPlayer.teleport(loc);
                } else {
                    Minigames.getCmpnntLogger().warn("Minigame " + minigame.getName() + " has no end location set! (Player: " + mgPlayer.getName() + ")");
                }

                mgPlayer.setStartPos(null);
                mgPlayer.removeMinigame();
                minigame.removeSpectator(mgPlayer);

                for (MinigamePlayer pl : minigame.getPlayers()) {
                    pl.getPlayer().showPlayer(plugin, mgPlayer.getPlayer());
                }

                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_SPECTATE_QUIT_PLAYERMSG,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()));
                MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_SPECTATE_QUIT_MINIGAMEMSG,
                        Placeholder.unparsed(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.getName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())), MinigameMessageType.ERROR, mgPlayer);
            } else {
                if (mgPlayer.getEndTime() == 0) {
                    mgPlayer.setEndTime(System.currentTimeMillis());
                }

                if (isWinner) {
                    GameOverModule.getMinigameModule(minigame).getWinners().remove(mgPlayer);

                    if (minigame.getShowCompletionTime()) {
                        mgPlayer.setCompleteTime(mgPlayer.getEndTime() - mgPlayer.getStartTime() + mgPlayer.getStoredTime());
                    }

                } else {
                    GameOverModule.getMinigameModule(minigame).getLosers().remove(mgPlayer);
                }

                if (!isWinner) {
                    if (!minigame.canSaveCheckpoint() && minigame.isEnabled()) {
                        StoredGameStats saveData = new StoredGameStats(minigame, mgPlayer);
                        saveData.addStat(MinigameStatistics.Attempts, 1);

                        for (DynamicMinigameStat stat : MinigameStatistics.getDynamicStats()) {
                            if (stat.doesApply(minigame, mgPlayer, false)) {
                                saveData.addStat(stat, stat.getValue(minigame, mgPlayer, false));
                            }
                        }

                        saveData.applySettings(minigame.getStatSettings(saveData));

                        plugin.queueStatSave(saveData, false);
                    }
                }

                //Call Types quit.
                mgManager.minigameType(minigame.getType()).quitMinigame(mgPlayer, minigame, forced);

                //Call Mechanic quit.
                minigame.getMechanic().quitMinigame(minigame, mgPlayer, forced);

                //Prepare player for quit
                if (mgPlayer.getPlayer().getVehicle() != null) {
                    Vehicle vehicle = (Vehicle) mgPlayer.getPlayer().getVehicle();
                    vehicle.eject();
                }
                mgPlayer.getPlayer().closeInventory();
                if (mgPlayer.getLoadout() != null) {
                    mgPlayer.getLoadout().removeLoadout(mgPlayer);
                }
                mgPlayer.removeMinigame();
                minigame.removePlayer(mgPlayer);
                for (PotionEffect potion : mgPlayer.getPlayer().getActivePotionEffects()) {
                    mgPlayer.getPlayer().removePotionEffect(potion.getType());
                }

                mgPlayer.getPlayer().setFallDistance(0);
                mgPlayer.getPlayer().setNoDamageTicks(60);
                final MinigamePlayer fplayer = mgPlayer;
                Bukkit.getScheduler().runTaskLater(plugin, () -> fplayer.getPlayer().setFireTicks(0), 0L);
                mgPlayer.resetAllStats();
                mgPlayer.setStartPos(null);
                if (mgPlayer.isLiving()) {
                    mgPlayer.restorePlayerData();
                    Location loc;
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
                        mgPlayer.teleport(loc);
                    } else {
                        Minigames.getCmpnntLogger().warn("Minigame " + minigame.getName() + " has no end location set! (Player: " + mgPlayer.getName() + ")");
                    }
                } else {
                    if (!isWinner) {
                        mgPlayer.setQuitPos(minigame.getQuitLocation());
                    } else {
                        mgPlayer.setQuitPos(minigame.getEndLocation());
                    }
                    mgPlayer.setRequiredQuit(true);
                }
                mgPlayer.setStartPos(null);

                //Reward Player
                if (isWinner) {
                    mgPlayer.claimTempRewardItems();
                }
                mgPlayer.claimRewards();

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

                    if (minigame.getMpTimer() != null) {
                        minigame.getMpTimer().pauseTimer();
                        minigame.getMpTimer().removeTimer();
                        minigame.setMpTimer(null);
                    }

                    if (minigame.getMpBets() != null) {
                        minigame.setMpBets(null);
                    }

                    mgManager.clearClaimedScore(minigame);

                    WeatherTimeModule mod = WeatherTimeModule.getMinigameModule(minigame);
                    if (mod != null) {
                        mod.stopTimeLoop();
                    }

                    GameOverModule.getMinigameModule(minigame).stopEndGameTimer();

                    for (Team team : TeamsModule.getMinigameModule(minigame).getTeams()) {
                        team.setScore(0);
                    }
                }

                minigame.getScoreboard().resetScores(mgPlayer.getName());

                for (MinigamePlayer pl : minigame.getSpectators()) {
                    mgPlayer.getPlayer().showPlayer(plugin, pl.getPlayer());
                }

                if (minigame.getPlayers().isEmpty() && !minigame.isRegenerating()) {
                    HandlerList.unregisterAll(minigame.getRecorderData());
                }

                //Send out messages
                if (!forced) {
                    MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_QUIT_PLAYERMSG,
                            Placeholder.unparsed(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.getName()),
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())), MinigameMessageType.ERROR, mgPlayer);
                }
                plugin.getLogger().info(mgPlayer.getName() + " quit " + minigame);
                mgPlayer.updateInventory();
            }
            if (ResourcePackModule.getMinigameModule(minigame).isEnabled()) {
                if (mgPlayer.applyResourcePack(plugin.getResourceManager().getResourcePack("empty"))) {
                    Minigames.getCmpnntLogger().warn("Could not apply empty resource pack to " + mgPlayer.getDisplayName());
                } else {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_RESSOURCEPACK_REMOVE);
                }
            }
            if (mgPlayer.getPlayer().getGameMode() != GameMode.CREATIVE)
                mgPlayer.setCanFly(false);

            if (!forced) {
                minigame.getScoreboardData().reload();
            }
        }
    }

    public void endMinigame(@NotNull MinigamePlayer mgPlayer) {
        if (mgPlayer.isInMinigame()) {
            List<MinigamePlayer> winner = List.of(mgPlayer);
            List<MinigamePlayer> losers = new ArrayList<>();
            endMinigame(mgPlayer.getMinigame(), winner, losers);
        }
    }

    public void endMinigame(@NotNull Minigame minigame, @NotNull List<@NotNull MinigamePlayer> winners, @NotNull List<@NotNull MinigamePlayer> losers) {
        //When the minigame ends, the flag for recognizing the start teleportation needs to be resetted
        minigame.setPlayersAtStart(false);
        EndPhaseMinigameEvent event = new EndPhaseMinigameEvent(winners, losers, minigame);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            winners = event.getWinners();
            losers = event.getLosers();
            //Call Mechanics End
            minigame.getMechanic().endMinigame(minigame, winners, losers);

            //Prepare split bet rewards
            double bets = 0;
            Set<ItemStack> betItems = new HashSet<>();
            if (minigame.getMpBets() != null && !winners.isEmpty()) {
                if (minigame.getMpBets().hasMoneyBets()) {
                    bets = Math.round(minigame.getMpBets().claimMoneyBets() / (double) winners.size());
                }

                //todo this  multiplies items, if the rest is over 0.5 and deletes items, if the rest is under it, but not 0
                // for items that are in the division rest me might want to give them to random winners.
                if (minigame.getMpBets().hasItemBets()) {
                    betItems = minigame.getMpBets().claimItemBets();

                    final List<MinigamePlayer> finalWinners = winners;
                    betItems.forEach(item -> item.setAmount((int) Math.round(item.getAmount() / (double) finalWinners.size())));
                }

                minigame.setMpBets(null);
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
                Minigames.getCmpnntLogger().warn("The Minigame \"" + minigame.getName() + "\" has no end position!");
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
                    MinigameMessageManager.sendMgMessage(mgWinner, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_COMPLETIONTIME,
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
                if (bets != 0) {
                    plugin.getEconomy().depositPlayer(mgWinner.getPlayer().getPlayer(), bets);
                    MinigameMessageManager.sendMgMessage(mgWinner, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_BET_WINMONEY,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MONEY.getKey(), Minigames.getPlugin().getEconomy().format(bets)));
                }

                // Record player completion and give rewards
                if (minigame.isEnabled()) {
                    plugin.queueStatSave(saveData, true);
                } else {
                    MinigameMessageManager.debugMessage("Skipping SQL data save for " + saveData + "; minigame is disabled");
                }

                //Item Bets (for non groups)
                if (minigame.getMpBets() != null) {
                    if (minigame.getMpBets().hasItemBets()) {
                        if (mgWinner.isInMinigame()) {
                            for (ItemStack i : betItems) {
                                mgWinner.addTempRewardItem(i);
                            }
                        } else {
                            for (ItemStack i : betItems) {
                                for (ItemStack notAdded : mgWinner.getPlayer().getInventory().addItem(i).values()) {
                                    //drop items the player had no room for
                                    mgWinner.getPlayer().getWorld().dropItemNaturally(mgWinner.getLocation(), notAdded);
                                }

                            }
                        }
                        minigame.setMpBets(null);
                    }
                }

                PlayMGSound.playSound(mgWinner, MGSounds.WIN.getSound());
            }

            if (!usedTimer) {
                gom.clearLosers();
                gom.clearWinners();
            }

            mgManager.clearClaimedScore(minigame);

            //Call Types End.
            mgManager.minigameType(minigame.getType()).endMinigame(winners, losers, minigame);
            minigame.getScoreboardData().reload();
        }
    }

    public void broadcastEndGame(@NotNull List<@NotNull MinigamePlayer> winners, @NotNull Minigame minigame) { // todo to much hardcoded here
        if (plugin.getConfig().getBoolean("broadcastCompletion") && minigame.isEnabled()) {
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
                    Component nscore = Component.text(", ").append(MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_TEAM_SCORE,
                            Placeholder.component(MinigamePlaceHolderKey.SCORE.getKey(), score)));
                    if (team.getScore() > 0) {
                        MinigameMessageManager.broadcastServer(MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_TEAM_WIN,
                                Placeholder.unparsed(MinigamePlaceHolderKey.TEAM.getKey(),
                                        "<" + team.getTextColor().asHexString() + ">" + team.getDisplayName() + "</" + team.getTextColor().asHexString() + ">"),
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName()),
                                Placeholder.component(MinigamePlaceHolderKey.SCORE.getKey(), nscore)
                        ), minigame, MinigameMessageType.WIN);
                    } else {
                        MinigameMessageManager.broadcastServer(MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_TEAM_WIN,
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(team.getDisplayName(), team.getTextColor())),
                                Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())), minigame, MinigameMessageType.WIN);
                    }
                } else {
                    MinigameMessageManager.broadcastServer(MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_BROADCAST_NOBODY,
                            Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())), minigame, MinigameMessageType.WIN);
                }
            } else {
                if (winners.size() == 1) {
                    Component score = Component.empty();
                    MinigamePlayer winner = winners.getFirst();
                    if (winner.getScore() != 0) {
                        score = MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_TEAM_SCORE,
                                Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), String.valueOf(winner.getScore())));
                    }

                    if (minigame.usePlayerDisplayNames()) {
                        MinigameMessageManager.broadcastServer(MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_BROADCAST_WIN,
                                Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), winner.displayName()),
                                Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()),
                                Placeholder.component(MinigamePlaceHolderKey.SCORE.getKey(), score)), minigame, MinigameMessageType.WIN);
                    } else {
                        MinigameMessageManager.broadcastServer(MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_BROADCAST_WIN,
                                Placeholder.unparsed(MinigamePlaceHolderKey.PLAYER.getKey(), winner.getName()),
                                Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()),
                                Placeholder.component(MinigamePlaceHolderKey.SCORE.getKey(), score)), minigame, MinigameMessageType.WIN);
                    }
                } else if (winners.size() > 1) {
                    TextComponent.Builder winCompBuilder = Component.text();
                    winners.sort(Comparator.comparingInt(MinigamePlayer::getScore));

                    for (MinigamePlayer pl : winners) {
                        if (winners.indexOf(pl) < 2) {
                            if (minigame.usePlayerDisplayNames()) {
                                winCompBuilder.append(pl.displayName());
                            } else {
                                winCompBuilder.append(Component.text(pl.getName()));
                            }
                            if (winners.indexOf(pl) + 2 >= winners.size()) {
                                winCompBuilder.appendSpace().append(MinigameMessageManager.getMgMessage(MgMiscLangKey.AND)).appendSpace();
                            } else {
                                winCompBuilder.append(Component.text(", "));
                            }
                        } else {
                            winCompBuilder.append(Component.text(String.valueOf(winners.size() - 3))).
                                    appendSpace().append(MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_BROADCAST_OTHERS));
                        }
                    }
                    MinigameMessageManager.broadcastServer(
                            MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_BROADCAST_WIN,
                                    Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), winCompBuilder.build()),
                                    Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()),
                                    Placeholder.unparsed(MinigamePlaceHolderKey.SCORE.getKey(), "")),
                            minigame, MinigameMessageType.WIN);
                } else {
                    MinigameMessageManager.broadcastServer(MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_END_BROADCAST_NOBODY,
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

    public void addMinigamePlayer(@NotNull Player player) {
        minigamePlayers.put(player.getUniqueId(), new MinigamePlayer(player));
    }

    public void removeMinigamePlayer(@NotNull Player player) {
        minigamePlayers.remove(player.getUniqueId());
    }

    /**
     * @return null, if the given player was null, the respecting MinigamePlayer object otherwise
     */
    @Contract("null -> null; !null -> !null")
    public @Nullable MinigamePlayer getMinigamePlayer(@Nullable Player player) {
        if (player == null) {
            return null;
        }

        if (!minigamePlayers.containsKey(player.getUniqueId())) {
            addMinigamePlayer(player);
        }

        return minigamePlayers.get(player.getUniqueId());
    }

    public @Nullable MinigamePlayer getMinigamePlayer(@NotNull UUID uuid) {
        MinigamePlayer mgPlayer = minigamePlayers.get(uuid);

        if (mgPlayer != null) {
            return mgPlayer;
        }

        return getMinigamePlayer(Bukkit.getPlayer(uuid));
    }


    /**
     * @see #getMinigamePlayer(UUID)
     */
    public @Nullable MinigamePlayer getMinigamePlayer(@NotNull String playerName) {
        return getMinigamePlayer(plugin.getServer().getPlayer(playerName));
    }

    public @NotNull Collection<MinigamePlayer> getAllMinigamePlayers() {
        return minigamePlayers.values();
    }

    /**
     * @see #hasMinigamePlayer(UUID)
     */
    public boolean hasMinigamePlayer(@NotNull String name) {
        for (MinigamePlayer mgPlayer : minigamePlayers.values()) {
            if (name.equals(mgPlayer.getName()))
                return true;
        }
        return false;
    }

    public boolean hasMinigamePlayer(@NotNull UUID uuid) {
        return minigamePlayers.containsKey(uuid);
    }

    public @NotNull List<@NotNull String> checkRequiredFlags(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame) {
        List<String> checkpoints = new ArrayList<>(minigame.getSinglePlayerFlags());
        List<String> pchecks = mgPlayer.getSinglePlayerFlags();

        if (!pchecks.isEmpty()) {
            checkpoints.removeAll(pchecks);
        }

        return checkpoints;
    }

    public boolean onPartyMode() {
        return partyMode;
    }

    public void setPartyMode(boolean mode) {
        partyMode = mode;
    }

    public void partyMode(@NotNull MinigamePlayer player) {
        if (onPartyMode()) {
            Location loc = player.getPlayer().getLocation();
            Firework firework = (Firework) player.getPlayer().getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
            FireworkMeta fwm = firework.getFireworkMeta();

            Random chance = new Random();
            Type type = Type.BALL_LARGE;
            if (chance.nextInt(100) < 50) {
                type = Type.BALL;
            }

            Color col = Color.fromRGB(chance.nextInt(255), chance.nextInt(255), chance.nextInt(255));

            FireworkEffect effect = FireworkEffect.builder().with(type).withColor(col).flicker(chance.nextBoolean()).trail(chance.nextBoolean()).build();
            fwm.addEffect(effect);
            fwm.setPower(1);
            firework.setFireworkMeta(fwm);
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
