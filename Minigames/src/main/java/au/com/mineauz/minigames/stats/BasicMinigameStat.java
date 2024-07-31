package au.com.mineauz.minigames.stats;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

class BasicMinigameStat extends MinigameStat {
    protected BasicMinigameStat(@NotNull String name, @NotNull MinigameLangKey displayName, @NotNull StatFormat format) {
        super(name, MinigameMessageManager.getMgMessage(displayName), format);
    }

    protected BasicMinigameStat(@NotNull String name, @Nullable Component displayName, @NotNull StatFormat format) {
        super(name, displayName, format);
    }

    /**
     * @param value    The value in milliseconds to display
     * @param settings The settings of this stat
     */
    @Override
    public @NotNull Component displayValue(long value, @NotNull StatSettings settings) {
        if (this == MinigameStatistics.CompletionTime) {
            return MinigameUtils.convertTime(Duration.ofMillis(value), false);
        } else {
            return Component.text(value).appendSpace().append(settings.getDisplayName());
        }
    }

    /**
     * @param value    The value in milliseconds to display
     * @param settings The settings of this stat
     */
    @Override
    public @NotNull Component displayValueSign(long value, @NotNull StatSettings settings) {
        if (this == MinigameStatistics.CompletionTime) {
            return MinigameUtils.convertTime(Duration.ofMillis(value), true);
        } else {
            return Component.text(value).appendSpace().append(settings.getDisplayName());
        }
    }
}
