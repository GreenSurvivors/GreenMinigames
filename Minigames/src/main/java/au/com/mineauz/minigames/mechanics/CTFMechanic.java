package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.events.DropFlagEvent;
import au.com.mineauz.minigames.events.FlagCaptureEvent;
import au.com.mineauz.minigames.events.TakeCTFFlagEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuItemPage;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamColor;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.objects.CTFFlag;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.signs.AMinigameSign;
import au.com.mineauz.minigames.signs.CTFFlagSign;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;

public class CTFMechanic extends AGameMechanic {
    // config
    private final @NotNull BooleanFlag useFlagAsCapturePoint = new BooleanFlag("useFlagAsCapturePoint", true);
    private final @NotNull BooleanFlag bringFlagBackManual = new BooleanFlag("bringFlagBackManual", false);
    private final @NotNull BooleanFlag carryFlagAsItem = new BooleanFlag("carryFlagAsItem", false);
    // runtime
    private final @NotNull Map<@NotNull MinigamePlayer, @NotNull CTFFlag> flagCarriers = new HashMap<>();
    private final @NotNull Map<@NotNull String, @NotNull CTFFlag> droppedFlag = new HashMap<>();

    public CTFMechanic(final @NotNull Minigames plugin, final @NotNull Key key, final @NotNull Minigame minigame) {
        super(plugin, key, minigame);
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
    public @NotNull EnumSet<@NotNull MinigameType> validTypes() {
        return EnumSet.of(MinigameType.MULTIPLAYER);
    }

    @Override
    public boolean checkCanStart(@Nullable MinigamePlayer caller) {
        TeamsModule teamsModule = TeamsModule.getMinigameModule(minigame);

        if (teamsModule != null && teamsModule.getTeams().size() >= 2 && minigame.isTeamGame()) {
            return true;

        } else {
            if (caller != null) {
                MinigameMessageManager.sendMgMessage(caller, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_START_NOT_ENOUGH_TEAMS);
            }

            return false;
        }
    }

    @Override
    public @NotNull MenuItemPage displayMechanicSettings(final @NotNull Menu previous) {
        final @NotNull Menu menu = new Menu(6, MgMenuLangKey.MENU_CTF_NAME, previous.getIntendedViewer());
        menu.addItem(new MenuItemBack(previous), menu.getSize() - 9);

        menu.addItem(useFlagAsCapturePoint.getMenuItem(ItemType.BLACK_BANNER, MgMenuLangKey.MENU_CTF_CAPTUREPOINT_NAME,
            MgMenuLangKey.MENU_CTF_CAPTUREPOINT_DESCRIPTION));
        menu.addItem(bringFlagBackManual.getMenuItem(ItemType.ENDER_EYE, MgMenuLangKey.MENU_CTF_FLAGBACKMANUALLY_NAME,
            MgMenuLangKey.MENU_CTF_FLAGBACKMANUALLY_DESCRIPTION));
        menu.addItem(carryFlagAsItem.getMenuItem(ItemType.OAK_SIGN, MgMenuLangKey.MENU_CTF_CARRYFLAGASITEM_NAME,
            MgMenuLangKey.MENU_CTF_CARRYFLAGASITEM_DESCRIPTION));
        return new MenuItemPage(ItemType.BLUE_BANNER, MgMenuLangKey.MENU_MINIGAME_MECHANIC_SETTINGS_NAME, menu);
    }

    @Override
    public void startMinigame(@Nullable MinigamePlayer caller) {
    }

    @Override
    public void stopMinigame() {
    }

    @Override
    public void onJoinMinigame(@NotNull MinigamePlayer player) {
    }

    @Override
    public void quitMinigame(final @NotNull MinigamePlayer mgPlayer, final boolean forced) {
        final @Nullable CTFFlag carriedFlag = getCarriedFlag(mgPlayer);
        if (carriedFlag != null) {
            carriedFlag.stopCarrierParticleEffect();
            carriedFlag.respawnFlag();
            removeFlagCarrier(mgPlayer);
        }
        if (minigame.getPlayers().size() == 1) {
            resetFlags();
        }
    }

    @Override
    public void endMinigame(final @NotNull List<@NotNull MinigamePlayer> winners,
                            final @NotNull List<@NotNull MinigamePlayer> losers) {
        for (MinigamePlayer mgPlayer : winners) {
            CTFFlag carriedFlag = getCarriedFlag(mgPlayer);
            if (carriedFlag != null) {
                carriedFlag.stopCarrierParticleEffect();
                carriedFlag.respawnFlag();
                removeFlagCarrier(mgPlayer);
            }
        }
        if (minigame.getPlayers().size() == 1) {
            resetFlags();
        }
    }

    @EventHandler
    private void takeFlag(final @NotNull PlayerInteractEvent event) { //todo better system of getting type of sign --> should be a getter in sign base
        final @NotNull Player player = event.getPlayer();
        final @NotNull MinigamePlayer mgPlayer = plugin.getPlayerManager().getMinigamePlayer(player);
        if (player != null && mgPlayer.isInMinigame() && !player.isDead() && mgPlayer.getMinigame().hasStarted()) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK &&
                event.getClickedBlock() != null &&
                event.getClickedBlock().getState() instanceof Sign sign &&

                player.getInventory().getItemInMainHand().isEmpty()) {
                SignSide signFrontSide = sign.getSide(Side.FRONT);
                PlainTextComponentSerializer plainTextSerializer = PlainTextComponentSerializer.plainText();

                if (minigame.equals(mgPlayer.getMinigame()) &&
                    new CTFFlagSign().isType(signFrontSide.line(1))) { // I hate that java does have this static inheritance restriction
                    Team team = mgPlayer.getTeam();

                    String sloc = MinigameUtils.createBlockLocationID(event.getClickedBlock().getLocation());
                    @Nullable TeamColor colorOnLine2 = TeamColor.matchColor(plainTextSerializer.serialize(signFrontSide.line(2)));
                    if (colorOnLine2 == team.getColor() && hasDroppedFlag(sloc) &&
                        !(sloc.equals(MinigameUtils.createBlockLocationID(getDroppedFlag(sloc).getSpawnLocation())))) { //todo this whole if/else needs a reordering

                        if (getBringFlagBackManual()) {
                            CTFFlag flag = getDroppedFlag(sloc);
                            flag.stopTimer();
                            removeDroppedFlag(sloc);
                            String newID = MinigameUtils.createBlockLocationID(flag.getSpawnLocation());
                            addDroppedFlag(newID, flag);
                            flag.respawnFlag();

                            MinigameMessageManager.sendMinigameMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_FLAG_RETURNEDTEAM,
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(team.getDisplayName(), team.getTextColor()))), MinigameMessageType.INFO);
                        }
                    } else if ((colorOnLine2 != team.getColor() && !CTFFlagSign.isCapture(signFrontSide.line(2))) ||
                        CTFFlagSign.isNeutral(signFrontSide.line(2))) {
                        if (getCarriedFlag(mgPlayer) == null) {
                            TakeCTFFlagEvent ev = null;
                            if (!hasDroppedFlag(sloc) &&
                                (TeamsModule.getMinigameModule(minigame).hasTeam(colorOnLine2) || CTFFlagSign.isNeutral(signFrontSide.line(2)))) {
                                Team oTeam = TeamsModule.getMinigameModule(minigame).getTeam(colorOnLine2);
                                CTFFlag flag = new CTFFlag(sign, oTeam, minigame);
                                ev = new TakeCTFFlagEvent(minigame, mgPlayer, flag);
                                Bukkit.getPluginManager().callEvent(ev);
                                if (!ev.isCancelled()) {
                                    addFlagCarrier(mgPlayer, flag);
                                    flag.removeFlag();
                                }
                            } else if (hasDroppedFlag(sloc)) {
                                CTFFlag flag = getDroppedFlag(sloc);
                                ev = new TakeCTFFlagEvent(minigame, mgPlayer, flag);
                                Bukkit.getPluginManager().callEvent(ev);
                                if (!ev.isCancelled()) {
                                    addFlagCarrier(mgPlayer, flag);

                                    if (!flag.isAtHome()) {
                                        flag.stopTimer();
                                    }
                                    flag.removeFlag();
                                }
                            }

                            if (getCarriedFlag(mgPlayer) != null && !ev.isCancelled()) {
                                if (getCarriedFlag(mgPlayer).getTeam() != null) {
                                    Team flagTeam = getCarriedFlag(mgPlayer).getTeam();
                                    sendCTFMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_CTF_STOLE,
                                        Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                                        Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(flagTeam.getDisplayName(), flagTeam.getTextColor())))
                                    );
                                    getCarriedFlag(mgPlayer).startCarrierParticleEffect(mgPlayer.getUUID());
                                } else {
                                    sendCTFMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_CTF_NEUTRAL_STOLE,
                                        Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()))
                                    );
                                    getCarriedFlag(mgPlayer).startCarrierParticleEffect(mgPlayer.getUUID());
                                }
                            }
                        }

                    } else if (team == TeamsModule.getMinigameModule(minigame).getTeam(colorOnLine2) && getUseFlagAsCapturePoint() ||
                        (team == TeamsModule.getMinigameModule(minigame).getTeam(TeamColor.matchColor(plainTextSerializer.serialize(signFrontSide.line(3)))) &&
                            CTFFlagSign.isCapture(signFrontSide.line(2))) ||
                        (CTFFlagSign.isCapture(signFrontSide.line(2)) && CTFFlagSign.isNeutral(signFrontSide.line(3)))) {

                        String clickID = MinigameUtils.createBlockLocationID(event.getClickedBlock().getLocation());

                        CTFFlag flag = getCarriedFlag(mgPlayer);
                        if (flag != null && (!hasDroppedFlag(clickID) || getDroppedFlag(clickID).isAtHome())) {
                            FlagCaptureEvent ev = new FlagCaptureEvent(minigame, mgPlayer, flag);
                            Bukkit.getPluginManager().callEvent(ev);
                            if (!ev.isCancelled()) {
                                flag.respawnFlag();
                                String id = MinigameUtils.createBlockLocationID(flag.getSpawnLocation());
                                addDroppedFlag(id, flag);
                                removeFlagCarrier(mgPlayer);

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
                                        resetFlags();
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

                                        plugin.getPlayerManager().winMinigame(mgPlayer);
                                        resetFlags();
                                    }
                                }
                            }
                        } else if (getCarriedFlag(mgPlayer) == null && hasDroppedFlag(clickID) && !getDroppedFlag(clickID).isAtHome()) {
                            flag = getDroppedFlag(sloc);
                            if (hasDroppedFlag(sloc)) {
                                removeDroppedFlag(sloc);
                                String newID = MinigameUtils.createBlockLocationID(flag.getSpawnLocation());
                                addDroppedFlag(newID, flag);
                            }
                            flag.respawnFlag();

                            sendCTFMessage(minigame, MinigameMessageManager.getMgMessage(MgMiscLangKey.PLAYER_CTF_RETURNED,
                                Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), mgPlayer.displayName()),
                                Placeholder.component(MinigamePlaceHolderKey.TEAM.getKey(), Component.text(mgPlayer.getTeam().getDisplayName(), mgPlayer.getTeam().getTextColor())))
                            );
                        } else if (getCarriedFlag(mgPlayer) != null && hasDroppedFlag(clickID) && !getDroppedFlag(clickID).isAtHome()) {
                            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.LOSS, MgMiscLangKey.PLAYER_CTF_RETURNFAIL);
                        }
                    }
                }
            }
        }
    }

    private void doDropFlag(final @NotNull CTFFlag flag,
                            final @NotNull MinigamePlayer mgPlayer, final @Nullable Location newFlagLocation) {
        if (newFlagLocation != null) {
            if (new DropFlagEvent(minigame, flag, mgPlayer).callEvent()) {
                String id = MinigameUtils.createBlockLocationID(newFlagLocation);
                Team team = getCarriedFlag(mgPlayer).getTeam();
                addDroppedFlag(id, flag);
                removeFlagCarrier(mgPlayer);

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
                flag.startReturnTimer(this);
            }
        } else {
            flag.respawnFlag();
            removeFlagCarrier(mgPlayer);
            flag.stopCarrierParticleEffect();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void placeFlag(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final @NotNull MinigamePlayer mgPlayer = plugin.getPlayerManager().getMinigamePlayer(player);

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && mgPlayer.isInMinigame() &&
            minigame.equals(mgPlayer.getMinigame()) &&
            !player.isDead() && minigame.hasStarted()) {

            if (shouldCarryFlagAsItem() && isFlagCarrier(mgPlayer) &&
                !(event.getClickedBlock().getState() instanceof Sign)) {
                final CTFFlag flag = getCarriedFlag(mgPlayer);

                final PlayerInventory inventory = player.getInventory();
                if (flag.isFlag(inventory.getItemInMainHand())) {
                    final @Nullable Location flagLocation = flag.spawnFlag(event.getClickedBlock().getLocation(), event.getBlockFace());

                    if (flagLocation != null) {
                        doDropFlag(flag, mgPlayer, flagLocation);
                    }

                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    private void dropFlagOnDeath(final @NotNull PlayerDeathEvent event) {
        final @NotNull Player player = event.getEntity();
        final @NotNull MinigamePlayer mgPlayer = plugin.getPlayerManager().getMinigamePlayer(player);
        if (mgPlayer.isInMinigame() && mgPlayer.getMinigame().equals(minigame)) {

            if (isFlagCarrier(mgPlayer)) {
                CTFFlag flag = getCarriedFlag(mgPlayer);

                event.getDrops().removeIf(flag::isFlag);
                doDropFlag(flag, mgPlayer, flag.spawnFlag(player.getLocation(), null));
            }
        }
    }

    @EventHandler
    private void playerAutoBalance(final @NotNull PlayerDeathEvent event) {
        final @NotNull MinigamePlayer mgPlayer = plugin.getPlayerManager().getMinigamePlayer(event.getEntity());
        if (mgPlayer.isInMinigame() && mgPlayer.getMinigame().getType() == MinigameType.MULTIPLAYER && mgPlayer.getMinigame().isTeamGame()) {
            if (minigame.equals(mgPlayer.getMinigame())) {
                autoBalanceOnDeath(mgPlayer, minigame);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void supportBreak(final @NotNull BlockBreakEvent event) {
        final @NotNull Location blockLocation = event.getBlock().getLocation().toBlockLocation();

        for (CTFFlag ctfFlag : getAllDroppedFlags()) {
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
                        MinigameMessageType.WARNING, MgMiscLangKey.SIGN_CTF_FLAG_BROKEN_SUPPORT);
                }

                return;
            }
        }
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        useFlagAsCapturePoint.saveValue(config);
        bringFlagBackManual.saveValue(config);
        carryFlagAsItem.saveValue(config);
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        useFlagAsCapturePoint.loadValue(config);
        bringFlagBackManual.loadValue(config);
        carryFlagAsItem.loadValue(config);
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    public Boolean getUseFlagAsCapturePoint() {
        return useFlagAsCapturePoint.getFlag();
    }

    public void setUseFlagAsCapturePoint(boolean useFlagAsCapturePoint) {
        this.useFlagAsCapturePoint.setFlag(useFlagAsCapturePoint);
    }

    public Boolean getBringFlagBackManual() {
        return bringFlagBackManual.getFlag();
    }

    public void setBringFlagBackManual(final boolean bringFlagBackManual) {
        this.bringFlagBackManual.setFlag(bringFlagBackManual);
    }

    public Boolean shouldCarryFlagAsItem() {
        return carryFlagAsItem.getFlag();
    }

    public void setCarryFlagAsItem(final boolean carryFlagAsItem) {
        this.carryFlagAsItem.setFlag(carryFlagAsItem);
    }

    public boolean isFlagCarrier(final @NotNull MinigamePlayer mgPlayer) {
        return flagCarriers.containsKey(mgPlayer);
    }

    public void addFlagCarrier(final @NotNull MinigamePlayer mgPlayer, final @NotNull CTFFlag flag) {
        flagCarriers.put(mgPlayer, flag);

        if (shouldCarryFlagAsItem()) {
            mgPlayer.getPlayer().getInventory().addItem(flag.getAsItem());
        }
    }

    public void removeFlagCarrier(final @NotNull MinigamePlayer mgPlayer) {
        final @Nullable CTFFlag flag = flagCarriers.remove(mgPlayer);

        if (shouldCarryFlagAsItem() && flag != null) {
            final @NotNull PlayerInventory inventory = mgPlayer.getPlayer().getInventory();
            final @Nullable ItemStack @NotNull[] items = inventory.getStorageContents();

            for (int i = 0; i < items.length; i++) {
                if (flag.isFlag(items[i])) {
                    inventory.setItem(i, ItemStack.empty());
                }
            }
        }
    }

    public CTFFlag getCarriedFlag(final @NotNull MinigamePlayer ply) {
        return flagCarriers.get(ply);
    }

    public void resetFlags() {
        for (CTFFlag ctfFlag : flagCarriers.values()) {
            ctfFlag.respawnFlag();
            ctfFlag.stopCarrierParticleEffect();
        }
        flagCarriers.clear();
        for (String id : droppedFlag.keySet()) {
            if (!getDroppedFlag(id).isAtHome()) {
                getDroppedFlag(id).stopTimer();
                getDroppedFlag(id).respawnFlag();
            }
        }
        droppedFlag.clear();
    }

    public boolean hasDroppedFlag(final @NotNull String id) {
        return droppedFlag.containsKey(id);
    }

    public void addDroppedFlag(final @NotNull String id, final @NotNull CTFFlag flag) {
        droppedFlag.put(id, flag);
    }

    public void removeDroppedFlag(final @NotNull String id) {
        droppedFlag.remove(id);
    }

    public @Nullable CTFFlag getDroppedFlag(final @NotNull String id) {
        return droppedFlag.get(id);
    }

    public @NotNull Collection<@NotNull CTFFlag> getAllDroppedFlags() {
        return droppedFlag.values();
    }
}
