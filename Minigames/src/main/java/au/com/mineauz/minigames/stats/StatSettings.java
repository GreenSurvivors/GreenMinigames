package au.com.mineauz.minigames.stats;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents per minigame settings for a stat
 */
public class StatSettings {
    private final @NotNull MinigameStat stat;
    private @Nullable StatFormat format;
    private @Nullable Component displayName;

    public StatSettings(@NotNull MinigameStat stat, @Nullable StatFormat format, @Nullable Component displayName) {
        this.stat = stat;
        this.format = format;
        this.displayName = displayName;
    }

    public StatSettings(@NotNull MinigameStat stat) {
        this(stat, null, null);
    }

    /**
     * @return Returns the stat
     */
    public @NotNull MinigameStat getStat() {
        return stat;
    }

    /**
     * @return Returns the current format of this stat for this minigame
     */
    @NotNull
    public StatFormat getFormat() {
        return Objects.requireNonNullElseGet(format, stat::getFormat);
    }

    /**
     * Sets the format of this stat for this minigame.
     *
     * @param format The new format to display. Setting to null will reset the format
     */
    public void setFormat(@Nullable StatFormat format) {
        this.format = format;
    }

    /**
     * @return Returns the current display name of this stat
     */
    public @NotNull Component getDisplayName() {
        return Objects.requireNonNullElseGet(displayName, stat::getDisplayName);
    }

    /**
     * Sets the display name of this stat for this minigame
     *
     * @param displayName The new name of this stat. Setting to null will reset the name
     */
    public void setDisplayName(@Nullable Component displayName) {
        this.displayName = displayName;
    }
}
