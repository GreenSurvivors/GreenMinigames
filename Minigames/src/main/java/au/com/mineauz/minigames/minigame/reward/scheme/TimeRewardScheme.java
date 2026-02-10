package au.com.mineauz.minigames.minigame.reward.scheme;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.stats.MinigameStatistics;
import au.com.mineauz.minigames.stats.StoredGameStats;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class TimeRewardScheme extends HierarchyRewardScheme<@NotNull Integer> {

    protected TimeRewardScheme(final @NotNull Key key) {
        super(key);
    }

    @Override
    protected @NotNull Integer decrement(final @NotNull Integer value) {
        return value - 1;
    }

    @Override
    protected @NotNull Integer increment(final @NotNull Integer value) {
        return value + 1;
    }

    @Override
    protected @NotNull Integer loadKey(final @NotNull Object key) throws IllegalArgumentException{
        final int value;
        if (key instanceof Number number) {
            value = number.intValue();
        } else {
            value = Integer.parseInt(key.toString());
        }

        if (value <= 0) {
            throw new IllegalArgumentException();
        }

        return value;
    }

    /**
     * in seconds
     */
    @Override
    protected @NotNull Component getMenuItemDescName(final @NotNull Integer value) {
        return MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_TIME_DESCRIPTION,
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(value), true)));
    }

    /**
     * in seconds
     */
    @Override
    protected @NotNull Integer getValue(final @Nullable MinigamePlayer player,
                                        final @NotNull StoredGameStats data,
                                        final @NotNull Minigame minigame) {
        return (int) TimeUnit.MILLISECONDS.toSeconds(data.getStat(MinigameStatistics.CompletionTime));
    }

    /**
     * in seconds
     */
    @Override
    protected @NotNull Component getMenuItemName(final @NotNull Integer value) {
        return MinigameUtils.convertTime(Duration.ofSeconds(value), true);
    }
}
