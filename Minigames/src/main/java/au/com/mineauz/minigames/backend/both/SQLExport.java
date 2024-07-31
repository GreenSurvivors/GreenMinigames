package au.com.mineauz.minigames.backend.both;

import au.com.mineauz.minigames.backend.*;
import au.com.mineauz.minigames.stats.StatFormat;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLExport {
    private final @NotNull ConnectionPool pool;
    private final @NotNull BackendImportCallback callback;
    private final @NotNull Notifier notifier;

    private final @NotNull StatementKey getPlayers;
    private final @NotNull StatementKey getMinigames;
    private final @NotNull StatementKey getStats;
    private final @NotNull StatementKey getStatMetadata;

    private ConnectionHandler handler;

    private String notifyState;
    private int notifyCount;
    private long notifyTime;


    public SQLExport(@NotNull ConnectionPool pool, @NotNull BackendImportCallback callback, @NotNull Notifier notifier) {
        this.pool = pool;
        this.callback = callback;
        this.notifier = notifier;

        // Prepare queries
        getPlayers = new StatementKey("SELECT * FROM `Players`;");
        getMinigames = new StatementKey("SELECT * FROM `Minigames`;");
        getStats = new StatementKey("SELECT * FROM `PlayerStats`;");
        getStatMetadata = new StatementKey("SELECT * FROM `StatMetadata`;");
    }

    public void beginExport() {
        try {
            handler = pool.getConnection();
            callback.begin();

            exportPlayers();
            exportMinigames();
            exportStats();
            exportStatMetadata();

            notifyNext("Done");

            callback.end();
            notifier.onComplete();
        } catch (SQLException | IllegalStateException e) {
            notifier.onError(e, notifyState, notifyCount);
        } finally {
            handler.release();
        }
    }

    private void exportPlayers() throws SQLException {
        notifyNext("Exporting players...");
        try (ResultSet rs = handler.executeQuery(getPlayers)) {
            while (rs.next()) {
                callback.acceptPlayer(UUID.fromString(rs.getString("player_id")), rs.getString("name"),
                        MiniMessage.miniMessage().deserialize(rs.getString("displayname")));
                ++notifyCount;
                notifyProgress();
            }
        }
    }

    private void exportMinigames() throws SQLException {
        notifyNext("Exporting minigames...");
        try (ResultSet rs = handler.executeQuery(getMinigames)) {
            while (rs.next()) {
                callback.acceptMinigame(rs.getInt("minigame_id"), rs.getString("name"));
                ++notifyCount;
                notifyProgress();
            }
        }
    }

    private void exportStats() throws SQLException {
        notifyNext("Exporting stats...");
        try (ResultSet rs = handler.executeQuery(getStats)) {
            while (rs.next()) {
                callback.acceptStat(UUID.fromString(rs.getString("player_id")), rs.getInt("minigame_id"), rs.getString("stat"), rs.getLong("value"));
                ++notifyCount;
                notifyProgress();
            }
        }
    }

    private void exportStatMetadata() throws SQLException {
        notifyNext("Exporting metadata...");
        try (ResultSet rs = handler.executeQuery(getStatMetadata)) {
            while (rs.next()) {
                String rawFormat = rs.getString("format");
                StatFormat format = null;
                for (StatFormat f : StatFormat.values()) {
                    if (f.name().equalsIgnoreCase(rawFormat)) {
                        format = f;
                        break;
                    }
                }

                if (format == null) {
                    continue;
                }

                callback.acceptStatMetadata(rs.getInt("minigame_id"), rs.getString("stat"), rs.getString("display_name"), format);
                ++notifyCount;
                notifyProgress();
            }
        }
    }

    private void notifyProgress() {
        if (System.currentTimeMillis() - notifyTime >= 2000) {
            notifier.onProgress(notifyState, notifyCount);
            notifyTime = System.currentTimeMillis();
        }
    }

    private void notifyNext(@NotNull String state) {
        if (notifyCount != 0) {
            notifier.onProgress(notifyState, notifyCount);
        }

        notifyTime = System.currentTimeMillis();
        notifyCount = 0;
        notifyState = state;

        notifier.onProgress(state, 0);
    }
}
