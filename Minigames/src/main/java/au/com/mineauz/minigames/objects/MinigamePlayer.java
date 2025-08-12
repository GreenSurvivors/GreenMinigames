package au.com.mineauz.minigames.objects;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.StoredPlayerCheckpoints;
import au.com.mineauz.minigames.config.MinigameSave;
import au.com.mineauz.minigames.display.DisplayCuboid;
import au.com.mineauz.minigames.managers.DependencyManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.modules.LoadoutModule;
import au.com.mineauz.minigames.script.ScriptObject;
import au.com.mineauz.minigames.script.ScriptReference;
import au.com.mineauz.minigames.script.ScriptValue;
import au.com.mineauz.minigames.script.ScriptWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * wrapper class to keep track of players with additional information.
 * A MinigamePlayer does NOT have to be in a Minigame to be valid!
 */
public class MinigamePlayer implements ScriptObject {
    private final @NotNull Player player;
//    private final @NotNull List<@NotNull String> singlePlayerFlags = new ArrayList<>(); // the whole singleplayer flag system is unused.
    private final @NotNull List<@NotNull String> tempClaimedRewards = new ArrayList<>();
    private final @NotNull List<@NotNull ItemStack> tempRewardItems = new ArrayList<>();
    private final @NotNull List<@NotNull ItemStack> rewardItems = new ArrayList<>();
    private final @NotNull List<@NotNull String> claimedScoreSigns = new ArrayList<>();
    private final @NotNull StoredPlayerCheckpoints spc;
    private boolean allowTP;
    private boolean allowGMChange;
    private boolean canFly;
    private @Nullable Scoreboard lastScoreboard;
    private @Nullable Minigame minigame;
    private @Nullable PlayerLoadout loadout;
    private boolean requiredQuit;
    private @Nullable Location startPos;
    private @Nullable Location quitPos;
    private @Nullable Location checkpoint;
    private int kills;
    private int deaths;
    private int score;
    private long startTime;
    private long endTime;
    private long storedTime;
    private long completeTime;
    private int reverts;
    private boolean isLatejoining;
    private boolean isFrozen;
    private boolean canPvP = true;
    private boolean isInvincible;
    private boolean canInteract = true;
    private @Nullable Team team;
    private @Nullable Menu menu;
    private boolean noClose;
    private @Nullable MenuItem manualEntry;
    private @Nullable Location selection1;
    private @Nullable Location selection2;
    private @Nullable DisplayCuboid selectionDisplay;
    private OfflineMinigamePlayer offlineMinigamePlayer;
    private @NotNull List<@NotNull String> claimedRewards = new ArrayList<>();
    private int lateJoinTimer = -1;

    public MinigamePlayer(final @NotNull Player player) {
        this.player = player;
        spc = new StoredPlayerCheckpoints(getUUID().toString());

        final File plcp = new File(Minigames.getPlugin().getDataFolder() + File.separator + "playerdata" +
                File.separator + "checkpoints" + File.separator + getUUID() + ".yml");
        if (plcp.exists()) {
            getStoredPlayerCheckpoints().loadCheckpoints();
        }
    }

    public @Nullable Location getStartPos() {
        return startPos;
    }

    public void setStartPos(final @Nullable Location startPos) {
        this.startPos = startPos;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull String getName() {
        return player.getName();
    }

    public @NotNull Component displayName() {
        return player.displayName();
    }

    public @NotNull UUID getUUID() {
        return player.getUniqueId();
    }

    public @NotNull Location getLocation() {
        return player.getLocation();
    }

    public void storePlayerData() {
        final ItemStack[] storedItems = player.getInventory().getContents();
        final ItemStack[] storedArmour = player.getInventory().getArmorContents();
        final int food = player.getFoodLevel();
        final double health = player.getHealth();
        final float saturation = player.getSaturation();
        lastScoreboard = player.getScoreboard();
        final GameMode lastGM = player.getGameMode();
        float exp = player.getExp();
        if (exp < 0) {
            Minigames.getCmpnntLogger().warn("Player Experience was less that 0: " + player.getName() + " " + player.getExp());
            exp = 0;
        }
        final int level = player.getLevel();

        player.setSaturation(15);
        player.setFoodLevel(20);
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getDefaultValue());
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setLevel(0);
        player.setExp(0);

        offlineMinigamePlayer = new OfflineMinigamePlayer(getPlayer().getUniqueId(), storedItems, storedArmour, food,
                health, saturation, lastGM, exp, level, getPlayer().getLocation());
        player.updateInventory();
    }

    public void restorePlayerData() {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        player.getInventory().setContents(offlineMinigamePlayer.getStoredItems());
        player.getInventory().setArmorContents(offlineMinigamePlayer.getStoredArmour());
        player.setFoodLevel(offlineMinigamePlayer.getFood());
        if (offlineMinigamePlayer.getHealth() > 20) { // todo don't hardcode. use attributes!
            player.setHealth(20);
        } else {
            player.setHealth(offlineMinigamePlayer.getHealth());
        }
        player.setSaturation(offlineMinigamePlayer.getSaturation());
        player.setScoreboard(Objects.requireNonNullElseGet(lastScoreboard, () -> player.getServer().getScoreboardManager().getMainScoreboard()));

        if (offlineMinigamePlayer.getExp() >= 0) {
            player.setExp(offlineMinigamePlayer.getExp());
            player.setLevel(offlineMinigamePlayer.getLevel());
        }
        startPos = null;
        player.resetPlayerWeather();
        player.resetPlayerTime();
        allowGMChange = true;
        allowTP = true;
        player.setGameMode(offlineMinigamePlayer.getLastGamemode());

        offlineMinigamePlayer.deletePlayerData();
        offlineMinigamePlayer = null;

        player.updateInventory();
    }

    public boolean hasStoredData() {
        return offlineMinigamePlayer != null;
    }

    public boolean getAllowTeleport() {
        return allowTP;
    }

    public void setAllowTeleport(final boolean allowTP) {
        this.allowTP = allowTP;
    }

    public boolean getAllowGamemodeChange() {
        return allowGMChange;
    }

    public void setAllowGamemodeChange(final boolean allowGMChange) {
        this.allowGMChange = allowGMChange;
    }

    /**
     * Will return null, whenever the player is not currently in a minigame
     */
    public @Nullable Minigame getMinigame() {
        return minigame;
    }

    public void setMinigame(final @NotNull Minigame minigame) {
        this.minigame = minigame;
    }

    public void removeMinigame() {
        minigame = null;
    }

    public boolean isInMinigame() {
        return minigame != null;
    }

    public boolean isRequiredQuit() {
        return requiredQuit;
    }

    public void setRequiredQuit(final boolean requiredQuit) {
        this.requiredQuit = requiredQuit;
    }

    public @Nullable Location getQuitPos() {
        return quitPos;
    }

    public void setQuitPos(final @Nullable Location quitPos) {
        this.quitPos = quitPos;
    }

    /**
     * will return null, if the player is NOT in a Minigame
     */
    public @Nullable PlayerLoadout getLoadout() {
        if (minigame != null) {
            LoadoutModule loadoutModule = LoadoutModule.getMinigameModule(minigame);

            if (loadout != null) {
                return loadout;
            } else if (team != null && loadoutModule.hasLoadout(team.getColor().toString().toLowerCase())) {
                return loadoutModule.getLoadout(team.getColor().toString().toLowerCase());
            }
            return loadoutModule.getLoadout("default");
        } else {
            return null;
        }
    }

    /**
     * will return null, if the player is NOT in a Minigame
     */
    public @Nullable PlayerLoadout getDefaultLoadout() {
        if (minigame != null) {
            LoadoutModule loadoutModule = LoadoutModule.getMinigameModule(minigame);
            if (team != null && loadoutModule.hasLoadout(team.getColor().toString().toLowerCase())) {
                return loadoutModule.getLoadout(team.getColor().toString().toLowerCase());
            }
            return loadoutModule.getLoadout("default");
        } else {
            return null;
        }
    }

    public boolean setLoadout(final @Nullable PlayerLoadout loadout) {
        if (getMinigame() == null) return false;
        if (loadout == null || !getMinigame().isTeamGame() || loadout.getTeamColor() == null || getTeam().getColor() == loadout.getTeamColor()) {
            this.loadout = loadout;
            return true;
        }
        return false;
    }

// the whole singleplayer flag system is unused.
//    public @NotNull List<@NotNull String> getSinglePlayerFlags() {
//        return singlePlayerFlags;
//    }

//    public void setSinglePlayerFlags(final @NotNull List<@NotNull String> singlePlayerFlags) {
//        this.singlePlayerFlags.addAll(singlePlayerFlags);
//    }

//    public boolean addFlag(final @NotNull String flag) {
//        if (!singlePlayerFlags.contains(flag)) {
//            singlePlayerFlags.add(flag);
//            return true;
//        }
//        return false;
//    }

//    public boolean hasFlag(final @NotNull String flagName) {
//        return singlePlayerFlags.contains(flagName);
//    }

//    public void clearFlags() {
//        singlePlayerFlags.clear();
//    }

    public @Nullable Location getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(final @Nullable Location checkpoint) {
        this.checkpoint = checkpoint;
    }

    public boolean hasCheckpoint() {
        return checkpoint != null;
    }

    public void removeCheckpoint() {
        checkpoint = null;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        kills++;
    }

    public void resetKills() {
        kills = 0;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(final int deaths) {
        this.deaths = deaths;
    }

    public void addDeath() {
        deaths++;
    }

    public void resetDeaths() {
        deaths = 0;
    }

    public int getScore() {
        return score;
    }

    public void setScore(final int score) {
        this.score = score;
    }

    public void addScore() {
        score++;
    }

    public void addScore(final int amount) {
        score += amount;
    }

    public void resetScore() {
        score = 0;
    }

    public void takeScore() {
        score--;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(final long ms) {
        startTime = ms;
    }

    /**
     * in milliseconds
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * in milliseconds
     */
    public void setEndTime(final long ms) {
        endTime = ms;
    }

    public void resetTime() {
        startTime = 0;
        endTime = 0;
        storedTime = 0;
    }

    /**
     * in milliseconds
     */
    public long getStoredTime() {
        return storedTime;
    }

    /**
     * in milliseconds
     */
    public void setStoredTime(final long ms) {
        storedTime = ms;
    }

    public long getCompletionTime() {
        return completeTime;
    }

    public void setCompleteTime(final long ms) {
        completeTime = ms;
    }

    public void addRevert() {
        reverts++;
    }

    public int getReverts() {
        return reverts;
    }

    public void setReverts(final int count) {
        reverts = count;
    }

    public void resetReverts() {
        reverts = 0;
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public void setFrozen(final boolean isFrozen) {
        this.isFrozen = isFrozen;
    }

    public boolean canPvP() {
        return canPvP;
    }

    public void setCanPvP(final boolean canPvP) {
        this.canPvP = canPvP;
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public void setInvincible(final boolean isInvincible) {
        this.isInvincible = isInvincible;
    }

    public boolean canInteract() {
        return canInteract;
    }

    public void setCanInteract(final boolean canInteract) {
        this.canInteract = canInteract;
    }

    public boolean canFly() {
        return canFly;
    }

    public void setCanFly(final boolean bool) {
        canFly = bool;
        player.setAllowFlight(bool);
    }

    public void resetAllStats() {
//        setLoadout(null);
        loadout = null;
        resetReverts();
        resetDeaths();
        resetKills();
        resetScore();
        resetTime();
        //clearFlags(); // the whole singleplayer flag system is unused.
        removeCheckpoint();
        setFrozen(false);
        setCanPvP(true);
        setInvincible(false);
        setCanInteract(true);
        setLatejoining(false);
        if (player.getGameMode() != GameMode.CREATIVE) {
            setCanFly(false);
        }
        tempClaimedRewards.clear();
        tempRewardItems.clear();
        claimedScoreSigns.clear();
        if (lateJoinTimer != -1) {
            Bukkit.getScheduler().cancelTask(lateJoinTimer);
            setLateJoinTimer(-1);
        }
    }

    public boolean isLatejoining() {
        return isLatejoining;
    }

    public void setLatejoining(final boolean isLatejoining) {
        this.isLatejoining = isLatejoining;
    }

    public @Nullable Menu getMenu() {
        return menu;
    }

    public void setMenu(final @Nullable Menu menu) {
        this.menu = menu;
    }

    public boolean isInMenu() {
        return menu != null;
    }

    public boolean getNoClose() {
        return noClose;
    }

    public void setNoClose(final boolean value) {
        noClose = value;
    }

    public @Nullable MenuItem getManualEntry() {
        return manualEntry;
    }

    public void setManualEntry(final @Nullable MenuItem item) {
        manualEntry = item;
    }

    public void addSelectionPoint(final @NotNull Location loc) {
        if (DependencyManager.isWorldEditEnabled()) {
            if (DependencyManager.getLocation1(player) != null) {
                if (DependencyManager.getLocation2(player) != null) {
                    DependencyManager.clearSelection(player);
                    DependencyManager.setPos1(player, loc);
                    MinigameMessageManager.sendMgMessage(this, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SELECT_RESTART);
                    MinigameMessageManager.sendMgMessage(this, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SELECT_POS1);
                } else {
                    DependencyManager.setPos2(player, loc);
                    MinigameMessageManager.sendMgMessage(this, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SELECT_POS2);
                }
            } else {
                DependencyManager.setPos1(player, loc);
                MinigameMessageManager.sendMgMessage(this, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SELECT_POS1);
            }
        } else {
            if (selection1 == null) {
                selection1 = loc;
                showSelection(true);
                MinigameMessageManager.sendMgMessage(this, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SELECT_POS1);
            } else if (selection2 == null) {
                selection2 = loc;
                showSelection(true);
                MinigameMessageManager.sendMgMessage(this, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SELECT_POS2);
            } else {
                showSelection(false);
                selection1 = loc;
                MinigameMessageManager.sendMgMessage(this, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SELECT_RESTART);
                MinigameMessageManager.sendMgMessage(this, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SELECT_POS1);
                selection2 = null;
                showSelection(true);
            }
        }
    }

    public boolean hasSelection() {
        if (DependencyManager.isWorldEditEnabled()) {
            return DependencyManager.hasSelection(player);
        } else {
            return selection1 != null && selection2 != null;
        }
    }

    public @Nullable Location @NotNull [] getSelectionLocations() {
        final Location[] loc = new Location[2];

        if (DependencyManager.isWorldEditEnabled()) {
            DependencyManager.SelectedRegionStatusWrapper statusWrapper = DependencyManager.getSelectedRegion(player);

            loc[0] = statusWrapper.pos1();
            loc[1] = statusWrapper.pos2();
        } else {
            loc[0] = selection1;
            loc[1] = selection2;
        }
        return loc;
    }

    public void clearSelection() {
        if (DependencyManager.isWorldEditEnabled()) {
            DependencyManager.clearSelection(player);
        } else {
            showSelection(false);
            selection1 = null;
            selection2 = null;
        }
    }

    public void setSelection1(final @NotNull Location point1) {
        if (DependencyManager.isWorldEditEnabled()) {
            DependencyManager.setPos1(player, point1);
        } else {
            selection1 = point1;
            showSelection(false);
        }
    }

    public void setSelection2(final @NotNull Location point2) {
        if (DependencyManager.isWorldEditEnabled()) {
            DependencyManager.setPos2(player, point2);
        } else {
            selection2 = point2;
            showSelection(true);
        }
    }

    public void setSelection(final @NotNull MgRegion region) {
        if (DependencyManager.isWorldEditEnabled()) {
            DependencyManager.setPos2(player, region.getLocation1());
            DependencyManager.setPos2(player, region.getLocation2());
        } else {
            selection1 = region.getLocation1();
            selection2 = region.getLocation2();

            showSelection(true);
        }
    }

    public void showSelection(final boolean show) {
        if (selectionDisplay != null) {
            selectionDisplay.remove();
            selectionDisplay = null;
        }

        if (show) {
            if (selection2 != null && selection1 != null) {
                selectionDisplay = Minigames.getPlugin().display.displayCuboid(getPlayer(), selection1, selection2.clone().add(1, 1, 1));
                selectionDisplay.show();
            } else if (selection1 != null) {
                selectionDisplay = Minigames.getPlugin().display.displayCuboid(getPlayer(), selection1, selection1.clone().add(1, 1, 1));
                selectionDisplay.show();
            } else if (selection2 != null) {
                selectionDisplay = Minigames.getPlugin().display.displayCuboid(getPlayer(), selection2, selection2.clone().add(1, 1, 1));
                selectionDisplay.show();
            }
        }
    }

    public @Nullable OfflineMinigamePlayer getOfflineMinigamePlayer() {
        return offlineMinigamePlayer;
    }

    public void setOfflineMinigamePlayer(final @NotNull OfflineMinigamePlayer oply) {
        offlineMinigamePlayer = oply;
    }

    public @NotNull StoredPlayerCheckpoints getStoredPlayerCheckpoints() {
        return spc;
    }

    public void setGamemode(final @NotNull GameMode gamemode) {
        setAllowGamemodeChange(true);
        player.setGameMode(gamemode);
        setAllowGamemodeChange(false);
    }

    public boolean teleport(final @NotNull Location location) {
        setAllowTeleport(true);
        boolean bool = getPlayer().teleport(location);
        setAllowTeleport(false);

        return bool;
    }

    public void updateInventory() {
        getPlayer().updateInventory();
    }

    public boolean isLiving() {
        return !player.isDead();
    }

    public @Nullable Team getTeam() {
        return team;
    }

    public void setTeam(final @Nullable Team team) {
        this.team = team;
    }

    public void removeTeam() {
        if (team != null) {
            team.removePlayer(this);
            team = null;
        }
    }

    public boolean hasClaimedReward(final @NotNull String reward) {
        return claimedRewards.contains(reward);
    }

    public boolean hasTempClaimedReward(final @NotNull String reward) {
        return tempClaimedRewards.contains(reward);
    }

    public void addTempClaimedReward(final @NotNull String reward) {
        tempClaimedRewards.add(reward);
    }

    public void addClaimedReward(final @NotNull String reward) {
        claimedRewards.add(reward);
    }

    public void saveClaimedRewards() {
        if (!claimedRewards.isEmpty()) {
            final MinigameSave save = new MinigameSave("playerdata" + File.separator + "data" + File.separator + getUUID());
            final FileConfiguration cfg = save.getConfig();
            cfg.set("claims", claimedRewards);
            save.saveConfig();
        }
    }

    public void loadClaimedRewards() {
        final File f = new File(Minigames.getPlugin().getDataFolder() + File.separator + "playerdata" +
                File.separator + "data" + File.separator + getUUID() + ".yml");
        if (f.exists()) {
            final MinigameSave save = new MinigameSave("playerdata" + File.separator + "data" + File.separator + getUUID());
            claimedRewards = save.getConfig().getStringList("claims");
        }
    }

    public void addTempRewardItem(final @NotNull ItemStack item) {
        tempRewardItems.add(item);
    }

    public @NotNull List<@NotNull ItemStack> getTempRewardItems() {
        return tempRewardItems;
    }

    public void addRewardItem(final @NotNull ItemStack item) {
        rewardItems.add(item);
    }

    public @NotNull List<@NotNull ItemStack> getRewardItems() {
        return rewardItems;
    }

    public boolean hasClaimedScore(final @NotNull Location loc) {
        final String id = MinigameUtils.createLocationID(loc);
        return claimedScoreSigns.contains(id);
    }

    public boolean applyResourcePack(final @NotNull ResourcePack pack) {
        try {
            player.getPlayer().setResourcePack(pack.getUrl().toString(), pack.getSH1Hash());
            return true;
        } catch (final IllegalArgumentException e) {
            Minigames.getCmpnntLogger().warn("Could not apply ressource pack to player " + getPlayer().getName(), e);
        }
        return false;
    }

    public void addClaimedScore(final @NotNull Location loc) {
        final String id = MinigameUtils.createLocationID(loc);
        claimedScoreSigns.add(id);
    }

    public void claimTempRewardItems() {
        if (isLiving()) {
            final List<ItemStack> tempItems = new ArrayList<>(getTempRewardItems());

            if (!tempItems.isEmpty()) {
                for (final ItemStack item : tempItems) {
                    final Map<Integer, ItemStack> m = player.getPlayer().getInventory().addItem(item);
                    if (!m.isEmpty()) {
                        for (final ItemStack i : m.values()) {
                            player.getPlayer().getWorld().dropItemNaturally(player.getPlayer().getLocation(), i);
                        }
                    }
                }
            }
        }
    }

    public void claimRewards() {
        if (isLiving()) {
            final List<ItemStack> tempItems = new ArrayList<>(getRewardItems());

            if (!tempItems.isEmpty()) {
                for (final ItemStack item : tempItems) {
                    final Map<Integer, ItemStack> m = player.getPlayer().getInventory().addItem(item);
                    if (!m.isEmpty()) {
                        for (final ItemStack i : m.values()) {
                            player.getPlayer().getWorld().dropItemNaturally(player.getPlayer().getLocation(), i);
                        }
                    }
                }
            }
        }
    }

    public void setLateJoinTimer(final int taskID) {
        lateJoinTimer = taskID;
    }

    @Override
    public @Nullable ScriptReference get(final @NotNull String name) {
        return switch (name.toLowerCase()) {
            case "name" -> ScriptValue.of(player.getName());
            case "displayname" -> ScriptValue.of(player.getDisplayName());
            case "score" -> ScriptValue.of(score);
            case "kills" -> ScriptValue.of(kills);
            case "deaths" -> ScriptValue.of(deaths);
            case "health" -> ScriptValue.of(player.getHealth());
            case "team" -> team;
            case "pos" -> ScriptWrapper.wrap(player.getLocation());
            case "minigame" -> minigame;
            default -> null;
        };
    }

    @Override
    public @NotNull Set<String> getKeys() {
        return Set.of("name", "displayname", "score", "kills", "deaths", "health", "team", "pos", "minigame");
    }

    @Override
    public @NotNull String getAsString() {
        return getName();
    }
}
