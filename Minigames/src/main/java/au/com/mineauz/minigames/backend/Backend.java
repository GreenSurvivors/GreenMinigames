package au.com.mineauz.minigames.backend;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.ScoreboardOrder;
import au.com.mineauz.minigames.stats.*;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Backend {
    /**
     * Initializes the backend. This may include creating / converting tables as needed
     *
     * @param config The configuration to load settings from
     * @return Returns true if the initialization succeeded
     */
    public abstract boolean initialize(@NotNull ConfigurationSection config);

    /**
     * Shutdown the backend cleaning up resources
     */
    public abstract void shutdown();

    /**
     * Cleans unused connections
     */
    public abstract void clean();

    /**
     * Saves the game stats to the backend. This method is blocking.
     *
     * @param stats The game stats to store
     */
    public abstract void saveGameStatus(@NotNull StoredGameStats stats);

    /**
     * Loads all player stats from the backend. This method is blocking.
     *
     * @param minigame The minigame to load stats for
     * @param stat     The stat to load
     * @param field    The field to load
     * @param order    The order to get the stats in
     * @return A list of stats matching the requirements
     */
    public abstract @NotNull List<@NotNull StoredStat> loadStats(@NotNull Minigame minigame, @NotNull MinigameStat stat, @NotNull StatisticValueField field, @NotNull ScoreboardOrder order);

    /**
     * Loads player stats from the backend. This method is blocking.
     *
     * @param minigame The minigame to load stats for
     * @param stat     The stat to load
     * @param field    The field to load
     * @param order    The order to get the stats in
     * @param offset   the starting index to load from
     * @param length   the maximum amount of data to return
     * @return A list of stats matching the requirements
     */
    public abstract @NotNull List<@NotNull StoredStat> loadStats(@NotNull Minigame minigame, @NotNull MinigameStat stat, @NotNull StatisticValueField field, @NotNull ScoreboardOrder order, int offset, int length);

    /**
     * Gets the value of a stat for a player. This method is blocking
     *
     * @param minigame The minigame that value should be for
     * @param playerId the UUID of the player in question
     * @param stat     the stat to load
     * @param field    the field of the stat to load
     * @return The value of the stat
     */
    public abstract long getStat(@NotNull Minigame minigame, @NotNull UUID playerId, @NotNull MinigameStat stat, @NotNull StatisticValueField field);

    /**
     * Loads stat settings for the minigame
     *
     * @param minigame The minigame to load settings from
     * @return A map of stats to their settings
     */
    public abstract @NotNull Map<@NotNull MinigameStat, @NotNull StatSettings> loadStatSettings(@NotNull Minigame minigame);

    /**
     * Saves the stat settings for the minigame
     *
     * @param minigame The minigame to save settings for
     * @param settings The settings to save
     */
    public abstract void saveStatSettings(@NotNull Minigame minigame, @NotNull Collection<@NotNull StatSettings> settings);

    /**
     * Exports this backend to another backend
     *
     * @param other    The backend to export to
     * @param notifier A callback to receive progress updates
     */
    public abstract void exportTo(@NotNull Backend other, @NotNull Notifier notifier);

    protected abstract @NotNull BackendImportCallback getImportCallback();

    protected final @NotNull BackendImportCallback getImportCallback(@NotNull Backend other) {
        return other.getImportCallback();
    }

    /**
     * Performs a conversion from a previous format
     *
     * @param notifier A notifier for progress updates
     * @return True if the conversion succeeded
     */
    public abstract boolean doConversion(@NotNull Notifier notifier);
}
