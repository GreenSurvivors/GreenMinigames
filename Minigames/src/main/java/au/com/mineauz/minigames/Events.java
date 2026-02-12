package au.com.mineauz.minigames;

import au.com.mineauz.minigames.events.RevertCheckpointEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.MinigameManager;
import au.com.mineauz.minigames.managers.MinigamePlayerManager;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.menu.consumer.BlockDataConsumer;
import au.com.mineauz.minigames.menu.consumer.EntityConsumer;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.MinigameState;
import au.com.mineauz.minigames.minigame.modules.GameOverModule;
import au.com.mineauz.minigames.minigame.modules.ResourcePackModule;
import au.com.mineauz.minigames.minigame.modules.WeatherTimeModule;
import au.com.mineauz.minigames.minigame.modules.loadout.PlayerLoadout;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.minigame.scoreboard.ScoreboardDisplay;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.OfflineMinigamePlayer;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import au.com.mineauz.minigames.signs.AMinigameSign;
import au.com.mineauz.minigames.signs.BetSign;
import au.com.mineauz.minigames.signs.JoinSign;
import au.com.mineauz.minigames.signs.SignBase;
import au.com.mineauz.minigames.tool.MinigameTool;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Events implements Listener {
    private static final @NotNull Minigames plugin = Minigames.getPlugin();
    private final @NotNull MinigamePlayerManager playerManager = plugin.getPlayerManager();
    private final @NotNull MinigameManager minigameManager = plugin.getMinigameManager();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerResourcePack(@NotNull PlayerResourcePackStatusEvent event) { //todo 1.20.3 + add ressource pack not set (redo with multible Ressoucepacks in mind.)
        final MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        List<MinigamePlayer> required = plugin.getPlayerManager().getApplyingPack();
        if (mgPlayer.isInMinigame() && required.contains(mgPlayer)) {
            ResourcePackModule module = ResourcePackModule.getMinigameModule(mgPlayer.getMinigame());
            if (module == null || !module.isEnabled()) return;
            if (!module.isForced()) return;
            switch (event.getStatus()) {
                case ACCEPTED, SUCCESSFULLY_LOADED -> required.remove(mgPlayer);
                case DECLINED -> {
                    plugin.getPlayerManager().quitMinigame(mgPlayer, true);
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_RESOURCEPACK_DECLINED);
                    required.remove(mgPlayer);
                }
                case FAILED_DOWNLOAD -> {
                    plugin.getPlayerManager().quitMinigame(mgPlayer, true);
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_RESOURCEPACK_FAILED);
                    required.remove(mgPlayer);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        final @NotNull MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getEntity());
        if (mgPlayer.isInMinigame()) {
            final @NotNull Minigame mgm = mgPlayer.getMinigame();
            final @NotNull Player player = mgPlayer.getPlayer();
            if (!mgm.hasDeathDrops()) {
                if (mgm.keepInventory()) {
                    List<ItemStack> drops = Arrays.asList(player.getInventory().getContents());
                    PlayerLoadout l = new PlayerLoadout("deathDrops");
                    for (int i = 0; i < drops.size(); i++) {
                        l.addItem(drops.get(i), i);
                    }
                    mgPlayer.setLoadout(l);
                }
                event.getDrops().clear();
            }

            final @Nullable Component msg = event.deathMessage();
            event.deathMessage(Component.empty());
            event.setDroppedExp(0);

            mgPlayer.addDeath();
            mgPlayer.addRevert();

            playerManager.partyMode(mgPlayer);

            if (player.getKiller() != null) {
                MinigamePlayer killer = playerManager.getMinigamePlayer(player.getKiller());
                if (killer != null)
                    killer.addKill();
            }

            if (msg != null && !PlainTextComponentSerializer.plainText().serialize(msg).isEmpty()) { //components really need a better way to check if they are empty
                MessageManager.sendMinigameMessage(mgm, msg, MinigameMessageType.ERROR);
            }
            if (mgm.getState() == MinigameState.STARTED) {
                if (mgm.getLives() > 0 && mgm.getLives() <= mgPlayer.getDeaths()) {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.PLAYER_QUIT_OUTOFLIVES);
                    if (!event.getDrops().isEmpty() && mgm.getPlayers().size() == 1) {
                        event.getDrops().clear();
                    }
                    playerManager.quitMinigame(mgPlayer, false);
                } else if (mgm.getLives() > 0) {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_LIVES_LIVESLEFT,
                        Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(mgm.getLives() - mgPlayer.getDeaths())));
                }
            } else if (mgm.getState() == MinigameState.ENDED) {
                plugin.getPlayerManager().quitMinigame(mgPlayer, true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void playerSpawn(final @NotNull PlayerRespawnEvent event) {
        final @NotNull MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        if (mgPlayer.isInMinigame()) {
            final WeatherTimeModule mod = WeatherTimeModule.getMinigameModule(mgPlayer.getMinigame());
            if (mod != null && mod.isUsingCustomWeather()) {
                // delay one tick to give the player time to respawn
                Bukkit.getScheduler().runTaskLater(plugin, () -> mgPlayer.getPlayer().setPlayerWeather(mod.getCustomWeather()), 1L);
            }

            if (mgPlayer.getMinigame().getState() == MinigameState.ENDED) {
                plugin.getPlayerManager().quitMinigame(mgPlayer, true);
            }
        }
        if (mgPlayer.isRequiredQuit()) {
            // delay one tick to give the player time to respawn
            Bukkit.getScheduler().runTaskLater(plugin, mgPlayer::restorePlayerData, 1L);
            if (mgPlayer.getQuitPos() != null) {
                event.setRespawnLocation(mgPlayer.getQuitPos().toLocation());
            }

            mgPlayer.setRequiredQuit(false);
            mgPlayer.setQuitPos(null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void playerDropItem(@NotNull PlayerDropItemEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        if (mgPlayer.isInMinigame()) {
            Minigame mgm = playerManager.getMinigamePlayer(event.getPlayer()).getMinigame();
            if (!mgm.hasItemDrops() ||
                mgm.isSpectator(playerManager.getMinigamePlayer(event.getPlayer()))) {

                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void itemPickup(@NotNull EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(player);
            if (mgPlayer.isInMinigame()) {
                Minigame mgm = mgPlayer.getMinigame();
                if (!mgm.hasItemPickup() || mgm.isSpectator(mgPlayer)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    // the priority was changed to lowest, since having it to normal would mean worldguard
    // would be served first unload the player and didn't allow them to teleport to the quit location inside a region. (pdata.quitMinigame)
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDisconnect(final @NotNull PlayerQuitEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        if (mgPlayer.isInMinigame()) {
            if (mgPlayer.getPlayer().isDead()) {
                mgPlayer.getOfflineMinigamePlayer().setLoginLocation(mgPlayer.getMinigame().getQuitLocation());
                try {
                    mgPlayer.getOfflineMinigamePlayer().savePlayerData();
                } catch (final @NotNull IOException e) {
                    plugin.getComponentLogger().error("Couldn't safe player data disconnect. " + mgPlayer.getName() + " (" + mgPlayer.getUUID() + ")", e);
                }
            }
            playerManager.quitMinigame(playerManager.getMinigamePlayer(event.getPlayer()), false);
        } else if (mgPlayer.isRequiredQuit()) {
            mgPlayer.getOfflineMinigamePlayer().setLoginLocation(mgPlayer.getQuitPos());
            try {
                mgPlayer.getOfflineMinigamePlayer().savePlayerData();
            } catch (final @NotNull IOException e) {
                plugin.getComponentLogger().error("Couldn't safe player data disconnect. " + mgPlayer.getName() + " (" + mgPlayer.getUUID() + ")", e);
            }
        }

        playerManager.removeMinigamePlayer(event.getPlayer());
        plugin.getDisplayManager().removeAll(event.getPlayer());

        if (Bukkit.getServer().getOnlinePlayers().isEmpty()) {
            for (String mgm : minigameManager.getAllMinigames().keySet()) {
                if (minigameManager.getMinigame(mgm).getType() == MinigameType.GLOBAL) {
                    if (minigameManager.getMinigame(mgm).getMinigameTimer() != null)
                        minigameManager.getMinigame(mgm).getMinigameTimer().stopTimer();
                }
            }
        }
        try {
            mgPlayer.saveClaimedRewards();
        } catch (final @NotNull IOException e) {
            plugin.getComponentLogger().error("Couldn't safe claimed rewards on player disconnect. " + mgPlayer.getName() + " (" + mgPlayer.getUUID() + ")", e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerConnect(final @NotNull PlayerJoinEvent event) {
        final @NotNull Path playerDataPath = plugin.getDataPath().resolve("playerdata").resolve(Path.of("inventories", event.getPlayer().getUniqueId() + ".yml"));
        final @NotNull MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        if (Files.isRegularFile(playerDataPath)) {
            try {
                mgPlayer.setOfflineMinigamePlayer(new OfflineMinigamePlayer(event.getPlayer().getUniqueId()));
            } catch (final @NotNull ConfigurateException e) {
                plugin.getComponentLogger().error("Couldn't load offline player data on join. " + mgPlayer.getName() + " (" + mgPlayer.getUUID() + ")", e);
            }
            final SafeFullLocation floc = mgPlayer.getOfflineMinigamePlayer().getLoginLocation();
            mgPlayer.setRequiredQuit(true);
            mgPlayer.setQuitPos(floc);

            if (!mgPlayer.getPlayer().isDead() && mgPlayer.isRequiredQuit()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, mgPlayer::restorePlayerData);
                mgPlayer.teleport(floc);

                mgPlayer.setRequiredQuit(false);
                mgPlayer.setQuitPos(null);
            }

            plugin.getLogger().info(mgPlayer.getName() + "'s data has been restored from file.");
        }

        try {
            mgPlayer.loadClaimedRewards();
        } catch (final @NotNull ConfigurateException e) {
            plugin.getComponentLogger().error("Couldn't load claimed rewards for player " + event.getPlayer().name() + " (" + event.getPlayer().getUniqueId() + ")", e);
        }

        if (Bukkit.getServer().getOnlinePlayers().size() == 1) {
            for (Minigame mgm : minigameManager.getAllMinigames().values()) {
                if (mgm.getType() == MinigameType.GLOBAL && mgm.getMinigameTimer() != null) {
                    mgm.getMinigameTimer().startTimer();
                }
            }
        }
    }

    @EventHandler
    private void player(@NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            final MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(player);

            if (mgPlayer.isInMenu() && mgPlayer.getMenuItemWaitingForManualInput() instanceof EntityConsumer entityConsumer) {
                event.setCancelled(true);
                entityConsumer.acceptEntity(event.getEntity());
                mgPlayer.setMenuItemWaitingForManualInput(null);
            }
        }
    }

    @EventHandler
    private void playerInteract(@NotNull PlayerInteractEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());

        if (mgPlayer.isInMinigame() && !mgPlayer.canInteract()) {
            event.setCancelled(true);
            return;
        }
        if (mgPlayer.isInMenu() &&
            event.getClickedBlock() != null &&
            mgPlayer.getMenuItemWaitingForManualInput() instanceof BlockDataConsumer blockDataConsumer) {

            event.setCancelled(true);
            blockDataConsumer.acceptBlockData(event.getClickedBlock().getBlockData());
            mgPlayer.setMenuItemWaitingForManualInput(null);
            return;
        }
        if (event.getClickedBlock() != null && event.getClickedBlock().getType().asBlockType() == BlockType.DRAGON_EGG) {
            if (!mgPlayer.getMinigame().allowDragonEggTeleport()) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK && !(event.useInteractedBlock() == Event.Result.DENY)) {
            Block cblock = event.getClickedBlock();
            if (cblock.getState() instanceof Sign sign && SignBase.isMinigameSign(sign.getSide(Side.FRONT).line(0))) {
                // wax signs automatically
                sign.setWaxed(true);
                sign.update();
                if (event.getPlayer().hasPermission("minigame.sign.use.details")) {
                    AMinigameSign mgSign = Minigames.getPlugin().getMinigameSigns().getMgSign(sign.getSide(Side.FRONT).line(1));

                    if (!mgPlayer.isInMinigame() && (mgSign instanceof BetSign || mgSign instanceof JoinSign)) {
                        Minigame mgm = AMinigameSign.getMinigame(sign);

                        if (mgm != null && (!mgm.getUsePermissions() || event.getPlayer().hasPermission("minigame.join." + mgm.getName().toLowerCase()))) {
                            if (!mgm.isEnabled()) {
                                MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTENABLED);
                            } else {
                                MessageManager.sendMessage(mgPlayer, MinigameMessageType.NONE, MgMiscLangKey.MINIGAME_INFO_HEADER);

                                if (mgm.getType() != MinigameType.SINGLEPLAYER) {

                                    Component status;
                                    if (!mgm.hasPlayers()) {
                                        status = MessageManager.getMessage(MgMiscLangKey.MINIGAME_INFO_STATUS_EMPTY);
                                    } else if (mgm.getMultiplayerTimer() == null || mgm.getMultiplayerTimer().getPlayerWaitTimeLeft() > 0) {
                                        status = MessageManager.getMessage(MgMiscLangKey.MINIGAME_INFO_STATUS_WAITINGFORPLAYERS);
                                    } else {
                                        status = MessageManager.getMessage(MgMiscLangKey.MINIGAME_INFO_STATUS_STARTED);
                                    }
                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.NONE, MgMiscLangKey.MINIGAME_INFO_STATUS_TITLE,
                                        Placeholder.component(MinigamePlaceHolderKey.TEXT.getKey(), status));

                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.NONE, MgMiscLangKey.MINIGAME_INFO_LATEJOIN_MSG,
                                        Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), MessageManager.getMessage(
                                            mgm.canLateJoin() ?
                                                MgMiscLangKey.MINIGAME_INFO_LATEJOIN_ENABLED :
                                                MgMiscLangKey.MINIGAME_INFO_LATEJOIN_DISABLED)));
                                }

                                if (mgm.getMinigameTimer() != null) {
                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.NONE, MgMiscLangKey.TIME_TIMELEFT,
                                        Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(),
                                            MinigameUtils.convertTime(Duration.ofSeconds(mgm.getMinigameTimer().getTimeLeft()))));
                                }

                                TeamsModule teamsModule = TeamsModule.getMinigameModule(mgm);
                                if (mgm.isTeamGame() && teamsModule != null) {
                                    List<ComponentLike> list = new ArrayList<>(teamsModule.getTeams().size());

                                    for (Team team : teamsModule.getTeams()) {
                                        list.add(Component.text().append(team.getColoredDisplayName()).appendSpace().append(Component.text(team.getScore())));
                                    }

                                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.NONE, MgMiscLangKey.MINIGAME_INFO_SCORE,
                                        Placeholder.component(MinigamePlaceHolderKey.SCORE.getKey(),
                                            Component.join(JoinConfiguration.separator(Component.text(" : ").color(NamedTextColor.WHITE)), list)));
                                }

                                MessageManager.sendMessage(mgPlayer, MinigameMessageType.NONE, MgMiscLangKey.MINIGAME_INFO_PLAYERCOUNT,
                                    Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(mgm.getPlayers().size())),
                                    Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), String.valueOf(
                                        mgm.getType() == MinigameType.SINGLEPLAYER ?
                                            0 :
                                            mgm.getMaxPlayers())));

                                Component players;
                                if (mgm.hasPlayers()) {
                                    players = Component.join(JoinConfiguration.commas(true),
                                        new Iterable<Component>() {
                                            @Override
                                            public @NotNull Iterator<Component> iterator() {
                                                return mgm.getPlayers().stream().map(MinigamePlayer::displayName).iterator();
                                            }
                                        });
                                } else {
                                    players = MessageManager.getMessage(MgMiscLangKey.QUANTIFIER_NONE);
                                }
                                MessageManager.sendMessage(mgPlayer, MinigameMessageType.NONE, MgMiscLangKey.MINIGAME_INFO_PLAYERS_TITLE,
                                    Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), players));
                            }
                        } else if (mgm == null) {
                            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOMINIGAME,
                                Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), sign.getSide(Side.FRONT).line(2)));
                        } else if (mgm.getUsePermissions()) {
                            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOPERMISSION,
                                Placeholder.unparsed(MinigamePlaceHolderKey.PERMISSION.getKey(), "minigame.join." + mgm.getName().toLowerCase()));
                        }
                    }
                }
            }
        }

        ItemStack item = event.getItem();
        //nullcheck in isMinigameTool()
        if (MinigameTool.isMinigameTool(item) && mgPlayer.getPlayer().hasPermission("minigame.tool")) {
            MinigameTool tool = new MinigameTool(item);
            event.setCancelled(true);

            if (event.getPlayer().isSneaking() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                tool.openMenu(mgPlayer);
                event.setCancelled(true);
            } else if (event.getClickedBlock() != null && (Tag.ALL_SIGNS.isTagged(event.getClickedBlock().getType()))) {
                Sign sign = (Sign) event.getClickedBlock().getState();

                AMinigameSign mgSign = plugin.getMinigameSigns().getMgSign(sign.getSide(Side.FRONT).line(1));
                Minigame minigame = AMinigameSign.getMinigame(sign);
                if (SignBase.isMinigameSign(sign.getSide(Side.FRONT).line(0)) && mgSign instanceof JoinSign && minigame != null) {
                    tool.setMinigame(minigame);
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.TOOL_SELECTED_MINIGAME_MSG,
                        Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()));
                    event.setCancelled(true);
                }
            } else {
                if (tool.getMinigame() != null) {
                    if (tool.getMode() != null) {
                        Minigame mg = tool.getMinigame();

                        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            tool.getMode().onRightClick(mgPlayer, mg, TeamsModule.getMinigameModule(mg).getTeam(tool.getTeamColor()), event);
                        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
                            tool.getMode().onLeftClick(mgPlayer, mg, TeamsModule.getMinigameModule(mg).getTeam(tool.getTeamColor()), event);
                        }
                    } else {
                        MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOMODE);
                    }
                } else {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_NOMINIGAME);
                }
            }
        }

        //Spectator disables:
        if (mgPlayer.isInMinigame() && playerManager.getMinigamePlayer(event.getPlayer()).getMinigame().isSpectator(playerManager.getMinigamePlayer(event.getPlayer()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onTeleportAway(@NotNull PlayerTeleportEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());

        if (mgPlayer.isInMinigame()) {
            final @NotNull Minigame minigame = mgPlayer.getMinigame();

            if (((event.getCause() == TeleportCause.COMMAND || event.getCause() == TeleportCause.PLUGIN) && !minigame.areThirdPartyTeleportationAllowed()) ||
                (!mgPlayer.getMinigame().isAllowedEnderpearls() && event.getCause() == TeleportCause.ENDER_PEARL)) {
                if (!mgPlayer.getAllowTeleport()) {
                    Location from = event.getFrom();
                    Location to = event.getTo();
                    if (from.getWorld() != to.getWorld() || from.distanceSquared(to) > 4) {
                        event.setCancelled(true);
                        MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOTELEPORTALLOWED);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onGMChange(@NotNull PlayerGameModeChangeEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        if (mgPlayer.isInMinigame() && !mgPlayer.getAllowGamemodeChange()) {
            event.setCancelled(true);
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOGAMEMODE);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onFlyToggle(@NotNull PlayerToggleFlightEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        if (mgPlayer.isInMinigame() && (!mgPlayer.getMinigame().isSpectator(mgPlayer) || !mgPlayer.getMinigame().canSpectateFly()) && !mgPlayer.canFly()) {
            event.setCancelled(true);
            playerManager.quitMinigame(mgPlayer, true);
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOFLY);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void playerRevert(@NotNull RevertCheckpointEvent event) {
        final @NotNull MinigamePlayer mgPlayer = event.getMinigamePlayer();
        if (event.getMinigamePlayer().isInMinigame() &&
            event.getMinigamePlayer().getMinigame().getType() == MinigameType.MULTIPLAYER &&
            !event.getMinigamePlayer().getMinigame().isAllowedMPCheckpoints() &&
            !event.getMinigamePlayer().isJoiningLate()) {

            event.setCancelled(true);
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOREVERT,
                Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), event.getMinigamePlayer().getMinigame().getType().getName()));
        } else if (!event.getMinigamePlayer().getMinigame().hasStarted()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void commandExecute(@NotNull PlayerCommandPreprocessEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        if (mgPlayer.isInMinigame()) {
            for (String comd : playerManager.getDeniedCommands()) {
                if (event.getMessage().contains(comd)) {
                    event.setCancelled(true);
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_ERROR_NOCOMMAND);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void entityDamageEntity(@NotNull EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            switch (event.getDamager()) {
                case Snowball sb -> {
                    MinigamePlayer mgPlayer = playerManager.getMinigamePlayer((Player) event.getEntity());
                    if (mgPlayer.isInMinigame() && mgPlayer.getMinigame().hasPaintBallMode()) {
                        if (sb.getShooter() instanceof Player player) {
                            MinigamePlayer shooter = playerManager.getMinigamePlayer(player);
                            Minigame mgm = mgPlayer.getMinigame();
                            if (shooter.isInMinigame() && shooter.getMinigame().equals(mgPlayer.getMinigame())) {
                                if (!shooter.canPvP()) {
                                    event.setCancelled(true);
                                    return;
                                }

                                Team plyTeam = mgPlayer.getTeam();
                                Team atcTeam = shooter.getTeam();
                                if (!mgm.isTeamGame() || plyTeam != atcTeam || (atcTeam != null && atcTeam.isFriendlyFireEnabled())) {
                                    int damage = mgm.getPaintBallDamage();
                                    event.setDamage(damage);
                                }
                            }
                        }
                    }
                }
                case Player damager -> {
                    MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(damager);
                    if (mgPlayer.isInMinigame() && !mgPlayer.canPvP()) {
                        event.setCancelled(true);
                    } else if (mgPlayer.isInMinigame() && mgPlayer.getMinigame().getState() == MinigameState.ENDED &&
                        GameOverModule.getMinigameModule(mgPlayer.getMinigame()).isHumiliationMode() &&
                        GameOverModule.getMinigameModule(mgPlayer.getMinigame()).getLosers().contains(mgPlayer)) {
                        event.setCancelled(true);
                    }
                }
                case Arrow arrow -> {
                    if (arrow.getShooter() instanceof Player player) {
                        MinigamePlayer mgpl = playerManager.getMinigamePlayer(player);

                        if (mgpl.isInMinigame() && !mgpl.canPvP())
                            event.setCancelled(true);
                    }
                }
                default -> {
                }
            }
        }
        if (event.getDamager() instanceof Player player) {
            MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(player);
            ItemStack item = player.getEquipment().getItemInMainHand();
            if (MinigameTool.isMinigameTool(item) && player.hasPermission("minigame.tool")) {
                if (mgPlayer.isInMinigame()) {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_INMINIGAME);
                    return;
                }
                MinigameTool tool = new MinigameTool(item);
                if (player.isSneaking()) {
                    tool.openMenu(mgPlayer);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void playerRightClickEntity(final @NotNull PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(player);
        ItemStack item = player.getEquipment().getItemInMainHand();
        if (MinigameTool.isMinigameTool(item) && player.hasPermission("minigame.tool")) {
            if (mgPlayer.isInMinigame()) {
                MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.TOOL_ERROR_INMINIGAME);
                return;
            }
            MinigameTool tool = new MinigameTool(item);
            if (player.isSneaking()) {
                tool.openMenu(mgPlayer);
                event.setCancelled(true);
            }
        }
    }

    @SuppressWarnings("UnstableApiUsage") // shutup itemtype
    @EventHandler(ignoreCancelled = true)
    private void playerShoot(final @NotNull ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof final @NotNull Player player) {
            final @NotNull MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(player);
            final @Nullable Minigame minigame = mgPlayer.getMinigame();

            if (mgPlayer.isInMinigame() && minigame != null && minigame.hasUnlimitedAmmo()) {
                final @NotNull ItemType usedItem;
                final @NotNull Supplier<@NotNull ItemStack> itemSupplier;
                // Even though, we just could call ThrowableProjectile#getItem() for all ThrowableProjectile's,
                // I rather would not, since the item is just an itemtype to display for the player and might get overwritten to display something ese
                // by any other plugin or even commandblocks.
                // However, I can see merit, why anyone would like to use it, since it can keep nbt data.
                switch (event.getEntityType()) {
                    case ARROW -> {
                        // get item from entity for tipped arrows
                        itemSupplier = () -> ((Arrow)event.getEntity()).getItemStack();
                        usedItem = ((Arrow)event.getEntity()).getItemStack().getType().asItemType();
                    }
                    case SPECTRAL_ARROW -> {
                        // get item from entity for custom glow duration
                        itemSupplier = () -> ((Arrow)event.getEntity()).getItemStack();
                        usedItem = ItemType.SPECTRAL_ARROW;
                    }
                    case EGG -> {
                        // get item from entity for egg varient...
                        itemSupplier = () -> ((ThrowableProjectile)event.getEntity()).getItem();
                        usedItem = ((ThrowableProjectile)event.getEntity()).getItem().getType().asItemType();
                    }
                    case SNOWBALL -> {
                        itemSupplier = () -> ((ThrowableProjectile)event.getEntity()).getItem();
                        usedItem = ItemType.SNOWBALL;
                    }
                    case BREEZE_WIND_CHARGE, WIND_CHARGE -> { // weirdly enough not a ThrowableProjectile
                        usedItem = ItemType.WIND_CHARGE;
                        itemSupplier = usedItem::createItemStack;
                    }
                    // don't allow infinit enderperls, tridents, potions or non-vanilla fireable items to be infinit;
                    // of course I could easily generalize the code above to the point where the item just gets
                    // automatically retrieved from the projectile, for most if not all entities.
                    // but I choose not to, to not break already existing minigames, expectations
                    // or minigames in future updates when some projectile becomes unlimited, that really shouldn't.
                    default -> {
                        return;
                    }
                }

                //wait for the inventory to update
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    final @NotNull ItemStack itemInMainHand = player.getInventory().getItemInMainHand();

                    if (usedItem.equals(itemInMainHand.getType().asItemType())) {
                        itemInMainHand.setAmount(itemInMainHand.getMaxStackSize());
                        player.updateInventory();
                    } else {
                        player.getInventory().addItem(itemSupplier.get());
                    }
                }, 1L);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void playerHurt(@NotNull EntityDamageEvent event) {
        if (event.getEntity() instanceof final @NotNull Player player) {
           final @NotNull MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(player);

            if (mgPlayer.isInMinigame()) {
                final @NotNull Minigame mgm = mgPlayer.getMinigame();
                if (mgm.isSpectator(mgPlayer)) {
                    event.setCancelled(true);
                } else if ((!mgPlayer.getMinigame().hasStarted() && mgPlayer.getMinigame().getState() != MinigameState.ENDED) ||
                    mgPlayer.isJoiningLate()) {

                    event.setCancelled(true);
                } else if (mgPlayer.isInvincible()) {
                    event.setCancelled(true);
                } else if (event.getCause() == DamageCause.FALL &&
                    mgPlayer.getLoadout() != null && !mgPlayer.getLoadout().hasFallDamage()) {

                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void spectatorAttack(final @NotNull EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(player);
            if (mgPlayer.isInMinigame() && mgPlayer.getMinigame().isSpectator(mgPlayer)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void clickMenu(final @NotNull InventoryClickEvent event) {
        final @NotNull MinigamePlayer mgPlayer = playerManager.getMinigamePlayer((Player) event.getWhoClicked());
        if (mgPlayer.isInMenu()) {
            if (event.getRawSlot() < mgPlayer.getMenu().getSize()) {
                if (!mgPlayer.getMenu().getAllowModify() || mgPlayer.getMenu().hasMenuItem(event.getRawSlot())) {
                    event.setCancelled(true);
                }

                final @NotNull AMenuItem item = mgPlayer.getMenu().getMenuItem(event.getRawSlot());
                if (item != null) {
                    @NotNull ItemStack disItem = ItemStack.empty();
                    switch (event.getClick()) {
                        case LEFT -> {
                            if (event.getCursor().getType().isAir()) {
                                disItem = item.onClickWithItem(event.getCursor());
                            } else {
                                disItem = item.onClick();
                            }
                        }
                        case RIGHT -> disItem = item.onRightClick();
                        case SHIFT_LEFT -> disItem = item.onShiftClick();
                        case SHIFT_RIGHT -> disItem = item.onShiftRightClick();
                        case DOUBLE_CLICK -> disItem = item.onDoubleClick();
                    }

                    event.setCurrentItem(disItem);
                }
                /*
                 * Cancel special cases, where event.getRawSlot() is not in the Menu inventory,
                 *  but the event modifies it anyway
                 */
            } else if (!mgPlayer.getMenu().getAllowModify()) {
                final @NotNull Inventory topInv = event.getView().getTopInventory();
                switch (event.getAction()) {
                    case NOTHING, DROP_ALL_CURSOR, DROP_ONE_CURSOR, CLONE_STACK, UNKNOWN -> {
                    } // nothing
                    case PICKUP_ALL, PICKUP_SOME, PICKUP_HALF, PICKUP_ONE, DROP_ALL_SLOT, DROP_ONE_SLOT, // may take
                         PLACE_ALL, PLACE_SOME, PLACE_ONE, /*may place*/
                         SWAP_WITH_CURSOR, HOTBAR_SWAP /*may give and take*/ -> {
                        if (event.getClickedInventory() == topInv) {
                            event.setCancelled(true);
                        }
                    }
                    case COLLECT_TO_CURSOR -> { // may take complex
                        if (topInv.contains(event.getCursor().getType())) {
                            event.setCancelled(true);
                        }
                    }
                    case MOVE_TO_OTHER_INVENTORY -> {
                        event.setCancelled(true);
                    } // definitely one or the other
                }
            }

        } else if (mgPlayer.isInMinigame()) {
            if (!mgPlayer.getLoadout().allowOffHand() && event.getSlot() == 40) {
                event.setCancelled(true);
            } else if ((mgPlayer.getLoadout().isArmourLocked() && event.getSlot() >= 36 && event.getSlot() <= 39) ||
                (mgPlayer.getLoadout().isInventoryLocked() && event.getSlot() >= 0 && event.getSlot() <= 35)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void onOffhandSwap(@NotNull PlayerSwapHandItemsEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        if (mgPlayer.isInMenu()) {
            event.setCancelled(true);
        } else if (mgPlayer.isInMinigame()) {
            if (!mgPlayer.getLoadout().allowOffHand()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void dragMenu(@NotNull InventoryDragEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer((Player) event.getWhoClicked());
        if (mgPlayer.isInMenu()) {
            if (!mgPlayer.getMenu().getAllowModify()) {
                for (int slot : event.getRawSlots()) {
                    if (slot < mgPlayer.getMenu().getSize()) {
                        event.setCancelled(true);
                        break;
                    }
                }
            } else {
                Set<Integer> slots = new HashSet<>(event.getRawSlots());

                for (int slot : slots) {
                    if (mgPlayer.getMenu().hasMenuItem(slot)) {
                        event.getRawSlots().remove(slot);
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void closeMenu(@NotNull InventoryCloseEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer((Player) event.getPlayer());

        if (mgPlayer.isInMenu() && !mgPlayer.isMenuWaitingForInput()) {
            mgPlayer.setMenu(null);
        }
    }

    @EventHandler
    private void manualItemEntry(@NotNull AsyncPlayerChatEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());
        if (mgPlayer.isInMenu() && mgPlayer.getMenuItemWaitingForManualInput() instanceof StringConsumer stringAcceptor) {
            event.setCancelled(true);
            stringAcceptor.acceptString(event.getMessage());
            mgPlayer.setMenuItemWaitingForManualInput(null);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void playerHungry(@NotNull FoodLevelChangeEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer((Player) event.getEntity());

        if (mgPlayer.isInMinigame() && mgPlayer.getLoadout() != null &&
            !mgPlayer.getLoadout().hasHunger()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void playerMove(@NotNull PlayerMoveEvent event) {
        MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(event.getPlayer());

        if (mgPlayer.isInMinigame()) {
            if (mgPlayer.isFrozen()) {
                if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                    event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                    mgPlayer.teleport(new Location(event.getFrom().getWorld(), event.getFrom().getBlockX() + 0.5,
                        event.getTo().getBlockY(), event.getFrom().getBlockZ() + 0.5,
                        event.getPlayer().getLocation().getYaw(), event.getPlayer().getLocation().getPitch()));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void breakScoreboard(final @NotNull BlockBreakEvent event) { // todo no permission here :(
        final @NotNull Block block = event.getBlock();
        if (Tag.WALL_SIGNS.isTagged(block.getType())) {
            final @Nullable String minigameName = ScoreboardDisplay.getMinigameOfScoreboardString((Sign) block.getState(false));

            if (minigameName != null) {
                final @Nullable Minigame minigame = plugin.getMinigameManager().getMinigame(minigameName);

                if (minigame != null) {
                    minigame.getScoreboardData().removeDisplay(block);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void potionAffectsPlayer(final @NotNull PotionSplashEvent event) {
        if (event.getPotion().getShooter() instanceof Player player) {
            MinigamePlayer mgPlayer = playerManager.getMinigamePlayer(player);
            if (!mgPlayer.isInMinigame()) return;
            if (mgPlayer.getMinigame().friendlyFireSplashPotions()) return;
            List<Player> list = event.getAffectedEntities().stream()
                .filter(e -> e instanceof Player)
                .map(p -> (Player) p)
                .filter(p -> playerManager.getMinigamePlayer(p).isInMinigame())
                .filter(p -> playerManager.getMinigamePlayer(p).getMinigame() == mgPlayer.getMinigame())
                .toList();
            if (list.isEmpty()) return;
            Collection<PotionEffect> effects = event.getPotion().getEffects();
            list.stream().filter(Predicate.not(p -> isEffectApplicable(effects, mgPlayer, playerManager.getMinigamePlayer(p)))).forEach(p -> event.setIntensity(p, 0.0));
        }
    }

    @EventHandler(ignoreCancelled = true)
    private void effectAreaAffectsPlayer(final @NotNull AreaEffectCloudApplyEvent event) {
        if (!(event.getEntity().getSource() instanceof Player sourcePlayer)) return;
        MinigamePlayer mgSourcePlayer = playerManager.getMinigamePlayer(sourcePlayer);

        if (!mgSourcePlayer.isInMinigame()) return;
        if (mgSourcePlayer.getMinigame().friendlyFireLingeringPotions()) return;

        final List<PotionEffect> effects = Stream.concat(
            event.getEntity().getBasePotionType().getPotionEffects().stream(),
            event.getEntity().getCustomEffects().stream()
        ).toList();
        if (effects.isEmpty()) return;

        for (Iterator<LivingEntity> iterator = event.getAffectedEntities().iterator(); iterator.hasNext(); ) {
            LivingEntity livingEntity = iterator.next();
            if (livingEntity instanceof Player playerInCloud) {
                if (playerManager.getMinigamePlayer(playerInCloud).isInMinigame()) {
                    if (playerManager.getMinigamePlayer(playerInCloud).getMinigame() == mgSourcePlayer.getMinigame()) {

                        if (!isEffectApplicable(effects, mgSourcePlayer, playerManager.getMinigamePlayer(playerInCloud))) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    private boolean isEffectApplicable(@NotNull Collection<@NotNull PotionEffect> effectTypes,
                                       @NotNull MinigamePlayer mgPlayerEffecting, @NotNull MinigamePlayer mgPlayerReceiving) {
        if (mgPlayerEffecting.getMinigame().isTeamGame()) {
            if (mgPlayerEffecting.getTeam() == mgPlayerReceiving.getTeam()) { // todo friendly fire setting here
                return effectTypes.stream().noneMatch(s -> s.getType().getEffectCategory() == PotionEffectType.Category.HARMFUL);
            } else {
                return effectTypes.stream().anyMatch(s -> s.getType().getEffectCategory() == PotionEffectType.Category.BENEFICIAL);
            }
        } else if (mgPlayerEffecting == mgPlayerReceiving) {
            return effectTypes.stream().noneMatch(s -> s.getType().getEffectCategory() == PotionEffectType.Category.HARMFUL);
        } else {
            return effectTypes.stream().noneMatch(s -> s.getType().getEffectCategory() == PotionEffectType.Category.BENEFICIAL);
        }
    }
}
