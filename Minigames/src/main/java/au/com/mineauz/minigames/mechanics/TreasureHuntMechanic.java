package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.MinigameTimer;
import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.events.MinigameTimerTickEvent;
import au.com.mineauz.minigames.events.TimerExpireEvent;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.RewardsModule;
import au.com.mineauz.minigames.minigame.reward.ARewardType;
import au.com.mineauz.minigames.minigame.reward.ItemReward;
import au.com.mineauz.minigames.minigame.reward.scheme.StandardRewardScheme;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.safelocation.ASafeLocation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.time.Duration;
import java.util.*;

public class TreasureHuntMechanic extends AGameMechanic {
    private final @NotNull StringFlag locationName = new StringFlag("location", null);
    private final @NotNull IntegerFlag maxRadius = new IntegerFlag("maxradius", 1000);
    private final @NotNull IntegerFlag maxHeight = new IntegerFlag("maxheight", 20);
    private final @NotNull IntegerFlag minTreasure = new IntegerFlag("mintreasure", 0);
    private final @NotNull IntegerFlag maxTreasure = new IntegerFlag("maxtreasure", 8);
    private final @NotNull TimeFlag treasureWaitTime = new TimeFlag("treasurehuntwait", Minigames.getPlugin().getConfig().getLong("treasurehunt.waittime"));
    private final @NotNull TimeFlag hintWaitTime = new TimeFlag("hintWaitTime", 500L);
    private final @NotNull ArrayList<@NotNull Component> curHints = new ArrayList<>();
    private final @NotNull Map<@NotNull UUID, @NotNull Long> hintUse = new HashMap<>();
    //Unsaved Data
    private @Nullable Location treasureLocation = null; // todo SafeLocation
    private boolean treasureFound = false;

    public TreasureHuntMechanic(final @NotNull Minigames plugin, final @NotNull Key key, final @NotNull Minigame minigame) {
        super(plugin, key, minigame);
    }

    public void removeTreasure() {
        clearHints();
        if (hasTreasureLocation()) {
            final @NotNull Location old = getTreasureLocation();
            if (old.getWorld() != null && !old.getWorld().isChunkLoaded(old.getChunk().getX(), old.getChunk().getZ())) {
                final boolean loaded = old.getChunk().load();
                @Nullable Chunk c = null;
                if (loaded) {
                    c = old.getChunk();
                    c.setForceLoaded(true);
                }
                if (old.getBlock().getState() instanceof Chest chest) {
                    chest.getInventory().clear();
                    old.getBlock().setBlockData(BlockType.AIR.createBlockData());
                }
                if (loaded) {
                    c.setForceLoaded(false);
                    c.unload();
                }
                setTreasureLocation(null);
            }
        }
    }

    public void spawnTreasure() {
        if (hasTreasureLocation())
            removeTreasure();
        if (!getCurrentHints().isEmpty())
            clearHints();
        setTreasureFound(false);

        ASafeLocation tcpos = minigame.getStartLocations().getFirst(); // todo use random one via shuffle

        if (tcpos.getWorld() == null) {
            plugin.getComponentLogger().error("Couldn't spawn treasure because the world for Minigame " + minigame.getName() + " was unloaded or removed!");
            return;
        }

        final @NotNull Location rpos = tcpos.toLocation();
        final double rx;
        final double ry;
        final double rz;
        final int maxradius;
        if (getMaxRadius() <= 0) {
            maxradius = 1000;
        } else {
            maxradius = getMaxRadius();
        }
        final int maxheight = getMaxHeight();

        final @NotNull Random rand = new Random();
        int rrad = rand.nextInt(maxradius);
        double randCir = 2 * Math.PI * rand.nextInt(360) / 360;
        rx = tcpos.x() - 0.5 + Math.round(rrad * Math.cos(randCir));
        rz = tcpos.z() - 0.5 + Math.round(rrad * Math.sin(randCir));

        ry = tcpos.y() + rand.nextInt(maxheight);

        rpos.setX(rx);
        rpos.setY(ry);
        rpos.setZ(rz);

        //Add a new Chest
        //TODO: Improve so no invalid spawns (Not over void, Strict containment)
        if (rpos.getBlock().getType().isAir()) {
            final int minWorldHeight = rpos.getWorld().getMinHeight();
            // find first block below that is not air anymore to spawn on top of
            while (rpos.getBlock().getType().isAir() && rpos.getY() > minWorldHeight) {
                rpos.setY(rpos.getY() - 1);
            }
            rpos.setY(rpos.getY() + 1);
        } else {
           final int maxWorldHeight = rpos.getWorld().getMaxHeight();
            // find first block above that is air to spawn into
            while (!rpos.getBlock().getType().isAir() && rpos.getY() < maxWorldHeight) {
                rpos.setY(rpos.getY() + 1);
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> rpos.getBlock().setBlockData(BlockType.CHEST.createBlockData()), 1L);

        //Fill new container
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (rpos.getBlock().getState() instanceof Container container) {

                // TODO: Treasure hunt needs own rewards specification
                final RewardsModule rewards = RewardsModule.getModule(minigame);
                if (rewards.getScheme() instanceof StandardRewardScheme) {
                    if (!((StandardRewardScheme) rewards.getScheme()).getPrimaryReward().getRewards().isEmpty()) {
                        int numItems = (int) Math.min(container.getInventory().getSize(), Math.round(Math.random() * (getMaxTreasure() - getMinTreasure())) + getMinTreasure());

                        final ItemStack @NotNull[] items = new ItemStack[27];
                        for (int i = 0; i < numItems; i++) {
                            ARewardType rew = ((StandardRewardScheme) rewards.getScheme()).getPrimaryReward().getReward().getFirst();
                            if (rew instanceof ItemReward irew) {
                                items[i] = irew.getRewardItem();
                            }
                        }
                        Collections.shuffle(Arrays.asList(items));
                        container.getInventory().setContents(items);
                    }
                }
            }
        }, 0L);

        setTreasureLocation(rpos);
        MessageManager.debugMessage(minigame.getName() + " treasure chest spawned at: " + rpos);
        MessageManager.broadcastServer(MessageManager.getMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_SPAWN,
                        Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(maxradius)),
                        Placeholder.unparsed(MinigamePlaceHolderKey.LOCATION.getKey(), getLocationName())),
            minigame, "minigame.treasure.announce");

        minigame.setMinigameTimer(new MinigameTimer(minigame, minigame.getTimer()));
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        locationName.saveValue(config);
        maxRadius.saveValue(config);
        minTreasure.saveValue(config);
        maxTreasure.saveValue(config);
        treasureWaitTime.saveValue(config);
        hintWaitTime.saveValue(config);
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        locationName.loadValue(config);
        maxRadius.loadValue(config);
        minTreasure.loadValue(config);
        maxTreasure.loadValue(config);
        treasureWaitTime.loadValue(config);
        hintWaitTime.loadValue(config);
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public @NotNull EnumSet<@NotNull MinigameType> validTypes() {
        return EnumSet.of(MinigameType.GLOBAL);
    }

    @Override
    public boolean checkCanStart(@Nullable MinigamePlayer caller) {
        return true;
    }

//    public @Nullable SequencedCollection<@NotNull TypeDependentDisplayData> addEditMenuOptions(@NotNull Menu menu) {
//        List<String> thDes = new ArrayList<>();
//        thDes.add("Treasure hunt related<newline>settings.");
//        menu.setItem(new MenuItemPage(ItemType.CHEST, "Treasure Hunt Settings", thDes, treasureHunt));
//        MenuItemDisplayLoadout defLoad = new MenuItemDisplayLoadout(ItemType.DIAMOND_SWORD, "Default Loadout", LoadoutModule.getMinigameModule(this).getDefaultPlayerLoadout(), this);
//        defLoad.setAllowDelete(false);
//        menu.setItem(defLoad);
//
//        return null;
//    }

    @SuppressWarnings("UnstableApiUsage") // shutup ItemType
    @Override
    public @NotNull MenuItemPage displayMechanicSettings(final @NotNull Menu previous) {
        final @NotNull Menu treasureHuntMenu = new Menu(6, minigame.getDisplayName(), previous.getIntendedViewer());

        final @NotNull List<@NotNull AMenuItem> itemsTreasureHunt = new ArrayList<>(5);
        itemsTreasureHunt.add(locationName.getMenuItem(ItemType.MAP, MgMenuLangKey.MENU_TREASUREHUNT_LOCATION_NAME,
            MgMenuLangKey.MENU_TREASUREHUNT_LOCATION_DESCRIPTION));
        itemsTreasureHunt.add(maxRadius.getMenuItem(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_TREASUREHUNT_MAX_RADIUS_NAME, 10, null));
        itemsTreasureHunt.add(maxHeight.getMenuItem(ItemType.BEACON, MgMenuLangKey.MENU_TREASUREHUNT_MAX_HEIGHT_NAME,
            MgMenuLangKey.MENU_TREASUREHUNT_MAX_HEIGHT_DESCRIPTION, 1, 256));
        itemsTreasureHunt.add(minTreasure.getMenuItem(ItemType.STONE_SLAB, MgMenuLangKey.MENU_TREASUREHUNT_MIN_ITEMS_NAME,
            MgMenuLangKey.MENU_TREASUREHUNT_MIN_ITEMS_DESCRIPTION, 0, 27));
        itemsTreasureHunt.add(maxTreasure.getMenuItem(ItemType.STONE, MgMenuLangKey.MENU_TREASUREHUNT_MAX_ITEMS_NAME,
            MgMenuLangKey.MENU_TREASUREHUNT_MAX_ITEMS_DESCRIPTION, 0, 27));
        itemsTreasureHunt.add(treasureWaitTime.getMenuItem(MenuDisplayTypes.timeType(), MgMenuLangKey.MENU_TREASUREHUNT_DELAY_RESTART_NAME, 0L, null));
        itemsTreasureHunt.add(hintWaitTime.getMenuItem(MenuDisplayTypes.timeType(), MgMenuLangKey.MENU_TREASUREHUNT_DELAY_HINT_NAME, 0L, null));
        treasureHuntMenu.addItems(itemsTreasureHunt);
        treasureHuntMenu.setItem(new MenuItemBack(previous), treasureHuntMenu.getSize() - 9);

        return new MenuItemPage(MenuDisplayTypes.genericSubMenu(), MgMenuLangKey.MENU_MINIGAME_MECHANIC_SETTINGS_NAME, treasureHuntMenu);
    }

    @Override
    public void startMinigame(final @Nullable MinigamePlayer caller) {
        if (getLocationName() != null) {
            spawnTreasure();

            if (Bukkit.getOnlinePlayers().isEmpty()) {
                minigame.getMinigameTimer().stopTimer();
            }
        } else {
            if (caller == null) {
                Minigames.getPlugin().getComponentLogger().info("Treasure Hunt \"" + minigame.getName() + "\" requires a location name to run!");
            } else {
                MessageManager.sendMessage(caller, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_TREASUREHUNT_ERROR_NOLOCATION);
            }
        }
    }

    @Override
    public void stopMinigame() {
        minigame.getMinigameTimer().stopTimer();
        minigame.setMinigameTimer(null);
        clearHints();

        if (hasTreasureLocation()) {
            removeTreasure();
            if (!isTreasureFound()) {
                MessageManager.broadcastServer(MessageManager.getMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_REMOVED,
                                Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName())),
                        minigame, "minigame.treasure.announce");
            }
        }
    }

    @Override
    public void onJoinMinigame(final @NotNull MinigamePlayer player) {
    }

    @Override
    public void quitMinigame(final @NotNull MinigamePlayer player, final boolean forced) {
    }

    @Override
    public void endMinigame(final @NotNull List<@NotNull MinigamePlayer> winners,
                            final @NotNull List<@NotNull MinigamePlayer> losers) {
    }

    @EventHandler
    private void timerTick(final @NotNull MinigameTimerTickEvent event) {
        if (event.getMinigame().getType() != MinigameType.GLOBAL ||
            !minigame.equals(event.getMinigame())) {
            return;
        }

        if (!hasTreasureLocation() || isTreasureFound()) return;

        long time = event.getTimeLeft();
        long hintTime1 = event.getMinigame().getTimer() - 1;
        int hintTime2 = (int) (event.getMinigame().getTimer() * 0.75);
        int hintTime3 = (int) (event.getMinigame().getTimer() * 0.50);
        int hintTime4 = (int) (event.getMinigame().getTimer() * 0.25);
        Location block = getTreasureLocation();

        if (time == hintTime1) {
            final double dfcx;
            final double dfcz;
            final @NotNull String xdir;
            final @NotNull String zdir;

            if (minigame.getStartLocations().getFirst().x() > block.getX()) {
                dfcx = minigame.getStartLocations().getFirst().x() - block.getX();
                xdir = MessageManager.getRawMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_WEST);
            } else {
                dfcx = block.getX() - minigame.getStartLocations().getFirst().x();
                xdir = MessageManager.getRawMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_EAST);
            }
            if (minigame.getStartLocations().getFirst().z() > block.getZ()) {
                dfcz = minigame.getStartLocations().getFirst().z() - block.getZ();
                zdir = MessageManager.getRawMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_NORTH);
            } else {
                dfcz = block.getZ() - minigame.getStartLocations().getFirst().z();
                zdir = MessageManager.getRawMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_SOUTH);
            }
            Component dir;
            MiniMessage miniMessage = MiniMessage.miniMessage();

            if (dfcz > dfcx) {
                if (dfcx > dfcz / 2) {
                    dir = miniMessage.deserialize(zdir + xdir.toLowerCase());
                } else {
                    dir = miniMessage.deserialize(zdir);
                }
            } else {
                if (dfcz > dfcx / 2) {
                    dir = miniMessage.deserialize(zdir + xdir.toLowerCase());
                } else {
                    dir = miniMessage.deserialize(xdir);
                }
            }
            final @NotNull Component hint1 = MessageManager.getMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_HINT1,
                    Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()),
                    Placeholder.component(MinigamePlaceHolderKey.DIRECTION.getKey(), dir),
                    Placeholder.parsed(MinigamePlaceHolderKey.LOCATION.getKey(), getLocationName()));
            MessageManager.broadcastServer(hint1, minigame, "minigame.treasure.announce");
            addHint(hint1);
        } else if (time == hintTime2) {
            block.setY(block.getY() - 1);
            final @NotNull Component hint2 = MessageManager.getMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_HINT2,
                    Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()),
                    Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), block.getBlock().getType().toString().toLowerCase().replace("_", " ")));
            MessageManager.broadcastServer(hint2, minigame, "minigame.treasure.announce");
            addHint(hint2);
            block.setY(block.getY() + 1);
        } else if (time == hintTime3) {
            int height = block.getBlockY();
            final @NotNull Component dir;
            int dist;
            if (height > 62) {
                dist = height - 62;
                dir = MessageManager.getMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_ABOVE);
            } else {
                dist = 62 - height;
                dir = MessageManager.getMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_BELOW);
            }
            final @NotNull Component hint3 = MessageManager.getMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_HINT3,
                    Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()),
                    Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(dist)),
                    Placeholder.component(MinigamePlaceHolderKey.DIRECTION.getKey(), dir));
            MessageManager.broadcastServer(hint3, minigame, "minigame.treasure.announce");
            addHint(hint3);
        } else if (time == hintTime4) {
            final @NotNull Component hint4 = MessageManager.getMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_HINT4,
                    Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()),
                    Placeholder.unparsed(MinigamePlaceHolderKey.BIOME.getKey(), block.getBlock().getBiome().toString().toLowerCase().replace("_", " ")));
            MessageManager.broadcastServer(hint4, minigame, "minigame.treasure.announce");
            addHint(hint4);
        }
    }

    @EventHandler
    private void timerExpire(final @NotNull TimerExpireEvent event) {
        if (event.getMinigame().getType() != MinigameType.GLOBAL ||
            !minigame.equals(event.getMinigame())) {
            return;
        }

        if (hasTreasureLocation()) {
            minigame.setMinigameTimer(new MinigameTimer(minigame, getTreasureWaitTime()));
            Location old = getTreasureLocation();
            removeTreasure();
            if (!isTreasureFound()) {
                MessageManager.broadcastServer(MessageManager.getMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_DESPAWN,
                                Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()),
                                Placeholder.component(MinigamePlaceHolderKey.LOCATION.getKey(), MessageManager.formatBlockLocation(old))),
                        minigame, "minigame.treasure.announce");
            }
            setTreasureFound(false);
        } else {
            spawnTreasure();
        }
    }

    @EventHandler
    private void interactEvent(final @NotNull PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final Block cblock = event.getClickedBlock();
            final boolean cancelled = (event.useInteractedBlock() == Event.Result.DENY || event.useItemInHand() == Event.Result.DENY);
            if (cblock != null && cblock.getState() instanceof Chest chest && !cancelled) {

                if (minigame.getType() == MinigameType.GLOBAL &&
                        minigame.getMinigameTimer() != null) {
                    if (!isTreasureFound() && hasTreasureLocation()) {
                        int x1 = getTreasureLocation().getBlockX();
                        int x2 = cblock.getLocation().getBlockX();
                        int y1 = getTreasureLocation().getBlockY();
                        int y2 = cblock.getLocation().getBlockY();
                        int z1 = getTreasureLocation().getBlockZ();
                        int z2 = cblock.getLocation().getBlockZ();
                        if (x2 == x1 && y2 == y1 && z2 == z1) {
                            MessageManager.broadcastServer(MessageManager.getMessage(MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERFOUND,
                                            Placeholder.component(MinigamePlaceHolderKey.PLAYER.getKey(), event.getPlayer().displayName()),
                                            Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName())),
                                    minigame, "minigame.treasure.announce"); //todo Permission manager
                            event.setCancelled(true);
                            event.getPlayer().openInventory(chest.getInventory());

                            setTreasureFound(true);
                            minigame.getMinigameTimer().setTimeLeft(300);
                        }
                    }
                }
            }
        }
    }

    public int getMaxRadius() {
        return maxRadius.getFlag();
    }

    public void setMaxRadius(final int maxRadius) {
        this.maxRadius.setFlag(maxRadius);
    }

    public int getMaxHeight() {
        return maxHeight.getFlag();
    }

    public void setMaxHeight(final int maxHeight) {
        this.maxHeight.setFlag(maxHeight);
    }

    public String getLocationName() {
        return locationName.getFlag();
    }

    public void setLocationName(final String locationName) {
        this.locationName.setFlag(locationName);
    }

    public int getMinTreasure() {
        return minTreasure.getFlag();
    }

    public void setMinTreasure(final int minTreasure) {
        this.minTreasure.setFlag(minTreasure);
    }

    public int getMaxTreasure() {
        return maxTreasure.getFlag();
    }

    public void setMaxTreasure(final int maxTreasure) {
        this.maxTreasure.setFlag(maxTreasure);
    }

    public Location getTreasureLocation() {
        return treasureLocation.clone();
    }

    public void setTreasureLocation(final @Nullable Location loc) {
        treasureLocation = loc;
    }

    public boolean hasTreasureLocation() {
        return treasureLocation != null;
    }

    public boolean isTreasureFound() {
        return treasureFound;
    }

    public void setTreasureFound(final boolean bool) {
        treasureFound = bool;
    }

    public @NotNull List<@NotNull Component> getCurrentHints() {
        return curHints;
    }

    public void addHint(final @NotNull Component hint) {
        curHints.add(hint.color(NamedTextColor.GRAY));
    }

    public void clearHints() {
        curHints.clear();
    }

    public long getTreasureWaitTime() {
        return treasureWaitTime.getFlag();
    }

    public void setTreasureWaitTime(final long time) {
        treasureWaitTime.setFlag(time);
    }

    public long getLastHintUse(final @NotNull MinigamePlayer player) {
        if (!hintUse.containsKey(player.getUUID()))
            return -1L;
        return hintUse.get(player.getUUID());
    }

    public boolean canUseHint(final @NotNull MinigamePlayer player) {
        if (hintUse.containsKey(player.getUUID())) {
            long curtime = System.currentTimeMillis();
            long lastuse = curtime - hintUse.get(player.getUUID());
            return lastuse >= getHintDelay() * 1000L;
        }
        return true;
    }

    public void addHintUse(final @NotNull MinigamePlayer player) {
        hintUse.put(player.getUUID(), System.currentTimeMillis());
    }

    public void clearHintUsage() {
        hintUse.clear();
    }

    public void getHints(final @NotNull MinigamePlayer mgPlayer) {
        if (!hasTreasureLocation()) return;
        final @NotNull Location block = getTreasureLocation();
        final @NotNull Location playerLoc = mgPlayer.getLocation();
        if (playerLoc.getWorld().getName().equals(getTreasureLocation().getWorld().getName())) {
            double distance = playerLoc.distance(block);
            int maxradius = getMaxRadius();
            if (canUseHint(mgPlayer)) {
                if (distance > maxradius) {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE6);
                } else if (distance > (double) maxradius / 2) {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE5);
                } else if (distance > (double) maxradius / 4) {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE4);
                } else if (distance > 50) {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE3);
                } else if (distance > 20) {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE2);
                } else if (distance < 20) {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE1);
                }
                MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_TIMELEFT,
                    Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(minigame.getMinigameTimer().getTimeLeft()))));

                MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_GLOBALHINTS);
                if (getCurrentHints().isEmpty()) {
                    MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_NOHINT);
                } else {
                    for (Component globalHint : getCurrentHints()) {
                        MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, globalHint);
                    }
                }

                addHintUse(mgPlayer);
            } else {
                int nextUse = (300000 - (int) (System.currentTimeMillis() - getLastHintUse(mgPlayer))) / 1000;

                MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_NOUSE,
                    Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getDisplayName()),
                    Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(nextUse)));

                MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_TIMELEFT,
                    Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(minigame.getMinigameTimer().getTimeLeft()))));
            }
        } else {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_WRONGWORLD,
                Placeholder.unparsed(MinigamePlaceHolderKey.WORLD.getKey(), block.getWorld().getName()));
        }
    }

    public long getHintDelay() {
        return hintWaitTime.getFlag();
    }

    public void setHintDelay(long time) {
        hintWaitTime.setFlag(time);
    }
}
