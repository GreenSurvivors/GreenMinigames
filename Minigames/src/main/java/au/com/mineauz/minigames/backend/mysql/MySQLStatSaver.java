package au.com.mineauz.minigames.backend.mysql;

import au.com.mineauz.minigames.backend.ConnectionHandler;
import au.com.mineauz.minigames.backend.StatementKey;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.stats.MinigameStat;
import au.com.mineauz.minigames.stats.StatFormat;
import au.com.mineauz.minigames.stats.StatisticValueField;
import au.com.mineauz.minigames.stats.StoredGameStats;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.UUID;

class MySQLStatSaver {
    private final MySQLBackend backend;
    private final ComponentLogger logger;

    private final @NotNull StatementKey insertStat;
    private final @NotNull StatementKey insertStatTotal;
    private final @NotNull StatementKey insertStatMin;
    private final @NotNull StatementKey insertStatMax;

    private final StatementKey[] insertStatements = new StatementKey[4];

    public MySQLStatSaver(MySQLBackend backend, ComponentLogger logger) {
        this.backend = backend;
        this.logger = logger;

        // Create statements
        insertStat = new StatementKey("INSERT INTO `PlayerStats` (`player_id`, `minigame_id`, `stat`, `value`, `entered`) VALUES (?, ?, ?, ?, Now()) ON DUPLICATE KEY UPDATE `value`=VALUES(`value`)");
        insertStatTotal = new StatementKey("INSERT INTO `PlayerStats` (`player_id`, `minigame_id`, `stat`, `value`, `entered`) VALUES (?, ?, ?, ?, Now()) ON DUPLICATE KEY UPDATE `value`=`value`+VALUES(`value`)");
        insertStatMin = new StatementKey("INSERT INTO `PlayerStats` (`player_id`, `minigame_id`, `stat`, `value`, `entered`) VALUES (?, ?, ?, ?, Now()) ON DUPLICATE KEY UPDATE `value`=LEAST(`value`, VALUES(`value`))");
        insertStatMax = new StatementKey("INSERT INTO `PlayerStats` (`player_id`, `minigame_id`, `stat`, `value`, `entered`) VALUES (?, ?, ?, ?, Now()) ON DUPLICATE KEY UPDATE `value`=GREATEST(`value`, VALUES(`value`))");

        // Prepare lookup table
        insertStatements[StatisticValueField.Last.ordinal()] = insertStat;
        insertStatements[StatisticValueField.Min.ordinal()] = insertStatMin;
        insertStatements[StatisticValueField.Max.ordinal()] = insertStatMax;
        insertStatements[StatisticValueField.Total.ordinal()] = insertStatTotal;
    }

    public void saveData(@NotNull StoredGameStats data) {
        MinigameMessageManager.debugMessage("MySQL beginning save of " + data);

        ConnectionHandler handler = null;
        try {
            handler = backend.getPool().getConnection();
            try {
                handler.beginTransaction();

                // Get the minigame id and update both the player and game
                int minigameId = backend.getMinigameId(handler, data.getMinigame());
                backend.updatePlayer(handler, data.getPlayer());

                saveStats(handler, data, data.getPlayer().getUUID(), minigameId);

                // Commit the changes
                handler.endTransaction();
            } catch (SQLException e) {
                logger.error("Failed to save stats for " + data.getPlayer().getName(), e);

                handler.endTransactionFail();
            } finally {
                MinigameMessageManager.debugMessage("MySQL completed save of " + data);
            }
        } catch (SQLException e) {
            logger.error("", e);
        } finally {
            if (handler != null) {
                handler.release();
            }
        }
    }

    private void saveStats(@NotNull ConnectionHandler handler, @NotNull StoredGameStats data, @NotNull UUID player, int minigameId) throws SQLException {
        MinigameMessageManager.debugMessage("MySQL saving stats for " + player + ", game " + minigameId);

        // Prepare all updates
        for (Entry<MinigameStat, Long> entry : data.getStats().entrySet()) {
            StatFormat format = data.getFormat(entry.getKey());
            // Only store this stat if it's required
            if (entry.getKey().shouldStoreStat(entry.getValue(), format)) {
                queueStat(handler, entry.getKey(), entry.getValue(), format, player, minigameId);
            }
        }

        // Push all to database
        handler.executeBatch(insertStat);
        handler.executeBatch(insertStatTotal);
        handler.executeBatch(insertStatMin);
        handler.executeBatch(insertStatMax);

        MinigameMessageManager.debugMessage("MySQL completed save for " + player + ", game " + minigameId);
    }

    private void queueStat(@NotNull ConnectionHandler handler, @NotNull MinigameStat stat, long value, @NotNull StatFormat format, @NotNull UUID player, int minigameId) throws SQLException {
        for (StatisticValueField field : format.getFields()) {
            handler.batchUpdate(insertStatements[field.ordinal()], player.toString(), minigameId, stat.getName() + field.getSuffix(), value);
        }
    }
}
