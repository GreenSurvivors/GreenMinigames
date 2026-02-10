package au.com.mineauz.minigames.minigame.reward.scheme;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface RewardSchemeFactory extends Keyed {
    @NotNull ARewardScheme makeScheme();
}
