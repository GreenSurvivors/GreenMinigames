package au.com.mineauz.minigames.minigame.reward.scheme;

import org.jetbrains.annotations.NotNull;

public interface RewardSchemeFactory {
    @NotNull RewardScheme makeScheme();

    @NotNull String getSchemeName();
}
