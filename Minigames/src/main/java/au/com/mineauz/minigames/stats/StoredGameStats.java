package au.com.mineauz.minigames.stats;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StoredGameStats {
    private final @NotNull MinigamePlayer player;
    private final @NotNull Minigame minigame;
    private final @NotNull Map<@NotNull MinigameStat, @NotNull Long> stats;
    private final@NotNull  Map<@NotNull MinigameStat, @NotNull StatSettings> settings;

    public StoredGameStats(@NotNull Minigame minigame, @NotNull MinigamePlayer player) {
        this.minigame = minigame;
        this.player = player;

        stats = new HashMap<>();
        settings = new HashMap<>();
    }

    public @NotNull MinigamePlayer getPlayer() {
        return player;
    }

    public @NotNull Minigame getMinigame() {
        return minigame;
    }

    public void addStat(@NotNull MinigameStat stat, long value) {
        stats.put(stat, value);
    }

    public @NotNull Map<@NotNull MinigameStat, @NotNull Long> getStats() {
        Map<MinigameStat, Long> newStats = new HashMap<>(stats);

        return Collections.unmodifiableMap(newStats);
    }

    public boolean hasStat(@NotNull MinigameStat stat) {
        return stats.containsKey(stat);
    }

    public long getStat(@NotNull MinigameStat stat) {
        Long value = stats.get(stat);
        return Objects.requireNonNullElse(value, 0L);
    }

    public void applySettings(@NotNull Map<@NotNull MinigameStat, @NotNull StatSettings> settings) {
        this.settings.putAll(settings);
    }

    public @NotNull StatFormat getFormat(@NotNull MinigameStat stat) {
        return settings.get(stat).getFormat();
    }

    @Override
    public @NotNull String toString() {
        return String.format("%s in %s", player.getName(), minigame.getName());
    }
}
