package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.MinigameMessageType;
import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.events.DropFlagEvent;
import au.com.mineauz.minigames.events.FlagCaptureEvent;
import au.com.mineauz.minigames.events.TakeFlagEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.MessageManager;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.minigame.modules.CTFModule;
import au.com.mineauz.minigames.minigame.modules.MinigameModule;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;
import au.com.mineauz.minigames.objects.CTFFlag;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.signs.MinigameSign;
import au.com.mineauz.minigames.signs.SignBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class CTFMechanic extends GameMechanicBase {

    @Override
    public String getMechanic() {
        return "ctf";
    }

    @Override
    public EnumSet<MinigameType> validTypes() {
        return EnumSet.of(MinigameType.MULTIPLAYER);
    }

    @Override
    public boolean checkCanStart(Minigame minigame, MinigamePlayer caller) {
        return true;
    }

    @Override
    public MinigameModule displaySettings(Minigame minigame) {
        return CTFModule.getMinigameModule(minigame);
    }

    @Override
    public void startMinigame(Minigame minigame, MinigamePlayer caller) {
    }

    @Override
    public void stopMinigame(Minigame minigame, MinigamePlayer caller) {
    }

    @Override
    public void onJoinMinigame(Minigame minigame, MinigamePlayer player) {
    }

    @Override
    public void quitMinigame(Minigame minigame, MinigamePlayer player,
                             boolean forced) {
        final CTFModule ctfModule = CTFModule.getMinigameModule(minigame);

        if (ctfModule.isFlagCarrier(player)) {
            ctfModule.getCarriedFlag(player).stopCarrierParticleEffect();
            ctfModule.getCarriedFlag(player).respawnFlag();
            ctfModule.removeFlagCarrier(player);
        }
        if (minigame.getPlayers().size() == 1) {
            ctfModule.resetFlags();
        }
    }

    @Override
    public void endMinigame(Minigame minigame, List<MinigamePlayer> winners,
                            List<MinigamePlayer> losers) {
        final CTFModule ctfModule = CTFModule.getMinigameModule(minigame);

        for (MinigamePlayer pl : winners) {
            if (ctfModule.isFlagCarrier(pl)) {
                ctfModule.getCarriedFlag(pl).stopCarrierParticleEffect();
                ctfModule.getCarriedFlag(pl).respawnFlag();
                ctfModule.removeFlagCarrier(pl);
            }
        }
        if (minigame.getPlayers().size() == 1) {
            ctfModule.resetFlags();
        }
    }

    @EventHandler
    public void takeFlag(final @NotNull PlayerInteractEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        if (mgPlayer.isInMinigame() && !mgPlayer.getPlayer().isDead() && mgPlayer.getMinigame().hasStarted()) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (event.getClickedBlock().getState() instanceof Sign sign) && mgPlayer.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
                Minigame minigame = mgPlayer.getMinigame();
                if (minigame.getMechanicName().equals("ctf") && sign.getLine(1).equals(ChatColor.GREEN + "Flag")) {
                    Team team = mgPlayer.getTeam();

                    String sloc = MinigameUtils.createLocationID(event.getClickedBlock().getLocation());

                    final CTFModule ctfModule = CTFModule.getMinigameModule(minigame);
                    if (sign.getLine(2).equalsIgnoreCase(team.getChatColor() + team.getColor().toString()) &&
                            ctfModule.hasDroppedFlag(sloc) &&
                            !(sloc.equals(MinigameUtils.createLocationID(ctfModule.getDroppedFlag(sloc).getSpawnLocation())))) {
                        if (ctfModule.getBringFlagBackManual()) {
                            CTFFlag flag = ctfModule.getDroppedFlag(sloc);
                            flag.stopTimer();
                            ctfModule.removeDroppedFlag(sloc);
                            String newID = MinigameUtils.createLocationID(flag.getSpawnLocation());
                            ctfModule.addDroppedFlag(newID, flag);
                            flag.respawnFlag();
                            for (MinigamePlayer pl : minigame.getPlayers()) {
                                pl.sendInfoMessage(
                                        MessageManager.getMinigamesMessage("minigame.flag.returnedTeam", team.getChatColor() + team.getDisplayName() + ChatColor.WHITE));
                            }
                        }
                    } else if ((!sign.getLine(2).equalsIgnoreCase(team.getChatColor() + team.getColor().toString()) && !sign.getLine(2).equalsIgnoreCase(ChatColor.GREEN + "Capture")) ||
                            sign.getLine(2).equalsIgnoreCase(ChatColor.GRAY + "Neutral")) {
                        if (ctfModule.getCarriedFlag(mgPlayer) == null) {
                            TakeFlagEvent ev = null;
                            if (!ctfModule.hasDroppedFlag(sloc) &&
                                    (TeamsModule.getMinigameModule(minigame).hasTeam(TeamColor.matchColor(ChatColor.stripColor(sign.getLine(2)))) ||
                                            sign.getLine(2).equalsIgnoreCase(ChatColor.GRAY + "Neutral"))) {
                                Team oTeam = TeamsModule.getMinigameModule(minigame).getTeam(TeamColor.matchColor(ChatColor.stripColor(sign.getLine(2))));
                                CTFFlag flag = new CTFFlag(sign, oTeam, minigame);
                                ev = new TakeFlagEvent(minigame, mgPlayer, flag);
                                Bukkit.getPluginManager().callEvent(ev);
                                if (!ev.isCancelled()) {
                                    ctfModule.addFlagCarrier(mgPlayer, flag);
                                    flag.removeFlag();
                                }
                            } else if (ctfModule.hasDroppedFlag(sloc)) {
                                CTFFlag flag = ctfModule.getDroppedFlag(sloc);
                                ev = new TakeFlagEvent(minigame, mgPlayer, flag);
                                Bukkit.getPluginManager().callEvent(ev);
                                if (!ev.isCancelled()) {
                                    ctfModule.addFlagCarrier(mgPlayer, flag);

                                    if (!flag.isAtHome()) {
                                        flag.stopTimer();
                                    }
                                    flag.removeFlag();
                                }
                            }

                            if (ctfModule.getCarriedFlag(mgPlayer) != null && !ev.isCancelled()) {
                                if (ctfModule.getCarriedFlag(mgPlayer).getTeam() != null) {
                                    Team fteam = ctfModule.getCarriedFlag(mgPlayer).getTeam();
                                    String message = mgPlayer.getName() + " stole " + fteam.getChatColor() + fteam.getDisplayName() + ChatColor.WHITE + "'s flag!";
                                    minigameManager.sendCTFMessage(minigame, message, MinigameMessageType.INFO, null);
                                    ctfModule.getCarriedFlag(mgPlayer).startCarrierParticleEffect(mgPlayer.getPlayer());
                                } else {
                                    String message = mgPlayer.getName() + " stole the " + ChatColor.GRAY + "neutral" + ChatColor.WHITE + " flag!";
                                    minigameManager.sendCTFMessage(minigame, message, MinigameMessageType.INFO, null);
                                    ctfModule.getCarriedFlag(mgPlayer).startCarrierParticleEffect(mgPlayer.getPlayer());
                                }
                            }
                        }

                    } else if (team == TeamsModule.getMinigameModule(minigame).getTeam(TeamColor.matchColor(ChatColor.stripColor(sign.getLine(2)))) && ctfModule.getUseFlagAsCapturePoint() ||
                            (team == TeamsModule.getMinigameModule(minigame).getTeam(TeamColor.matchColor(ChatColor.stripColor(sign.getLine(3)))) && sign.getLine(2).equalsIgnoreCase(ChatColor.GREEN + "Capture")) ||
                            (sign.getLine(2).equalsIgnoreCase(ChatColor.GREEN + "Capture") && sign.getLine(3).equalsIgnoreCase(ChatColor.GRAY + "Neutral"))) {

                        String clickID = MinigameUtils.createLocationID(event.getClickedBlock().getLocation());

                        if (ctfModule.getCarriedFlag(mgPlayer) != null && (!ctfModule.hasDroppedFlag(clickID) || ctfModule.getDroppedFlag(clickID).isAtHome())) {
                            CTFFlag flag = ctfModule.getCarriedFlag(mgPlayer);
                            FlagCaptureEvent ev = new FlagCaptureEvent(minigame, mgPlayer, flag);
                            Bukkit.getPluginManager().callEvent(ev);
                            if (!ev.isCancelled()) {
                                flag.respawnFlag();
                                String id = MinigameUtils.createLocationID(flag.getSpawnLocation());
                                ctfModule.addDroppedFlag(id, flag);
                                ctfModule.removeFlagCarrier(mgPlayer);

                                boolean end = false;

                                if (minigame.isTeamGame()) {
                                    mgPlayer.getTeam().addScore();
                                    if (minigame.getMaxScore() != 0 && mgPlayer.getTeam().getScore() >= minigame.getMaxScorePerPlayer())
                                        end = true;

                                    if (!end) {
                                        String message = MessageManager.getMinigamesMessage("player.ctf.capture",
                                                mgPlayer.getName(), mgPlayer.getTeam().getChatColor() + mgPlayer.getTeam().getDisplayName());
                                        minigameManager.sendCTFMessage(minigame, message, MinigameMessageType.INFO, null);
                                    }
                                    flag.stopCarrierParticleEffect();
                                    mgPlayer.addScore();
                                    minigame.setScore(mgPlayer, mgPlayer.getScore());

                                    if (end) {
                                        minigameManager.sendCTFMessage(minigame, MessageManager.getMinigamesMessage("player.ctf.captureFinal", mgPlayer.getName(),
                                                mgPlayer.getTeam().getChatColor() + mgPlayer.getTeam().getDisplayName()), MinigameMessageType.INFO, null);
                                        List<MinigamePlayer> w = new ArrayList<>(mgPlayer.getTeam().getPlayers());
                                        List<MinigamePlayer> l = new ArrayList<>(minigame.getPlayers().size() - mgPlayer.getTeam().getPlayers().size());
                                        for (Team t : TeamsModule.getMinigameModule(minigame).getTeams()) {
                                            if (t != mgPlayer.getTeam())
                                                l.addAll(t.getPlayers());
                                        }
                                        plugin.getPlayerManager().endMinigame(minigame, w, l);
                                        ctfModule.resetFlags();
                                    }
                                } else {
                                    mgPlayer.addScore();
                                    minigame.setScore(mgPlayer, mgPlayer.getScore());
                                    if (minigame.getMaxScore() != 0 && mgPlayer.getScore() >= minigame.getMaxScorePerPlayer()) {
                                        end = true;
                                    }

                                    minigameManager.sendCTFMessage(minigame, MessageManager.getMinigamesMessage("player.ctf.captureNeutral", mgPlayer.getName()), MinigameMessageType.INFO, null);
                                    flag.stopCarrierParticleEffect();

                                    if (end) {
                                        minigameManager.sendCTFMessage(minigame, MessageManager.getMinigamesMessage("player.ctf.captureNeutralFinal", mgPlayer.getName()), MinigameMessageType.INFO, null);

                                        playerManager.endMinigame(mgPlayer);
                                        ctfModule.resetFlags();
                                    }
                                }
                            }
                        } else if (ctfModule.getCarriedFlag(mgPlayer) == null && ctfModule.hasDroppedFlag(clickID) && !ctfModule.getDroppedFlag(clickID).isAtHome()) {
                            CTFFlag flag = ctfModule.getDroppedFlag(sloc);
                            if (ctfModule.hasDroppedFlag(sloc)) {
                                ctfModule.removeDroppedFlag(sloc);
                                String newID = MinigameUtils.createLocationID(flag.getSpawnLocation());
                                ctfModule.addDroppedFlag(newID, flag);
                            }
                            flag.respawnFlag();
                            minigameManager.sendCTFMessage(minigame, MessageManager.getMinigamesMessage("player.ctf.returned", mgPlayer.getName(),
                                    mgPlayer.getTeam().getChatColor() + mgPlayer.getTeam().getDisplayName() + ChatColor.WHITE), MinigameMessageType.INFO, null);
                        } else if (ctfModule.getCarriedFlag(mgPlayer) != null && ctfModule.hasDroppedFlag(clickID) && !ctfModule.getDroppedFlag(clickID).isAtHome()) {
                            mgPlayer.sendMessage(MinigameUtils.getLang("player.ctf.returnFail"), MinigameMessageType.LOSS);
                        }
                    }
                }
            }
        }
    }

    private void doDropFlag(final @NotNull Minigame minigame, final @NotNull CTFModule ctfModule, final @NotNull CTFFlag flag,
                            final @NotNull MinigamePlayer mgPlayer, final @Nullable Location newFlagLocation) {
        if (newFlagLocation != null) {
            if (new DropFlagEvent(minigame, flag, mgPlayer).callEvent()) {
                String id = MinigameUtils.createLocationID(newFlagLocation);
                Team team = flag.getTeam();
                ctfModule.addDroppedFlag(id, flag);
                ctfModule.removeFlagCarrier(mgPlayer);

                if (team != null)
                    minigameManager.sendCTFMessage(minigame, MessageManager.getMinigamesMessage("player.ctf.dropped", mgPlayer.getName(),
                        team.getChatColor() + team.getDisplayName() + ChatColor.WHITE), MinigameMessageType.INFO, null);
                else
                    minigameManager.sendCTFMessage(minigame, MessageManager.getMinigamesMessage("player.ctf.droppedNeutral", mgPlayer.getName()), MinigameMessageType.INFO, null);
                flag.stopCarrierParticleEffect();
                flag.startReturnTimer();
            }
        } else {
            flag.respawnFlag();
            ctfModule.removeFlagCarrier(mgPlayer);
            flag.stopCarrierParticleEffect();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void placeFlag(final PlayerInteractEvent event) {
        final MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        final Minigame minigame = mgPlayer.getMinigame();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && mgPlayer.isInMinigame() &&
            !mgPlayer.getPlayer().isDead() && minigame.hasStarted()) {
            final CTFModule ctfModule = CTFModule.getMinigameModule(minigame);

            if (ctfModule.shouldCarryFlagAsItem() && ctfModule.isFlagCarrier(mgPlayer) &&
                !(event.getClickedBlock().getState() instanceof Sign)) {
                final CTFFlag flag = ctfModule.getCarriedFlag(mgPlayer);

                final PlayerInventory inventory = mgPlayer.getPlayer().getInventory();
                if (flag.isFlag(inventory.getItemInMainHand())) {

                    final @Nullable Location flagLocation = flag.spawnFlag(event.getClickedBlock().getLocation());

                    if (flagLocation != null) {
                        doDropFlag(minigame, ctfModule, flag, mgPlayer, flagLocation);
                    }

                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void dropFlagOnDeath(PlayerDeathEvent event) {
        final MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getEntity());
        if (mgPlayer.isInMinigame()) {
            final Minigame minigame = mgPlayer.getMinigame();
            final CTFModule ctfModule = CTFModule.getMinigameModule(minigame);

            if (ctfModule.isFlagCarrier(mgPlayer)) {
                final CTFFlag flag = ctfModule.getCarriedFlag(mgPlayer);
                event.getDrops().removeIf(flag::isFlag);

                doDropFlag(minigame, ctfModule, flag, mgPlayer, flag.spawnFlag(mgPlayer.getPlayer().getLocation()));
            }
        }
    }

    @EventHandler
    public void playerAutoBalance(PlayerDeathEvent event) {
        MinigamePlayer ply = playerManager.getMinigamePlayer(event.getEntity());
        if (ply.isInMinigame() && ply.getMinigame().getType() == MinigameType.MULTIPLAYER && ply.getMinigame().isTeamGame()) {
            Minigame mgm = ply.getMinigame();
            if (mgm.getMechanicName().equals("ctf")) {
                autoBalanceonDeath(ply, mgm);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void supportBreak(final @NotNull BlockBreakEvent event) {
        Location blockLocation = event.getBlock().getLocation().toBlockLocation();

        for (Minigame minigame : minigameManager.getAllMinigames().values()) {
            final CTFModule ctfModule = CTFModule.getMinigameModule(minigame);

            if (ctfModule != null) {
                for (CTFFlag ctfFlag : ctfModule.getAllDroppedFlags()) {
                    if (ctfFlag.getAttachedToLocation().equals(blockLocation)) {
                        MinigameSign mgSign = SignBase.getMinigameSignById("Flag");

                        if (mgSign.getCreatePermission() != null && !event.getPlayer().hasPermission(mgSign.getCreatePermission())) {
                            event.setCancelled(true);
                        } else { // waring: may lead to floating flags, or them maybe plopping of upon returning
                            MessageManager.sendMessage(Minigames.getPlugin().getPlayerManager().getMinigamePlayer(event.getPlayer()),
                                MinigameMessageType.WARN, null, "sign.flag.broken.support");
                        }

                        return;
                    }
                }
            }
        }
    }
}
