package au.com.mineauz.minigames.minigame;

import au.com.mineauz.minigames.*;
import au.com.mineauz.minigames.config.*;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.mechanics.AGameMechanic;
import au.com.mineauz.minigames.mechanics.GameMechanicRegistry;
import au.com.mineauz.minigames.mechanics.IGameMechanicFactory;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.minigame.modules.AMinigameModule;
import au.com.mineauz.minigames.minigame.modules.ModuleFactory;
import au.com.mineauz.minigames.minigame.modules.team.TeamsModule;
import au.com.mineauz.minigames.minigame.scoreboard.ScoreboardDisplayManger;
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
import org.bukkit.inventory.ItemType;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.ApiStatus;
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
    private final @NotNull Map<@NotNull String, @NotNull AFlag<?>> configFlags = new HashMap<>();
    private final @NotNull ComponentFlag displayName = new ComponentFlag("displayName", null);
    private final @NotNull ComponentFlag objective = new ComponentFlag("objective", null);
    private final @NotNull ComponentFlag gameTypeName = new ComponentFlag("gametypeName", null);
    private final @NotNull EnumFlag<@NotNull MinigameType> type = new EnumFlag<>("type", MinigameType.SINGLEPLAYER);
    private final @NotNull BooleanFlag enabled = new BooleanFlag("enabled", false);
    private final @NotNull IntegerFlag minPlayers = new IntegerFlag("minplayers", 2);
    private final @NotNull IntegerFlag maxPlayers = new IntegerFlag("maxplayers", 4);
    private final @NotNull BooleanFlag spMaxPlayers = new BooleanFlag("spMaxPlayers", false);
    private final @NotNull StrListFlag singlePlayerFlags = new StrListFlag("flags", null);
    private final @NotNull EnumFlag<FloorDegenerator.@NotNull DegeneratorType> degenType = new EnumFlag<>("degentype", FloorDegenerator.DegeneratorType.INWARD);
    private final @NotNull IntegerFlag degenRandomChance = new IntegerFlag("degenrandom", 15);
    private final @NotNull RegionFlag floorDegen = new RegionFlag("sfloor", null, "sfloorpos.1", "sfloorpos.2");
    private final @NotNull TimeFlag floorDegenTime = new TimeFlag("floordegentime", plugin.getConfig().getLong("multiplayer.floordegenerator.time"));
    private final @NotNull LocationListFlag<@NotNull SafeFullLocation> startLocations = new LocationListFlag<>("startpos", null, SafeFullLocation.class);
    private final @NotNull BooleanFlag randomizeStart = new BooleanFlag("ranndomizeStart", false);
    private final @NotNull LocationFlag<@Nullable SafeFullLocation> endLocation = new LocationFlag<>("endpos", null, SafeFullLocation.class);
    private final @NotNull LocationFlag<@Nullable SafeFullLocation> quitLocation = new LocationFlag<>("quitpos", null, SafeFullLocation.class);
    private final @NotNull LocationFlag<@Nullable SafeFullLocation> lobbyLocation = new LocationFlag<>("lobbypos", null, SafeFullLocation.class);
    private final @NotNull LocationFlag<@Nullable SafeFullLocation> spectatorPosition = new LocationFlag<>("spectatorpos", null, SafeFullLocation.class);
    private final @NotNull BooleanFlag usePermissions = new BooleanFlag("usepermissions", false);
    private final @NotNull TimeFlag timer = new TimeFlag("timer", 0L);
    private final @NotNull EnumFlag<MinigameTimer.DisplayType> timerDisplayType = new EnumFlag<>("timerDisplayType", MinigameTimer.DisplayType.XP_BAR);
    private final @NotNull TimeFlag startWaitTime = new TimeFlag("startWaitTime", 0L);
    private final @NotNull BooleanFlag showCompletionTime = new BooleanFlag("showCompletionTime", false);
    private final @NotNull BooleanFlag itemDrops = new BooleanFlag("itemdrops", false);
    private final @NotNull BooleanFlag deathDrops = new BooleanFlag("deathdrops", false);
    private final @NotNull BooleanFlag itemPickup = new BooleanFlag("itempickup", true);
    private final @NotNull BooleanFlag blockBreak = new BooleanFlag("blockbreak", false);
    private final @NotNull BooleanFlag blockPlace = new BooleanFlag("blockplace", false);
    private final @NotNull EnumFlag<@NotNull GameMode> defaultGamemode = new EnumFlag<>("gamemode", GameMode.ADVENTURE);
    private final @NotNull BooleanFlag blocksDrop = new BooleanFlag("blocksdrop", true);
    private final @NotNull BooleanFlag allowEnderPearls = new BooleanFlag("allowEnderpearls", false);
    private final @NotNull BooleanFlag allowThirdPartyTeleportation = new BooleanFlag("allowThirdPartyTeleportation", false);
    private final @NotNull BooleanFlag allowMPCheckpoints = new BooleanFlag("allowMPCheckpoints", false);
    private final @NotNull BooleanFlag allowFlight = new BooleanFlag("allowFlight", false);
    private final @NotNull BooleanFlag enableFlight = new BooleanFlag("enableFlight", false);
    private final @NotNull BooleanFlag allowDragonEggTeleport = new BooleanFlag("allowDragonEggTeleport", true);
    private final @NotNull BooleanFlag showPlayerBroadcasts = new BooleanFlag("showPlayerBroadcasts", true);
    private final @NotNull BooleanFlag showCTFBroadcasts = new BooleanFlag("showCTFBroadcasts", true); // todo move to ctf mechanic
    private final @NotNull BooleanFlag keepInventory = new BooleanFlag("keepInventory", false);
    private final @NotNull BooleanFlag friendlyFireSplashPotions = new BooleanFlag("friendlyFireSplashPotions", true);
    private final @NotNull BooleanFlag friendlyFireLingeringPotions = new BooleanFlag("friendlyFireLingeringPotions", true);
    private @NotNull AGameMechanic mechanic; // todo loading / saving, datafixerupper, including below
    //private final StringFlag mechanic = new StringFlag("scoretype", "custom"); // todo rename and create a datafixerupper
    private final @NotNull BooleanFlag paintBallMode = new BooleanFlag("paintball", false);
    private final @NotNull IntegerFlag paintBallDamage = new IntegerFlag("paintballdmg", 2);
    private final @NotNull BooleanFlag unlimitedAmmo = new BooleanFlag("unlimitedammo", false);
    private final @NotNull BooleanFlag saveCheckpoints = new BooleanFlag("saveCheckpoints", false);
    private final @NotNull BooleanFlag lateJoin = new BooleanFlag("latejoin", false);
    // just to stay backwards compatible we have to save this int as a float
    private final @NotNull FloatFlag lives = new FloatFlag("lives", 0F); // todo make a datafixerupper
    private final @NotNull RegionListFlag regenRegions = new RegionListFlag("regenRegions", new ArrayList<>(), "regenarea.1", "regenarea.2");
    private final @NotNull TimeFlag regenDelay = new TimeFlag("regenDelay", 0L);
    private final @NotNull IntegerFlag maxBlocksRegenRegions = new IntegerFlag("maxBlocksRegenRegions", 300000);
    private final @NotNull Map<@NotNull Key, @NotNull AMinigameModule> modules = new HashMap<>();
    private final @NotNull IntegerFlag minScore = new IntegerFlag("minscore", 5);
    private final @NotNull IntegerFlag maxScore = new IntegerFlag("maxscore", 10);
    private final @NotNull BooleanFlag displayScoreboard = new BooleanFlag("displayScoreboard", true);
    private final @NotNull BooleanFlag canSpectateFly = new BooleanFlag("canspectatefly", false);
    private final @NotNull BooleanFlag randomizeChests = new BooleanFlag("randomizechests", false);
    private final @NotNull IntegerFlag minChestRandom = new IntegerFlag("minchestrandom", 5);
    private final @NotNull IntegerFlag maxChestRandom = new IntegerFlag("maxchestrandom", 10);
    private final @NotNull ScoreboardDisplayManger sbData = new ScoreboardDisplayManger();
    private final @NotNull Map<@NotNull MinigameStat, @NotNull StatSettings> statSettings = new HashMap<>();
    private final BooleanFlag playerRecorderActivate = new BooleanFlag("activatePlayerRecorder", true);
    //Unsaved data
    private final @NotNull List<@NotNull MinigamePlayer> players = new ArrayList<>();
    private final @NotNull List<@NotNull MinigamePlayer> spectators = new ArrayList<>();
    private final @NotNull RecorderData blockRecorder = new RecorderData(this);
    private @NotNull MinigameState state = MinigameState.IDLE;
    private @Nullable FloorDegenerator sFloorDegen = null;
    private final @NotNull Scoreboard scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
    //Multiplayer
    private @Nullable MultiplayerTimer multiplayerTimer = null;
    private @Nullable MinigameTimer miniTimer = null;
    private @Nullable MultiplayerBets multiplayerBets = null;
    private boolean playersAtStart = false;

    public Minigame(final @NotNull String name, final @NotNull MinigameType type, final @NotNull SafeFullLocation start) {
        this.name = name;
        setup(type, start);
        mechanic = GameMechanicRegistry.CUSTOM.makeNewMechanic(plugin, this);
    }

    public Minigame(final @NotNull String name) {
        this.name = name;
        setup(MinigameType.SINGLEPLAYER, null);
        mechanic = GameMechanicRegistry.CUSTOM.makeNewMechanic(plugin, this);
    }

    public boolean isPlayersAtStart() {
        return playersAtStart;
    }

    public void setPlayersAtStart(final boolean playersAtStart) {
        this.playersAtStart = playersAtStart;
    }

    private void setup(final @NotNull MinigameType minigameType, final @Nullable SafeFullLocation start) {
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

        singlePlayerFlags.setFlag(new ArrayList<>());

        addConfigFlag(playerRecorderActivate);
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
        addConfigFlag(singlePlayerFlags);
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

    public @NotNull MinigameState getState() {
        return state;
    }

    public void setState(final @NotNull MinigameState state) {
        this.state = state;
    }

    private void addConfigFlag(final @NotNull AFlag<?> flag) {
        configFlags.put(flag.getName(), flag);
    }

    public @Nullable AFlag<?> getConfigFlag(final @NotNull String name) {
        return configFlags.get(name);
    }

    /**
     * returns the old module registed with the same name or null if there wasn't one.
     */
    public @Nullable AMinigameModule addModule(@NotNull ModuleFactory factory) {
        return modules.put(factory.getKey(), factory.makeNewModule(this));
    }

    public void removeModule(final @NotNull Key moduleKey) {
        modules.remove(moduleKey);
    }

    public @NotNull List<@NotNull AMinigameModule> getModules() {
        return new ArrayList<>(modules.values());
    }

    /**
     * Please use the Modules getMinigameModule() methode whenever possible - simply because its less error-prone.
     */
    public @Nullable AMinigameModule getModule(final @NotNull Key key) {
        return modules.get(key);
    }

    public boolean isTeamGame() {
        TeamsModule teamsModule = TeamsModule.getMinigameModule(this);
        return getType() == MinigameType.MULTIPLAYER && teamsModule != null && !teamsModule.getTeams().isEmpty();
    }

    public boolean hasSinglePlayerFlags() {
        return !singlePlayerFlags.getFlag().isEmpty();
    }

    public void addSinglePlayerFlag(String flag) {
        singlePlayerFlags.getFlag().add(flag);
    }

    public @NotNull List<@NotNull String> getSinglePlayerFlags() {
        return singlePlayerFlags.getFlag();
    }

    public void setSinglePlayerFlags(final @NotNull List<@NotNull String> singlePlayerFlags) {
        this.singlePlayerFlags.setFlag(singlePlayerFlags);
    }

    public boolean removeSinglePlayerFlag(final @NotNull String flag) {
       return singlePlayerFlags.getFlag().remove(flag);
    }

    /// replaces the first location, does not clear the rest!
    /// why? I don't know.
    @ApiStatus.Obsolete
    public void setStartLocation(final @NotNull SafeFullLocation loc) {
        if (startLocations.getFlag().isEmpty()) {
            startLocations.getFlag().add(loc);
        } else {
            startLocations.getFlag().set(0, loc);
        }
    }

    public void addStartLocation(final @NotNull SafeFullLocation loc) {
        startLocations.getFlag().add(loc);
    }

    public void setStartLocation(final @NotNull SafeFullLocation loc, final int number) {
        if (startLocations.getFlag().size() >= number) {
            startLocations.getFlag().set(number - 1, loc);
        } else {
            startLocations.getFlag().add(loc);
        }
    }

    public @NotNull List<@NotNull SafeFullLocation> getStartLocations() {
        return startLocations.getFlag();
    }

    public boolean removeStartLocation(final int locNumber) {
        return startLocations.getFlag().remove(locNumber) != null;
    }

    public boolean isRandomizeStart() {
        return randomizeStart.getFlag();
    }

    public void setRandomizeStart(final boolean bool) {
        randomizeStart.setFlag(bool);
    }

    public @Nullable SafeFullLocation getSpectatorLocation() {
        return spectatorPosition.getFlag();
    }

    public void setSpectatorLocation(@Nullable SafeFullLocation loc) {
        spectatorPosition.setFlag(loc);
    }

    public boolean isEnabled() {
        return enabled.getFlag();
    }

    public void setEnabled(final boolean enabled) {
        this.enabled.setFlag(enabled);
    }

    public int getMinPlayers() {
        return minPlayers.getFlag();
    }

    public void setMinPlayers(final int minPlayers) {
        this.minPlayers.setFlag(minPlayers);
    }

    public boolean keepInventory() {
        return keepInventory.getFlag();
    }

    public void setKeepInventory(final boolean value) {
        keepInventory.setFlag(value);
    }

    public boolean friendlyFireSplashPotions() { // todo move this to a per team basis to integrate into friendlyFire setting.
        return friendlyFireSplashPotions.getFlag();
    }

    public void setFriendlyFireSplashPotions(final boolean value) {
        friendlyFireSplashPotions.setFlag(value);
    }

    public boolean friendlyFireLingeringPotions() {
        return friendlyFireLingeringPotions.getFlag();
    }

    public void setFriendlyFireLingeringPotions(final boolean value) {
        friendlyFireLingeringPotions.setFlag(value);
    }

    public int getMaxPlayers() {
        return maxPlayers.getFlag();
    }

    public void setMaxPlayers(final int maxPlayers) {
        this.maxPlayers.setFlag(maxPlayers);
    }

    public boolean isSpMaxPlayers() {
        return spMaxPlayers.getFlag();
    }

    public void setSpMaxPlayers(final boolean spMaxPlayers) {
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

    public void setFloorDegen(final @Nullable MgRegion region) {
        floorDegen.setFlag(region);
    }

    public void removeFloorDegen() {
        floorDegen.setFlag(null);
    }

    public @NotNull FloorDegenerator.DegeneratorType getDegenType() {
        return degenType.getFlag();
    }

    public void setDegenType(final @NotNull FloorDegenerator.DegeneratorType degenType) {
        this.degenType.setFlag(degenType);
    }

    public int getDegenRandomChance() {
        return degenRandomChance.getFlag();
    }

    public void setDegenRandomChance(final int degenRandomChance) {
        this.degenRandomChance.setFlag(degenRandomChance);
    }

    public @Nullable SafeFullLocation getEndLocation() {
        return endLocation.getFlag();
    }

    public void setEndLocation(final SafeFullLocation endLocation) {
        this.endLocation.setFlag(endLocation);
    }

    public @Nullable SafeFullLocation getQuitLocation() {
        return quitLocation.getFlag();
    }

    public void setQuitLocation(final @NotNull SafeFullLocation quitLocation) {
        this.quitLocation.setFlag(quitLocation);
    }

    public @Nullable SafeFullLocation getLobbyLocation() {
        return lobbyLocation.getFlag();
    }

    public void setLobbyLocation(final SafeFullLocation lobbyLocation) {
        this.lobbyLocation.setFlag(lobbyLocation);
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull Component getDisplayName() {
        if (displayName.getFlag() != null) {
            return displayName.getFlag();
        }
        return Component.text(name);
    }

    public void setDisplayName(final Component displayName) {
        this.displayName.setFlag(displayName);
    }

    public void setShowPlayerBroadcasts(final @Nullable Boolean showPlayerBroadcasts) {
        this.showPlayerBroadcasts.setFlag(showPlayerBroadcasts);
    }

    public boolean getShowPlayerBroadcasts() {
        return showPlayerBroadcasts.getFlag();
    }

    public boolean getShowCTFBroadcasts() {
        return showCTFBroadcasts.getFlag();
    }

    public void setShowCTFBroadcasts(final @Nullable Boolean showCTFBroadcasts) {
        this.showCTFBroadcasts.setFlag(showCTFBroadcasts);
    }

    public @NotNull MinigameType getType() {
        return type.getFlag();
    }

    public void setType(final @NotNull MinigameType type) {
        this.type.setFlag(type);
    }

    public @Nullable MultiplayerTimer getMultiplayerTimer() {
        return multiplayerTimer;
    }

    public void setMultiplayerTimer(final @Nullable MultiplayerTimer multiplayerTimer) {
        this.multiplayerTimer = multiplayerTimer;
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

    public void setMinigameTimer(final @Nullable MinigameTimer mgTimer) {
        this.miniTimer = mgTimer;
    }

    public @Nullable MultiplayerBets getMultiplayerBets() {
        return multiplayerBets;
    }

    public void setMultiplayerBets(final @Nullable MultiplayerBets multiplayerBets) {
        this.multiplayerBets = multiplayerBets;
    }

    public boolean getUsePermissions() {
        return usePermissions.getFlag();
    }

    public void setUsePermissions(final boolean usePermissions) {
        this.usePermissions.setFlag(usePermissions);
    }

    public @NotNull List<@NotNull MinigamePlayer> getPlayers() {
        return players;
    }

    public void addPlayer(final @NotNull MinigamePlayer player) {
        players.add(player);
    }

    public void removePlayer(final @NotNull MinigamePlayer player) {
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

    public void addSpectator(final @NotNull MinigamePlayer player) {
        spectators.add(player);
    }

    public void removeSpectator(final @NotNull MinigamePlayer player) {
        spectators.remove(player);
    }

    public boolean isSpectator(final @NotNull MinigamePlayer player) {
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

    public void setMinScore(final int minScore) {
        this.minScore.setFlag(minScore);
    }

    public int getMaxScore() {
        return maxScore.getFlag();
    }

    public void setMaxScore(final int maxScore) {
        this.maxScore.setFlag(maxScore);
    }

    public int getMaxScorePerPlayer() {
        final float scorePerPlayer = (float) getMaxScore() / getMaxPlayers();
        int score = Math.round(scorePerPlayer * getPlayers().size());
        if (score < minScore.getFlag()) {
            score = minScore.getFlag();
        }
        return score;
    }

    public @Nullable FloorDegenerator getFloorDegenerator() {
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

    public void setTimerDisplayType(final @NotNull MinigameTimer.DisplayType type) {
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
    public void setStartWaitTime(final long startWaitTime) {
        this.startWaitTime.setFlag(startWaitTime);
    }

    public boolean hasItemDrops() {
        return itemDrops.getFlag();
    }

    public void setItemDrops(final boolean itemDrops) {
        this.itemDrops.setFlag(itemDrops);
    }

    public boolean hasDeathDrops() {
        return deathDrops.getFlag();
    }

    public void setDeathDrops(final boolean deathDrops) {
        this.deathDrops.setFlag(deathDrops);
    }

    public boolean hasItemPickup() {
        return itemPickup.getFlag();
    }

    public void setItemPickup(final boolean itemPickup) {
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

    public void setCanBlockBreak(final boolean blockBreak) {
        this.blockBreak.setFlag(blockBreak);
    }

    public boolean canBlockPlace() {
        return blockPlace.getFlag();
    }

    public void setCanBlockPlace(final boolean blockPlace) {
        this.blockPlace.setFlag(blockPlace);
    }

    public @NotNull GameMode getDefaultGamemode() {
        return defaultGamemode.getFlag();
    }

    public void setDefaultGamemode(final @NotNull GameMode defaultGamemode) {
        this.defaultGamemode.setFlag(defaultGamemode);
    }

    public boolean canBlocksDrop() {
        return blocksDrop.getFlag();
    }

    public void setBlocksDrop(final boolean blocksDrop) {
        this.blocksDrop.setFlag(blocksDrop);
    }

    public @MonotonicNonNull AGameMechanic getMechanic() {
        return mechanic;
    }

    public void setMechanic(final @NotNull AGameMechanic gameMechanicBase) {
        this.mechanic = gameMechanicBase;
    }

    public boolean hasPaintBallMode() {
        return paintBallMode.getFlag();
    }

    public void setPaintBallMode(final boolean paintBallMode) {
        this.paintBallMode.setFlag(paintBallMode);
    }

    public int getPaintBallDamage() {
        return paintBallDamage.getFlag();
    }

    public void setPaintBallDamage(final int paintBallDamage) {
        this.paintBallDamage.setFlag(paintBallDamage);
    }

    public boolean hasUnlimitedAmmo() {
        return unlimitedAmmo.getFlag();
    }

    public void setUnlimitedAmmo(final boolean unlimitedAmmo) {
        this.unlimitedAmmo.setFlag(unlimitedAmmo);
    }

    public boolean canSaveCheckpoint() {
        return saveCheckpoints.getFlag();
    }

    public void setSaveCheckpoint(final boolean saveCheckpoint) {
        this.saveCheckpoints.setFlag(saveCheckpoint);
    }

    public boolean canLateJoin() {
        return lateJoin.getFlag();
    }

    public void setLateJoin(final boolean lateJoin) {
        this.lateJoin.setFlag(lateJoin);
    }

    public boolean canSpectateFly() {
        return canSpectateFly.getFlag();
    }

    public void setCanSpectateFly(final boolean canSpectateFly) {
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
    public boolean setChestRandoms(final int minChestRandom, final int maxChestRandom) {
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
        return playerRecorderActivate.getFlag();
    }

    public void setPlayerRecorderActivate(final boolean playerRecorderActivate) {
        this.playerRecorderActivate.setFlag(playerRecorderActivate);
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
    public @NotNull RegenRegionChangeResult setRegenRegion(final @NotNull MgRegion newRegenRegion) {
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

    public boolean isInRegenArea(final @NotNull Location location) {
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

    public void setLives(final int lives) {
        this.lives.setFlag((float) lives);
    }

    public long getFloorDegenTime() {
        return floorDegenTime.getFlag();
    }

    public void setFloorDegenTime(final long floorDegenTime) {
        this.floorDegenTime.setFlag(floorDegenTime);
    }

    public boolean isAllowedEnderpearls() {
        return allowEnderPearls.getFlag();
    }

    public void setAllowEnderPearls(final boolean allowEnderPearls) {
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

    public void setAllowMPCheckpoints(final boolean allowMPCheckpoints) {
        this.allowMPCheckpoints.setFlag(allowMPCheckpoints);
    }

    public boolean isAllowedFlight() {
        return allowFlight.getFlag();
    }

    public void setAllowedFlight(final boolean allowFlight) {
        this.allowFlight.setFlag(allowFlight);
    }

    public boolean isFlightEnabled() {
        return enableFlight.getFlag();
    }

    public void setFlightEnabled(final boolean enableFlight) {
        this.enableFlight.setFlag(enableFlight);
    }

    public @NotNull Scoreboard getScoreboard() {
        return scoreboard;
    }

    public @Nullable Component getObjective() {
        return objective.getFlag();
    }

    public void setObjective(final @Nullable Component objective) {
        this.objective.setFlag(objective);
    }

    public @Nullable Component getGameTypeName() {
        return gameTypeName.getFlag();
    }

    public void setGameTypeName(final @Nullable Component gameTypeName) {
        this.gameTypeName.setFlag(gameTypeName);
    }

    public boolean canDisplayScoreboard() {
        return displayScoreboard.getFlag();
    }

    public void setDisplayScoreboard(final boolean bool) {
        displayScoreboard.setFlag(bool);
    }

    public boolean allowDragonEggTeleport() {
        return allowDragonEggTeleport.getFlag();
    }

    public void setAllowDragonEggTeleport(final boolean allow) {
        allowDragonEggTeleport.setFlag(allow);
    }

    public boolean getShowCompletionTime() {
        return showCompletionTime.getFlag();
    }

    public void setShowCompletionTime(final boolean bool) {
        showCompletionTime.setFlag(bool);
    }

    public @NotNull StatSettings getSettings(final @NotNull MinigameStat stat) {
        return statSettings.computeIfAbsent(stat, StatSettings::new);
    }

    public @NotNull Map<@NotNull MinigameStat, @NotNull StatSettings> getStatSettings(final @NotNull StoredGameStats stats) {
        final @NotNull Map<@NotNull MinigameStat, @NotNull StatSettings> settings = new HashMap<>();

        for (final @NotNull MinigameStat stat : stats.getStats().keySet()) {
            settings.put(stat, getSettings(stat));
        }

        return settings;
    }
    public void displayMenu(final @NotNull MinigamePlayer mgPlayer) {
        // store already created Mechanics in this map to not lose all your settings,
        // because you just wanted to have a look into what other mechanics there are
        final @NotNull Map<@NotNull Key, @NotNull AGameMechanic> alreadyCreatedMechanics = new HashMap<>();
        alreadyCreatedMechanics.put(getMechanic().key(), getMechanic());
        displayMenu(mgPlayer, alreadyCreatedMechanics);
    }

    protected void displayMenu(final @NotNull MinigamePlayer mgPlayer, final @NotNull Map<@NotNull Key, @NotNull AGameMechanic> alreadyCreatedMechanics) {
        final @NotNull Menu mainMenu = new Menu(6, getDisplayName(), mgPlayer);
        final @NotNull Menu playerMenu = new Menu(6, getDisplayName(), mgPlayer);
        final @NotNull Menu singlplayerFlagsMenu = new Menu(6, getDisplayName(), mgPlayer);

        int currentPosMainMenu = 0;

        mainMenu.setItem(enabled.getMenuItem(ItemType.PAPER, MgMenuLangKey.MENU_MINIGAME_ENABLED_NAME), currentPosMainMenu);
        mainMenu.setItem(usePermissions.getMenuItem(ItemType.PAPER, MgMenuLangKey.MENU_MINIGAME_USEPERNS_NAME), ++currentPosMainMenu);

        final @NotNull Map<@NotNull String, @NotNull Key> mechanicNames = new LinkedHashMap<>();
        for (final @NotNull IGameMechanicFactory iGameMechanicFactory : GameMechanicRegistry.getAllFactories()) {
            final @NotNull String mechanicName = iGameMechanicFactory.getKey().asMinimalString().replace('_', ' ');
            mechanicNames.put(WordUtils.capitalizeFully(mechanicName), iGameMechanicFactory.getKey());
        }

        final @NotNull MenuItemList<@NotNull String> mechanicTypeMenuItem = new MenuItemList<>(ItemType.ROTTEN_FLESH,
            MgMenuLangKey.MENU_MINIGAME_MECHANIC_NAME, new Callback<>() {

            @Override
            public String getValue() {
                return WordUtils.capitalizeFully(mechanic.key().asMinimalString().replace('_', ' '));
            }

            @Override
            public void setValue(@NotNull String value) {
                final @NotNull Key mechanicKey = mechanicNames.get(value);

                if (!mechanic.key().equals(mechanicKey)) {
                    mechanic = alreadyCreatedMechanics.computeIfAbsent(mechanicKey, key -> GameMechanicRegistry.getMechanicFactory(key).makeNewMechanic(plugin, Minigame.this));

                    if (!mechanic.validTypes().contains(getType())) {
                        type.setFlag(mechanic.validTypes().stream().findFirst().orElse(type.getFlag()));
                    }
                    displayMenu(mgPlayer, alreadyCreatedMechanics); // reopen menu
                }
            }
        }, new ArrayList<>(mechanicNames.keySet()));

        mainMenu.setItem(mechanicTypeMenuItem, ++currentPosMainMenu);

        currentPosMainMenu++;
        final @Nullable AMenuItem mechSettings = getMechanic().displayMechanicSettings(mainMenu);
        if (mechSettings != null) {
            mainMenu.setItem(mechSettings, currentPosMainMenu);
        }

        mainMenu.setItem(new MenuItemList<>(ItemType.PAPER, MgMenuLangKey.MENU_MINIGAME_TYPE_NAME, new Callback<>() {
            @Override
            public MinigameType getValue() {
                return type.getFlag();
            }

            @Override
            public void setValue(final @NotNull MinigameType value) {
                if (value != type.getFlag()) {
                    type.setFlag(value);
                    displayMenu(mgPlayer, alreadyCreatedMechanics); // reopen menu
                }
            }
        }, List.copyOf(mechanic.validTypes())), ++currentPosMainMenu);

        MenuItemComponent cmpntItem = (MenuItemComponent) objective.getMenuItem(ItemType.DIAMOND,
            MgMenuLangKey.MENU_MINIGAME_OBJECTIVEDESCRIPTION_NAME);
        cmpntItem.setAllowNull(true);
        mainMenu.setItem(cmpntItem, ++currentPosMainMenu);

        cmpntItem = (MenuItemComponent) gameTypeName.getMenuItem(ItemType.WRITTEN_BOOK, MgMenuLangKey.MENU_MINIGAME_TYPEDESCRIPTION_NAME);
        cmpntItem.setAllowNull(true);
        mainMenu.setItem(cmpntItem, ++currentPosMainMenu);

        cmpntItem = (MenuItemComponent) displayName.getMenuItem(MenuDisplayTypes.nameType(), MgMenuLangKey.MENU_DISPLAYNAME_NAME);
        cmpntItem.setAllowNull(true);
        mainMenu.setItem(cmpntItem, ++currentPosMainMenu);

        mainMenu.setItem(new MenuItemNewLine(), ++currentPosMainMenu);
        currentPosMainMenu += 9 - currentPosMainMenu % 9; // skip to next line

        currentPosMainMenu++;
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            final AMenuItem scoreMinMenuItem = minScore.getMenuItem(ItemType.STONE_SLAB, MgMenuLangKey.MENU_MINIGAME_SCORE_MIN_NAME);
            mainMenu.setItem(scoreMinMenuItem, currentPosMainMenu);
        }

       currentPosMainMenu++;
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            final AMenuItem scoreMaxMenuItem = maxScore.getMenuItem(ItemType.STONE, MgMenuLangKey.MENU_MINIGAME_SCORE_MAX_NAME);
            mainMenu.setItem(scoreMaxMenuItem, currentPosMainMenu);
        }

        currentPosMainMenu++;
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            final AMenuItem minPlayersMenuItem = minPlayers.getMenuItem(ItemType.STONE_SLAB, MgMenuLangKey.MENU_MINIGAME_PLAYERS_MIN_NAME);
            mainMenu.setItem(minPlayersMenuItem, currentPosMainMenu);
        }

        currentPosMainMenu++;
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            final AMenuItem maxPlayersMenuItem = maxPlayers.getMenuItem(ItemType.STONE, MgMenuLangKey.MENU_MINIGAME_PLAYERS_MAX_NAME);
            mainMenu.setItem(maxPlayersMenuItem, currentPosMainMenu);
        }

        currentPosMainMenu++;
        if (type.getFlag() == MinigameType.SINGLEPLAYER) {
            final MenuItemBoolean SinglePlayerAmountCappedMenuItem = spMaxPlayers.getMenuItem(ItemType.IRON_BARS,
                MgMenuLangKey.MENU_MINIGAME_PLAYERS_SINGLEPLAYER_CAPPED_NAME);
            mainMenu.setItem(SinglePlayerAmountCappedMenuItem, currentPosMainMenu);
        }

        mainMenu.setItem(displayScoreboard.getMenuItem(ItemType.OAK_SIGN, MgMenuLangKey.MENU_MINIGAME_SCOREBOARD_DISPLAY_NAME), ++currentPosMainMenu);

        // placeholder for lobby settings at this pos
        currentPosMainMenu++;

        mainMenu.setItem(new MenuItemNewLine(), ++currentPosMainMenu);
        currentPosMainMenu += 9 - currentPosMainMenu % 9; // skip to next line

        currentPosMainMenu++;
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            final MenuItemTime gamLengthMenuItem = timer.getMenuItem(MenuDisplayTypes.timeType(), MgMenuLangKey.MENU_MINIGAME_TIME_GAMELENGTH_NAME, 0L, null);
            mainMenu.setItem(gamLengthMenuItem, currentPosMainMenu);
        }

        mainMenu.setItem(timerDisplayType.getMenuItem(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_MINIGAME_TIME_DISPLAYTYPE_NAME), ++currentPosMainMenu);

        currentPosMainMenu++;
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            final MenuItemTime startWaitTimeMenuItem = startWaitTime.getMenuItem(MenuDisplayTypes.timeType(), MgMenuLangKey.MENU_MINIGAME_TIME_STARTWAIT_NAME, 3L, null);
            mainMenu.setItem(startWaitTimeMenuItem, currentPosMainMenu);
        }

        mainMenu.setItem(showCompletionTime.getMenuItem(ItemType.PAPER, MgMenuLangKey.MENU_MINIGAME_TIME_SHOWCOMPLETION_NAME), ++currentPosMainMenu);

        currentPosMainMenu++;
        if (type.getFlag() == MinigameType.MULTIPLAYER) {
            final AMenuItem allowLateJoinMenuItem = lateJoin.getMenuItem(ItemType.DEAD_BUSH, MgMenuLangKey.MENU_MINIGAME_ALLOWLATEJOIN_NAME);
            mainMenu.setItem(allowLateJoinMenuItem, currentPosMainMenu);
        }

        mainMenu.setItem(randomizeStart.getMenuItem(ItemType.LIGHT_BLUE_GLAZED_TERRACOTTA, MgMenuLangKey.MENU_MINIGAME_STARTPOINT_RANDOMIZE_NAME,
            MgMenuLangKey.MENU_MINIGAME_STARTPOINT_RANDOMIZE_DESCRIPTION), ++currentPosMainMenu);

        mainMenu.setItem(new MenuItemDisplayWhitelist(ItemType.WHITE_BUNDLE,
            MessageManager.getMessage(MgMenuLangKey.MENU_MINIGAME_WHITELIST_BLOCK_NAME), // Block Whitelist/Blacklist
            MessageManager.getMessageList(MgMenuLangKey.MENU_MINIGAME_WHITELIST_BLOCK_DESCRIPTION_MAIN),
            getRecorderData().getWBBlocks(), getRecorderData().getWhitelistModeCallback(),
            MessageManager.getMessageList(MgMenuLangKey.MENU_MINIGAME_WHITELIST_BLOCK_DESCRIPTION_SECOND)), ++currentPosMainMenu);

        mainMenu.setItem(new MenuItemNewLine(), ++currentPosMainMenu);
        currentPosMainMenu += 9 - currentPosMainMenu % 9; // skip to next line

        // double pack, since the type shows / hides random chance percent
        final int degenChancePos = currentPosMainMenu + 2; // +1 for type at pos before
        final MenuItemInteger randomFloorDegenChanceMenuItem = degenRandomChance.getMenuItem(ItemType.SNOW,
            MessageManager.getMessage(MgMenuLangKey.MENU_MINIGAME_DEGEN_RANDOMCHANCE_NAME),
            MessageManager.getMessageList(MgMenuLangKey.MENU_MINIGAME_DEGEN_RANDOMCHANCE_DESCRIPTION), 1, 100);
        mainMenu.setItem(new MenuItemList<>(ItemType.SNOW_BLOCK, MgMenuLangKey.MENU_MINIGAME_DEGEN_TYPE_NAME,
            MessageManager.getMessageList(MgMenuLangKey.MENU_MINIGAME_DEGEN_TYPE_DESCRIPTION), new Callback<>() {

            @Override
            public FloorDegenerator.DegeneratorType getValue() {
                return degenType.getFlag();
            }

            @Override
            public void setValue(FloorDegenerator.DegeneratorType value) {
                degenType.setFlag(value);

                if (value == FloorDegenerator.DegeneratorType.RANDOM) {
                    mainMenu.setItem(randomFloorDegenChanceMenuItem, degenChancePos);
                } else {
                    mainMenu.removeItem(degenChancePos);
                }
            }

        }, List.of(FloorDegenerator.DegeneratorType.values())), ++currentPosMainMenu);
        if (degenType.getFlag() == FloorDegenerator.DegeneratorType.RANDOM) {
            mainMenu.setItem(randomFloorDegenChanceMenuItem, currentPosMainMenu);
        }
        currentPosMainMenu++; // we already used the pos for degenChance

        mainMenu.addItem(floorDegenTime.getMenuItem(MenuDisplayTypes.timeType(), MgMenuLangKey.MENU_MINIGAME_DEGEN_DELAY_NAME, 1L, null));

        mainMenu.addItem(regenDelay.getMenuItem(MenuDisplayTypes.timeType(), MgMenuLangKey.MENU_MINIGAME_REGENDELAY_NAME,
            MessageManager.getMessageList(MgMenuLangKey.MENU_MINIGAME_REGENDELAY_DESCRIPTION), 0L, null));

        mainMenu.addItem(new MenuItemNewLine());
        currentPosMainMenu += 9 - currentPosMainMenu % 9; // skip to next line

        mainMenu.addItem(new MenuItemPage(MenuDisplayTypes.playerType(), MgMenuLangKey.MENU_PLAYERSETTINGS_NAME, playerMenu));

        mainMenu.addItem(canSpectateFly.getMenuItem(ItemType.WHITE_HARNESS, MgMenuLangKey.MENU_MINIGAME_ALLOWSPECTATORFLY_NAME));

        mainMenu.addItem(randomizeChests.getMenuItem(ItemType.CHEST, MgMenuLangKey.MENU_MINIGAME_RANDOMCHESTS_NAME,
            MgMenuLangKey.MENU_MINIGAME_RANDOMCHESTS_DESCRIPTION));

        mainMenu.addItem(minChestRandom.getMenuItem(ItemType.STONE_SLAB, MgMenuLangKey.MENU_MINIGAME_RANDOMCHESTS_MIN_NAME,
            MgMenuLangKey.MENU_MINIGAME_RANDOMCHESTS_MIN_DESCRIPTION, 0, null));

        mainMenu.addItem(maxChestRandom.getMenuItem(ItemType.STONE, MgMenuLangKey.MENU_MINIGAME_RANDOMCHESTS_MAX_NAME,
            MgMenuLangKey.MENU_MINIGAME_RANDOMCHESTS_MAX_DESCRIPTION, 0, null));

        mainMenu.addItem(new MenuItemStatisticsSettings(MenuDisplayTypes.statistics(), MgMenuLangKey.MENU_MINIGAME_STATISTIC_NAME, this));
        mainMenu.addItem(playerRecorderActivate.getMenuItem(ItemType.COMMAND_BLOCK, MgMenuLangKey.MENU_PLAYER_BLOCK_RECORDER));

        mainMenu.addItem(new MenuItemNewLine());
        currentPosMainMenu += 9 - currentPosMainMenu % 9; // skip to next line

        mainMenu.setItem(new MenuItemSaveMinigame(MenuDisplayTypes.saveType(),
            MessageManager.getMessage(MgMenuLangKey.MENU_MINIGAME_SAVE_NAME,
                Placeholder.component(MinigamePlaceHolderKey.MINIGAME.getKey(), getDisplayName())),
            this), mainMenu.getSize() - 1);

        //----------------------//
        //Minigame Player Settings
        //----------------------//
        final @NotNull List<@NotNull AMenuItem> itemsPlayer = new ArrayList<>(20);
        itemsPlayer.add(defaultGamemode.getMenuItem(ItemType.CRAFTING_TABLE, MgMenuLangKey.MENU_PLAYERSETTINGS_GAMEMODE_NAME));
        itemsPlayer.add(allowEnderPearls.getMenuItem(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_PLAYERSETTINGS_ENDERPERLS_NAME));
        itemsPlayer.add(allowThirdPartyTeleportation.getMenuItem(ItemType.COMMAND_BLOCK, MgMenuLangKey.MENU_PLAYERSETTINGS_THIRDPARTY_TELEPORTATION_NAME));
        itemsPlayer.add(itemDrops.getMenuItem(ItemType.DIAMOND_SWORD, MgMenuLangKey.MENU_PLAYERSETTINGS_DROP_ITEM_NAME));
        itemsPlayer.add(keepInventory.getMenuItem(ItemType.ZOMBIE_HEAD, MgMenuLangKey.MENU_PLAYERSETTINGS_KEEPINVENTORY_NAME));
        itemsPlayer.add(deathDrops.getMenuItem(ItemType.SKELETON_SKULL, MgMenuLangKey.MENU_PLAYERSETTINGS_DROP_DEATH_NAME));
        itemsPlayer.add(itemPickup.getMenuItem(ItemType.DIAMOND, MgMenuLangKey.MENU_PLAYERSETTINGS_ITEMPICKUP_NAME));
        itemsPlayer.add(blockBreak.getMenuItem(ItemType.DIAMOND_PICKAXE, MgMenuLangKey.MENU_PLAYERSETTINGS_BLOCK_BREAK_NAME));
        itemsPlayer.add(blockPlace.getMenuItem(ItemType.STONE, MgMenuLangKey.MENU_PLAYERSETTINGS_BLOCK_PLACE_NAME));
        itemsPlayer.add(blocksDrop.getMenuItem(ItemType.COBBLESTONE, MgMenuLangKey.MENU_PLAYERSETTINGS_BLOCK_DROPS_NAME));
        itemsPlayer.add(lives.getMenuItem(ItemType.APPLE, MgMenuLangKey.MENU_PLAYERSETTINGS_LIVES_NAME));
        itemsPlayer.add(paintBallMode.getMenuItem(ItemType.SNOWBALL, MgMenuLangKey.MENU_PLAYERSETTINGS_PAINTBALL_MODE_NAME));
        itemsPlayer.add(paintBallDamage.getMenuItem(ItemType.ARROW, MgMenuLangKey.MENU_PLAYERSETTINGS_PAINTBALL_DAMAGE_NAME, 1, null));
        itemsPlayer.add(unlimitedAmmo.getMenuItem(ItemType.SNOW_BLOCK, MgMenuLangKey.MENU_PLAYERSETTINGS_UNLIMITEDAMMO_NAME));
        if (getType() == MinigameType.MULTIPLAYER) {
            itemsPlayer.add(allowMPCheckpoints.getMenuItem(ItemType.OAK_SIGN, MgMenuLangKey.MENU_PLAYERSETTINGS_CHECKPOINT_MULTIPLAYER_NAME,
                MgMenuLangKey.MENU_MINIGAME_MULTIPLAYERONLY_DESCRIPTION));
        }
        if (getType() == MinigameType.SINGLEPLAYER) {
            itemsPlayer.add(saveCheckpoints.getMenuItem(ItemType.OAK_SIGN, MgMenuLangKey.MENU_PLAYERSETTINGS_CHECKPOINT_SAVE_NAME,
                MgMenuLangKey.MENU_MINIGAME_SINGLEPLAYERONLY_DESCRIPTION));
            itemsPlayer.add(new MenuItemPage(ItemType.OAK_SIGN, MgMenuLangKey.MENU_PLAYERSETTINGS_SINGLEPLAYERFLAG_NAME,
                MessageManager.getMessageList(MgMenuLangKey.MENU_MINIGAME_SINGLEPLAYERONLY_DESCRIPTION), singlplayerFlagsMenu));
        }
        itemsPlayer.add(allowFlight.getMenuItem(ItemType.FEATHER, MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ALLOW_NAME,
            MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ALLOW_DESCRIPTION));
        itemsPlayer.add(enableFlight.getMenuItem(ItemType.FEATHER, MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ENABLE_NAME,
            MgMenuLangKey.MENU_PLAYERSETTINGS_FLIGHT_ENABLE_DESCRIPTION));
        itemsPlayer.add(allowDragonEggTeleport.getMenuItem(ItemType.DRAGON_EGG, MgMenuLangKey.MENU_PLAYERSETTINGS_DRAGONEGGTELEPORT_NAME));
        if (getType() == MinigameType.SINGLEPLAYER) {
            itemsPlayer.add(showPlayerBroadcasts.getMenuItem(ItemType.PAPER, MgMenuLangKey.MENU_PLAYERSETTINGS_BROADCASTS_JOINEXIT_NAME,
                MgMenuLangKey.MENU_PLAYERSETTINGS_BROADCASTS_JOINEXIT_DESCRIPTION));
        }
        if (getMechanic().key().equals(GameMechanicRegistry.MgDefaultMechanic.CTF.getKey())) {
            itemsPlayer.add(showCTFBroadcasts.getMenuItem(ItemType.PAPER, MgMenuLangKey.MENU_PLAYERSETTINGS_BROADCASTS_CTF_NAME,
                MgMenuLangKey.MENU_PLAYERSETTINGS_BROADCASTS_CTF_DESCRIPTION));
        }
        if (getType() == MinigameType.MULTIPLAYER) {
            itemsPlayer.add(friendlyFireSplashPotions.getMenuItem(ItemType.SPLASH_POTION,
                MgMenuLangKey.MENU_PLAYERSETTINGS_FRIENDLYFIRE_SPLASH_NAME));
            itemsPlayer.add(friendlyFireLingeringPotions.getMenuItem(ItemType.LINGERING_POTION,
                MgMenuLangKey.MENU_PLAYERSETTINGS_FRIENDLYFIRE_LINGERING_NAME));
        }
        playerMenu.addItems(itemsPlayer);
        playerMenu.setItem(new MenuItemBack(mainMenu), mainMenu.getSize() - 9);

        //--------------//
        //Minigame Flags//
        //--------------//
        if (getType() == MinigameType.SINGLEPLAYER) {
            final @NotNull List<@NotNull AMenuItem> itemsFlags = new ArrayList<>(getSinglePlayerFlags().size());
            for (final @NotNull String flag : getSinglePlayerFlags()) {
                itemsFlags.add(new MenuItemSingleplayerFlag(ItemType.OAK_SIGN, flag, this::removeSinglePlayerFlag));
            }
            singlplayerFlagsMenu.setItem(new MenuItemBack(playerMenu), singlplayerFlagsMenu.getSize() - 9);
            singlplayerFlagsMenu.setItem(new MenuItemAddSingleplayerFlag(MenuDisplayTypes.createType(), MgMenuLangKey.MENU_FLAGADD_NAME,
                this), singlplayerFlagsMenu.getSize() - 1);
            singlplayerFlagsMenu.addItems(itemsFlags);
        }

        for (final @NotNull AMinigameModule mod : getModules()) {
           mod.addEditMenuOptions(mainMenu);
        }

        mainMenu.displayMenu();
    }

    public @NotNull ScoreboardDisplayManger getScoreboardData() {
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
        minigameSaveRoot.removeChild(name);

        boolean allSuccess = true;

        final @NotNull CommentedConfigurationNode cfg = minigameSaveRoot.node(name);
        for (final @NotNull AMinigameModule module : getModules()) {
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

        if (!mechanic.useSeparateConfig()) {
            try {
                mechanic.save(cfg);
            } catch (final @NotNull SerializationException e) {
                plugin.getComponentLogger().error("Couldn't save mechanic " + mechanic.key() + " of Minigame " + getName() + ". Data loss is imminent!", e);
                allSuccess = false; // we failed. Let's try to save the other data anyway to keep data loss as small as possible!
            }
        } else {
            final @NotNull Key mechanicKey = mechanic.key();
            final @NotNull Path mechanicPath;
            if (mechanicKey.namespace().equals(plugin.namespace())) {
                mechanicPath = Path.of(mechanicKey.value());
            } else {
                mechanicPath = Path.of(mechanicKey.namespace(), mechanicKey.value());
            }

            final @NotNull MinigameSave mechanicSave = MinigameSave.forMinigameData(this, mechanicPath);
            final @NotNull CommentedConfigurationNode mechanicSaveRoot;
            try {
                mechanicSaveRoot = mechanicSave.getConfigRoot();

                mechanicSaveRoot.removeChild(name);
                try {
                    mechanic.save(mechanicSaveRoot.node(name));
                    mechanicSave.saveConfig();
                } catch (final @NotNull IOException e) {
                    plugin.getComponentLogger().error("Couldn't save separate config file for mechanic " + mechanic.key() + " of Minigame " + getName() + " located at " + mechanicPath + ". Data loss is imminent!", e);
                    allSuccess = false; // we failed. Let's try to save the other data anyway to keep data loss as small as possible!
                }
            } catch (final @NotNull ConfigurateException e) {
                plugin.getComponentLogger().error("Couldn't obtain mechanic config file of " + mechanic.key() + " for Minigame " + getName() + "to safe it. Data loss is imminent!", e);
                allSuccess = false; // we failed. Let's try to save the other data anyway to keep data loss as small as possible!
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

        for (final @NotNull AMinigameModule module : getModules()) {
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

        if (!mechanic.useSeparateConfig()) {
            try {
                mechanic.load(cfg);
            } catch (ConfigurateException e) {
                plugin.getComponentLogger().error("Couldn't load Minigames config file of mechanic " + mechanic.key() + " for minigame " + getName() + ". Some thinks may stop working.", e);
                allSuccess = false;
            }
        } else {
            final @NotNull Key mechanicKey = mechanic.key();
            final @NotNull Path mechanicPath;
            if (mechanicKey.namespace().equals(plugin.namespace())) {
                mechanicPath = Path.of(mechanicKey.value());
            } else {
                mechanicPath = Path.of(mechanicKey.namespace(), mechanicKey.value());
            }

            final @NotNull MinigameSave modsave = MinigameSave.forMinigameData(this, mechanicPath);
            try {
                mechanic.load(modsave.getConfigRoot().node(name));
            } catch (final @NotNull ConfigurateException e) {
                plugin.getComponentLogger().error("Couldn't load Minigames config file of mechanic " + mechanic.key() + " for minigame " + getName() + ". Some thinks may stop working.", e);
                allSuccess = false;
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

        final @NotNull CompletableFuture<Map<MinigameStat, StatSettings>> settingsFuture = plugin.getBackend().loadStatSettings(this);
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

    @Override
    public @Nullable ScriptReference resolveReference(final @NotNull String name) {
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

    @Override
    public @NotNull Set<@NotNull String> getReferenceKeys() {
        return Set.of("players", "teams", "name", "displayname");
    }

    @Override
    public @NotNull String getAsString() {
        return getName();
    }
}
