package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.events.DropFlagEvent;
import au.com.mineauz.minigames.events.FlagCaptureEvent;
import au.com.mineauz.minigames.events.TakeCTFFlagEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.TeamColor;
import au.com.mineauz.minigames.minigame.modules.CTFModule;
import au.com.mineauz.minigames.minigame.modules.MgModules;
import au.com.mineauz.minigames.minigame.modules.MinigameModule;
import au.com.mineauz.minigames.minigame.modules.TeamsModule;
import au.com.mineauz.minigames.objects.CTFFlag;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.signs.AMinigameSign;
import au.com.mineauz.minigames.signs.CTFFlagSign;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
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

    protected CTFMechanic() {
    }

    /**
     * Sending a ctf relevant message
     *
     * @param minigame The minigame in which this message shall be sent
     * @param message  The message
     */
    private static void sendCTFMessage(final @NotNull Minigame minigame, final @NotNull Component message) {
        if (!minigame.getShowCTFBroadcasts()) {
            return;
        }
        MinigameMessageManager.sendBroadcastMessageUnchecked(minigame, message, MinigameMessageType.INFO, null);
    }

    @Override
    public @NotNull String getMechanicName() {
        return "ctf";
    }

    @Override
    public @NotNull EnumSet<@NotNull MinigameType> validTypes() {
        return EnumSet.of(MinigameType.MULTIPLAYER);
    }

    @Override
    public boolean checkCanStart(@NotNull Minigame minigame, @Nullable MinigamePlayer caller) {
        TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);

        if (teamsModule != null && teamsModule.getTeams().size() >= 2 && minigame.isTeamGame()) {
            return true;

        } else {
            caller.sendMessage(Component.text("Capture the flag needs at least two teams!"), MinigameMessageType.ERROR);
            return false;
        }
    }

    @Override
    public MinigameModule displaySettings(@NotNull Minigame minigame) {
        return minigame.getModule(MgModules.CAPTURE_THE_FLAG.getName());
    }

    @Override
    public void startMinigame(@NotNull Minigame minigame, @Nullable MinigamePlayer caller) {
    }

    @Override
    public void stopMinigame(@NotNull Minigame minigame) {
    }

    @Override
    public void onJoinMinigame(@NotNull Minigame minigame, @NotNull MinigamePlayer player) {
    }

    @Override
    public void quitMinigame(@NotNull Minigame minigame, @NotNull MinigamePlayer mgPlayer, boolean forced) {
        final CTFModule ctfModule = CTFModule.getMinigameModule(minigame);

        CTFFlag carriedFlag = ctfModule.getCarriedFlag(mgPlayer);
        if (carriedFlag != null) {
            carriedFlag.stopCarrierParticleEffect();
            carriedFlag.respawnFlag();
            ctfModule.removeFlagCarrier(mgPlayer);
        }
        if (minigame.getPlayers().size() == 1) {
            ctfModule.resetFlags();
        }
    }

    @Override
    public void endMinigame(@NotNull Minigame minigame, @NotNull List<@NotNull MinigamePlayer> winners,
                            @NotNull List<@NotNull MinigamePlayer> losers) {
        final CTFModule ctfModule = CTFModule.getMinigameModule(minigame);

        for (MinigamePlayer mgPlayer : winners) {
            CTFFlag carriedFlag = ctfModule.getCarriedFlag(mgPlayer);
            if (carriedFlag != null) {
                carriedFlag.stopCarrierParticleEffect();
                carriedFlag.respawnFlag();
                ctfModule.removeFlagCarrier(mgPlayer);
            }
        }
        if (minigame.getPlayers().size() == 1) {
            ctfModule.resetFlags();
        }
    }

    @EventHandler
    private void takeFlag(final @NotNull PlayerInteractEvent event) { //todo better system of getting type of sign --> should be a getter in sign base
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        if (mgPlayer.isInMinigame() && !mgPlayer.getPlayer().isDead() && mgPlayer.getMinigame().hasStarted()) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                    event.getClickedBlock() != null &&
                    event.getClickedBlock().getState() instanceof Sign sign &&
                    mgPlayer.getPlayer().getInventory().getItemInMainHand().getType() == Material.AIR) {
                SignSide signFrontSide = sign.getSide(Side.FRONT);
                PlainTextComponentSerializer plainTextSerializer = PlainTextComponentSerializer.plainText();

                Minigame minigame = mgPlayer.getMinigame();
                if (minigame.getMechanic() == GameMechanics.MgMechanics.CTF.getMechanic() && new CTFFlagSign().isType(signFrontSide.line(1))) { // I hate that java does have this static inheritance restriction
                    Team team = mgPlayer.getTeam();

                    String sloc = MinigameUtils.createLocationID(event.getClickedBlock().getLocation());
                    final CTFModule ctfModule = CTFModule.getMinigameModule(minigame);
                    @Nullable TeamColor colorOnLine2 = TeamColor.matchColor(plainTextSerializer.serialize(signFrontSide.line(2)));
                    if (colorOnLine2 == team.getColor() &&
                            ctfModule.hasDroppedFlag(sloc) &&
                            !(sloc.equals(MinigameUtils.createLocationID(ctfModule.getDroppedFlag(sloc).getSpawnLocation())))) { //todo this whole if/else needs a reordering
                        if (ctfModule.getBringFlagBackManual()) {
                            CTFFlag flag = ctfModule.getDroppedFlag(sloc);
                            flag.stopTimer();
                            ctfModule.removeDroppedFlag(sloc);
                            String newID = MinigameUtils.createLocationID(flag.getSpawnLocation());
                            ctfModule.addDroppedFlag(newID, flag);
                            flag.respawnFlag();

                            MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_FLAG_RETURNEDTEAM,
                                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(team.getDisplayName(), team.getTextColor()))), MinigameMessageType.INFO);
                        }
                    } else if ((colorOnLine2 != team.getColor() && !CTFFlagSign.isCapture(signFrontSide.line(2))) ||
                            CTFFlagSign.isNeutral(signFrontSide.line(2))) {
                        if (ctfModule.getCarriedFlag(mgPlayer) == null) {
                            TakeCTFFlagEvent ev = null;
                            if (!ctfModule.hasDroppedFlag(sloc) &&
                                    (TeamsModule.getMinigameModule(minigame).hasTeam(colorOnLine2) || CTFFlagSign.isNeutral(signFrontSide.line(2)))) {
                                Team oTeam = TeamsModule.getMinigameModule(minigame).getTeam(colorOnLine2);
                                CTFFlag flag = new CTFFlag(sign, oTeam, minigame);
                                ev = new TakeCTFFlagEvent(minigame, mgPlayer, flag);
                                Bukkit.getPluginManager().callEvent(ev);
                                if (!ev.isCancelled()) {
                                    ctfModule.addFlagCarrier(mgPlayer, flag);
                                    flag.removeFlag();
                                }
                            } else if (ctfModule.hasDroppedFlag(sloc)) {
                                CTFFlag flag = ctfModule.getDroppedFlag(sloc);
                                ev = new TakeCTFFlagEvent(minigame, mgPlayer, flag);
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
                                    Team flagTeam = ctfModule.getCarriedFlag(mgPlayer).getTeam();
                                    sendCTFMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_CTF_STOLE,
                                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                                            Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(flagTeam.getDisplayName(), flagTeam.getTextColor())))
                                    );
                                    ctfModule.getCarriedFlag(mgPlayer).startCarrierParticleEffect(mgPlayer.getPlayer());
                                } else {
                                    sendCTFMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_CTF_NEUTRAL_STOLE,
                                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()))
                                    );
                                    ctfModule.getCarriedFlag(mgPlayer).startCarrierParticleEffect(mgPlayer.getPlayer());
                                }
                            }
                        }

                    } else if (team == TeamsModule.getMinigameModule(minigame).getTeam(colorOnLine2) && ctfModule.getUseFlagAsCapturePoint() ||
                            (team == TeamsModule.getMinigameModule(minigame).getTeam(TeamColor.matchColor(plainTextSerializer.serialize(signFrontSide.line(3)))) &&
                                    CTFFlagSign.isCapture(signFrontSide.line(2))) ||
                            (CTFFlagSign.isCapture(signFrontSide.line(2)) && CTFFlagSign.isNeutral(signFrontSide.line(3)))) {

                        String clickID = MinigameUtils.createLocationID(event.getClickedBlock().getLocation());

                        CTFFlag flag = ctfModule.getCarriedFlag(mgPlayer);
                        if (flag != null && (!ctfModule.hasDroppedFlag(clickID) || ctfModule.getDroppedFlag(clickID).isAtHome())) {
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
                                    if (minigame.getMaxScore() != 0 && mgPlayer.getTeam().getScore() >= minigame.getMaxScorePerPlayer()) {
                                        end = true;
                                    }

                                    if (!end) {
                                        sendCTFMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_CTF_CAPTURE,
                                                Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(mgPlayer.getTeam().getDisplayName(), mgPlayer.getTeam().getTextColor()))
                                        ));
                                    }
                                    flag.stopCarrierParticleEffect();
                                    mgPlayer.addScore();
                                    minigame.setScore(mgPlayer, mgPlayer.getScore());

                                    if (end) {
                                        sendCTFMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_CTF_CAPTUREFINAL,
                                                Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(mgPlayer.getTeam().getDisplayName(), mgPlayer.getTeam().getTextColor())))
                                        );
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

                                    sendCTFMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_CTF_NEUTRAL_CAPTURE,
                                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName())));
                                    flag.stopCarrierParticleEffect();

                                    if (end) {
                                        sendCTFMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_CTF_NEUTRAL_CAPTUREFINAL,
                                                Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName())));

                                        playerManager.endMinigame(mgPlayer);
                                        ctfModule.resetFlags();
                                    }
                                }
                            }
                        } else if (ctfModule.getCarriedFlag(mgPlayer) == null && ctfModule.hasDroppedFlag(clickID) && !ctfModule.getDroppedFlag(clickID).isAtHome()) {
                            flag = ctfModule.getDroppedFlag(sloc);
                            if (ctfModule.hasDroppedFlag(sloc)) {
                                ctfModule.removeDroppedFlag(sloc);
                                String newID = MinigameUtils.createLocationID(flag.getSpawnLocation());
                                ctfModule.addDroppedFlag(newID, flag);
                            }
                            flag.respawnFlag();

                            sendCTFMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_CTF_RETURNED,
                                    Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                                    Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(mgPlayer.getTeam().getDisplayName(), mgPlayer.getTeam().getTextColor())))
                            );
                        } else if (ctfModule.getCarriedFlag(mgPlayer) != null && ctfModule.hasDroppedFlag(clickID) && !ctfModule.getDroppedFlag(clickID).isAtHome()) {
                            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.LOSS, MgMiscLangKey.PLAYER_CTF_RETURNFAIL);
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
                Team team = ctfModule.getCarriedFlag(mgPlayer).getTeam();
                ctfModule.addDroppedFlag(id, flag);
                ctfModule.removeFlagCarrier(mgPlayer);

                if (team != null) {
                    sendCTFMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_CTF_DROPPED,
                        Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                        Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(team.getDisplayName(), team.getTextColor())))
                    );
                } else {
                    sendCTFMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_CTF_NEUTRAL_DROPPED,
                        Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()))
                    );
                }
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
                    final @Nullable Location flagLocation = flag.spawnFlag(event.getClickedBlock().getLocation(), event.getBlockFace());

                    if (flagLocation != null) {
                        doDropFlag(minigame, ctfModule, flag, mgPlayer, flagLocation);
                    }

                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    private void dropFlagOnDeath(PlayerDeathEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getEntity());
        if (mgPlayer.isInMinigame()) {
            final Minigame minigame = mgPlayer.getMinigame();
            final CTFModule ctfModule = CTFModule.getMinigameModule(minigame);

            if (ctfModule.isFlagCarrier(mgPlayer)) {
                CTFFlag flag = ctfModule.getCarriedFlag(mgPlayer);

                event.getDrops().removeIf(flag::isFlag);
                doDropFlag(minigame, ctfModule, flag, mgPlayer, flag.spawnFlag(mgPlayer.getPlayer().getLocation(), null));
            }
        }
    }

    @EventHandler
    private void playerAutoBalance(@NotNull PlayerDeathEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getEntity());
        if (mgPlayer.isInMinigame() && mgPlayer.getMinigame().getType() == MinigameType.MULTIPLAYER && mgPlayer.getMinigame().isTeamGame()) {
            Minigame mgm = mgPlayer.getMinigame();
            if (mgm.getMechanicName().equalsIgnoreCase(MgModules.CAPTURE_THE_FLAG.getName())) {
                autoBalanceOnDeath(mgPlayer, mgm);
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
                    if (ctfFlag.getSpawnAttachedToLocation().equals(blockLocation)) {
                        // new creation for easy access to permissions.
                        // I wish once again that you could define abstract static methods,
                        // so we could access static values like permissions without an object
                        // but guarantee that this methode exists
                        AMinigameSign mgSign = new CTFFlagSign();

                        if (mgSign.getCreatePermission() != null && !event.getPlayer().hasPermission(mgSign.getCreatePermission())) {
                            event.setCancelled(true);
                        } else { // waring: may lead to floating flags, or them maybe plopping of upon returning
                            MinigameMessageManager.sendMgMessage(Minigames.getPlugin().getPlayerManager().getMinigamePlayer(event.getPlayer()),
                                MinigameMessageType.WARNING, MgMiscLangKey.SIGN_FLAG_BROKEN_SUPPORT);
                        }

                        return;
                    }
                }
            }
        }
    }
}
