package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.IntegerFlag;
import au.com.mineauz.minigames.config.StringFlag;
import au.com.mineauz.minigames.config.TimeFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;

public class TreasureHuntModule extends MinigameModule {
    private final @NotNull StringFlag location = new StringFlag(null, "location");
    private final @NotNull IntegerFlag maxRadius = new IntegerFlag(1000, "maxradius");
    private final @NotNull IntegerFlag maxHeight = new IntegerFlag(20, "maxheight");
    private final @NotNull IntegerFlag minTreasure = new IntegerFlag(0, "mintreasure");
    private final @NotNull IntegerFlag maxTreasure = new IntegerFlag(8, "maxtreasure");
    private final @NotNull TimeFlag treasureWaitTime = new TimeFlag(Minigames.getPlugin().getConfig().getLong("treasurehunt.waittime"), "treasurehuntwait");
    private final @NotNull TimeFlag hintWaitTime = new TimeFlag(500L, "hintWaitTime");
    private final @NotNull ArrayList<@NotNull Component> curHints = new ArrayList<>();
    private final @NotNull Map<@NotNull UUID, @NotNull Long> hintUse = new HashMap<>();
    //Unsaved Data
    private @Nullable Location treasureLocation = null;
    private boolean treasureFound = false;

    public TreasureHuntModule(@NotNull Minigame mgm, @NotNull String name) {
        super(mgm, name);
    }

    public static @Nullable TreasureHuntModule getMinigameModule(@NotNull Minigame mgm) {
        return ((TreasureHuntModule) mgm.getModule(MgModules.TREASURE_HUNT.getName()));
    }

    @Override
    public boolean useSeparateConfig() {
        return false;
    }

    @Override
    public void save(@NotNull FileConfiguration config, @NotNull String path) {
        location.saveValue(config, path);
        maxRadius.saveValue(config, path);
        minTreasure.saveValue(config, path);
        maxTreasure.saveValue(config, path);
        treasureWaitTime.saveValue(config, path);
        hintWaitTime.saveValue(config, path);
    }

    @Override
    public void load(@NotNull FileConfiguration config, @NotNull String path) {
        location.loadValue(config, path);
        maxRadius.loadValue(config, path);
        minTreasure.loadValue(config, path);
        maxTreasure.loadValue(config, path);
        treasureWaitTime.loadValue(config, path);
        hintWaitTime.loadValue(config, path);
    }

    @Override
    public void addEditMenuOptions(@NotNull Menu menu) {

    }

    @Override
    public boolean displayMechanicSettings(@NotNull Menu previous) {
        Menu treasureHunt = new Menu(6, getMinigame().getDisplayName(), previous.getViewer());

        List<MenuItem> itemsTreasureHunt = new ArrayList<>(5);
        itemsTreasureHunt.add(location.getMenuItem(Material.WHITE_BED, MgMenuLangKey.MENU_TREASUREHUNT_LOCATION_NAME,
                MgMenuLangKey.MENU_TREASUREHUNT_LOCATION_DESCRIPTION));
        itemsTreasureHunt.add(maxRadius.getMenuItem(Material.ENDER_PEARL, MgMenuLangKey.MENU_TREASUREHUNT_MAX_RADIUS_NAME, 10, null));
        itemsTreasureHunt.add(maxHeight.getMenuItem(Material.BEACON, MgMenuLangKey.MENU_TREASUREHUNT_MAX_HEIGHT_NAME,
                MgMenuLangKey.MENU_TREASUREHUNT_MAX_HEIGHT_DESCRIPTION, 1, 256));
        itemsTreasureHunt.add(minTreasure.getMenuItem(Material.STONE_SLAB, MgMenuLangKey.MENU_TREASUREHUNT_MIN_ITEMS_NAME,
                MgMenuLangKey.MENU_TREASUREHUNT_MIN_ITEMS_DESCRIPTION, 0, 27));
        itemsTreasureHunt.add(maxTreasure.getMenuItem(Material.STONE, MgMenuLangKey.MENU_TREASUREHUNT_MAX_ITEMS_NAME,
                MgMenuLangKey.MENU_TREASUREHUNT_MAX_ITEMS_DESCRIPTION, 0, 27));
        itemsTreasureHunt.add(treasureWaitTime.getMenuItem(Material.CLOCK, MgMenuLangKey.MENU_TREASUREHUNT_DELAY_RESTART_NAME, 0L, null));
        itemsTreasureHunt.add(hintWaitTime.getMenuItem(Material.CLOCK, MgMenuLangKey.MENU_TREASUREHUNT_DELAY_HINT_NAME, 0L, null));
        treasureHunt.addItems(itemsTreasureHunt);
        treasureHunt.addItem(new MenuItemBack(previous), treasureHunt.getSize() - 9);
        treasureHunt.displayMenu(treasureHunt.getViewer());
        return true;
    }

    public int getMaxRadius() {
        return maxRadius.getFlag();
    }

    public void setMaxRadius(int maxRadius) {
        this.maxRadius.setFlag(maxRadius);
    }

    public int getMaxHeight() {
        return maxHeight.getFlag();
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight.setFlag(maxHeight);
    }

    public String getLocation() {
        return location.getFlag();
    }

    public void setLocation(String location) {
        this.location.setFlag(location);
    }

    public int getMinTreasure() {
        return minTreasure.getFlag();
    }

    public void setMinTreasure(int minTreasure) {
        this.minTreasure.setFlag(minTreasure);
    }

    public int getMaxTreasure() {
        return maxTreasure.getFlag();
    }

    public void setMaxTreasure(int maxTreasure) {
        this.maxTreasure.setFlag(maxTreasure);
    }

    public Location getTreasureLocation() {
        return treasureLocation.clone();
    }

    public void setTreasureLocation(Location loc) {
        treasureLocation = loc;
    }

    public boolean hasTreasureLocation() {
        return treasureLocation != null;
    }

    public boolean isTreasureFound() {
        return treasureFound;
    }

    public void setTreasureFound(boolean bool) {
        treasureFound = bool;
    }

    public @NotNull List<@NotNull Component> getCurrentHints() {
        return curHints;
    }

    public void addHint(@NotNull Component hint) {
        curHints.add(hint.color(NamedTextColor.GRAY));
    }

    public void clearHints() {
        curHints.clear();
    }

    public long getTreasureWaitTime() {
        return treasureWaitTime.getFlag();
    }

    public void setTreasureWaitTime(long time) {
        treasureWaitTime.setFlag(time);
    }

    public long getLastHintUse(@NotNull MinigamePlayer player) {
        if (!hintUse.containsKey(player.getUUID()))
            return -1L;
        return hintUse.get(player.getUUID());
    }

    public boolean canUseHint(@NotNull MinigamePlayer player) {
        if (hintUse.containsKey(player.getUUID())) {
            long curtime = System.currentTimeMillis();
            long lastuse = curtime - hintUse.get(player.getUUID());
            return lastuse >= getHintDelay() * 1000L;
        }
        return true;
    }

    public void addHintUse(@NotNull MinigamePlayer player) {
        hintUse.put(player.getUUID(), System.currentTimeMillis());
    }

    public void clearHintUsage() {
        hintUse.clear();
    }

    public void getHints(@NotNull MinigamePlayer mgPlayer) {
        if (!hasTreasureLocation()) return;
        Location block = getTreasureLocation();
        if (mgPlayer.getPlayer().getWorld().getName().equals(getTreasureLocation().getWorld().getName())) {
            Location ploc = mgPlayer.getLocation();
            double distance = ploc.distance(block);
            int maxradius = getMaxRadius();
            if (canUseHint(mgPlayer)) {
                if (distance > maxradius) {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE6);
                } else if (distance > (double) maxradius / 2) {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE5);
                } else if (distance > (double) maxradius / 4) {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE4);
                } else if (distance > 50) {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE3);
                } else if (distance > 20) {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE2);
                } else if (distance < 20) {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_DISTANCE1);
                }
                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_TIMELEFT,
                        Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(getMinigame().getMinigameTimer().getTimeLeft()))));

                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_GLOBALHINTS);
                if (getCurrentHints().isEmpty()) {
                    MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_NOHINT);
                } else {
                    for (Component globalHint : getCurrentHints()) {
                        MinigameMessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, globalHint);
                    }
                }

                addHintUse(mgPlayer);
            } else {
                int nextUse = (300000 - (int) (System.currentTimeMillis() - getLastHintUse(mgPlayer))) / 1000;

                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_NOUSE,
                        Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), getMinigame().getDisplayName()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(nextUse)));

                MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_TIMELEFT,
                        Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(getMinigame().getMinigameTimer().getTimeLeft()))));
            }
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_TREASUREHUNT_PLAYERSPECIFICHINT_WRONGWORLD,
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
