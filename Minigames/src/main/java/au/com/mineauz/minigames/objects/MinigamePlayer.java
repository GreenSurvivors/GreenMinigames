package au.com.mineauz.minigames.objects;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
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
import au.com.mineauz.minigames.minigame.modules.loadout.LoadoutModule;
import au.com.mineauz.minigames.minigame.modules.loadout.PlayerLoadout;
import au.com.mineauz.minigames.minigame.modules.team.Team;
import au.com.mineauz.minigames.objects.safelocation.ASafeLocation;
import au.com.mineauz.minigames.objects.safelocation.SafeFineLocation;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import au.com.mineauz.minigames.script.ScriptObject;
import au.com.mineauz.minigames.script.ScriptReference;
import au.com.mineauz.minigames.script.ScriptValue;
import au.com.mineauz.minigames.script.ScriptWrapper;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * wrapper class to keep track of players with additional information.
 * A MinigamePlayer does NOT have to be in a Minigame to be valid!
 */
public class MinigamePlayer implements ScriptObject, ScoreHolder, ForwardingAudience.Single {
    private final @NotNull UUID uuid;
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
    private @Nullable SafeFullLocation startPos;
    private @Nullable SafeFullLocation quitPos;
    private @Nullable SafeFullLocation checkpoint;
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
    private @Nullable MenuItem menuItemWaitingForManualInput;
    private @Nullable SafeFineLocation selection1;
    private @Nullable SafeFineLocation selection2;
    private @Nullable DisplayCuboid selectionDisplay;
    private OfflineMinigamePlayer offlineMinigamePlayer;
    private @NotNull List<@NotNull String> claimedRewards = new ArrayList<>();
    private int lateJoinTimer = -1;
    private final @NotNull Minigames plugin = Minigames.getPlugin();

    public MinigamePlayer(final @NotNull Player player) {
        this.uuid = player.getUniqueId();
        spc = new StoredPlayerCheckpoints(getUUID());

        final @NotNull Path checkpointPath = plugin.getDataPath()
            .resolve("playerdata")
            .resolve("checkpoints")
            .resolve(getUUID() + ".yml");
        if (Files.isRegularFile(checkpointPath)) {
            try {
                getStoredPlayerCheckpoints().loadCheckpoints();
            } catch (final @NotNull ConfigurateException e) {
                plugin.getComponentLogger().error("Couldn't load checkpoint data for player " + getName() + "(" + getUUID() + ")", e);
            }
        }
    }

    public @Nullable SafeFullLocation getStartPos() {
        return startPos;
    }

    public void setStartPos(final @Nullable SafeFullLocation startPos) {
        this.startPos = startPos;
    }

    public @Nullable Player getPlayer() {
        return plugin.getServer().getPlayer(uuid);
    }

    public @NotNull OfflinePlayer getOfflinePlayer() {
        return plugin.getServer().getOfflinePlayer(uuid);
    }

    public @NotNull String getName() {
        // we can guarantee not nullness here, since every MinigamePlayer gets created with a player object, meaning the player has played before!
        return getOfflinePlayer().getName();
    }

    public @Nullable Component displayName() {
        final @Nullable Player player = getPlayer();
        return player == null ? null : player.displayName();
    }

    public @NotNull UUID getUUID() {
        return uuid;
    }

    public @NotNull Location getLocation() {
        // we can guarantee not nullness here, since every MinigamePlayer gets created with a player object, meaning the player has played before!
        return getOfflinePlayer().getLocation();
    }

    public @NotNull SafeFullLocation getSafeLocation() {
        return new SafeFullLocation(getLocation());
    }

    public void storePlayerData() {
        final @Nullable Player player = getPlayer();

        if (player == null) {
            return;
        }

        final ItemStack[] storedItems = player.getInventory().getContents();
        final ItemStack[] storedArmour = player.getInventory().getArmorContents();
        final int food = player.getFoodLevel();
        final double health = player.getHealth();
        final float saturation = player.getSaturation();
        lastScoreboard = player.getScoreboard();
        final GameMode lastGM = player.getGameMode();
        float exp = player.getExp();
        if (exp < 0) {
            plugin.getComponentLogger().warn("Player Experience was less that 0: " + player.getName() + " " + player.getExp());
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
            health, saturation, lastGM, exp, level, new SafeFullLocation(getPlayer().getLocation()));
        player.updateInventory();
    }

    public void restorePlayerData() {
        final @Nullable Player player = getPlayer();
        if (player == null) {
            return;
        }

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

    public @Nullable SafeFullLocation getQuitPos() {
        return quitPos;
    }

    public void setQuitPos(final @Nullable SafeFullLocation quitPos) {
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

    public @Nullable SafeFullLocation getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(final @Nullable SafeFullLocation checkpoint) {
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

    public int addScore() {
        return addScore(1);
    }

    public int addScore(final int amount) {
        return score += amount;
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
        getPlayer().setAllowFlight(bool);
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
        if (getPlayer().getGameMode() != GameMode.CREATIVE) {
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

    public boolean isMenuWaitingForInput() {
        return menuItemWaitingForManualInput != null;
    }

    public @Nullable MenuItem getMenuItemWaitingForManualInput() {
        return menuItemWaitingForManualInput;
    }

    public void setMenuItemWaitingForManualInput(final @Nullable MenuItem item) {
        menuItemWaitingForManualInput = item;
    }

    public void addSelectionPoint(final @NotNull Location loc) {
        final Player player = getPlayer();

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
                selection1 = new SafeFineLocation(loc);
                showSelection(true);
                MinigameMessageManager.sendMgMessage(this, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SELECT_POS1);
            } else if (selection2 == null) {
                selection2 = new SafeFineLocation(loc);
                showSelection(true);
                MinigameMessageManager.sendMgMessage(this, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SELECT_POS2);
            } else {
                showSelection(false);
                selection1 = new SafeFineLocation(loc);
                MinigameMessageManager.sendMgMessage(this, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SELECT_RESTART);
                MinigameMessageManager.sendMgMessage(this, MinigameMessageType.INFO, MgMiscLangKey.PLAYER_SELECT_POS1);
                selection2 = null;
                showSelection(true);
            }
        }
    }

    public boolean hasSelection() {
        if (DependencyManager.isWorldEditEnabled()) {
            return DependencyManager.hasSelection(getPlayer());
        } else {
            return selection1 != null && selection2 != null;
        }
    }

    public @Nullable SafeFineLocation @NotNull [] getSelectionLocations() {
        final SafeFineLocation[] loc = new SafeFineLocation[2];

        if (DependencyManager.isWorldEditEnabled()) {
            DependencyManager.SelectedRegionStatusWrapper statusWrapper = DependencyManager.getSelectedRegion(getPlayer());

            loc[0] = new SafeFineLocation(statusWrapper.pos1());
            loc[1] = new SafeFineLocation(statusWrapper.pos2());
        } else {
            loc[0] = selection1;
            loc[1] = selection2;
        }
        return loc;
    }

    public void clearSelection() {
        if (DependencyManager.isWorldEditEnabled()) {
            DependencyManager.clearSelection(getPlayer());
        } else {
            showSelection(false);
            selection1 = null;
            selection2 = null;
        }
    }

    public void setSelection1(final @NotNull Location point1) {
        if (DependencyManager.isWorldEditEnabled()) {
            DependencyManager.setPos1(getPlayer(), point1);
        } else {
            selection1 = new SafeFineLocation(point1);
            showSelection(false);
        }
    }

    public void setSelection2(final @NotNull Location point2) {
        if (DependencyManager.isWorldEditEnabled()) {
            DependencyManager.setPos2(getPlayer(), point2);
        } else {
            selection2 = new SafeFineLocation(point2);
            showSelection(true);
        }
    }

    public void setSelection(final @NotNull MgRegion region) {
        final Player player = getPlayer();

        if (DependencyManager.isWorldEditEnabled()) {
            DependencyManager.setPos2(player, region.getFirstPoint().toLocation());
            DependencyManager.setPos2(player, region.getSecondPoint().toLocation());
        } else {
            selection1 = region.getFirstPoint();
            selection2 = region.getSecondPoint();

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
                selectionDisplay = plugin.getDisplayManager().displayCuboid(getPlayer(), selection1, selection2.offset(1.0, 1.0, 1.0));
                selectionDisplay.show();
            } else if (selection1 != null) {
                selectionDisplay = plugin.getDisplayManager().displayCuboid(getPlayer(), selection1, selection1.offset(1.0, 1.0, 1.0));
                selectionDisplay.show();
            } else if (selection2 != null) {
                selectionDisplay = plugin.getDisplayManager().displayCuboid(getPlayer(), selection2, selection2.offset(1.0, 1.0, 1.0));
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
        getPlayer().setGameMode(gamemode);
        setAllowGamemodeChange(false);
    }

    /// returns true, if the player was successfully teleported.
    @ApiStatus.Obsolete
    public boolean teleport(final @NotNull Location location) {
        final Player player = getPlayer();

        if (player == null) {
            return false;
        }

        setAllowTeleport(true);
        boolean bool = player.teleport(location);
        setAllowTeleport(false);

        return bool;
    }

    /// returns true if the player was successfully teleported.
    public boolean teleport(final @NotNull ASafeLocation safeLocation) {
        if (safeLocation.getWorld() != null) {
            return teleport(safeLocation.toLocation());
        }

        return false;
    }

    public void updateInventory() {
        getPlayer().updateInventory();
    }

    public boolean isLiving() {
        return !getPlayer().isDead();
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

    public void saveClaimedRewards() throws IOException {
        if (!claimedRewards.isEmpty()) {
            final @NotNull MinigameSave save = MinigameSave.forPlayerData(getUUID(), Path.of("data"));
            final @NotNull ConfigurationNode cfg = save.getConfigRoot();
            cfg.node("claims").setList(String.class, claimedRewards);
            save.saveConfig();
        }
    }

    public void loadClaimedRewards() throws ConfigurateException {
        final @NotNull MinigameSave save = MinigameSave.forPlayerData(getUUID(), Path.of("data"));

        if (save.existsOnDisk()) {
            claimedRewards = save.getConfigRoot().node("claims").getList(String.class, List.of());
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
        final String id = MinigameUtils.createBlockLocationID(loc);
        return claimedScoreSigns.contains(id);
    }

    public boolean applyResourcePack(final @NotNull ResourcePack pack) {
        try {
            getPlayer().setResourcePack(pack.getUrl().toString(), pack.getSH1Hash());
            return true;
        } catch (final IllegalArgumentException e) {
            plugin.getComponentLogger().warn("Could not apply resource pack to player " + getPlayer().getName(), e);
        }
        return false;
    }

    public void addClaimedScore(final @NotNull Location loc) {
        final String id = MinigameUtils.createBlockLocationID(loc);
        claimedScoreSigns.add(id);
    }

    public void claimTempRewardItems() {
        if (isLiving() && !getTempRewardItems().isEmpty()) {
            getPlayer().give(getTempRewardItems());
        }
    }

    public void claimRewards() {
        if (isLiving() && !getRewardItems().isEmpty()) {
            getPlayer().give(getTempRewardItems());
        }
    }

    public void setLateJoinTimer(final int taskID) {
        lateJoinTimer = taskID;
    }

    @Override
    public @Nullable ScriptReference resolveReference(final @NotNull String name) {
        final Player player = getPlayer();

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
    public @NotNull Set<String> getReferenceKeys() {
        return Set.of("name", "displayname", "score", "kills", "deaths", "health", "team", "pos", "minigame");
    }

    @Override
    public @NotNull String getAsString() {
        return getName();
    }

    @Override
    public @NotNull Audience audience() {
        return getPlayer();
    }
}
