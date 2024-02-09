package au.com.mineauz.minigames.minigame;

import au.com.mineauz.minigames.*;
import au.com.mineauz.minigames.config.*;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.mechanics.GameMechanicBase;
import au.com.mineauz.minigames.mechanics.GameMechanics;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.modules.*;
import au.com.mineauz.minigames.objects.CTFFlag;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.RegenRegionChangeResult;
import au.com.mineauz.minigames.recorder.RecorderData;
import au.com.mineauz.minigames.script.ScriptCollection;
import au.com.mineauz.minigames.script.ScriptObject;
import au.com.mineauz.minigames.script.ScriptReference;
import au.com.mineauz.minigames.script.ScriptValue;
import au.com.mineauz.minigames.stats.MinigameStat;
import au.com.mineauz.minigames.stats.StatSettings;
import au.com.mineauz.minigames.stats.StoredGameStats;
import org.apache.commons.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Minigame implements ScriptObject {
    private final String name; //todo maybe component
    private final Map<String, Flag<?>> configFlags = new HashMap<>();
    private final StringFlag displayName = new StringFlag(null, "displayName");
    private final StringFlag objective = new StringFlag(null, "objective");
    private final StringFlag gameTypeName = new StringFlag(null, "gametypeName");
    private final EnumFlag<MinigameType> type = new EnumFlag<>(MinigameType.SINGLEPLAYER, "type");
    private final BooleanFlag enabled = new BooleanFlag(false, "enabled");
    private final IntegerFlag minPlayers = new IntegerFlag(2, "minplayers");
    private final IntegerFlag maxPlayers = new IntegerFlag(4, "maxplayers");
    private final BooleanFlag spMaxPlayers = new BooleanFlag(false, "spMaxPlayers");
    private final StrListFlag flags = new StrListFlag(null, "flags");
    private final StringFlag degenType = new StringFlag("inward", "degentype");
    private final IntegerFlag degenRandomChance = new IntegerFlag(15, "degenrandom");
    private final RegionFlag floorDegen = new RegionFlag(null, "sfloor", "sfloorpos.1", "sfloorpos.2");
    private final TimeFlag floorDegenTime = new TimeFlag(Minigames.getPlugin().getConfig().getLong("multiplayer.floordegenerator.time"), "floordegentime");
    // Respawn Module
    private final BooleanFlag respawn = new BooleanFlag(Minigames.getPlugin().getConfig().getBoolean("has-respawn"), "respawn");
    private final LocationListFlag startLocations = new LocationListFlag(null, "startpos");
    private final BooleanFlag randomizeStart = new BooleanFlag(false, "ranndomizeStart");
    private final LocationFlag endLocation = new LocationFlag(null, "endpos");
    private final LocationFlag quitLocation = new LocationFlag(null, "quitpos");
    private final LocationFlag lobbyLocation = new LocationFlag(null, "lobbypos");
    private final LocationFlag spectatorPosition = new LocationFlag(null, "spectatorpos");
    private final BooleanFlag usePermissions = new BooleanFlag(false, "usepermissions");
    private final TimeFlag timer = new TimeFlag(0L, "timer");
    private final EnumFlag<MinigameTimer.DisplayType> timerDisplayType = new EnumFlag<>(MinigameTimer.DisplayType.XP_BAR, "timerDisplayType");
    private final TimeFlag startWaitTime = new TimeFlag(0L, "startWaitTime");
    private final BooleanFlag showCompletionTime = new BooleanFlag(false, "showCompletionTime");
    private final BooleanFlag itemDrops = new BooleanFlag(false, "itemdrops");
    private final BooleanFlag deathDrops = new BooleanFlag(false, "deathdrops");
    private final BooleanFlag itemPickup = new BooleanFlag(true, "itempickup");
    private final BooleanFlag blockBreak = new BooleanFlag(false, "blockbreak");
    private final BooleanFlag blockPlace = new BooleanFlag(false, "blockplace");
    private final EnumFlag<GameMode> defaultGamemode = new EnumFlag<>(GameMode.ADVENTURE, "gamemode");
    private final BooleanFlag blocksDrop = new BooleanFlag(true, "blocksdrop");
    private final BooleanFlag allowEnderPearls = new BooleanFlag(false, "allowEnderpearls");
    private final BooleanFlag allowMPCheckpoints = new BooleanFlag(false, "allowMPCheckpoints");
    private final BooleanFlag allowFlight = new BooleanFlag(false, "allowFlight");
    private final BooleanFlag enableFlight = new BooleanFlag(false, "enableFlight");
    private final BooleanFlag allowDragonEggTeleport = new BooleanFlag(true, "allowDragonEggTeleport");
    private final BooleanFlag usePlayerDisplayNames = new BooleanFlag(true, "usePlayerDisplayNames");
    private final BooleanFlag showPlayerBroadcasts = new BooleanFlag(true, "showPlayerBroadcasts");
    private final BooleanFlag showCTFBroadcasts = new BooleanFlag(true, "showCTFBroadcasts");
    private final BooleanFlag keepInventory = new BooleanFlag(false, "keepInventory");
    private final BooleanFlag friendlyFireSplashPotions = new BooleanFlag(true, "friendlyFireSplashPotions");
    private final BooleanFlag friendlyFireLingeringPotions = new BooleanFlag(true, "friendlyFireLingeringPotions");
    private final StringFlag mechanic = new StringFlag("custom", "scoretype");
    private final BooleanFlag paintBallMode = new BooleanFlag(false, "paintball");
    private final IntegerFlag paintBallDamage = new IntegerFlag(2, "paintballdmg");
    private final BooleanFlag unlimitedAmmo = new BooleanFlag(false, "unlimitedammo");
    private final BooleanFlag saveCheckpoints = new BooleanFlag(false, "saveCheckpoints");
    private final BooleanFlag lateJoin = new BooleanFlag(false, "latejoin");
    // just to stay backwards compatible we have to save this int as a float
    private final FloatFlag lives = new FloatFlag(0F, "lives");
    private final RegionMapFlag regenRegions = new RegionMapFlag(new HashMap<>(), "regenRegions", "regenarea.1", "regenarea.2");
    private final TimeFlag regenDelay = new TimeFlag(0L, "regenDelay");
    private final IntegerFlag maxBlocksRegenRegions = new IntegerFlag(300000, "maxBlocksRegenRegions");
    private final @NotNull Map<@NotNull String, @NotNull MinigameModule> modules = new HashMap<>();
    private final IntegerFlag minScore = new IntegerFlag(5, "minscore");
    private final IntegerFlag maxScore = new IntegerFlag(10, "maxscore");
    private final BooleanFlag displayScoreboard = new BooleanFlag(true, "displayScoreboard");
    private final BooleanFlag canSpectateFly = new BooleanFlag(false, "canspectatefly");
    private final BooleanFlag randomizeChests = new BooleanFlag(false, "randomizechests");
    private final IntegerFlag minChestRandom = new IntegerFlag(5, "minchestrandom");
    private final IntegerFlag maxChestRandom = new IntegerFlag(10, "maxchestrandom");
    @NotNull
    private final ScoreboardData sbData = new ScoreboardData();
    private final Map<MinigameStat, StatSettings> statSettings = new HashMap<>();
    private final BooleanFlag activatePlayerRecorder = new BooleanFlag(true, "activatePlayerRecorder");
    //Unsaved data
    private final List<MinigamePlayer> players = new ArrayList<>();
    private final List<MinigamePlayer> spectators = new ArrayList<>();
    private final RecorderData blockRecorder = new RecorderData(this);
    //CTF
    private final Map<MinigamePlayer, CTFFlag> flagCarriers = new HashMap<>();
    private final Map<String, CTFFlag> droppedFlag = new HashMap<>();
    private MinigameState state = MinigameState.IDLE;
    private FloorDegenerator sFloorDegen;
    private Scoreboard sbManager = Minigames.getPlugin().getServer().getScoreboardManager().getNewScoreboard();
    //Multiplayer
    private MultiplayerTimer mpTimer = null;
    private MinigameTimer miniTimer = null;
    private MultiplayerBets mpBets = null;
    private boolean playersAtStart = false;

    public Minigame(@NotNull String name, @NotNull MinigameType type, @NotNull Location start) {
        this.name = name;
        setup(type, start);
    }

    public Minigame(String name) {
        this.name = name;
        setup(MinigameType.SINGLEPLAYER, null);
    }

    public boolean isPlayersAtStart() {
        return playersAtStart;
    }

    public void setPlayersAtStart(boolean playersAtStart) {
        this.playersAtStart = playersAtStart;
    }

    private void setup(@NotNull MinigameType type, @Nullable Location start) {
        this.type.setFlag(type);
        startLocations.setFlag(new ArrayList<>());

        if (start != null)
            startLocations.getFlag().add(start);
        if (sbManager != null) {
            sbManager.registerNewObjective(this.name, Criteria.DUMMY, this.name);
            sbManager.getObjective(this.name).setDisplaySlot(DisplaySlot.SIDEBAR);
        }

        for (ModuleFactory factory : Minigames.getPlugin().getMinigameManager().getModules()) {
            addModule(factory);
        }

        flags.setFlag(new ArrayList<>());

        addConfigFlag(activatePlayerRecorder);
        addConfigFlag(allowEnderPearls);
        addConfigFlag(allowFlight);
        addConfigFlag(allowMPCheckpoints);
        addConfigFlag(blockBreak);
        addConfigFlag(blockPlace);
        addConfigFlag(blocksDrop);
        addConfigFlag(canSpectateFly);
        addConfigFlag(deathDrops);
        addConfigFlag(defaultGamemode);
        addConfigFlag(degenRandomChance);
        addConfigFlag(degenType);
        addConfigFlag(displayName);
        addConfigFlag(enableFlight);
        addConfigFlag(enabled);
        addConfigFlag(endLocation);
        addConfigFlag(flags);
        addConfigFlag(floorDegen);
        addConfigFlag(floorDegenTime);
        addConfigFlag(gameTypeName);
        addConfigFlag(itemDrops);
        addConfigFlag(itemPickup);
        addConfigFlag(lateJoin);
        addConfigFlag(lives);
        addConfigFlag(lobbyLocation);
        addConfigFlag(maxChestRandom);
        addConfigFlag(maxPlayers);
        addConfigFlag(maxScore);
        addConfigFlag(minChestRandom);
        addConfigFlag(minPlayers);
        addConfigFlag(usePlayerDisplayNames);
        addConfigFlag(keepInventory);
        addConfigFlag(friendlyFireSplashPotions);
        addConfigFlag(friendlyFireLingeringPotions);
        addConfigFlag(showPlayerBroadcasts);
        addConfigFlag(showCTFBroadcasts);
        addConfigFlag(minScore);
        addConfigFlag(objective);
        addConfigFlag(paintBallDamage);
        addConfigFlag(paintBallMode);
        addConfigFlag(quitLocation);
        addConfigFlag(randomizeChests);
        addConfigFlag(regenRegions);
        addConfigFlag(regenDelay);
        addConfigFlag(maxBlocksRegenRegions);
        addConfigFlag(saveCheckpoints);
        addConfigFlag(mechanic);
        addConfigFlag(spMaxPlayers);
        addConfigFlag(startLocations);
        addConfigFlag(randomizeStart);
        addConfigFlag(startWaitTime);
        addConfigFlag(timer);
        addConfigFlag(this.type);
        addConfigFlag(unlimitedAmmo);
        addConfigFlag(usePermissions);
        addConfigFlag(timerDisplayType);
        addConfigFlag(spectatorPosition);
        addConfigFlag(displayScoreboard);
        addConfigFlag(allowDragonEggTeleport);
        addConfigFlag(showCompletionTime);
    }

    public MinigameState getState() {
        return state;
    }

    public void setState(MinigameState state) {
        this.state = state;
    }

    private void addConfigFlag(Flag<?> flag) {
        configFlags.put(flag.getName(), flag);
    }

    public Flag<?> getConfigFlag(String name) {
        return configFlags.get(name);
    }

    /**
     * returns the old module registed with the same name or null if there wasn't one.
     *
     * @param factory
     * @return
     */
    public @Nullable MinigameModule addModule(@NotNull ModuleFactory factory) {
        return modules.put(factory.getName(), factory.makeNewModule(this));
    }

    public void removeModule(String moduleName) {
        modules.remove(moduleName);
    }

    public List<MinigameModule> getModules() {
        return new ArrayList<>(modules.values());
    }

    /**
     * Please use the Modules getMinigameModule() methode whenever possible - simply because its less error-prone.
     *
     * @param name
     * @return
     */
    public @Nullable MinigameModule getModule(@NotNull String name) {
        return modules.get(name);
    }

    public boolean isTeamGame() {
        TeamsModule teamsModule = TeamsModule.getMinigameModule(this);
        return getType() == MinigameType.MULTIPLAYER && teamsModule != null && !teamsModule.getTeams().isEmpty();
    }

    public boolean hasFlags() {
        return !flags.getFlag().isEmpty();
    }

    public void addFlag(String flag) {
        flags.getFlag().add(flag);
    }

    public List<String> getFlags() {
        return flags.getFlag();
    }

    public void setFlags(List<String> flags) {
        this.flags.setFlag(flags);
    }

    public boolean removeFlag(String flag) {
        if (flags.getFlag().contains(flag)) {
            flags.getFlag().remove(flag);
            return true;
        }
        return false;
    }

    public void setStartLocation(Location loc) {
        if (startLocations.getFlag().isEmpty()) {
            startLocations.getFlag().add(loc);
        } else {
            startLocations.getFlag().set(0, loc);
        }
    }

    public void addStartLocation(Location loc) {
        startLocations.getFlag().add(loc);
    }

    public void addStartLocation(Location loc, int number) {
        if (startLocations.getFlag().size() >= number) {
            startLocations.getFlag().set(number - 1, loc);
        } else {
            startLocations.getFlag().add(loc);
        }
    }

    public List<Location> getStartLocations() {
        return startLocations.getFlag();
    }

    public boolean removeStartLocation(int locNumber) {
        if (startLocations.getFlag().size() < locNumber) {
            startLocations.getFlag().remove(locNumber);
            return true;
        }
        return false;
    }

    public boolean isRandomizeStart() {
        return randomizeStart.getFlag();
    }

    public void setRandomizeStart(boolean bool) {
        randomizeStart.setFlag(bool);
    }

    public Location getSpectatorLocation() {
        return spectatorPosition.getFlag();
    }

    public void setSpectatorLocation(Location loc) {
        spectatorPosition.setFlag(loc);
    }

    public boolean isEnabled() {
        return enabled.getFlag();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.setFlag(enabled);
    }

    public int getMinPlayers() {
        return minPlayers.getFlag();
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers.setFlag(minPlayers);
    }

    public boolean usePlayerDisplayNames() {
        return usePlayerDisplayNames.getFlag();
    }

    public void setUsePlayerDisplayNames(Boolean value) {
        usePlayerDisplayNames.setFlag(value);
    }

    public boolean keepInventory() {
        return keepInventory.getFlag();
    }

    public void setKeepInventory(boolean value) {
        keepInventory.setFlag(value);
    }

    public boolean friendlyFireSplashPotions() {
        return friendlyFireSplashPotions.getFlag();
    }

    public void setFriendlyFireSplashPotions(boolean value) {
        friendlyFireSplashPotions.setFlag(value);
    }

    public boolean friendlyFireLingeringPotions() {
        return friendlyFireLingeringPotions.getFlag();
    }

    public void setFriendlyFireLingeringPotions(boolean value) {
        friendlyFireLingeringPotions.setFlag(value);
    }

    public int getMaxPlayers() {
        return maxPlayers.getFlag();
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers.setFlag(maxPlayers);
    }

    public boolean isSpMaxPlayers() {
        return spMaxPlayers.getFlag();
    }

    public void setSpMaxPlayers(boolean spMaxPlayers) {
        this.spMaxPlayers.setFlag(spMaxPlayers);
    }

    public boolean isGameFull() {
        if ((getType() == MinigameType.SINGLEPLAYER && isSpMaxPlayers()) ||
                getType() == MinigameType.MULTIPLAYER) {
            return getPlayers().size() >= getMaxPlayers();
        }
        return false;
    }

    public @Nullable MgRegion getFloorDegen() {
        return floorDegen.getFlag();
    }

    public void setFloorDegen(@Nullable MgRegion region) {
        floorDegen.setFlag(region);
    }

    public void removeFloorDegen() {
        floorDegen.setFlag(null);
    }

    public String getDegenType() {
        return degenType.getFlag();
    }

    public void setDegenType(String degenType) {
        this.degenType.setFlag(degenType);
    }

    public int getDegenRandomChance() {
        return degenRandomChance.getFlag();
    }

    public void setDegenRandomChance(int degenRandomChance) {
        this.degenRandomChance.setFlag(degenRandomChance);
    }

    public @Nullable Location getEndLocation() {
        return endLocation.getFlag();
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation.setFlag(endLocation);
    }

    public @Nullable Location getQuitLocation() {
        return quitLocation.getFlag();
    }

    public void setQuitLocation(Location quitLocation) {
        this.quitLocation.setFlag(quitLocation);
    }

    public @Nullable Location getLobbyLocation() {
        return lobbyLocation.getFlag();
    }

    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation.setFlag(lobbyLocation);
    }

    public String getName(boolean useDisplay) {
        if (useDisplay && displayName.getFlag() != null)
            return displayName.getFlag();
        return name;
    }

    public void setDisplayName(String displayName) {
        this.displayName.setFlag(displayName);
    }

    public void setshowPlayerBroadcasts(Boolean showPlayerBroadcasts) {
        this.showPlayerBroadcasts.setFlag(showPlayerBroadcasts);
    }

    public Boolean getShowPlayerBroadcasts() {
        return showPlayerBroadcasts.getFlag();
    }

    public Boolean getShowCTFBroadcasts() {
        return showCTFBroadcasts.getFlag();
    }

    public void setShowCTFBroadcasts(Boolean showCTFBroadcasts) {
        this.showCTFBroadcasts.setFlag(showCTFBroadcasts);
    }

    public @NotNull MinigameType getType() {
        return type.getFlag();
    }

    public void setType(@NotNull MinigameType type) {
        this.type.setFlag(type);
    }

    public MultiplayerTimer getMpTimer() {
        return mpTimer;
    }

    public void setMpTimer(MultiplayerTimer mpTimer) {
        this.mpTimer = mpTimer;
    }

    @Deprecated
    public boolean isNotWaitingForPlayers() {
        return getState() != MinigameState.WAITING;
    }

    public boolean isWaitingForPlayers() {
        return getState() == MinigameState.WAITING;
    }

    public boolean hasStarted() {
        return getState() == MinigameState.STARTED || getState() == MinigameState.OCCUPIED;
    }

    public MinigameTimer getMinigameTimer() {
        return miniTimer;
    }

    public void setMinigameTimer(MinigameTimer mgTimer) {
        this.miniTimer = mgTimer;
    }

    public MultiplayerBets getMpBets() {
        return mpBets;
    }

    public void setMpBets(MultiplayerBets mpBets) {
        this.mpBets = mpBets;
    }

    public boolean getUsePermissions() {
        return usePermissions.getFlag();
    }

    public void setUsePermissions(boolean usePermissions) {
        this.usePermissions.setFlag(usePermissions);
    }

    public List<MinigamePlayer> getPlayers() {
        return players;
    }

    public void addPlayer(MinigamePlayer player) {
        players.add(player);
    }

    public void removePlayer(MinigamePlayer player) {
        players.remove(player);
    }

    public boolean hasPlayers() {
        return !players.isEmpty();
    }

    public boolean hasSpectators() {
        return !spectators.isEmpty();
    }

    public List<MinigamePlayer> getSpectators() {
        return spectators;
    }

    public void addSpectator(MinigamePlayer player) {
        spectators.add(player);
    }

    public void removeSpectator(MinigamePlayer player) {
        spectators.remove(player);
    }

    public boolean isSpectator(MinigamePlayer player) {
        return spectators.contains(player);
    }

    public void setScore(@NotNull MinigamePlayer mgPlayer, int amount) {
        if (sbManager == null) {
            ScoreboardManager s = Minigames.getPlugin().getServer().getScoreboardManager();
            sbManager = s.getNewScoreboard();
            Minigames.getCmpnntLogger().info("ScoreBoardManager was null - Created new Scoreboard - for:" + name);
        }
        Objective o = sbManager.getObjective(getName(false));
        if (o != null) {
            o.getScore(mgPlayer.getName()).setScore(amount);
        }
    }

    public int getMinScore() {
        return minScore.getFlag();
    }

    public void setMinScore(int minScore) {
        this.minScore.setFlag(minScore);
    }

    public int getMaxScore() {
        return maxScore.getFlag();
    }

    public void setMaxScore(int maxScore) {
        this.maxScore.setFlag(maxScore);
    }

    public int getMaxScorePerPlayer() {
        float scorePerPlayer = (float) getMaxScore() / getMaxPlayers();
        int score = Math.round(scorePerPlayer * getPlayers().size());
        if (score < minScore.getFlag()) {
            score = minScore.getFlag();
        }
        return score;
    }

    public FloorDegenerator getFloorDegenerator() {
        return sFloorDegen;
    }

    public void addFloorDegenerator() {
        sFloorDegen = new FloorDegenerator(floorDegen.getFlag(), this);
    }

    public long getTimer() {
        return timer.getFlag();
    }

    public void setTimer(long time) {
        timer.setFlag(time);
    }

    public @NotNull MinigameTimer.DisplayType getTimerDisplayType() {
        return timerDisplayType.getFlag();
    }

    public void setTimerDisplayType(@NotNull MinigameTimer.DisplayType type) {
        this.timerDisplayType.setFlag(type);
    }

    public long getStartWaitTime() {
        return startWaitTime.getFlag();
    }

    public void setStartWaitTime(long startWaitTime) {
        this.startWaitTime.setFlag(startWaitTime);
    }

    public boolean hasItemDrops() {
        return itemDrops.getFlag();
    }

    public void setItemDrops(boolean itemDrops) {
        this.itemDrops.setFlag(itemDrops);
    }

    public boolean hasDeathDrops() {
        return deathDrops.getFlag();
    }

    public void setDeathDrops(boolean deathDrops) {
        this.deathDrops.setFlag(deathDrops);
    }

    public boolean hasItemPickup() {
        return itemPickup.getFlag();
    }

    public void setItemPickup(boolean itemPickup) {
        this.itemPickup.setFlag(itemPickup);
    }

    /**
     * get the recorder data holder of this minigame.
     * This holds all block and entity changes recorded while the minigame was running.
     */
    public @NotNull RecorderData getRecorderData() {
        return blockRecorder;
    }

    public boolean isRegenerating() {
        return state == MinigameState.REGENERATING;
    }

    public boolean canBlockBreak() {
        return blockBreak.getFlag();
    }

    public void setCanBlockBreak(boolean blockBreak) {
        this.blockBreak.setFlag(blockBreak);
    }

    public boolean canBlockPlace() {
        return blockPlace.getFlag();
    }

    public void setCanBlockPlace(boolean blockPlace) {
        this.blockPlace.setFlag(blockPlace);
    }

    public @NotNull GameMode getDefaultGamemode() {
        return defaultGamemode.getFlag();
    }

    public void setDefaultGamemode(@NotNull GameMode defaultGamemode) {
        this.defaultGamemode.setFlag(defaultGamemode);
    }

    public boolean canBlocksdrop() {
        return blocksDrop.getFlag();
    }

    public void setBlocksDrop(boolean blocksDrop) {
        this.blocksDrop.setFlag(blocksDrop);
    }

    public String getMechanicName() {
        return mechanic.getFlag();
    }

    public GameMechanicBase getMechanic() {
        return GameMechanics.getGameMechanic(mechanic.getFlag());
    }

    public void setMechanic(@NotNull GameMechanicBase gameMechanicBase) {
        this.mechanic.setFlag(gameMechanicBase.getMechanic());
    }

    public boolean isFlagCarrier(@Nullable MinigamePlayer mgPlayer) {
        return flagCarriers.containsKey(mgPlayer);
    }

    public void addFlagCarrier(@NotNull MinigamePlayer mgPlayer, @NotNull CTFFlag flag) {
        flagCarriers.put(mgPlayer, flag);
    }

    public void removeFlagCarrier(@NotNull MinigamePlayer mgPlayer) {
        flagCarriers.remove(mgPlayer);
    }

    public @Nullable CTFFlag getFlagCarrier(@NotNull MinigamePlayer mgPlayer) {
        return flagCarriers.get(mgPlayer);
    }

    public void resetFlags() {
        for (MinigamePlayer mgPlayer : flagCarriers.keySet()) {
            getFlagCarrier(mgPlayer).respawnFlag();
            getFlagCarrier(mgPlayer).stopCarrierParticleEffect();
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

    public boolean hasDroppedFlag(String id) {
        return droppedFlag.containsKey(id);
    }

    public void addDroppedFlag(String id, CTFFlag flag) {
        droppedFlag.put(id, flag);
    }

    public void removeDroppedFlag(String id) {
        droppedFlag.remove(id);
    }

    public @Nullable CTFFlag getDroppedFlag(String id) {
        return droppedFlag.get(id);
    }

    public boolean hasPaintBallMode() {
        return paintBallMode.getFlag();
    }

    public void setPaintBallMode(boolean paintBallMode) {
        this.paintBallMode.setFlag(paintBallMode);
    }

    public int getPaintBallDamage() {
        return paintBallDamage.getFlag();
    }

    public void setPaintBallDamage(int paintBallDamage) {
        this.paintBallDamage.setFlag(paintBallDamage);
    }

    public boolean hasUnlimitedAmmo() {
        return unlimitedAmmo.getFlag();
    }

    public void setUnlimitedAmmo(boolean unlimitedAmmo) {
        this.unlimitedAmmo.setFlag(unlimitedAmmo);
    }

    public boolean canSaveCheckpoint() {
        return saveCheckpoints.getFlag();
    }

    public void setSaveCheckpoint(boolean saveCheckpoint) {
        this.saveCheckpoints.setFlag(saveCheckpoint);
    }

    public boolean canLateJoin() {
        return lateJoin.getFlag();
    }

    public void setLateJoin(boolean lateJoin) {
        this.lateJoin.setFlag(lateJoin);
    }

    public boolean canSpectateFly() {
        return canSpectateFly.getFlag();
    }

    public void setCanSpectateFly(boolean canSpectateFly) {
        this.canSpectateFly.setFlag(canSpectateFly);
    }

    public boolean isRandomizeChests() {
        return randomizeChests.getFlag();
    }

    public int getMinChestRandom() {
        return minChestRandom.getFlag();
    }

    /**
     * @param minChestRandom
     * @param maxChestRandom
     * @return true whenever the parameters where valid and randomizing chests is enabled (true) or not (false)
     */
    public boolean setChestRandoms(int minChestRandom, int maxChestRandom) {
        int min;
        int max;
        boolean returnValue;
        if (minChestRandom >= 0 && maxChestRandom > 0) {
            this.randomizeChests.setFlag(true);

            min = Math.min(minChestRandom, maxChestRandom);
            max = Math.max(minChestRandom, maxChestRandom);
            returnValue = true;
        } else { // bounds are not meet. disable random chests
            this.randomizeChests.setFlag(false);

            min = minChestRandom;
            max = maxChestRandom;
            returnValue = false;
        }

        this.minChestRandom.setFlag(min);
        this.maxChestRandom.setFlag(max);

        return returnValue;
    }

    public int getMaxChestRandom() {
        return maxChestRandom.getFlag();
    }

    public boolean getActivatePlayerRecorder() {
        return activatePlayerRecorder.getFlag();
    }

    public void setActivatePlayerRecorder(boolean activatePlayerRecorder) {
        this.activatePlayerRecorder.setFlag(activatePlayerRecorder);
    }

    public Collection<MgRegion> getRegenRegions() {
        return regenRegions.getFlag().values();
    }

    public MgRegion getRegenRegion(String name) {
        return regenRegions.getFlag().get(name);
    }

    public RegenRegionChangeResult removeRegenRegion(String name) {
        boolean removed = regenRegions.getFlag().remove(name) != null;

        long numOfBlocksTotal = 0;
        for (MgRegion region : regenRegions.getFlag().values()) {
            numOfBlocksTotal += (long) Math.ceil(region.getVolume());
        }

        return new RegenRegionChangeResult(removed, numOfBlocksTotal);
    }

    /**
     * checks if the limit of all regen regions together,
     * if we are still under it, add the new region to the list
     * Please note: The regions are name unique,
     * setting a new one with a name that already exists, it will overwrite the old one.
     *
     * @param newRegenRegion new regeneration region.
     * @return a record containing whenever this was a success or not
     * and the total number of all blocks in regen regions after the setting would happen
     */
    public RegenRegionChangeResult setRegenRegion(MgRegion newRegenRegion) {
        long numOfBlocksTotal = (long) Math.ceil(newRegenRegion.getVolume());

        for (MgRegion region : regenRegions.getFlag().values()) {
            numOfBlocksTotal += (long) Math.ceil(region.getVolume());
        }

        if (numOfBlocksTotal <= maxBlocksRegenRegions.getFlag()) {
            regenRegions.getFlag().put(newRegenRegion.getName(), newRegenRegion);
            return new RegenRegionChangeResult(true, numOfBlocksTotal);
        } else {
            return new RegenRegionChangeResult(false, numOfBlocksTotal);
        }
    }

    public long getRegenBlocklimit() {
        return maxBlocksRegenRegions.getFlag();
    }

    public boolean hasRegenArea() {
        return !regenRegions.getFlag().isEmpty();
    }

    public boolean isInRegenArea(Location location) {
        for (MgRegion region : regenRegions.getFlag().values()) {
            if (region.isInRegen(location)) {
                return true;
            }
        }

        return false;
    }

    public int getRegenDelay() {
        return regenDelay.getFlag();
    }

    public void setRegenDelay(int regenDelay) {
        if (regenDelay < 0)
            regenDelay = 0;
        this.regenDelay.setFlag(regenDelay);
    }

    public int getLives() {
        return lives.getFlag().intValue();
    }

    public void setLives(int lives) {
        this.lives.setFlag((float) lives);
    }

    public long getFloorDegenTime() {
        return floorDegenTime.getFlag();
    }

    public void setFloorDegenTime(long floorDegenTime) {
        this.floorDegenTime.setFlag(floorDegenTime);
    }

    public boolean isAllowedEnderpearls() {
        return allowEnderPearls.getFlag();
    }

    public void setAllowEnderPearls(boolean allowEnderPearls) {
        this.allowEnderPearls.setFlag(allowEnderPearls);
    }

    public boolean isAllowedMPCheckpoints() {
        return allowMPCheckpoints.getFlag();
    }

    public void setAllowMPCheckpoints(boolean allowMPCheckpoints) {
        this.allowMPCheckpoints.setFlag(allowMPCheckpoints);
    }

    public boolean isAllowedFlight() {
        return allowFlight.getFlag();
    }

    public void setAllowedFlight(boolean allowFlight) {
        this.allowFlight.setFlag(allowFlight);
    }

    public boolean isFlightEnabled() {
        return enableFlight.getFlag();
    }

    public void setFlightEnabled(boolean enableFlight) {
        this.enableFlight.setFlag(enableFlight);
    }

    public Scoreboard getScoreboardManager() {
        return sbManager;
    }

    public String getObjective() {
        return objective.getFlag();
    }

    public void setObjective(String objective) {
        this.objective.setFlag(objective);
    }

    public @Nullable String getGameTypeName() {
        return gameTypeName.getFlag();
    }

    public void setGameTypeName(@Nullable String gameTypeName) {
        this.gameTypeName.setFlag(gameTypeName);
    }

    public boolean canDisplayScoreboard() {
        return displayScoreboard.getFlag();
    }

    public void setDisplayScoreboard(boolean bool) {
        displayScoreboard.setFlag(bool);
    }

    public boolean allowDragonEggTeleport() {
        return allowDragonEggTeleport.getFlag();
    }

    public void setAllowDragonEggTeleport(boolean allow) {
        allowDragonEggTeleport.setFlag(allow);
    }

    public boolean getShowCompletionTime() {
        return showCompletionTime.getFlag();
    }

    public void setShowCompletionTime(boolean bool) {
        showCompletionTime.setFlag(bool);
    }

    public StatSettings getSettings(MinigameStat stat) {
        return statSettings.computeIfAbsent(stat, StatSettings::new);
    }

    public Map<MinigameStat, StatSettings> getStatSettings(StoredGameStats stats) {
        Map<MinigameStat, StatSettings> settings = new HashMap<>();

        for (MinigameStat stat : stats.getStats().keySet()) {
            settings.put(stat, getSettings(stat));
        }

        return settings;
    }

    public void displayMenu(MinigamePlayer player) {
        Menu main = new Menu(6, getName(false), player);
        Menu playerMenu = new Menu(6, getName(false), player);
        Menu loadouts = new Menu(6, getName(false), player);
        Menu flags = new Menu(6, getName(false), player);
        Menu lobby = new Menu(6, getName(false), player);

        List<MenuItem> itemsMain = new ArrayList<>();
        itemsMain.add(enabled.getMenuItem("Enabled", Material.PAPER));
        itemsMain.add(usePermissions.getMenuItem("Use Permissions", Material.PAPER));
        itemsMain.add(type.getMenuItem(Material.PAPER, "Game Type"));
        List<String> scoreTypes = new ArrayList<>();
        for (GameMechanicBase val : GameMechanics.getGameMechanics()) {
            scoreTypes.add(WordUtils.capitalizeFully(val.getMechanic()));
        }
        itemsMain.add(new MenuItemList<>(Material.ROTTEN_FLESH, "Game Mechanic", List.of("Multiplayer Only"), new Callback<>() {

            @Override
            public String getValue() {
                return WordUtils.capitalizeFully(mechanic.getFlag());
            }

            @Override
            public void setValue(String value) {
                mechanic.setFlag(value.toLowerCase());
            }
        }, scoreTypes));

        final MenuItemCustom mechSettings = new MenuItemCustom(Material.PAPER, "Game Mechanic Settings");
        final Minigame mgm = this;
        final Menu fmain = main;
        mechSettings.setClick(object -> {
            if (getMechanic().displaySettings(mgm) != null &&
                    getMechanic().displaySettings(mgm).displayMechanicSettings(fmain))
                return null;
            return mechSettings.getDisplayItem();
        });
        itemsMain.add(mechSettings);
        MenuItemString obj = (MenuItemString) objective.getMenuItem("Objective Description", Material.DIAMOND);
        obj.setAllowNull(true);
        itemsMain.add(obj);
        obj = (MenuItemString) gameTypeName.getMenuItem("Gametype Description", Material.OAK_SIGN);
        obj.setAllowNull(true);
        itemsMain.add(obj);
        obj = (MenuItemString) displayName.getMenuItem("Display Name", Material.OAK_SIGN);
        obj.setAllowNull(true);
        itemsMain.add(obj);
        itemsMain.add(new MenuItemNewLine());
        itemsMain.add(minScore.getMenuItem(Material.STONE_SLAB, "Min. Score", List.of("Multiplayer Only")));
        itemsMain.add(maxScore.getMenuItem(Material.STONE, "Max. Score", List.of("Multiplayer Only")));
        itemsMain.add(minPlayers.getMenuItem(Material.STONE_SLAB, "Min. Players", List.of("Multiplayer Only")));
        itemsMain.add(maxPlayers.getMenuItem(Material.STONE, "Max. Players", List.of("Multiplayer Only")));
        itemsMain.add(spMaxPlayers.getMenuItem("Enable Singleplayer Max Players", Material.IRON_BARS));
        itemsMain.add(displayScoreboard.getMenuItem("Display Scoreboard", Material.OAK_SIGN));
        itemsMain.add(new MenuItemPage("Lobby Settings", List.of("Multiplayer Only"), Material.OAK_DOOR, lobby));
        itemsMain.add(new MenuItemNewLine());
        itemsMain.add(timer.getMenuItem(List.of("Multiplayer Only"), "Time Length", Material.CLOCK, 0, null));
        itemsMain.add(timerDisplayType.getMenuItem("Use XP bar as Timer", Material.ENDER_PEARL));
        itemsMain.add(startWaitTime.getMenuItem(List.of("Multiplayer Only"), "Start Wait Time", Material.CLOCK, 3, null));
        itemsMain.add(showCompletionTime.getMenuItem("Show completion time", Material.PAPER));
        itemsMain.add(lateJoin.getMenuItem(Material.DEAD_BUSH, "Allow Late Join", List.of("Multiplayer Only")));
        itemsMain.add(randomizeStart.getMenuItem(Material.LIGHT_BLUE_GLAZED_TERRACOTTA, "Randomize Start Point", List.of("The location will be", "chosen at random", "from global or team lists.")));
        itemsMain.add(new MenuItemDisplayWhitelist(Material.CHEST, "Block Whitelist/Blacklist", List.of("Blocks that can/can't", "be broken"),
                getRecorderData().getWBBlocks(), getRecorderData().getWhitelistModeCallback(), List.of("If whitelist mode only", "added items can be", "broken.")));
        itemsMain.add(new MenuItemNewLine());
        List<String> floorDegenDes = new ArrayList<>();
        floorDegenDes.add("Mainly used to prevent");
        floorDegenDes.add("islanding in spleef Minigames.");
        List<String> floorDegenOpt = new ArrayList<>();
        floorDegenOpt.add("Inward");
        floorDegenOpt.add("Circle");
        floorDegenOpt.add("Random");
        itemsMain.add(new MenuItemList("Floor Degenerator Type", floorDegenDes, Material.SNOW_BLOCK, new Callback<>() {

            @Override
            public String getValue() {
                return WordUtils.capitalizeFully(degenType.getFlag());
            }

            @Override
            public void setValue(String value) {
                degenType.setFlag(value.toLowerCase());
            }
        }, floorDegenOpt));

        List<String> degenRandDes = new ArrayList<>();
        degenRandDes.add("Chance of block being");
        degenRandDes.add("removed on random");
        degenRandDes.add("degeneration.");
        itemsMain.add(degenRandomChance.getMenuItem(Material.SNOW, "Random Floor Degen Chance", degenRandDes, 1, 100));
        itemsMain.add(floorDegenTime.getMenuItem(Material.CLOCK, "Floor Degenerator Delay", 1L, null));
        itemsMain.add(regenDelay.getMenuItem(List.of("Time in seconds before", "Minigame regeneration starts"), "Regeneration Delay",
                Material.CLOCK, 0, null));
        itemsMain.add(new MenuItemNewLine());
        itemsMain.add(new MenuItemPage("Player Settings", Material.SKELETON_SKULL, playerMenu));
//      List<String> thDes = new ArrayList<>();
//        thDes.add("Treasure hunt related");
//        thDes.add("settings.");
//        itemsMain.add(new MenuItemPage("Treasure Hunt Settings", thDes, Material.CHEST, treasureHunt));
//        MenuItemDisplayLoadout defLoad = new MenuItemDisplayLoadout("Default Loadout", Material.DIAMOND_SWORD, LoadoutModule.getMinigameModule(this).getDefaultPlayerLoadout(), this);
//        defLoad.setAllowDelete(false);
//        itemsMain.add(defLoad);
        itemsMain.add(new MenuItemPage("Loadouts", Material.CHEST, loadouts));
        itemsMain.add(canSpectateFly.getMenuItem("Allow Spectator Fly", Material.FEATHER));
        List<String> rndChstDes = new ArrayList<>();
        rndChstDes.add("Randomize items in");
        rndChstDes.add("chest upon first opening");
        itemsMain.add(randomizeChests.getMenuItem(Material.CHEST, "Randomize Chests", rndChstDes));
        rndChstDes.clear();
        rndChstDes.add("Min. item randomization");
        itemsMain.add(minChestRandom.getMenuItem(Material.OAK_STAIRS, "Min. Chest Random", rndChstDes, 0, null));
        rndChstDes.clear();
        rndChstDes.add("Max. item randomization");
        itemsMain.add(maxChestRandom.getMenuItem(Material.STONE, "Max. Chest Random", rndChstDes, 0, null));
        itemsMain.add(new MenuItemStatisticsSettings(Material.WRITABLE_BOOK, "Stat Settings", this));
        itemsMain.add(activatePlayerRecorder.getMenuItem("Activate Player Block Recorder", Material.COMMAND_BLOCK));
        itemsMain.add(new MenuItemNewLine());

        //--------------//
        //Loadout Settings
        //--------------//
        List<MenuItem> mi = new ArrayList<>();
        List<Component> des = new ArrayList<>();
        des.add("Shift + Right Click to Delete");


        LoadoutModule loadoutModule = LoadoutModule.getMinigameModule(this);
        if (loadoutModule != null) {
            for (String ld : loadoutModule.getLoadouts()) {
                Material material = Material.GLASS_PANE;
                PlayerLoadout playerLoadout = loadoutModule.getLoadout(ld);

                if (!playerLoadout.getItemSlots().isEmpty()) {
                    material = playerLoadout.getItem((Integer) playerLoadout.getItemSlots().toArray()[0]).getType();
                }
                if (playerLoadout.isDeleteable()) {
                    mi.add(new MenuItemDisplayLoadout(ld, des, material, playerLoadout, this));
                } else {
                    mi.add(new MenuItemDisplayLoadout(ld, material, playerLoadout, this));
                }
            }

            loadouts.addItem(new MenuItemLoadoutAdd("Add Loadout", MenuUtility.getCreateMaterial(), loadoutModule.getLoadoutMap(), this), 53);
            loadouts.addItem(new MenuItemBack(main), loadouts.getSize() - 9);
            loadouts.addItems(mi);
        }

        main.addItems(itemsMain);
        main.addItem(new MenuItemSaveMinigame(MenuUtility.getSaveMaterial(), "Save " + getName(false), this), main.getSize() - 1);

        //----------------------//
        //Minigame Player Settings
        //----------------------//
        List<MenuItem> itemsPlayer = new ArrayList<>(20);
        itemsPlayer.add(defaultGamemode.getMenuItem("Players Gamemode", Material.CRAFTING_TABLE));
        itemsPlayer.add(allowEnderPearls.getMenuItem("Allow Enderpearls", Material.ENDER_PEARL));
        itemsPlayer.add(itemDrops.getMenuItem("Allow Item Drops", Material.DIAMOND_SWORD));
        itemsPlayer.add(deathDrops.getMenuItem("Allow Death Drops", Material.SKELETON_SKULL));
        itemsPlayer.add(itemPickup.getMenuItem("Allow Item Pickup", Material.DIAMOND));
        itemsPlayer.add(blockBreak.getMenuItem("Allow Block Break", Material.DIAMOND_PICKAXE));
        itemsPlayer.add(blockPlace.getMenuItem("Allow Block Place", Material.STONE));
        itemsPlayer.add(blocksDrop.getMenuItem("Allow Block Drops", Material.COBBLESTONE));
        itemsPlayer.add(lives.getMenuItem(Material.APPLE, "Lives", null));
        itemsPlayer.add(paintBallMode.getMenuItem("Paintball Mode", Material.SNOWBALL));
        itemsPlayer.add(paintBallDamage.getMenuItem(Material.ARROW, "Paintball Damage", 1, null));
        itemsPlayer.add(unlimitedAmmo.getMenuItem("Unlimited Ammo", Material.SNOW_BLOCK));
        itemsPlayer.add(allowMPCheckpoints.getMenuItem("Enable Multiplayer Checkpoints", Material.OAK_SIGN));
        itemsPlayer.add(saveCheckpoints.getMenuItem(Material.OAK_SIGN, "Save Checkpoints", List.of("Singleplayer Only")));
        itemsPlayer.add(new MenuItemPage("Flags", List.of("Singleplayer flags"), Material.OAK_SIGN, flags));
        itemsPlayer.add(allowFlight.getMenuItem(Material.FEATHER, "Allow Flight", List.of("Allow flight to", "be toggled")));
        itemsPlayer.add(enableFlight.getMenuItem(Material.FEATHER, "Enable Flight", List.of("Start players", "in flight", "(Must have Allow", "Flight)")));
        itemsPlayer.add(allowDragonEggTeleport.getMenuItem("Allow Dragon Egg Teleport", Material.DRAGON_EGG));
        itemsPlayer.add(usePlayerDisplayNames.getMenuItem(Material.POTATO, "Use Players Display Names", List.of("Use Player Nicks or Real Names")));
        itemsPlayer.add(showPlayerBroadcasts.getMenuItem(Material.PAPER, "Show Join/Exit Broadcasts", List.of("Show Join and Exit broadcasts", "Plus other Player broadcasts")));
        itemsPlayer.add(showCTFBroadcasts.getMenuItem(Material.PAPER, "Show CTF Broadcasts", List.of("Show Flag captures and home returns")));
        itemsPlayer.add(keepInventory.getMenuItem("Keep Inventory", Material.ZOMBIE_HEAD));
        itemsPlayer.add(friendlyFireSplashPotions.getMenuItem("Allow friendly fire with splash potions", Material.SPLASH_POTION));
        itemsPlayer.add(friendlyFireLingeringPotions.getMenuItem("Allow friendly fire with lingering potions", Material.LINGERING_POTION));
        playerMenu.addItems(itemsPlayer);
        playerMenu.addItem(new MenuItemBack(main), main.getSize() - 9);

        //--------------//
        //Minigame Flags//
        //--------------//
        List<MenuItem> itemsFlags = new ArrayList<>(getFlags().size());
        for (String flag : getFlags()) {
            itemsFlags.add(new MenuItemFlag(Material.OAK_SIGN, flag, getFlags()));
        }
        flags.addItem(new MenuItemBack(playerMenu), flags.getSize() - 9);
        flags.addItem(new MenuItemAddFlag(MgMenuLangKey.MENU_FLAGADD_NAME,
                MenuUtility.getCreateMaterial(), this), flags.getSize() - 1);
        flags.addItems(itemsFlags);

        //--------------//
        //Lobby Settings//
        //--------------//
        LobbySettingsModule lobbySettingsModule = LobbySettingsModule.getMinigameModule(this);
        if (lobbySettingsModule != null) {
            List<MenuItem> itemsLobby = new ArrayList<>(4);
            itemsLobby.add(new MenuItemBoolean(Material.STONE_BUTTON, "Can Interact on Player Wait", lobbySettingsModule.getCanInteractPlayerWaitCallback()));
            itemsLobby.add(new MenuItemBoolean(Material.STONE_BUTTON, "Can Interact on Start Wait", lobbySettingsModule.getCanInteractStartWaitCallback()));
            itemsLobby.add(new MenuItemBoolean(Material.ICE, "Can Move on Player Wait", lobbySettingsModule.getCanMovePlayerWaitCallback()));
            itemsLobby.add(new MenuItemBoolean(Material.ICE, "Can Move on Start Wait", lobbySettingsModule.getCanMoveStartWaitCallback()));
            itemsLobby.add(new MenuItemBoolean(Material.ENDER_PEARL, "Teleport After Player Wait", List.of("Should players be teleported", "after player wait time?"),
                    lobbySettingsModule.getTeleportOnPlayerWaitCallback()));
            itemsLobby.add(new MenuItemBoolean(Material.ENDER_PEARL, "Teleport on Start", List.of("Should players teleport", "to the start position", "after lobby?"),
                    lobbySettingsModule.getTeleportOnStartCallback()));
            itemsLobby.add(new MenuItemTime(Material.CLOCK, "Waiting for Players Time", List.of("The time in seconds", "the game will wait for", "more players to join.", "A value of 0 will use", "the config setting"),
                    lobbySettingsModule.getPlayerWaitTimeCallback(), 0, Long.MAX_VALUE));
            lobby.addItems(itemsLobby);
            lobby.addItem(new MenuItemBack(main), lobby.getSize() - 9);
        }

        for (MinigameModule mod : getModules()) {
            mod.addEditMenuOptions(main);
        }
        main.displayMenu(player);

    }

    @NotNull
    public ScoreboardData getScoreboardData() {
        return sbData;
    }

    public void saveMinigame() {
        MinigameSave minigame = new MinigameSave(name, "config");
        FileConfiguration cfg = minigame.getConfig();
        cfg.set(name, null);
        cfg.createSection(name);

        for (MinigameModule module : getModules()) {
            if (!module.useSeparateConfig()) {
                module.save(cfg);

                if (module.getFlags() != null) {
                    for (Flag<?> flag : module.getFlags().values()) {
                        if (flag.getFlag() != null && (flag.getDefaultFlag() == null || !flag.getDefaultFlag().equals(flag.getFlag())))
                            flag.saveValue(name, cfg);
                    }
                }
            } else {
                MinigameSave modsave = new MinigameSave("minigames/" + name + "/" + module.getName().toLowerCase());
                modsave.getConfig().set(name, null);
                modsave.getConfig().createSection(name);
                module.save(modsave.getConfig());

                if (module.getFlags() != null) {
                    for (Flag<?> flag : module.getFlags().values()) {
                        if (flag.getFlag() != null && (flag.getDefaultFlag() == null || !flag.getDefaultFlag().equals(flag.getFlag())))
                            flag.saveValue(name, modsave.getConfig());
                    }
                }

                modsave.saveConfig();
            }
        }

        for (String configOpt : configFlags.keySet()) {
            if (configFlags.get(configOpt).getFlag() != null &&
                    (configFlags.get(configOpt).getDefaultFlag() == null ||
                            !configFlags.get(configOpt).getDefaultFlag().equals(configFlags.get(configOpt).getFlag())))
                configFlags.get(configOpt).saveValue(name, cfg);
        }

        //dataFixerUpper
        if (cfg.contains(name + ".useXPBarTimer")) {
            cfg.set(name + ".useXPBarTimer", null);
        }

        if (!getRecorderData().getWBBlocks().isEmpty()) {
            List<String> blocklist = new ArrayList<>();
            for (Material mat : getRecorderData().getWBBlocks()) {
                blocklist.add(mat.toString());
            }
            minigame.getConfig().set(name + ".whitelistblocks", blocklist);
        }

        if (getRecorderData().getWhitelistMode()) {
            minigame.getConfig().set(name + ".whitelistmode", getRecorderData().getWhitelistMode());
        }

        getScoreboardData().saveDisplays(minigame, name);
        getScoreboardData().refreshDisplays();
        Minigames.getPlugin().getBackend().saveStatSettings(this, statSettings.values());

        minigame.saveConfig();
    }

    public void loadMinigame() {
        MinigameSave minigame = new MinigameSave(name, "config");
        FileConfiguration cfg = minigame.getConfig();
        for (MinigameModule module : getModules()) {
            if (!module.useSeparateConfig()) {
                module.load(cfg);

                if (module.getFlags() != null) {
                    for (String flag : module.getFlags().keySet()) {
                        if (cfg.contains(name + "." + flag))
                            module.getFlags().get(flag).loadValue(name, cfg);
                    }
                }
            } else {
                MinigameSave modsave = new MinigameSave("minigames/" + name + "/" + module.getName().toLowerCase());
                module.load(modsave.getConfig());

                if (module.getFlags() != null) {
                    for (String flag : module.getFlags().keySet()) {
                        if (modsave.getConfig().contains(name + "." + flag)) {
                            module.getFlags().get(flag).loadValue(name, modsave.getConfig());
                        }
                    }
                }
            }
        }

        for (String flag : configFlags.keySet()) {
            if (cfg.contains(name + "." + flag)) {
                configFlags.get(flag).loadValue(name, cfg);
            }
        }

        //dataFixerUpper
        if (cfg.contains(name + ".useXPBarTimer")) {
            if (cfg.getBoolean(name + ".useXPBarTimer")){
                timerDisplayType.setFlag(MinigameTimer.DisplayType.XP_BAR);
            } else {
                timerDisplayType.setFlag(MinigameTimer.DisplayType.NONE);
            }
        }

        if (minigame.getConfig().contains(name + ".whitelistmode")) {
            getRecorderData().setWhitelistMode(minigame.getConfig().getBoolean(name + ".whitelistmode"));
        }

        if (minigame.getConfig().contains(name + ".whitelistblocks")) {
            List<String> blocklist = minigame.getConfig().getStringList(name + ".whitelistblocks");
            for (String block : blocklist) {
                Material material = Material.matchMaterial(block);
                if (material == null) {
                    material = Material.matchMaterial(block, true);
                    if (material == null) {
                        Minigames.getCmpnntLogger().info(" Failed to match config material.");
                        Minigames.getCmpnntLogger().info(block + " did not match a material please update config: " + this.name);
                    } else {
                        Minigames.getCmpnntLogger().info(block + " is a legacy material please review the config we will attempt to auto update..but you may want to add newer materials GAME: " + this.name);
                        getRecorderData().addWBBlock(material);
                    }
                } else {
                    getRecorderData().addWBBlock(material);
                }
            }
        }

        final Minigame mgm = this;

        if (getType() == MinigameType.GLOBAL && isEnabled()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Minigames.getPlugin(), () -> Minigames.getPlugin().getMinigameManager().startGlobalMinigame(mgm, null));
        }

        getScoreboardData().loadDisplays(minigame, this);

        CompletableFuture<Map<MinigameStat, StatSettings>> settingsFuture = Minigames.getPlugin().getBackend().loadStatSettings(this);
        // as far as I know it isn't defined what thread will run thenApply,
        // so we pull it back on the main thread with the BukkitScheduler
        settingsFuture.thenApply(result -> Bukkit.getScheduler().runTask(Minigames.getPlugin(), () -> {
            statSettings.clear();
            statSettings.putAll(result);

            getScoreboardData().reload();
        })).exceptionally(t -> {
            Minigames.getCmpnntLogger().error("", t);
            return null;
        });

        saveMinigame();
    }

    @Override
    public String toString() {
        return getName(false);
    }

    @Override
    public ScriptReference get(String name) {
        if (name.equalsIgnoreCase("players")) {
            return ScriptCollection.of(players);
        } else if (name.equalsIgnoreCase("teams")) {
            TeamsModule module = TeamsModule.getMinigameModule(this);
            if (module != null) {
                return ScriptCollection.of(module.getTeamsNameMap());
            }
        } else if (name.equalsIgnoreCase("name")) {
            return ScriptValue.of(getName(false));
        } else if (name.equalsIgnoreCase("displayname")) {
            return ScriptValue.of(getName(true));
        }

        return null;
    }

    @Override
    public Set<String> getKeys() {
        return Set.of("players", "teams", "name", "displayname");
    }

    @Override
    public String getAsString() {
        return getName(false);
    }
}
