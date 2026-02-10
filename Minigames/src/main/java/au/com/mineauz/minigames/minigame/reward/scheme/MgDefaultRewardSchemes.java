package au.com.mineauz.minigames.minigame.reward.scheme;

import au.com.mineauz.minigames.objects.MinigamesKey;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public enum MgDefaultRewardSchemes implements RewardSchemeFactory {
    STANDARD("standard", StandardRewardScheme::new),
    SCORE("score", ScoreRewardScheme::new),
    TIME("time", TimeRewardScheme::new),
    KILLS("kills", KillsRewardScheme::new),
    DEATHS("deaths", DeathsRewardScheme::new),
    REVERTS("reverts", RevertsRewardScheme::new);

    final @NotNull Key key;
    final @NotNull Function<@NotNull Key, @NotNull ARewardScheme> constructor;


    MgDefaultRewardSchemes(final @NotNull String name, final @NotNull Function<@NotNull Key, @NotNull ARewardScheme> constructor) {
        this.key = MinigamesKey.minigames(name);
        this.constructor = constructor;
    }

    @Override
    public @NotNull ARewardScheme makeScheme() {
        return constructor.apply(key);
    }

    @Override
    public @NotNull String toString() {
        return key.asMinimalString();
    }

    @Override
    public @NotNull Key key() {
        return key;
    }
}
