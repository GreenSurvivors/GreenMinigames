package au.com.mineauz.minigames;

import au.com.mineauz.minigames.backend.BackendManager;
import au.com.mineauz.minigames.commands.CommandDispatcher;
import au.com.mineauz.minigames.display.DisplayManager;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.gametypes.MultiplayerType;
import au.com.mineauz.minigames.gametypes.SingleplayerType;
import au.com.mineauz.minigames.managers.MinigameManager;
import au.com.mineauz.minigames.managers.MinigamePlayerManager;
import au.com.mineauz.minigames.managers.PlaceHolderManager;
import au.com.mineauz.minigames.managers.ResourcePackManager;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.mechanics.TreasureHuntMechanic;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.ModuleFactory;
import au.com.mineauz.minigames.minigame.modules.RewardsModule;
import au.com.mineauz.minigames.minigame.modules.loadout.LoadoutModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.ResourcePack;
import au.com.mineauz.minigames.presets.PresetLoader;
import au.com.mineauz.minigames.recorder.BasicRecorder;
import au.com.mineauz.minigames.signs.SignBase;
import au.com.mineauz.minigames.stats.MinigameStatistics;
import au.com.mineauz.minigames.stats.StatisticValueField;
import au.com.mineauz.minigames.stats.StoredGameStats;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.milkbowl.vault.economy.Economy;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.MultiLineChart;
import org.bstats.charts.SimpleBarChart;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Minigames extends JavaPlugin { // todo move a lot of these assignments to the constructor
    private static @Deprecated @MonotonicNonNull Minigames plugin;
    private Economy econ;
    private SignBase minigameSigns;
    private ComparableVersion pluginVersion;
    private final @NotNull StartUpLogHandler startUpHandler;
    private final @NotNull MessageManager messageManager = new MessageManager();
    private DisplayManager displayManager;
    private ResourcePackManager resourcePackManager;
    private MinigamePlayerManager playerManager;
    private MinigameManager minigameManager;
    private PlaceHolderManager placeHolderManager;
    private CommandDispatcher commandDispatcher;
    private boolean debug;
    private boolean hasPlaceholderAPI = false;
    private BackendManager backend;
    private Metrics metrics;

    public Minigames() {
        startUpHandler = new StartUpLogHandler();
        plugin = this;
    }

    @Override
    public void onDisable() {
        if (getPlugin() == null) {
            getComponentLogger().info("Minigames is disabled");
            return;
        }

        boolean allSuccess = true;

        for (final @NotNull Player player : getServer().getOnlinePlayers()) {
            if (playerManager.getMinigamePlayer(player).isInMinigame()) {
                playerManager.quitMinigame(playerManager.getMinigamePlayer(player), true);
            }
        }
        for (final @NotNull Minigame minigame : minigameManager.getAllMinigames().values()) {
            if (minigame.getType() == MinigameType.GLOBAL && minigame.getMechanic() instanceof TreasureHuntMechanic treasureHuntMechanic
                && minigame.isEnabled()) { // todo move this into the Treasure mechanic

                if (minigame.getMinigameTimer() != null) {
                    minigame.getMinigameTimer().stopTimer();
                }
                treasureHuntMechanic.removeTreasure();
            }
        }
        for (final @NotNull Minigame mg : minigameManager.getAllMinigames().values()) {
            allSuccess &= mg.saveMinigame();
        }

        backend.shutdown();
        playerManager.saveDeniedCommands();

        try {
            LoadoutModule.saveGlobalLoadouts();
        } catch (final @NotNull IOException e) {
            getComponentLogger().error("Couldn't save global loadouts. Data loss is imminent!", e);
            allSuccess = false;
        }
        try {
            minigameManager.saveRewardSigns();
        } catch (final @NotNull IOException e) {
            getComponentLogger().error("Couldn't save reward signs. Data loss is imminent!", e);
            allSuccess = false;
        }
        try {
            resourcePackManager.saveResources();
        } catch (final @NotNull IOException e) {
            getComponentLogger().error("Couldn't save resources. Data loss is imminent!", e);
            allSuccess = false;
        }

        if (allSuccess) {
            getComponentLogger().info(getPluginMeta().getName() + " successfully disabled.");
        }
    }

    @Override
    public void onEnable() {
        getLogger().addHandler(startUpHandler); // todo this currently does nothing, since we are using the Component logger
        ComponentLogger logger = getComponentLogger();
        pluginVersion = new ComparableVersion(getPluginMeta().getVersion());
        try {
            if (checkVersion()) return;
            ConfigurationSerialization.registerClass(ResourcePack.class); // todo
            MessageManager.registerCoreLanguage();
            PresetLoader.initPresets(this);
            setupMinigames();
            if (!setupEconomy()) {
                logger.info("No Vault plugin found! You may only reward items.");
            }
            backend = new BackendManager(getComponentLogger());
            if (!backend.initialize(getConfig())) {
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            getConfig().options().copyDefaults(true);
            saveConfig();
            playerManager.loadDeniedCommands();
            LoadoutModule.setupGlobalLoadOuts(this);
            minigameSigns = new SignBase();
            minigameManager.loadRewardSigns();

            commandDispatcher = new CommandDispatcher();
            getServer().getCommandMap().register(getPluginMeta().getName().toLowerCase(Locale.ENGLISH), commandDispatcher);

            for (final Player player : getServer().getOnlinePlayers()) {
                playerManager.addMinigamePlayer(player);
            }

            try {
                initMetrics();
            } catch (final IllegalStateException | NoClassDefFoundError | NoSuchMethodError | ExceptionInInitializerError e) {
                logger.info("Metrics will not be available(enable debug for more details): " + e.getMessage());
                if (debug) {
                    logger.info("", e);
                }
            }

            logger.info(getPluginMeta().getName() + " successfully enabled.");
            hookPlaceHolderApi();
        } catch (final Exception e) {
            plugin = null;
            logger.error("Failed to enable Minigames " + getPluginMeta().getVersion() + ": ", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
        getLogger().removeHandler(startUpHandler);
    }

    /// returns true if the check failed and the plugin should get disabled
    private boolean checkVersion() {
        final ComparableVersion paperVersion = new ComparableVersion(getPluginMeta().getAPIVersion());
        final ComparableVersion serverVersion = new ComparableVersion(getServer().getMinecraftVersion());
        switch (paperVersion.compareTo(serverVersion)) {
            case -1 -> {
                getComponentLogger().warn("""
                        This version of Minigames ({}) is designed for Paper Version: {}
                        Your version is newer: {}
                        Please check for an update!""",
                    pluginVersion.getCanonical(), paperVersion.getCanonical(), Bukkit.getBukkitVersion());
            }
            case 0 -> {
            }
            case 1 -> {
                if (!getConfig().getBoolean("forceload", true)) {
                    getComponentLogger().warn("""
                            This version of Minigames ({}) is designed for Paper Version: {}
                            Your version is {}
                            Bypass this by setting forceload: true in the config.
                            DISABLING MINIGAMES....""",
                        pluginVersion.getCanonical(), paperVersion.getCanonical(), Bukkit.getVersion());
                    plugin = null;
                    onDisable();
                    return true;
                } else {
                    getComponentLogger().warn("""
                            Version incompatible - Force Loading Minigames.
                            This version of Minigames ({}) is designed for Bukkit Version: {}
                            Your version is {}""",
                        pluginVersion.getCanonical(), paperVersion.getCanonical(), Bukkit.getBukkitVersion());
                }
            }
        }

        return false;
    }

    private void setupMinigames() {
        minigameManager = new MinigameManager(this);
        playerManager = new MinigamePlayerManager(this);
        displayManager = new DisplayManager(this);

        resourcePackManager = new ResourcePackManager(this);
        resourcePackManager.initialize();
        minigameManager.addMinigameType(new SingleplayerType());
        minigameManager.addMinigameType(new MultiplayerType());

        getServer().getPluginManager().registerEvents(new Events(), this);
        //always active recorder, don't get confused with RegenRecorder, that is only active, if the minigame has a regen area
        getServer().getPluginManager().registerEvents(new BasicRecorder(), this);

        saveDefaultConfig();

        try {
            final List<String> mgs;
            if (getConfig().contains("minigames")) {
                mgs = getConfig().getStringList("minigames");
            } else {
                mgs = Collections.emptyList();
            }

            debug = getConfig().getBoolean("debug", false);

            if (!mgs.isEmpty()) {
                for (final @NotNull String minigameName : mgs) {
                    final @NotNull Minigame game = new Minigame(minigameName);
                    if (game.loadMinigame()) {
                        minigameManager.addMinigame(game);
                    }
                }
            }
        } catch (final Exception e) {
            getComponentLogger().error("Failed to load config!", e);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        final RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    private void initMetrics() {
        metrics = new Metrics(this, 1190);
        final MultiLineChart chart = new MultiLineChart("Players_in_Minigames", () -> {
            final Map<String, Integer> result = new HashMap<>();
            result.put("Total_Players", playerManager.getAllMinigamePlayers().size());
            for (final MinigamePlayer pl : playerManager.getAllMinigamePlayers()) {
                if (pl.isInMinigame()) {
                    int count = result.getOrDefault(pl.getMinigame().getType().getName(), 0);
                    result.put(pl.getMinigame().getType().getName(), count + 1);
                }
            }
            return result;
        });
        final SimpleBarChart barChart = new SimpleBarChart("Modules_v_Servers", () -> {
            final Map<String, Integer> result = new HashMap<>();
            for (final ModuleFactory module : minigameManager.getModules()) {
                result.put(module.getKey().asString(), 1);
            }
            return result;
        });
        metrics.addCustomChart(chart);
        metrics.addCustomChart(barChart);
    }

    @SuppressWarnings("LoggingSimilarMessage")
    private void hookPlaceHolderApi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            hasPlaceholderAPI = true;
            getComponentLogger().info("--------------------");
            getComponentLogger().info("Hooking PlaceHolder API");
            placeHolderManager = new PlaceHolderManager(this);
            placeHolderManager.register();
            getComponentLogger().info("Adding Placeholders for " + getMinigameManager().getAllMinigames().size() + " games");
            for (Map.Entry<String, Minigame> game : getMinigameManager().getAllMinigames().entrySet()) {
                getComponentLogger().trace("Adding Placeholders for " + game.getKey());
                placeHolderManager.addGameIdentifiers(game.getValue());
            }
            getComponentLogger().info("PlaceHolders: " + placeHolderManager.getRegisteredPlaceHolders());
            getComponentLogger().info("--------------------");
        }
    }

    public boolean hasEconomy() {
        return econ != null;
    }

    public @Nullable Economy getEconomy() {
        return econ;
    }

    public void addMetric(final @NotNull CustomChart chart) {
        metrics.addCustomChart(chart);
    }

    public ComparableVersion getVersion() {
        return pluginVersion;
    }

    @Deprecated
    public static @MonotonicNonNull Minigames getPlugin() {
        return plugin;
    }

    public @NotNull MessageManager getMessageManager() {
        return messageManager;
    }

    public @NotNull String getStartupLog() {
        return startUpHandler.getNormalLog();
    }

    public @NotNull String getStartupExceptionLog() {
        return startUpHandler.getExceptionLog();
    }

    public PlaceHolderManager getPlaceHolderManager() {
        return placeHolderManager;
    }

    public CommandDispatcher getCommandDispatcher() {
        return commandDispatcher;
    }

    public BackendManager getBackend() {
        return backend;
    }

    public SignBase getMinigameSigns() {
        return minigameSigns;
    }

    public void queueStatSave(final @NotNull StoredGameStats saveData, final boolean winner) {
        MessageManager.debugMessage("Scheduling SQL data save for " + saveData);

        final CompletableFuture<Long> winCountFuture = backend.loadSingleStat(saveData.getMinigame(), MinigameStatistics.Wins, StatisticValueField.Total, saveData.getPlayer().getUUID());
        backend.saveStats(saveData);

        winCountFuture.thenApply(winCount -> Bukkit.getScheduler().runTask(Minigames.getPlugin(), () -> {
            final @NotNull Minigame minigame = saveData.getMinigame();
            final @NotNull MinigamePlayer player = saveData.getPlayer();

            // Do rewards
            if (winner) {
                RewardsModule.getModule(minigame).awardPlayer(player, saveData, minigame, winCount == 0);
            } else {
                RewardsModule.getModule(minigame).awardPlayerOnLoss(player, saveData, minigame);
            }
        }));
    }

    public void toggleDebug() {
        debug = !debug;
        backend.toggleDebug();
        if (backend.isDebugging() && !debug) {
            backend.toggleDebug();
        }
    }

    public boolean isDebugging() {
        return debug;
    }

    public MinigamePlayerManager getPlayerManager() {
        return playerManager;
    }

    public MinigameManager getMinigameManager() {
        return minigameManager;
    }

    public ResourcePackManager getResourcePackManager() {
        return resourcePackManager;
    }

    public boolean includesPlaceholderAPI() {
        return hasPlaceholderAPI;
    }

    public DisplayManager getDisplayManager() {
        return displayManager;
    }
}
