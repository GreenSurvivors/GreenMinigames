package au.com.mineauz.minigames.minigame.reward.scheme;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public enum MgRewardSchemes implements RewardSchemeFactory {
    STANDARD("standard", StandardRewardScheme::new),
    SCORE("score", ScoreRewardScheme::new),
    TIME("time", TimeRewardScheme::new),
    KILLS("kills", KillsRewardScheme::new),
    DEATHS("deaths", DeathsRewardScheme::new),
    REVERTS("reverts", RevertsRewardScheme::new);

    final @NotNull String name;
    final @NotNull Function<@NotNull String, @NotNull ARewardScheme> constructor;


    MgRewardSchemes(@NotNull String name, @NotNull Function<@NotNull String, @NotNull ARewardScheme> constructor) {
        this.name = name;
        this.constructor = constructor;
    }

    public @NotNull String getSchemeName() {
        return name;
    }

    @Override
    public @NotNull ARewardScheme makeScheme() {
        return constructor.apply(name);
    }

    @Override
    public @NotNull String toString() {
        return name;
    }
}
