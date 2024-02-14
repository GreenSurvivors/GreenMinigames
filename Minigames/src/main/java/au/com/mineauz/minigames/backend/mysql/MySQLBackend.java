package au.com.mineauz.minigames.backend.mysql;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.backend.*;
import au.com.mineauz.minigames.backend.both.SQLExport;
import au.com.mineauz.minigames.backend.both.SQLImport;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.ScoreboardOrder;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.stats.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class MySQLBackend extends Backend {
    private final ComponentLogger logger;
    private final MySQLStatLoader loader;
    private final MySQLStatSaver saver;
    private ConnectionPool pool;
    private String database;
    private StatementKey insertMinigame;
    private StatementKey insertPlayer;
    private StatementKey loadStatSettings;
    private StatementKey saveStatSettings;

    public MySQLBackend(ComponentLogger logger) {
        this.logger = logger;

        loader = new MySQLStatLoader(this, logger);
        saver = new MySQLStatSaver(this, logger);
    }

    public boolean initialize(ConfigurationSection config) {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            database = config.getString("database", "database");

            // Create the pool
            String url = String.format("jdbc:mysql://%s/%s",
                    config.getString("host", "localhost:3306"), database);
            MinigameMessageManager.debugMessage("URL: " + url);
            Properties props = new Properties();
            props.put("user", config.getString("username", "username"));
            props.put("password", config.getString("password", "password"));
            ConfigurationSection dbprops = config.getConfigurationSection("properties");
            if (dbprops != null) {
                for (Map.Entry<String, Object> entry : dbprops.getValues(false).entrySet()) {
                    props.put(entry.getKey(), entry.getValue().toString());
                }
            }
            MinigameMessageManager.debugMessage("Properties: " + props);
            pool = new ConnectionPool(url, props);

            createStatements();

            // Test the connection
            try {
                ConnectionHandler handler = pool.getConnection();
                ensureTables(handler);
                handler.release();
                return true;
            } catch (SQLException e) {
                logger.error("Failed to connect to the MySQL database. Please check your database settings. ", e);
            }
        } catch (ClassNotFoundException e) {
            logger.error("Failed to find MySQL JDBC driver. This version of craftbukkit is defective.", e);
        }

        return false;
    }

    @Override
    public void shutdown() {
        pool.closeConnections();
    }

    private void ensureTables(ConnectionHandler connection) throws SQLException {
        try (Statement statement = connection.getConnection().createStatement()) {
            // Check the players table
            try {
                statement.executeQuery("SELECT 1 FROM `Players` LIMIT 0;");
            } catch (SQLException e) {
                statement.executeUpdate("CREATE TABLE `Players` (`player_id` CHAR(36) PRIMARY KEY, `name` VARCHAR(30) NOT NULL, `displayname` VARCHAR(30), INDEX (`name`, `player_id`));");
            }

            // Check the minigames table
            try {
                statement.executeQuery("SELECT 1 FROM `Minigames` LIMIT 0;");
            } catch (SQLException e) {
                statement.executeUpdate("CREATE TABLE `Minigames` (`minigame_id` INTEGER AUTO_INCREMENT PRIMARY KEY, `name` VARCHAR(30) NOT NULL, UNIQUE INDEX (`name`));");
            }

            // Check the player stats table
            try {
                statement.executeQuery("SELECT 1 FROM `PlayerStats` LIMIT 0;");
            } catch (SQLException e) {
                statement.executeUpdate("CREATE TABLE `PlayerStats` (`player_id` CHAR(36) REFERENCES `Players` (`player_id`) ON DELETE CASCADE, `minigame_id` INTEGER REFERENCES `Minigames` (`minigame_id`) ON DELETE CASCADE, `stat` VARCHAR(20) NOT NULL, `value` BIGINT, `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, `entered` datetime DEFAULT NULL, PRIMARY KEY (`player_id`, `minigame_id`, `stat`));");
            }

            // Check for column last_updated on the PlayerStats table
            try {
                ResultSet rs = statement.executeQuery("SHOW COLUMNS FROM `PlayerStats` WHERE Field = 'last_updated';");

                if (!rs.next()) {
                    logger.info("Adding MySQL column 'last_updated' to table PlayerStats");
                    statement.executeUpdate("ALTER TABLE `PlayerStats` ADD COLUMN `last_updated` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;");
                }

            } catch (SQLException e) {
                logger.error("Failed to add column last_updated to the PlayerStats table in the MySQL Minigames database", e);
            }

            // Check for column entered on the PlayerStats table
            try {
                ResultSet rs = statement.executeQuery("SHOW COLUMNS FROM `PlayerStats` WHERE Field = 'entered';");

                if (!rs.next()) {
                    logger.info("Adding MySQL column 'entered' to table PlayerStats");
                    statement.executeUpdate("ALTER TABLE `PlayerStats` ADD COLUMN `entered` DATETIME DEFAULT NULL;");
                }

            } catch (SQLException e) {
                logger.error("Failed to add column entered to the PlayerStats table in the MySQL Minigames database", e);
            }

            // Check the stat metadata table
            try {
                statement.executeQuery("SELECT 1 FROM `StatMetadata` LIMIT 0;");
            } catch (SQLException e) {
                statement.executeUpdate("CREATE TABLE `StatMetadata` (`minigame_id` INTEGER REFERENCES `Minigames` (`minigame_id`) ON DELETE CASCADE, `stat` VARCHAR(20) NOT NULL, `display_name` VARCHAR(20), `format` ENUM('LAST', 'LASTANDTOTAL', 'MIN', 'MINANDTOTAL', 'MAX', 'MAXANDTOTAL', 'MINMAX', 'MINMAXANDTOTAL', 'TOTAL'), PRIMARY KEY (`minigame_id`, `stat`));");
            }
        }
    }

    private void createStatements() {
        insertMinigame = new StatementKey("INSERT INTO `Minigames` (`name`) VALUES (?) ON DUPLICATE KEY UPDATE `minigame_id`=LAST_INSERT_ID(`minigame_id`);", true);
        insertPlayer = new StatementKey("INSERT INTO `Players` VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `displayname` = VALUES(`displayname`);");
        loadStatSettings = new StatementKey("SELECT `stat`, `display_name`, `format` FROM `StatMetadata` WHERE `minigame_id`=?;");
        saveStatSettings = new StatementKey("REPLACE INTO `StatMetadata` VALUES (?, ?, ?, ?);");
    }

    ConnectionPool getPool() {
        return pool;
    }

    @Override
    public void clean() {
        pool.removeExpired();
    }

    @Override
    public void saveGameStatus(StoredGameStats stats) {
        saver.saveData(stats);
    }

    @Override
    public List<StoredStat> loadStats(Minigame minigame, MinigameStat stat, StatisticValueField field, ScoreboardOrder order) {
        return loadStats(minigame, stat, field, order, 0, Integer.MAX_VALUE);
    }

    @Override
    public List<StoredStat> loadStats(Minigame minigame, MinigameStat stat, StatisticValueField field, ScoreboardOrder order, int offset, int length) {
        return loader.loadStatValues(minigame, stat, field, order, offset, length);
    }

    @Override
    public long getStat(Minigame minigame, UUID playerId, MinigameStat stat, StatisticValueField field) {
        return loader.loadSingleValue(minigame, stat, field, playerId);
    }

    public int getMinigameId(ConnectionHandler handler, Minigame minigame) throws SQLException {

        try (ResultSet rs = handler.executeUpdateWithResults(insertMinigame, minigame.getName())) {
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                // Insert should always return the value
                throw new AssertionError();
            }
        }
    }

    public void updatePlayer(ConnectionHandler handler, MinigamePlayer player) throws SQLException {
        handler.executeUpdate(insertPlayer, player.getUUID().toString(), player.getName(), player.getDisplayName());
    }

    @Override
    public Map<MinigameStat, StatSettings> loadStatSettings(Minigame minigame) {
        ConnectionHandler handler = null;
        try {
            handler = pool.getConnection();

            int minigameId = getMinigameId(handler, minigame);

            Map<MinigameStat, StatSettings> settings = new HashMap<>();

            try (ResultSet rs = handler.executeQuery(loadStatSettings, minigameId)) {
                while (rs.next()) {
                    String statName = rs.getString("stat");
                    String rawFormat = rs.getString("format");
                    Component displayName = MiniMessage.miniMessage().deserialize(rs.getString("display_name"));

                    MinigameStat stat = MinigameStatistics.getStat(statName);
                    if (stat == null) {
                        // Just ignore it
                        continue;
                    }

                    // Decode format
                    StatFormat format = null;
                    for (StatFormat f : StatFormat.values()) {
                        if (f.name().equalsIgnoreCase(rawFormat)) {
                            format = f;
                            break;
                        }
                    }

                    if (format == null) {
                        format = stat.getFormat();
                    }

                    StatSettings setting = new StatSettings(stat, format, displayName);
                    settings.put(stat, setting);
                }

                return settings;
            }
        } catch (SQLException e) {
            Minigames.getCmpnntLogger().error("", e);
            return Collections.emptyMap();
        } finally {
            if (handler != null) {
                handler.release();
            }
        }
    }

    @Override
    public void saveStatSettings(Minigame minigame, Collection<StatSettings> settings) {
        ConnectionHandler handler = null;
        try {
            handler = pool.getConnection();
            handler.beginTransaction();

            int minigameId = getMinigameId(handler, minigame);
            for (StatSettings setting : settings) {
                handler.batchUpdate(saveStatSettings, minigameId, setting.getStat().getName(),
                        MiniMessage.miniMessage().serialize(setting.getDisplayName()),
                        setting.getFormat().name().toUpperCase());
            }

            handler.executeBatch(saveStatSettings);
            handler.endTransaction();
        } catch (SQLException e) {
            Minigames.getCmpnntLogger().error("", e);
            handler.endTransactionFail();
        } finally {
            if (handler != null) {
                handler.release();
            }
        }
    }

    @Override
    protected BackendImportCallback getImportCallback() {
        return new SQLImport(pool);
    }

    @Override
    public void exportTo(Backend other, Notifier notifier) {
        BackendImportCallback callback = getImportCallback(other);
        SQLExport exporter = new SQLExport(pool, callback, notifier);
        exporter.beginExport();
    }

    @Override
    public boolean doConversion(Notifier notifier) {
        BackendImportCallback callback = getImportCallback();
        LegacyExporter exporter = new LegacyExporter(pool, database, callback, notifier);
        return exporter.doExport();
    }
}
