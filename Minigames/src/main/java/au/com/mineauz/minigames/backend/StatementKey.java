package au.com.mineauz.minigames.backend;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementKey {
    private final @NotNull String sql;
    private final boolean returnGeneratedKeys;
    private boolean valid;

    public StatementKey(@NotNull String sql) {
        this(sql, false);
    }

    public StatementKey(@NotNull String sql, boolean returnGeneratedKeys) {
        this.sql = sql;
        this.returnGeneratedKeys = returnGeneratedKeys;
        this.valid = true;
    }

    public @NotNull String getSQL() {
        return sql;
    }

    public boolean returnsGeneratedKeys() {
        return returnGeneratedKeys;
    }

    public boolean isValid() {
        return valid;
    }

    protected @NotNull PreparedStatement createPreparedStatement(@NotNull Connection connection) throws SQLException {
        try {
            return connection.prepareStatement(sql, (returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS));
        } catch (SQLException e) {
            valid = false;
            throw new SQLException(e);
        }
    }
}
