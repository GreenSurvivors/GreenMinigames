package au.com.mineauz.minigames.minigame;

import au.com.mineauz.minigames.*;
import au.com.mineauz.minigames.config.*;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.mechanics.AGameMechanic;
import au.com.mineauz.minigames.mechanics.GameMechanics;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.modules.*;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.RegenRegionChangeResult;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import au.com.mineauz.minigames.recorder.RecorderData;
import au.com.mineauz.minigames.script.ScriptCollection;
import au.com.mineauz.minigames.script.ScriptObject;
import au.com.mineauz.minigames.script.ScriptReference;
import au.com.mineauz.minigames.script.ScriptValue;
import au.com.mineauz.minigames.stats.MinigameStat;
import au.com.mineauz.minigames.stats.StatSettings;
import au.com.mineauz.minigames.stats.StoredGameStats;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.text.WordUtils;
import org.bukkit.*;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("UnstableApiUsage") // shut up ItemType.Typed, we aren't even using the experimental part
public class Minigame implements ScriptObject {
    private final Minigames plugin = Minigames.getPlugin();

    private final @NotNull String name;
    private final Map<String, AFlag<?>> configFlags = new HashMap<>();
    private final ComponentFlag displayName = new ComponentFlag("displayName", null);
    private final ComponentFlag objective = new ComponentFlag("objective", null);
    private final ComponentFlag gameTypeName = new ComponentFlag("gametypeName", null);
    private final EnumFlag<MinigameType> type = new EnumFlag<>("type", MinigameType.SINGLEPLAYER);
    private final BooleanFlag enabled = new BooleanFlag("enabled", false);
    private final IntegerFlag minPlayers = new IntegerFlag("minplayers", 2);
    private final IntegerFlag maxPlayers = new IntegerFlag("maxplayers", 4);
    private final BooleanFlag spMaxPlayers = new BooleanFlag("spMaxPlayers", false);
    private final StrListFlag SinglePlayerFlags = new StrListFlag("flags", null);
    private final EnumFlag<FloorDegenerator.DegeneratorType> degenType = new EnumFlag<>("degentype", FloorDegenerator.DegeneratorType.INWARD);
    private final IntegerFlag degenRandomChance = new IntegerFlag("degenrandom", 15);
    private final RegionFlag floorDegen = new RegionFlag("sfloor", null, "sfloorpos.1", "sfloorpos.2");
    private final TimeFlag floorDegenTime = new TimeFlag("floordegentime", plugin.getConfig().getLong("multiplayer.floordegenerator.time"));
    private final LocationListFlag<SafeFullLocation> startLocations = new LocationListFlag<>("startpos", null, SafeFullLocation.class);
    private final BooleanFlag randomizeStart = new BooleanFlag("ranndomizeStart", false);
    private final LocationFlag<SafeFullLocation> endLocation = new LocationFlag<>("endpos", null, SafeFullLocation.class);
    private final LocationFlag<SafeFullLocation> quitLocation = new LocationFlag<>("quitpos", null, SafeFullLocation.class);
    private final LocationFlag<SafeFullLocation> lobbyLocation = new LocationFlag<>("lobbypos", null, SafeFullLocation.class);
    private final LocationFlag<SafeFullLocation> spectatorPosition = new LocationFlag<>("spectatorpos", null, SafeFullLocation.class);
    private final BooleanFlag usePermissions = new BooleanFlag("usepermissions", false);
    private final TimeFlag timer = new TimeFlag("timer", 0L);
    private final EnumFlag<MinigameTimer.DisplayType> timerDisplayType = new EnumFlag<>("timerDisplayType", MinigameTimer.DisplayType.XP_BAR);
    private final TimeFlag startWaitTime = new TimeFlag("startWaitTime", 0L);
    private final BooleanFlag showCompletionTime = new BooleanFlag("showCompletionTime", false);
    private final BooleanFlag itemDrops = new BooleanFlag("itemdrops", false);
    private final BooleanFlag deathDrops = new BooleanFlag("deathdrops", false);
    private final BooleanFlag itemPickup = new BooleanFlag("itempickup", true);
    private final BooleanFlag blockBreak = new BooleanFlag("blockbreak", false);
    private final BooleanFlag blockPlace = new BooleanFlag("blockplace", false);
    private final EnumFlag<GameMode> defaultGamemode = new EnumFlag<>("gamemode", GameMode.ADVENTURE);
    private final BooleanFlag blocksDrop = new BooleanFlag("blocksdrop", true);
    private final BooleanFlag allowEnderPearls = new BooleanFlag("allowEnderpearls", false);
    private final BooleanFlag allowThirdPartyTeleportation = new BooleanFlag("allowThirdPartyTeleportation", false);
    private final BooleanFlag allowMPCheckpoints = new BooleanFlag("allowMPCheckpoints", false);
    private final BooleanFlag allowFlight = new BooleanFlag("allowFlight", false);
    private final BooleanFlag enableFlight = new BooleanFlag("enableFlight", false);
    private final BooleanFlag allowDragonEggTeleport = new BooleanFlag("allowDragonEggTeleport", true);
    private final BooleanFlag showPlayerBroadcasts = new BooleanFlag("showPlayerBroadcasts", true);
    private final BooleanFlag showCTFBroadcasts = new BooleanFlag("showCTFBroadcasts", true);
    private final BooleanFlag keepInventory = new BooleanFlag("keepInventory", false);
    private final BooleanFlag friendlyFireSplashPotions = new BooleanFlag("friendlyFireSplashPotions", true);
    private final BooleanFlag friendlyFireLingeringPotions = new BooleanFlag("friendlyFireLingeringPotions", true);
    private final StringFlag mechanic = new StringFlag("scoretype", "custom"); // todo rename and create a datafixerupper
    private final BooleanFlag paintBallMode = new BooleanFlag("paintball", false);
    private final IntegerFlag paintBallDamage = new IntegerFlag("paintballdmg", 2);
    private final BooleanFlag unlimitedAmmo = new BooleanFlag("unlimitedammo", false);
    private final BooleanFlag saveCheckpoints = new BooleanFlag("saveCheckpoints", false);
    private final BooleanFlag lateJoin = new BooleanFlag("latejoin", false);
    // just to stay backwards compatible we have to save this int as a float
    private final FloatFlag lives = new FloatFlag("lives", 0F);
    private final RegionListFlag regenRegions = new RegionListFlag("regenRegions", new ArrayList<>(), "regenarea.1", "regenarea.2");
    private final TimeFlag regenDelay = new TimeFlag("regenDelay", 0L);
    private final IntegerFlag maxBlocksRegenRegions = new IntegerFlag("maxBlocksRegenRegions", 300000);
    private final @NotNull Map<@NotNull Key, @NotNull MinigameModule> modules = new HashMap<>();
    private final IntegerFlag minScore = new IntegerFlag("minscore", 5);
    private final IntegerFlag maxScore = new IntegerFlag("maxscore", 10);
    private final BooleanFlag displayScoreboard = new BooleanFlag("displayScoreboard", true);
    private final BooleanFlag canSpectateFly = new BooleanFlag("canspectatefly", false);
    private final BooleanFlag randomizeChests = new BooleanFlag("randomizechests", false);
    private final IntegerFlag minChestRandom = new IntegerFlag("minchestrandom", 5);
    private final IntegerFlag maxChestRandom = new IntegerFlag("maxchestrandom", 10);
    private final @NotNull ScoreboardDisplayManger sbData = new ScoreboardDisplayManger();
    private final Map<MinigameStat, StatSettings> statSettings = new HashMap<>();
    private final BooleanFlag PlayerRecorderactivate = new BooleanFlag("activatePlayerRecorder", true);
    //Unsaved data
    private final List<MinigamePlayer> players = new ArrayList<>();
    private final List<MinigamePlayer> spectators = new ArrayList<>();
    private final RecorderData blockRecorder = new RecorderData(this);
    private MinigameState state = MinigameState.IDLE;
    private FloorDegenerator sFloorDegen;
    private final @NotNull Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
    //Multiplayer
    private @Nullable MultiplayerTimer mpTimer = null;
    private @Nullable MinigameTimer miniTimer = null;
    private @Nullable MultiplayerBets mpBets = null;
    private boolean playersAtStart = false;

    public Minigame(final @NotNull String name, final @NotNull MinigameType type, final @NotNull SafeFullLocation start) {
        this.name = name;
        setup(type, start);
    }

    public Minigame(final @NotNull String name) {
        this.name = name;
        setup(MinigameType.SINGLEPLAYER, null);
    }

    public boolean isPlayersAtStart() {
        return playersAtStart;
    }

    public void setPlayersAtStart(boolean playersAtStart) {
        this.playersAtStart = playersAtStart;
    }

    private void setup(@NotNull MinigameType minigameType, @Nullable SafeFullLocation start) {
        this.type.setFlag(minigameType);
        startLocations.setFlag(new ArrayList<>());

        if (start != null) {
            startLocations.getFlag().add(start);
        }
        Objective newObjective = scoreboard.registerNewObjective(this.name, Criteria.DUMMY, Component.text(this.name));
        newObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (ModuleFactory factory : plugin.getMinigameManager().getModules()) {
            addModule(factory);
        }

        SinglePlayerFlags.setFlag(new ArrayList<>());

        addConfigFlag(PlayerRecorderactivate);
        addConfigFlag(allowEnderPearls);
        addConfigFlag(allowThirdPartyTeleportation);
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
        addConfigFlag(SinglePlayerFlags);
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
        addConfigFlag(type);
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

    private void addConfigFlag(@NotNull AFlag<?> flag) {
        configFlags.put(flag.getName(), flag);
    }

    public AFlag<?> getConfigFlag(String name) {
        return configFlags.get(name);
    }

    /**
     * returns the old module registed with the same name or null if there wasn't one.
     */
    public @Nullable MinigameModule addModule(@NotNull ModuleFactory factory) {
        return modules.put(factory.getKey(), factory.makeNewModule(this));
    }

    public void removeModule(final @NotNull Key moduleKey) {
        modules.remove(moduleKey);
    }

    public @NotNull List<@NotNull MinigameModule> getModules() {
        return new ArrayList<>(modules.values());
    }

    /**
     * Please use the Modules getMinigameModule() methode whenever possible - simply because its less error-prone.
     */
    public @Nullable MinigameModule getModule(final @NotNull Key key) {
        return modules.get(key);
    }

    public boolean isTeamGame() {
        TeamsModule teamsModule = TeamsModule.getMinigameModule(this);
        return getType() == MinigameType.MULTIPLAYER && teamsModule != null && !teamsModule.getTeams().isEmpty();
    }

    public boolean hasSinglePlayerFlags() {
        return !SinglePlayerFlags.getFlag().isEmpty();
    }

    public void addSinglePlayerFlag(String flag) {
        SinglePlayerFlags.getFlag().add(flag);
    }

    public List<String> getSinglePlayerFlags() {
        return SinglePlayerFlags.getFlag();
    }

    public void setSinglePlayerFlags(List<String> singlePlayerFlags) {
        this.SinglePlayerFlags.setFlag(singlePlayerFlags);
    }

    public boolean removeSinglePlayerFlag(String flag) {
        if (SinglePlayerFlags.getFlag().contains(flag)) {
            SinglePlayerFlags.getFlag().remove(flag);
            return true;
        }
        return false;
    }

    public void setStartLocation(SafeFullLocation loc) {
        if (startLocations.getFlag().isEmpty()) {
            startLocations.getFlag().add(loc);
        } else {
            startLocations.getFlag().set(0, loc);
        }
    }

    public void addStartLocation(SafeFullLocation loc) {
        startLocations.getFlag().add(loc);
    }

    public void addStartLocation(SafeFullLocation loc, int number) {
        if (startLocations.getFlag().size() >= number) {
            startLocations.getFlag().set(number - 1, loc);
        } else {
            startLocations.getFlag().add(loc);
        }
    }

    public List<SafeFullLocation> getStartLocations() {
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

    public SafeFullLocation getSpectatorLocation() {
        return spectatorPosition.getFlag();
    }

    public void setSpectatorLocation(SafeFullLocation loc) {
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

    public boolean keepInventory() {
        return keepInventory.getFlag();
    }

    public void setKeepInventory(boolean value) {
        keepInventory.setFlag(value);
    }

    public boolean friendlyFireSplashPotions() { // todo move this to a per team basis to integrate into friendlyFire setting.
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

    public FloorDegenerator.DegeneratorType getDegenType() {
        return degenType.getFlag();
    }

    public void setDegenType(FloorDegenerator.DegeneratorType degenType) {
        this.degenType.setFlag(degenType);
    }

    public int getDegenRandomChance() {
        return degenRandomChance.getFlag();
    }

    public void setDegenRandomChance(int degenRandomChance) {
        this.degenRandomChance.setFlag(degenRandomChance);
    }

    public @Nullable SafeFullLocation getEndLocation() {
        return endLocation.getFlag();
    }

    public void setEndLocation(SafeFullLocation endLocation) {
        this.endLocation.setFlag(endLocation);
    }

    public @Nullable SafeFullLocation getQuitLocation() {
        return quitLocation.getFlag();
    }

    public void setQuitLocation(SafeFullLocation quitLocation) {
        this.quitLocation.setFlag(quitLocation);
    }

    public @Nullable SafeFullLocation getLobbyLocation() {
        return lobbyLocation.getFlag();
    }

    public void setLobbyLocation(SafeFullLocation lobbyLocation) {
        this.lobbyLocation.setFlag(lobbyLocation);
    }

    public @NotNull String getName() {
        return name;
    }

    public Component getDisplayName() {
        if (displayName.getFlag() != null) {
            return displayName.getFlag();
        }
        return Component.text(name);
    }

    public void setDisplayName(Component displayName) {
        this.displayName.setFlag(displayName);
    }

    public void setShowPlayerBroadcasts(Boolean showPlayerBroadcasts) {
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

    public @Nullable MultiplayerTimer getMpTimer() {
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

    public @Nullable MinigameTimer getMinigameTimer() {
        return miniTimer;
    }

    public void setMinigameTimer(MinigameTimer mgTimer) {
        this.miniTimer = mgTimer;
    }

    public @Nullable MultiplayerBets getMpBets() {
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

    @NotNull
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

    public @NotNull List<@NotNull MinigamePlayer> getSpectators() {
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

    public void setScore(final @NotNull MinigamePlayer mgPlayer, final int amount) {
        Objective objective = scoreboard.getObjective(getName());
        if (objective != null) {
            objective.getScore(mgPlayer.getPlayer()).setScore(amount);
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

    /**
     * in seconds
     */
    public long getStartWaitTime() {
        return startWaitTime.getFlag();
    }

    /**
     * in seconds
     */
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

    public @NotNull String getMechanicName() {
        return mechanic.getFlag();
    }

    public @Nullable AGameMechanic getMechanic() {
        return GameMechanics.getGameMechanic(mechanic.getFlag());
    }

    public void setMechanic(@NotNull AGameMechanic gameMechanicBase) {
        this.mechanic.setFlag(gameMechanicBase.getMechanicName());
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

    public boolean isPlayerRecorderActivate() {
        return PlayerRecorderactivate.getFlag();
    }

    public void setPlayerRecorderActivate(boolean playerRecorderActivate) {
        this.PlayerRecorderactivate.setFlag(playerRecorderActivate);
    }

    public @NotNull @UnmodifiableView List<@NotNull MgRegion> getRegenRegions() {
        return Collections.unmodifiableList(regenRegions.getFlag());
    }

    public @Nullable MgRegion getRegenRegion(final @NotNull String name) {
        for (final @NotNull MgRegion region : regenRegions.getFlag()) {
            if (region.getName().equals(name)) {
                return region;
            }
        }
        return null;
    }

    public @NotNull RegenRegionChangeResult removeRegenRegion(final @NotNull String name) {
        boolean removed = regenRegions.getFlag().removeIf(it -> it.getName().equals(name));

        long numOfBlocksTotal = 0;
        for (MgRegion region : regenRegions.getFlag()) {
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
    @NotNull
    public RegenRegionChangeResult setRegenRegion(@NotNull MgRegion newRegenRegion) {
        long numOfBlocksTotal = (long) Math.ceil(newRegenRegion.getVolume());

        for (MgRegion region : regenRegions.getFlag()) {
            numOfBlocksTotal += (long) Math.ceil(region.getVolume());
        }

        if (numOfBlocksTotal <= maxBlocksRegenRegions.getFlag()) {
            regenRegions.getFlag().add(newRegenRegion);
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

    public boolean isInRegenArea(@NotNull Location location) {
        for (MgRegion region : regenRegions.getFlag()) {
            if (region.isInRegen(location)) {
                return true;
            }
        }

        return false;
    }

    public long getRegenDelay() {
        return regenDelay.getFlag();
    }

    public void setRegenDelay(long regenDelay) {
        if (regenDelay < 0) {
            regenDelay = 0;
        }
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

    public boolean areThirdPartyTeleportationAllowed() {
        return allowThirdPartyTeleportation.getFlag();
    }

    public void setThirdPartyTeleportationAllowed(final boolean allowThirdPartyTeleportation) {
        this.allowThirdPartyTeleportation.setFlag(allowThirdPartyTeleportation);
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

    public @NotNull Scoreboard getScoreboard() {
        return scoreboard;
    }

    public @Nullable Component getObjective() {
        return objective.getFlag();
    }

    public void setObjective(@Nullable Component objective) {
        this.objective.setFlag(objective);
    }

    public @Nullable Component getGameTypeName() {
        return gameTypeName.getFlag();
    }

    public void setGameTypeName(@Nullable Component gameTypeName) {
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

    @NotNull
    public StatSettings getSettings(MinigameStat stat) {
        return statSettings.computeIfAbsent(stat, StatSettings::new);
    }

    @NotNull
    public Map<MinigameStat, StatSettings> getStatSettings(@NotNull StoredGameStats stats) {
        Map<MinigameStat, StatSettings> settings = new HashMap<>();

        for (MinigameStat stat : stats.getStats().keySet()) {
            settings.put(stat, getSettings(stat));
        }

        return settings;
    }

    public void displayMenu(final @NotNull MinigamePlayer player) {
        Menu mainMenu = new Menu(6, getDisplayName(), player);
        Menu playerMenu = new Menu(6, getDisplayName(), player);
        Menu loadouts = new Menu(6, getDisplayName(), player);
        Menu flags = new Menu(6, getDisplayName(), player);
        //Menu lobby = new Menu(6, getDisplayName(), player);

        mainMenu.addItem(enabled.getMenuItem(ItemType.PAPER, MgMenuLangKey.MENU_MINIGAME_ENABLED_NAME), 0);
        mainMenu.addItem(usePermissions.getMenuItem(ItemType.PAPER, MgMenuLangKey.MENU_MINIGAME_USEPERNS_NAME), 1);

        List<TypeDependentDisplayData> typeDependentDisplayData = new ArrayList<>();
        mainMenu.addItem(new MenuItemEnum<>(ItemType.PAPER, MgMenuLangKey.MENU_MINIGAME_TYPE_NAME, new Callback<>() {

            @Override
            public MinigameType getValue() {
                return type.getFlag();
            }

            @Override
            public void setValue(MinigameType value) {
                type.setFlag(value);

                for (TypeDependentDisplayData data : typeDependentDisplayData) {
                    if (!data.applicableTypes.contains(value)) {
                        mainMenu.removeItem(data.slot);
                    } else {
                        mainMenu.addItem(data.menuItem, data.slot);
                    }
                }
            }
        }, MinigameType.class), 2);

        List<String> mechanicNames = new ArrayList<>();
        for (AGameMechanic val : GameMechanics.getGameMechanics()) {
            mechanicNames.add(WordUtils.capitalizeFully(val.getMechanicName()));
        }
        final MenuItemList<String> mechanicMenuItem = new MenuItemList<>(ItemType.ROTTEN_FLESH,
            MgMenuLangKey.MENU_MINIGAME_MECHANIC_NAME, new Callback<>() {

            @Override
            public String getValue() {
                return WordUtils.capitalizeFully(mechanic.getFlag());
            }

            @Override
            public void setValue(@NotNull String value) {
                mechanic.setFlag(value.toLowerCase());
            }
        }, mechanicNames);
        typeDependentDisplayData.add(new TypeDependentDisplayData(mechanicMenuItem, List.of(MinigameType.MULTIPLAYER), 3));
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            mainMenu.addItem(mechanicMenuItem, 3);
        }

        final MenuItemCustom mechSettings = new MenuItemCustom(ItemType.PAPER, MgMenuLangKey.MENU_MINIGAME_MECHANIC_SETTINGS_NAME);
        final Minigame mgm = this;
        final Menu fmain = mainMenu;
        mechSettings.setClick(() -> {
            if (getMechanic().displaySettings(mgm) != null &&
                getMechanic().displaySettings(mgm).displayMechanicSettings(fmain)) {
                return ItemStack.empty();
            } else {
                return mechSettings.getDisplayItem();
            }
        });
        typeDependentDisplayData.add(new TypeDependentDisplayData(mechSettings, List.of(MinigameType.MULTIPLAYER), 4));
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            mainMenu.addItem(mechSettings, 4);
        }

        MenuItemComponent cmpntItem = (MenuItemComponent) objective.getMenuItem(ItemType.DIAMOND,
            MgMenuLangKey.MENU_MINIGAME_OBJECTIVEDESCRIPTION_NAME);
        cmpntItem.setAllowNull(true);
        mainMenu.addItem(cmpntItem, 5);

        cmpntItem = (MenuItemComponent) gameTypeName.getMenuItem(ItemType.WRITTEN_BOOK, MgMenuLangKey.MENU_MINIGAME_TYPEDESCRIPTION_NAME);
        cmpntItem.setAllowNull(true);
        mainMenu.addItem(cmpntItem, 6);

        cmpntItem = (MenuItemComponent) displayName.getMenuItem(ItemType.OAK_SIGN, MgMenuLangKey.MENU_DISPLAYNAME_NAME);
        cmpntItem.setAllowNull(true);
        mainMenu.addItem(cmpntItem, 7);

        mainMenu.addItem(new MenuItemNewLine(), 8);

        final MenuItem scoreMinMenuItem = minScore.getMenuItem(ItemType.STONE_SLAB, MgMenuLangKey.MENU_MINIGAME_SCORE_MIN_NAME);
        typeDependentDisplayData.add(new TypeDependentDisplayData(scoreMinMenuItem, List.of(MinigameType.MULTIPLAYER), 9));
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            mainMenu.addItem(scoreMinMenuItem, 9);
        }

        final MenuItem scoreMaxMenuItem = maxScore.getMenuItem(ItemType.STONE, MgMenuLangKey.MENU_MINIGAME_SCORE_MAX_NAME);
        typeDependentDisplayData.add(new TypeDependentDisplayData(scoreMaxMenuItem, List.of(MinigameType.MULTIPLAYER), 10));
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            mainMenu.addItem(scoreMaxMenuItem, 10);
        }

        final MenuItem minPlayersMenuItem = minPlayers.getMenuItem(ItemType.STONE_SLAB, MgMenuLangKey.MENU_MINIGAME_PLAYERS_MIN_NAME);
        typeDependentDisplayData.add(new TypeDependentDisplayData(minPlayersMenuItem, List.of(MinigameType.MULTIPLAYER), 11));
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            mainMenu.addItem(minPlayersMenuItem, 11);
        }

        final MenuItem maxPlayersMenuItem = maxPlayers.getMenuItem(ItemType.STONE, MgMenuLangKey.MENU_MINIGAME_PLAYERS_MAX_NAME);
        typeDependentDisplayData.add(new TypeDependentDisplayData(maxPlayersMenuItem, List.of(MinigameType.MULTIPLAYER), 12));
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            mainMenu.addItem(maxPlayersMenuItem, 12);
        }

        final MenuItemBoolean SinglePlayerAmountCappedMenuItem = spMaxPlayers.getMenuItem(ItemType.IRON_BARS,
            MgMenuLangKey.MENU_MINIGAME_PLAYERS_SINGLEPLAYER_CAPPED_NAME);
        typeDependentDisplayData.add(new TypeDependentDisplayData(maxPlayersMenuItem, List.of(MinigameType.SINGLEPLAYER), 13));
        if (type.getFlag() == MinigameType.SINGLEPLAYER) {
            mainMenu.addItem(SinglePlayerAmountCappedMenuItem, 13);
        }

        mainMenu.addItem(displayScoreboard.getMenuItem(ItemType.OAK_SIGN, MgMenuLangKey.MENU_MINIGAME_SCOREBOARD_DISPLAY_NAME), 14);

        final MenuItemPage lobbySettingsMenuItemPage = new MenuItemPage(ItemType.OAK_DOOR, MgMenuLangKey.MENU_MINIGAME_LOBBY_SETTINGS_NAME, lobby);
        typeDependentDisplayData.add(new TypeDependentDisplayData(lobbySettingsMenuItemPage, List.of(MinigameType.MULTIPLAYER), 14));
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            mainMenu.addItem(lobbySettingsMenuItemPage, 14);
        }

        mainMenu.addItem(new MenuItemNewLine(), 15);

        final MenuItemTime gamLengthMenuItem = timer.getMenuItem(ItemType.CLOCK, MgMenuLangKey.MENU_MINIGAME_TIME_GAMELENGTH_NAME, 0L, null);
        typeDependentDisplayData.add(new TypeDependentDisplayData(gamLengthMenuItem, List.of(MinigameType.MULTIPLAYER), 18));
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            mainMenu.addItem(gamLengthMenuItem, 18);
        }

        mainMenu.addItem(timerDisplayType.getMenuItem(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_MINIGAME_TIME_DISPLAYTYPE_NAME), 19);

        final MenuItemTime startWaitTimeMenuItem = startWaitTime.getMenuItem(ItemType.CLOCK, MgMenuLangKey.MENU_MINIGAME_TIME_STARTWAIT_NAME, 3L, null);
        typeDependentDisplayData.add(new TypeDependentDisplayData(startWaitTimeMenuItem, List.of(MinigameType.MULTIPLAYER), 19));
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            mainMenu.addItem(startWaitTimeMenuItem, 19);
        }

        mainMenu.addItem(showCompletionTime.getMenuItem(ItemType.PAPER, MgMenuLangKey.MENU_MINIGAME_TIME_SHOWCOMPLETION_NAME), 20);

        final MenuItem allowLateJoinMenuItem = lateJoin.getMenuItem(ItemType.DEAD_BUSH, MgMenuLangKey.MENU_MINIGAME_ALLOWLATEJOIN_NAME);
        typeDependentDisplayData.add(new TypeDependentDisplayData(allowLateJoinMenuItem, List.of(MinigameType.MULTIPLAYER), 21));
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            mainMenu.addItem(allowLateJoinMenuItem, 21);
        }

        mainMenu.addItem(randomizeStart.getMenuItem(ItemType.LIGHT_BLUE_GLAZED_TERRACOTTA, MgMenuLangKey.MENU_MINIGAME_STARTPOINT_RANDOMIZE_NAME,
            MgMenuLangKey.MENU_MINIGAME_STARTPOINT_RANDOMIZE_DESCRIPTION), 22);

        mainMenu.addItem(new MenuItemDisplayWhitelist(ItemType.CHEST,
            MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_MINIGAME_WHITELIST_BLOCK_NAME), // Block Whitelist/Blacklist
            MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_MINIGAME_WHITELIST_BLOCK_DESCRIPTION_MAIN),
            getRecorderData().getWBBlocks(), getRecorderData().getWhitelistModeCallback(),
            MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_MINIGAME_WHITELIST_BLOCK_DESCRIPTION_SECOND)), 23);

        mainMenu.addItem(new MenuItemNewLine(), 24);

        // double pack, since the type shows / hides random chance percent
        final MenuItemInteger randomFloorDegenChanceMenuItem = degenRandomChance.getMenuItem(ItemType.SNOW,
            MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_MINIGAME_DEGEN_RANDOMCHANCE_NAME),
            MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_MINIGAME_DEGEN_RANDOMCHANCE_DESCRIPTION), 1, 100);
        mainMenu.addItem(new MenuItemList<>(ItemType.SNOW_BLOCK, MgMenuLangKey.MENU_MINIGAME_DEGEN_TYPE_NAME,
            MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_MINIGAME_DEGEN_TYPE_DESCRIPTION), new Callback<>() {

            @Override
            public FloorDegenerator.DegeneratorType getValue() {
                return degenType.getFlag();
            }

            @Override
            public void setValue(FloorDegenerator.DegeneratorType value) {
                degenType.setFlag(value);

                if (value == FloorDegenerator.DegeneratorType.RANDOM) {
                    mainMenu.addItem(randomFloorDegenChanceMenuItem, 28);
                } else {
                    mainMenu.removeItem(28);
                }
            }

        }, List.of(FloorDegenerator.DegeneratorType.values())), 27);
        if (degenType.getFlag() == FloorDegenerator.DegeneratorType.RANDOM) {
            mainMenu.addItem(randomFloorDegenChanceMenuItem, 28);
        }

        mainMenu.addItem(floorDegenTime.getMenuItem(ItemType.CLOCK, MgMenuLangKey.MENU_MINIGAME_DEGEN_DELAY_NAME, 1L, null));


        mainMenu.addItem(regenDelay.getMenuItem(ItemType.CLOCK, MgMenuLangKey.MENU_MINIGAME_REGENDELAY_NAME,
            MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_MINIGAME_REGENDELAY_DESCRIPTION), 0L, null));

        mainMenu.addItem(new MenuItemNewLine());

        mainMenu.addItem(new MenuItemPage(ItemType.SKELETON_SKULL, MgMenuLangKey.MENU_PLAYERSETTINGS_NAME, playerMenu));

//        List<String> thDes = new ArrayList<>();
//        thDes.add("Treasure hunt related<newline>settings.");
//        itemsMain.add(new MenuItemPage(ItemType.CHEST, "Treasure Hunt Settings", thDes, treasureHunt));
//        MenuItemDisplayLoadout defLoad = new MenuItemDisplayLoadout(ItemType.DIAMOND_SWORD, "Default Loadout", LoadoutModule.getMinigameModule(this).getDefaultPlayerLoadout(), this);
//        defLoad.setAllowDelete(false);
//        itemsMain.add(defLoad);

        mainMenu.addItem(new MenuItemPage(ItemType.CHEST, MgMenuLangKey.MENU_MINIGAME_LOADOUTS_NAME, loadouts));

        mainMenu.addItem(canSpectateFly.getMenuItem(ItemType.FEATHER, MgMenuLangKey.MENU_MINIGAME_ALLOWSPECTATORFLY_NAME));

        mainMenu.addItem(randomizeChests.getMenuItem(ItemType.CHEST, MgMenuLangKey.MENU_MINIGAME_RANDOMCHESTS_NAME,
            MgMenuLangKey.MENU_MINIGAME_RANDOMCHESTS_DESCRIPTION));

        mainMenu.addItem(minChestRandom.getMenuItem(ItemType.OAK_STAIRS, MgMenuLangKey.MENU_MINIGAME_RANDOMCHESTS_MIN_NAME,
            MgMenuLangKey.MENU_MINIGAME_RANDOMCHESTS_MIN_DESCRIPTION, 0, null));

        mainMenu.addItem(maxChestRandom.getMenuItem(ItemType.STONE, MgMenuLangKey.MENU_MINIGAME_RANDOMCHESTS_MAX_NAME,
            MgMenuLangKey.MENU_MINIGAME_RANDOMCHESTS_MAX_DESCRIPTION, 0, null));

        mainMenu.addItem(new MenuItemStatisticsSettings(ItemType.WRITABLE_BOOK, MgMenuLangKey.MENU_MINIGAME_STATISTIC_NAME, this));
        mainMenu.addItem(PlayerRecorderactivate.getMenuItem(ItemType.COMMAND_BLOCK, MgMenuLangKey.MENU_PLAYER_BLOCK_RECORDER));

        mainMenu.addItem(new MenuItemNewLine());

        mainMenu.addItem(new MenuItemSaveMinigame(MenuUtility.getSaveType(),
            MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_MINIGAME_SAVE_NAME,
                Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), getDisplayName())),
            this), mainMenu.getSize() - 1);

        //--------------//
        //Loadout Settings
        //--------------//
        final @NotNull List<@NotNull MenuItem> mi = new ArrayList<>();

        LoadoutModule loadoutModule = LoadoutModule.getMinigameModule(this);
        if (loadoutModule != null) {

            for (PlayerLoadout playerLoadout : loadoutModule.getLoadouts()) {
                @NotNull ItemType itemType = ItemType.GLASS_PANE;

                if (!playerLoadout.getItemSlots().isEmpty()) {
                    itemType = playerLoadout.getItem((Integer) playerLoadout.getItemSlots().toArray()[0]).getType().asItemType();
                }
                if (playerLoadout.isDeletable()) {
                    mi.add(new MenuItemDisplayLoadout(itemType, playerLoadout.getDisplayName(),
                        MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK), playerLoadout, this));
                } else {
                    mi.add(new MenuItemDisplayLoadout(itemType, playerLoadout.getDisplayName(), playerLoadout, this));
                }
            }

            loadouts.addItem(new MenuItemLoadoutAdd(MenuUtility.getCreateType(), MgMenuLangKey.MENU_LOADOUT_ADD_NAME,
                loadoutModule.getLoadoutMap(), this), 53);
            loadouts.addItem(new MenuItemBack(mainMenu), loadouts.getSize() - 9);
            loadouts.addItems(mi);
        }

        //----------------------//
        //Minigame Player Settings
        //----------------------//
        List<MenuItem> itemsPlayer = new ArrayList<>(20);
        itemsPlayer.add(defaultGamemode.getMenuItem(ItemType.CRAFTING_TABLE, MgMenuLangKey.MENU_PLAYERSETTINGS_GAMEMODE_NAME));
        itemsPlayer.add(allowEnderPearls.getMenuItem(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_PLAYERSETTINGS_ENDERPERLS_NAME));
        itemsPlayer.add(allowThirdPartyTeleportation.getMenuItem(ItemType.COMMAND_BLOCK, MgMenuLangKey.MENU_PLAYERSETTINGS_THIRDPARTY_TELEPORTATION_NAME));
        itemsPlayer.add(itemDrops.getMenuItem(ItemType.DIAMOND_SWORD, MgMenuLangKey.MENU_PLAYERSETTINGS_DROP_ITEM_NAME));
        itemsPlayer.add(deathDrops.getMenuItem(ItemType.SKELETON_SKULL, MgMenuLangKey.MENU_PLAYERSETTINGS_DROP_DEATH_NAME));
        itemsPlayer.add(itemPickup.getMenuItem(ItemType.DIAMOND, MgMenuLangKey.MENU_PLAYERSETTINGS_ITEMPICKUP_NAME));
        itemsPlayer.add(blockBreak.getMenuItem(ItemType.DIAMOND_PICKAXE, MgMenuLangKey.MENU_PLAYERSETTINGS_BLOCK_BREAK_NAME));
        itemsPlayer.add(blockPlace.getMenuItem(ItemType.STONE, MgMenuLangKey.MENU_PLAYERSETTINGS_BLOCK_PLACE_NAME));
        itemsPlayer.add(blocksDrop.getMenuItem(ItemType.COBBLESTONE, MgMenuLangKey.MENU_PLAYERSETTINGS_BLOCK_DROPS_NAME));
        itemsPlayer.add(lives.getMenuItem(ItemType.APPLE, MgMenuLangKey.MENU_PLAYERSETTINGS_LIVES_NAME));
        itemsPlayer.add(paintBallMode.getMenuItem(ItemType.SNOWBALL, MgMenuLangKey.MENU_PLAYERSETTINGS_PAINTBALL_MODE_NAME));
        itemsPlayer.add(paintBallDamage.getMenuItem(ItemType.ARROW, MgMenuLangKey.MENU_PLAYERSETTINGS_PAINTBALL_DAMAGE_NAME, 1, null));
        itemsPlayer.add(unlimitedAmmo.getMenuItem(ItemType.SNOW_BLOCK, MgMenuLangKey.MENU_PLAYERSETTINGS_UNLIMITEDAMMO_NAME));
        itemsPlayer.add(allowMPCheckpoints.getMenuItem(ItemType.OAK_SIGN, MgMenuLangKey.MENU_PLAYERSETTINGS_CHECKPOINT_MULTIPLAYER_NAME,
            MgMenuLangKey.MENU_MINIGAME_MULTIPLAYERONLY_DESCRIPTION)); // todo hide if not multiplayer
        itemsPlayer.add(saveCheckpoints.getMenuItem(ItemType.OAK_SIGN, MgMenuLangKey.MENU_PLAYERSETTINGS_CHECKPOINT_SAVE_NAME,
            MgMenuLangKey.MENU_MINIGAME_SINGLEPLAYERONLY_DESCRIPTION)); // todo hide if not SinglePlayer
        itemsPlayer.add(new MenuItemPage(ItemType.OAK_SIGN, MgMenuLangKey.MENU_PLAYERSETTINGS_SINGLEPLAYERFLAG_NAME,
            MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_MINIGAME_SINGLEPLAYERONLY_DESCRIPTION), flags)); // todo hide if not SinglePlayer
        itemsPlayer.add(allowFlight.getMenuItem(ItemType.FEATHER, MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ALLOW_NAME,
            MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ALLOW_DESCRIPTION));
        itemsPlayer.add(enableFlight.getMenuItem(ItemType.FEATHER, MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ENABLE_NAME,
            MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ENABLE_DESCRIPTION));
        itemsPlayer.add(allowDragonEggTeleport.getMenuItem(ItemType.DRAGON_EGG, MgMenuLangKey.MENU_PLAYERSETTINGS_DRAGONEGGTELEPORT_NAME));
        itemsPlayer.add(showPlayerBroadcasts.getMenuItem(ItemType.PAPER, MgMenuLangKey.MENU_PLAYERSETTINGS_BROADCASTS_JOINEXIT_NAME,
            MgMenuLangKey.MENU_PLAYERSETTINGS_BROADCASTS_JOINEXIT_DESCRIPTION)); // todo hide if not multiplayer
        itemsPlayer.add(showCTFBroadcasts.getMenuItem(ItemType.PAPER, MgMenuLangKey.MENU_PLAYERSETTINGS_BROADCASTS_CTF_NAME,
            MgMenuLangKey.MENU_PLAYERSETTINGS_BROADCASTS_CTF_DESCRIPTION)); //todo hide if not ctf
        itemsPlayer.add(keepInventory.getMenuItem(ItemType.ZOMBIE_HEAD, MgMenuLangKey.MENU_PLAYERSETTINGS_KEEPINVENTORY_NAME));
        itemsPlayer.add(friendlyFireSplashPotions.getMenuItem(ItemType.SPLASH_POTION,
            MgMenuLangKey.MENU_PLAYERSETTINGS_FRIENDLYFIRE_SPLASH_NAME)); // todo hide if not multiplayer
        itemsPlayer.add(friendlyFireLingeringPotions.getMenuItem(ItemType.LINGERING_POTION,
            MgMenuLangKey.MENU_PLAYERSETTINGS_FRIENDLYFIRE_LINGERING_NAME)); // todo hide if not multiplayer
        playerMenu.addItems(itemsPlayer);
        playerMenu.addItem(new MenuItemBack(mainMenu), mainMenu.getSize() - 9);

        //--------------//
        //Minigame Flags//
        //--------------//
        List<MenuItem> itemsFlags = new ArrayList<>(getSinglePlayerFlags().size());
        for (String flag : getSinglePlayerFlags()) {
            itemsFlags.add(new MenuItemFlag(ItemType.OAK_SIGN, flag, getSinglePlayerFlags()));
        }
        flags.addItem(new MenuItemBack(playerMenu), flags.getSize() - 9);
        flags.addItem(new MenuItemAddFlag(MenuUtility.getCreateType(), MgMenuLangKey.MENU_FLAGADD_NAME,
            this), flags.getSize() - 1);
        flags.addItems(itemsFlags);

        //--------------//
        //Lobby Settings//
        //--------------//
        LobbySettingsModule lobbySettingsModule = LobbySettingsModule.getMinigameModule(this);
        if (lobbySettingsModule != null) {
            List<MenuItem> itemsLobby = new ArrayList<>(4);

            itemsLobby.add(new MenuItemBoolean(ItemType.STONE_BUTTON, MgMenuLangKey.MENU_LOBBY_WAIT_PLAYER_INTERACT_NAME,
                lobbySettingsModule.getCanInteractPlayerWaitCallback()));
            itemsLobby.add(new MenuItemBoolean(ItemType.STONE_BUTTON, MgMenuLangKey.MENU_LOBBY_WAIT_START_INTERACT_NAME,
                lobbySettingsModule.getCanInteractStartWaitCallback()));
            itemsLobby.add(new MenuItemBoolean(ItemType.ICE, MgMenuLangKey.MENU_LOBBY_WAIT_PLAYER_MOVE_NAME,
                lobbySettingsModule.getCanMovePlayerWaitCallback()));
            itemsLobby.add(new MenuItemBoolean(ItemType.ICE, MgMenuLangKey.MENU_LOBBY_WAIT_START_MOVE_NAME,
                lobbySettingsModule.getCanMoveStartWaitCallback()));
            itemsLobby.add(new MenuItemBoolean(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_LOBBY_WAIT_PLAYER_TELEPORT_NAME,
                MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_LOBBY_WAIT_PLAYER_TELEPORT_DESCRIPTION),
                lobbySettingsModule.getTeleportOnPlayerWaitCallback()));
            itemsLobby.add(new MenuItemBoolean(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_LOBBY_WAIT_START_TELEPORT_NAME,
                MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_LOBBY_WAIT_START_TELEPORT_DESCRIPTION),
                lobbySettingsModule.getTeleportOnStartCallback()));
            itemsLobby.add(new MenuItemTime(ItemType.CLOCK, MgMenuLangKey.MENU_LOBBY_WAIT_PLAYER_TIME_NAME,
                MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_LOBBY_WAIT_PLAYER_TIME_DESCRIPTION),
                lobbySettingsModule.getPlayerWaitTimeCallback(), 0L, Long.MAX_VALUE));
            lobby.addItems(itemsLobby);
            lobby.addItem(new MenuItemBack(mainMenu), lobby.getSize() - 9);
        }

        for (final @NotNull MinigameModule mod : getModules()) {
            mod.addEditMenuOptions(mainMenu);
        }

        mainMenu.displayMenu(player);
    }

    @NotNull
    public ScoreboardDisplayManger getScoreboardData() {
        return sbData;
    }

    public boolean saveMinigame() {
        final @NotNull MinigameSave minigameSave = MinigameSave.forMinigameData(this, Path.of("config"));
        final @NotNull CommentedConfigurationNode minigameSaveRoot;
        try {
            minigameSaveRoot = minigameSave.getConfigRoot();
        } catch (final @NotNull ConfigurateException e) {
            plugin.getComponentLogger().error("Couldn't obtain Minigames config file of " + getName() + "to safe it. Data loss is imminent!", e);
            return false;
        }
        // I hate this, since it erases potentially data when failing.
        // however, at the same time we shouldn't keep broken data
        // also this erases comments!
        // todo come back to this and investigate if we can do anything about this issue
        minigameSaveRoot.removeChild(name);

        boolean allSuccess = true;

        final @NotNull CommentedConfigurationNode cfg = minigameSaveRoot.node(name);
        for (final @NotNull MinigameModule module : getModules()) {
            if (!module.useSeparateConfig()) {
                try {
                    module.save(cfg);
                } catch (final @NotNull SerializationException e) {
                    plugin.getComponentLogger().error("Couldn't save module " + module.key() + " of Minigame " + getName() + ". Data loss is imminent!", e);
                    allSuccess = false; // we failed. Let's try to save the other modules anyway to keep data loss as small as possible!
                }
            } else {
                final @NotNull Key moduleKey = module.key();
                final @NotNull Path modulePath;
                if (moduleKey.namespace().equals(plugin.namespace())) {
                    modulePath = Path.of(moduleKey.value());
                } else {
                    modulePath = Path.of(moduleKey.namespace(), moduleKey.value());
                }

                final @NotNull MinigameSave moduleSave = MinigameSave.forMinigameData(this, modulePath);
                final @NotNull CommentedConfigurationNode moduleSaveRoot;
                try {
                    moduleSaveRoot = moduleSave.getConfigRoot();
                } catch (final @NotNull ConfigurateException e) {
                    plugin.getComponentLogger().error("Couldn't obtain Module config file of " + module.key() + " for Minigame " + getName() + "to safe it. Data loss is imminent!", e);
                    allSuccess = false; // we failed. Let's try to save the other modules anyway to keep data loss as small as possible!
                    continue;
                }

                moduleSaveRoot.removeChild(name);
                try {
                    module.save(moduleSaveRoot.node(name));
                    moduleSave.saveConfig();
                } catch (final @NotNull IOException e) {
                    plugin.getComponentLogger().error("Couldn't save separate config file for module " + module.key() + " of Minigame " + getName() + " located at " + modulePath + ". Data loss is imminent!", e);
                    allSuccess = false; // we failed. Let's try to save the other modules anyway to keep data loss as small as possible!
                }
            }
        }

        for (final @NotNull AFlag<?> configFlag : configFlags.values()) {
            if (configFlag.getDefaultFlag() == null || !configFlag.getDefaultFlag().equals(configFlag.getFlag())) {
                try {
                    configFlag.saveValue(cfg);
                } catch (final @NotNull SerializationException e) {
                    plugin.getComponentLogger().error("Couldn't save config flag " + configFlag.getName() + " of Minigame " + getName() + ". Data loss is imminent!", e);
                    allSuccess = false; // we failed. Let's try to save the other flags anyway to keep data loss as small as possible!
                }
            }
        }

        if (!getRecorderData().getWBBlocks().isEmpty()) {
            final @NotNull List<@NotNull String> blocklist = new ArrayList<>();
            for (final @NotNull BlockType blockType : getRecorderData().getWBBlocks()) {
                blocklist.add(blockType.key().asMinimalString());
            }
            try {
                cfg.node("whitelistblocks").setList(String.class, blocklist);
            } catch (final @NotNull SerializationException e) {
                plugin.getComponentLogger().error("Couldn't save config flag whitelistblocks of Minigame " + getName() + ". Data loss is imminent!", e);
                allSuccess = false; // we failed. Let's try to save the other data anyway to keep data loss as small as possible!
            }
        }

        if (getRecorderData().getWhitelistMode()) {
            cfg.node("whitelistmode").raw(getRecorderData().getWhitelistMode());
        }

        try {
            getScoreboardData().saveDisplays(cfg);
        } catch (final @NotNull SerializationException e) {
            plugin.getComponentLogger().error("Couldn't save scoreboard displays of Minigame " + getName() + ". Data loss is imminent!", e);
            allSuccess = false; // we failed. Let's try to save the other data anyway to keep data loss as small as possible!
        }
        getScoreboardData().refreshDisplays();

        plugin.getBackend().saveStatSettings(this, statSettings.values());

        if (!allSuccess) {
            try {
                minigameSave.createBackup();
            } catch (final @NotNull IOException e) {
                // uh oh. fascism has hit the fan.
                // Yes this is a political statement in the code, how unprofessional. deal with it. fascism I mean, go out and try to solve it! I would recomment perchloric acid
                plugin.getComponentLogger().error("Couldn't back up config of Minigame " + getName() + " after failure. Data loss is doubly imminent! Abandoning to overwrite the original file.", e);

                // refrain from damaging the original data since something has horrible wrong
                return false;
            }
        }

        try {
            minigameSave.saveConfig();
        } catch (final @NotNull IOException e) {
            plugin.getComponentLogger().error("Couldn't save config of Minigame " + getName() + ". Data loss is imminent!", e);
            allSuccess = false; // we failed.
        }

        return allSuccess;
    }

    public boolean loadMinigame() {
        final @NotNull MinigameSave minigameSave = MinigameSave.forMinigameData(this, Path.of("config"));
        final @NotNull CommentedConfigurationNode cfg;
        try {
            cfg = minigameSave.getConfigRoot().node(name);
        } catch (ConfigurateException e) {
            plugin.getComponentLogger().error("Couldn't obtain Minigames config file of " + getName() + "to load it. Data loss is imminent!", e);

            return false;
        }

        boolean allSuccess = true;

        for (final @NotNull MinigameModule module : getModules()) {
            if (!module.useSeparateConfig()) {
                try {
                    module.load(cfg);
                } catch (ConfigurateException e) {
                    plugin.getComponentLogger().error("Couldn't load Minigames config file of module " + module.key() + " for minigame " + getName() + ". Some thinks may stop working.", e);
                    allSuccess = false;
                }
            } else {
                final @NotNull Key moduleKey = module.key();
                final @NotNull Path modulePath;
                if (moduleKey.namespace().equals(plugin.namespace())) {
                    modulePath = Path.of(moduleKey.value());
                } else {
                    modulePath = Path.of(moduleKey.namespace(), moduleKey.value());
                }

                final @NotNull MinigameSave modsave = MinigameSave.forMinigameData(this, modulePath);
                try {
                    module.load(modsave.getConfigRoot().node(name));
                } catch (final @NotNull ConfigurateException e) {
                    plugin.getComponentLogger().error("Couldn't load Minigames config file of module " + module.key() + " for minigame " + getName() + ". Some thinks may stop working.", e);
                    allSuccess = false;
                }
            }
        }

        for (final @NotNull String flagName : configFlags.keySet()) {
            if (cfg.hasChild(flagName)) {
                try {
                    configFlags.get(flagName).loadValue(cfg);
                } catch (final @NotNull ConfigurateException e) {
                    plugin.getComponentLogger().warn("Couldn't load config flag: " + flagName + " did not match any block type. for minigame " + getName() + ". Some thinks may stop working.", e);
                    allSuccess = false;
                }
            }
        }

        //dataFixerUpper
        if (cfg.hasChild("useXPBarTimer")) {
            if (cfg.node("useXPBarTimer").getBoolean()) {
                timerDisplayType.setFlag(MinigameTimer.DisplayType.XP_BAR);
            } else {
                timerDisplayType.setFlag(MinigameTimer.DisplayType.NONE);
            }
        }

        // todo when moving whitelisting anywhere else where it does make more sense than the recorder, also integrate config loading there, to stay in line with everything else
        if (cfg.hasChild("whitelistmode")) {
            getRecorderData().setWhitelistMode(cfg.node("whitelistmode").getBoolean());
        }

        if (cfg.hasChild("whitelistblocks")) {
            try {
                final @NotNull List<@NotNull String> blocklist = cfg.node("whitelistblocks").getList(String.class, List.of());

                for (final @NotNull String blockKeyStr : blocklist) {
                    final @Nullable Key key = NamespacedKey.fromString(blockKeyStr.toLowerCase(Locale.ROOT));

                    if (key != null) {
                        final @Nullable BlockType blockType = Registry.BLOCK.get(key);
                        if (blockType != null) {
                            getRecorderData().addWBBlock(blockType);
                        } else {
                            plugin.getComponentLogger().warn("Failed to match config block type: " + key.asMinimalString() + " did not match any block type. Please update config: " + getName());
                            allSuccess = false;
                        }
                    } else { // todo
                        plugin.getComponentLogger().warn(blockKeyStr + " in config file of Minigame " + getName() + " is not a valid Key.");
                        allSuccess = false;
                    }
                }
            } catch (final @NotNull SerializationException e) {
                plugin.getComponentLogger().error("Couldn't get \"whitelistblocks\" from Minigames config of " + getName() + ". Some thinks may stop working.", e);
                allSuccess = false;
            }
        }

        if (getType() == MinigameType.GLOBAL && isEnabled()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.getMinigameManager().startGlobalMinigame(this, null));
        }

        try {
            getScoreboardData().loadDisplays(cfg, this);
        } catch (final @NotNull SerializationException e) {
            plugin.getComponentLogger().error("Couldn't load scoreboard displays from Minigames config of " + getName() + ". They may stop working.", e);
            allSuccess = false;
        }

        CompletableFuture<Map<MinigameStat, StatSettings>> settingsFuture = plugin.getBackend().loadStatSettings(this);
        // as far as I know it isn't defined what thread will run thenApply,
        // so we pull it back on the main thread with the BukkitScheduler
        settingsFuture.thenApply(result -> Bukkit.getScheduler().runTask(plugin, () -> {
            statSettings.clear();
            statSettings.putAll(result);

            getScoreboardData().reload();
        })).exceptionally(t -> {
            plugin.getComponentLogger().error("", t);
            return null;
        });

        if (allSuccess) {
            saveMinigame();
        }

        return allSuccess;
    }

    @Override
    @Deprecated(forRemoval = true)
    public String toString() {
        return getName();
    }

    @Nullable
    @Override
    public ScriptReference resolveReference(final @NotNull String name) {
        if (name.equalsIgnoreCase("players")) {
            return ScriptCollection.of(players);
        } else if (name.equalsIgnoreCase("teams")) {
            TeamsModule module = TeamsModule.getMinigameModule(this);
            if (module != null) {
                return ScriptCollection.of(module.getTeamsNameMap());
            }
        } else if (name.equalsIgnoreCase("name")) {
            return ScriptValue.of(getName());
        } else if (name.equalsIgnoreCase("displayname")) {
            return ScriptValue.of(getName());
        }

        return null;
    }

    @NotNull
    @Override
    public Set<String> getReferenceKeys() {
        return Set.of("players", "teams", "name", "displayname");
    }

    @Override
    public @NotNull String getAsString() {
        return getName();
    }

    private record TypeDependentDisplayData(@NotNull MenuItem menuItem,
                                            @NotNull List<@NotNull MinigameType> applicableTypes, int slot) {
    }
}
