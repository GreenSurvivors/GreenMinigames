package au.com.mineauz.minigames;

import au.com.mineauz.minigames.backend.BackendManager;
import au.com.mineauz.minigames.commands.CommandDispatcher;
import au.com.mineauz.minigames.config.MinigameSave;
import au.com.mineauz.minigames.display.DisplayManager;
import au.com.mineauz.minigames.gametypes.MinigameType;
import au.com.mineauz.minigames.gametypes.MultiplayerType;
import au.com.mineauz.minigames.gametypes.SingleplayerType;
import au.com.mineauz.minigames.managers.MinigameManager;
import au.com.mineauz.minigames.managers.MinigamePlayerManager;
import au.com.mineauz.minigames.managers.PlaceHolderManager;
import au.com.mineauz.minigames.managers.ResourcePackManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.mechanics.TreasureHuntMechanic;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.modules.ModuleFactory;
import au.com.mineauz.minigames.minigame.modules.RewardsModule;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.objects.ResourcePack;
import au.com.mineauz.minigames.recorder.BasicRecorder;
import au.com.mineauz.minigames.signs.SignBase;
import au.com.mineauz.minigames.stats.MinigameStatistics;
import au.com.mineauz.minigames.stats.StatisticValueField;
import au.com.mineauz.minigames.stats.StoredGameStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.milkbowl.vault.economy.Economy;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.CustomChart;
import org.bstats.charts.MultiLineChart;
import org.bstats.charts.SimpleBarChart;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class Minigames extends JavaPlugin {
    private static final Pattern COMPILE = Pattern.compile("-?[0-9]+");
    private static ComponentLogger componentLogger = null;
    private static Minigames plugin;
    private static Economy econ;
    private static SignBase minigameSigns;
    private static ComparableVersion VERSION;
    private static ComparableVersion PAPER_VERSION;
    private final @NotNull StartUpLogHandler startUpHandler;
    private static final MinigameMessageManager minigameMessageManager = new MinigameMessageManager();
    public DisplayManager display;
    private ResourcePackManager resourceManager;
    private MinigamePlayerManager playerManager;
    private MinigameManager minigameManager;
    private PlaceHolderManager placeHolderManager;
    private CommandDispatcher disp;
    private boolean debug;
    private boolean hasPAPI = false;
    private long lastUpdateCheck;
    private BackendManager backend;
    private Metrics metrics;

    public Minigames() {
        super();
        startUpHandler = new StartUpLogHandler();
    }

    public static ComparableVersion getVERSION() {
        return VERSION;
    }

    public static Minigames getPlugin() {
        return plugin;
    }

    public static @NotNull ComponentLogger getCmpnntLogger() {
        if (Minigames.componentLogger == null) {
            Minigames.componentLogger = Minigames.getPlugin().getComponentLogger();
        }

        return Minigames.componentLogger;
    }

    @VisibleForTesting
    public static MinigameMessageManager getMinigameMessageManager () {
        return minigameMessageManager;
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
        return disp;
    }

    public void onDisable() {
        if (getPlugin() == null) {
            getComponentLogger().info("Minigames is disabled");
            return;
        }

        for (final Player p : getServer().getOnlinePlayers()) {
            if (playerManager.getMinigamePlayer(p).isInMinigame()) {
                playerManager.quitMinigame(playerManager.getMinigamePlayer(p), true);
            }
        }
        for (final Minigame minigame : minigameManager.getAllMinigames().values()) {
            if (minigame.getType() == MinigameType.GLOBAL &&
                    "treasure_hunt".equals(minigame.getMechanicName())
                    && minigame.isEnabled()) {
                if (minigame.getMinigameTimer() != null) {
                    minigame.getMinigameTimer().stopTimer();
                }
                TreasureHuntMechanic.removeTreasure(minigame);
            }
        }
        for (final Minigame mg : minigameManager.getAllMinigames().values()) {
            mg.saveMinigame();
        }

        backend.shutdown();
        playerManager.saveDeniedCommands();

        final MinigameSave globalLoadouts = new MinigameSave("globalLoadouts");
        Configuration globalConfig = globalLoadouts.getConfig();
        if (minigameManager.hasLoadouts()) {
            for (final PlayerLoadout loadout : minigameManager.getGlobalLoadouts()) {
                char globalPathSeparator = globalConfig.options().pathSeparator();

                for (final Integer slot : loadout.getItemSlots()) {
                    globalConfig.set(loadout.getName() + globalPathSeparator + slot, loadout.getItem(slot));
                }
                if (!loadout.getAllPotionEffects().isEmpty()) {
                    for (final PotionEffect eff : loadout.getAllPotionEffects()) {
                        globalConfig.set(loadout.getName() +
                                globalPathSeparator + "potions" +
                                globalPathSeparator + eff.getType().getKey().getKey() +
                                globalPathSeparator + "amp", eff.getAmplifier());
                        globalConfig.set(loadout.getName() +
                                globalPathSeparator + "potions" +
                                globalPathSeparator + eff.getType().getKey().getKey() +
                                globalPathSeparator + "dur", eff.getDuration());
                    }
                } else {
                    globalConfig.set(loadout.getName() + globalPathSeparator + "potions", null);
                }
                if (loadout.getUsePermissions()) {
                    globalConfig.set(loadout.getName() + globalPathSeparator + "usepermissions", true);
                } else {
                    globalConfig.set(loadout.getName() + globalPathSeparator + "usepermissions", null);
                }
            }
        } else {
            globalConfig.set("globalloadouts", null);
        }
        globalLoadouts.saveConfig();
        minigameManager.saveRewardSigns();
        resourceManager.saveResources();
        getCmpnntLogger().info(getPluginMeta().getName() + " successfully disabled.");
    }

    public void onEnable() {
        getLogger().addHandler(startUpHandler);
        ComponentLogger logger = getComponentLogger();
        try {
            plugin = this;
            switch (checkVersion()) {
                case -1 -> {
                    logger.warn("This version of Minigames (" + VERSION.getCanonical() + ") is designed for Paper Version: " + PAPER_VERSION.getCanonical());
                    logger.warn("Your version is newer: " + Bukkit.getBukkitVersion());
                    logger.warn("Please check for an update!");
                }
                case 0 -> {
                }
                case 1 -> {
                    if (!getConfig().getBoolean("forceload", true)) {
                        logger.warn("This version of Minigames (" + VERSION.getCanonical() + ") " +
                                "is designed for Paper Version: " + PAPER_VERSION.getCanonical());
                        logger.warn("Your version is " + Bukkit.getVersion());
                        logger.warn(" Bypass this by setting forceload: true in the config");

                        logger.warn("DISABLING MINIGAMES....");
                        plugin = null;
                        onDisable();
                        return;
                    } else {
                        logger.warn("Version incompatible - Force Loading Minigames.");
                        logger.warn("This version of Minigames (" + VERSION.getCanonical() + ") " +
                                "is designed for Bukkit Version: " + PAPER_VERSION.getCanonical());
                        logger.warn("Your version is " + Bukkit.getBukkitVersion());
                    }
                }
            }
            ConfigurationSerialization.registerClass(ResourcePack.class);
            MinigameMessageManager.registerCoreLanguage();
            loadPresets();
            setupMinigames();
            if (!setupEconomy()) {
                getLogger().info("No Vault plugin found! You may only reward items.");
            }
            backend = new BackendManager(getComponentLogger());
            if (!backend.initialize(getConfig())) {
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
            getConfig().options().copyDefaults(true);
            saveConfig();
            //        playerManager.loadDCPlayers();
            playerManager.loadDeniedCommands();
            setupLoadOuts();
            minigameSigns = new SignBase();
            minigameManager.loadRewardSigns();

            disp = new CommandDispatcher();
            getServer().getCommandMap().register(getPluginMeta().getName().toLowerCase(Locale.ENGLISH), disp);

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

    private void setupLoadOuts() {
        final MinigameSave globalLoadouts = new MinigameSave("globalLoadouts");
        Configuration globalConfig = globalLoadouts.getConfig();
        char globalPathSeparator = globalConfig.options().pathSeparator();

        final Set<String> keys = globalConfig.getKeys(false);
        for (final String loadoutName : keys) {
            minigameManager.addGlobalLoadout(loadoutName);
            ConfigurationSection loadOutSection = globalConfig.getConfigurationSection(loadoutName);
            if (loadOutSection != null) {
                final Set<String> items = loadOutSection.getKeys(false);
                for (final String slot : items) {
                    if (COMPILE.matcher(slot).matches()) {
                        minigameManager.getLoadout(loadoutName).addItem(globalConfig.getItemStack(loadoutName + '.' + slot), Integer.parseInt(slot));
                    }
                }
            }
            if (globalConfig.contains(loadoutName + globalPathSeparator + "potions")) {
                ConfigurationSection potionLoadOutSection = globalConfig.getConfigurationSection(loadoutName + globalPathSeparator + "potions");
                if (potionLoadOutSection != null) {
                    final Set<String> pots = potionLoadOutSection.getKeys(false);
                    for (final String eff : pots) {
                        PotionEffectType type = Registry.EFFECT.get(NamespacedKey.fromString(eff));
                        if (type != null) {
                            final PotionEffect effect = new PotionEffect(type,
                                    globalConfig.getInt(loadoutName + globalPathSeparator + "potions" + globalPathSeparator + eff + globalPathSeparator + "dur"),
                                    globalConfig.getInt(loadoutName + globalPathSeparator + "potions" + globalPathSeparator + eff + globalPathSeparator + "amp"));
                            minigameManager.getLoadout(loadoutName).addPotionEffect(effect);
                        }
                    }
                }
            }
            if (globalConfig.contains(loadoutName + globalPathSeparator + "usepermissions")) {
                minigameManager.getLoadout(loadoutName).setUsePermissions(globalConfig.getBoolean(loadoutName + globalPathSeparator + "usepermissions"));
            }
        }
    }

    private void loadPresets() {
        final String prespath = getDataFolder() + File.separator + "presets" + File.separator;
        final String[] presets = {"spleef", "lms", "ctf", "infection"};
        File pres;
        for (String preset : presets) {
            pres = new File(prespath + preset + ".yml");
            if (!pres.exists()) {
                saveResource("presets" + File.separator + preset + ".yml", false);
            }
        }
    }

    private void setupMinigames() {
        minigameManager = new MinigameManager();
        playerManager = new MinigamePlayerManager();
        display = new DisplayManager();

        resourceManager = new ResourcePackManager();
        final MinigameSave resources = new MinigameSave("resources");
        minigameManager.addConfigurationFile("resources", resources.getConfig());
        resourceManager.initialize(resources);
        minigameManager.addMinigameType(new SingleplayerType());
        minigameManager.addMinigameType(new MultiplayerType());

        final MinigameSave completion = new MinigameSave("completion");
        minigameManager.addConfigurationFile("completion", completion.getConfig());

        getServer().getPluginManager().registerEvents(new Events(), this);
        //always active recorder, don't get confused with RegenRecorder, that is only active, if the minigame has a regen area
        getServer().getPluginManager().registerEvents(new BasicRecorder(), this);

        try {
            getConfig().load(getDataFolder() + File.separator + "config.yml");
            List<String> mgs = new ArrayList<>();
            if (getConfig().contains("minigames")) {
                mgs = getConfig().getStringList("minigames");
            }
            debug = getConfig().getBoolean("debug", false);
            final List<String> allMGS = new ArrayList<>(mgs);

            if (!mgs.isEmpty()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    for (final String minigame : allMGS) {
                        final Minigame game = new Minigame(minigame);
                        try {
                            game.loadMinigame();
                            minigameManager.addMinigame(game);
                        } catch (final Exception e) {
                            getComponentLogger().error(Component.text("Failed to load \"" + minigame + "\"! The configuration file may be corrupt or missing!", NamedTextColor.RED));
                            getCmpnntLogger().error("", e);
                        }
                    }
                }, 1L);
            }
        } catch (final FileNotFoundException ex) {
            getComponentLogger().info("Failed to load config, creating one.");
            try {
                getConfig().save(getDataFolder() + File.separator + "config.yml");
            } catch (final IOException e) {
                getComponentLogger().error("Could not save config.yml!", e);
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

    private void hookPlaceHolderApi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            hasPAPI = true;
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

    private int checkVersion() {
        VERSION = new ComparableVersion(getPluginMeta().getVersion());
        PAPER_VERSION = new ComparableVersion(getPluginMeta().getAPIVersion());
        final ComparableVersion serverversion = new ComparableVersion(getServer().getMinecraftVersion());
        return PAPER_VERSION.compareTo(serverversion);
    }

    /**
     * use {@link #getPlayerManager()}
     *
     * @return MinigamePlayeManager
     */
    @Deprecated
    public MinigamePlayerManager getPlayerData() {
        return playerManager;
    }

    /**
     * use {@link #minigameManager}
     *
     * @return MinigameManager
     */
    @Deprecated
    public MinigameManager getMinigameData() {
        return minigameManager;
    }

    public BackendManager getBackend() {
        return backend;
    }

    @Deprecated
    public long getLastUpdateCheck() {
        return lastUpdateCheck;
    }

    @Deprecated
    public void setLastUpdateCheck(final long time) {
        lastUpdateCheck = time;
    }

    /**
     * @return Signs
     */
    @SuppressWarnings("unused")
    public SignBase getMinigameSigns() {
        return minigameSigns;
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
                result.put(module.getName(), 1);
            }
            return result;
        });
        metrics.addCustomChart(chart);
        metrics.addCustomChart(barChart);
    }

    public void addMetric(final CustomChart chart) {
        metrics.addCustomChart(chart);
    }

    public void queueStatSave(final @NotNull StoredGameStats saveData, final boolean winner) {
        MinigameMessageManager.debugMessage("Scheduling SQL data save for " + saveData);

        final CompletableFuture<Long> winCountFuture = backend.loadSingleStat(saveData.getMinigame(), MinigameStatistics.Wins, StatisticValueField.Total, saveData.getPlayer().getUUID());
        backend.saveStats(saveData);

        winCountFuture.thenApply(winCount -> Bukkit.getScheduler().runTask(Minigames.getPlugin(), () -> {
            final Minigame minigame = saveData.getMinigame();
            final MinigamePlayer player = saveData.getPlayer();

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

    public ResourcePackManager getResourceManager() {
        return resourceManager;
    }

    public boolean includesPapi() {
        return hasPAPI;
    }
}
