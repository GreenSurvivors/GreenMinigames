package au.com.mineauz.minigames.backend.both;

import au.com.mineauz.minigames.backend.BackendImportCallback;
import au.com.mineauz.minigames.backend.ConnectionHandler;
import au.com.mineauz.minigames.backend.ConnectionPool;
import au.com.mineauz.minigames.backend.StatementKey;
import au.com.mineauz.minigames.stats.StatFormat;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

public class SQLImport implements BackendImportCallback {
    private final @NotNull ConnectionPool pool;

    private final @NotNull StatementKey clearPlayers;
    private final @NotNull StatementKey clearMinigames;
    private final @NotNull StatementKey clearStats;
    private final @NotNull StatementKey clearMetadata;

    private final @NotNull StatementKey insertPlayer;
    private final @NotNull StatementKey insertMinigame;
    private final @NotNull StatementKey insertStat;
    private final @NotNull StatementKey insertMetadata;

    private ConnectionHandler handler;
    private int playerBatchCount;
    private int minigameBatchCount;
    private int statBatchCount;
    private int metadataBatchCount;

    public SQLImport(@NotNull ConnectionPool pool) {
        this.pool = pool;

        // Prepare queries
        clearPlayers = new StatementKey("DELETE FROM `Players`;");
        clearMinigames = new StatementKey("DELETE FROM `Minigames`;");
        clearStats = new StatementKey("DELETE FROM `PlayerStats`;");
        clearMetadata = new StatementKey("DELETE FROM `StatMetadata`;");

        insertPlayer = new StatementKey("INSERT INTO `Players` VALUES (?,?,?);");
        insertMinigame = new StatementKey("INSERT INTO `Minigames` VALUES (?,?);");
        insertStat = new StatementKey("INSERT INTO `PlayerStats` (`player_id`, `minigame_id`, `stat`, `value`) VALUES (?,?,?,?);");
        insertMetadata = new StatementKey("INSERT INTO `StatMetadata` VALUES (?,?,?,?);");
    }

    @Override
    public void begin() {
        // Clear the database
        try {
            handler = pool.getConnection();
            handler.beginTransaction();

            handler.executeUpdate(clearPlayers);
            handler.executeUpdate(clearMinigames);
            handler.executeUpdate(clearStats);
            handler.executeUpdate(clearMetadata);

            playerBatchCount = 0;
            minigameBatchCount = 0;
            statBatchCount = 0;
        } catch (SQLException e) {
            if (handler != null) {
                handler.endTransactionFail();
                handler.release();
            }
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void acceptPlayer(@NotNull UUID playerId, @NotNull String name, @NotNull Component displayName) {
        try {
            handler.batchUpdate(insertPlayer, playerId.toString(), name, displayName);
            ++playerBatchCount;

            if (playerBatchCount > 50) {
                handler.executeBatch(insertPlayer);
                playerBatchCount = 0;
            }
        } catch (SQLException e) {
            handler.endTransactionFail();
            handler.release();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void acceptMinigame(int id, @NotNull String name) {
        try {
            // Handle any remaining players
            if (playerBatchCount >= 0) {
                handler.executeBatch(insertPlayer);
                playerBatchCount = 0;
            }

            // batch minigames
            handler.batchUpdate(insertMinigame, id, name);
            ++minigameBatchCount;

            if (minigameBatchCount > 50) {
                handler.executeBatch(insertMinigame);
                minigameBatchCount = 0;
            }
        } catch (SQLException e) {
            handler.endTransactionFail();
            handler.release();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void acceptStat(@NotNull UUID playerId, int minigameId, @NotNull String stat, long value) {
        try {
            // Handle any remaining minigames
            if (minigameBatchCount > 0) {
                handler.executeBatch(insertMinigame);
                minigameBatchCount = 0;
            }

            // batch stats
            handler.batchUpdate(insertStat, playerId.toString(), minigameId, stat, value);
            ++statBatchCount;

            if (statBatchCount > 50) {
                handler.executeBatch(insertStat);
                statBatchCount = 0;
            }
        } catch (SQLException e) {
            handler.endTransactionFail();
            handler.release();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void acceptStatMetadata(int minigameId, @NotNull String stat, @NotNull String displayName, @NotNull StatFormat format) {
        try {
            // Handle remaining stats
            if (statBatchCount > 0) {
                handler.executeBatch(insertStat);
                statBatchCount = 0;
            }

            // batch stats
            handler.batchUpdate(insertMetadata, minigameId, stat, displayName, format.name());
            ++metadataBatchCount;

            if (metadataBatchCount > 50) {
                handler.executeBatch(insertMetadata);
                metadataBatchCount = 0;
            }
        } catch (SQLException e) {
            handler.endTransactionFail();
            handler.release();
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void end() {
        try {
            // Handle remaining metadata
            if (metadataBatchCount > 0) {
                handler.executeBatch(insertMetadata);
                metadataBatchCount = 0;
            }

            handler.endTransaction();
        } catch (SQLException e) {
            handler.endTransactionFail();
            handler.release();
            throw new IllegalStateException(e);
        }
    }
}
