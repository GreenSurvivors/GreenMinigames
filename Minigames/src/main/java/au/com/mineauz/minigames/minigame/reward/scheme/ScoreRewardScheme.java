package au.com.mineauz.minigames.minigame.reward.scheme;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.stats.MinigameStatistics;
import au.com.mineauz.minigames.stats.StoredGameStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScoreRewardScheme extends HierarchyRewardScheme<Integer> {

    protected ScoreRewardScheme(@NotNull String name) {
        super(name);
    }

    @Override
    protected @NotNull Integer decrement(@NotNull Integer value) {
        return value - 1;
    }

    @Override
    protected @NotNull Integer increment(@NotNull Integer value) {
        return value + 1;
    }

    @Override
    protected @NotNull Integer loadKey(@NotNull String key) throws NumberFormatException {
        return Integer.valueOf(key);
    }

    @Override
    protected @NotNull Component getMenuItemDescName(@NotNull Integer value) {
        return MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_SCORE_DESCRIPTION,
                Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(value)));
    }

    @Override
    protected @NotNull Integer getValue(@Nullable MinigamePlayer player, @NotNull StoredGameStats data, @Nullable Minigame minigame) {
        return (int) data.getStat(MinigameStatistics.Score);
    }

    @Override
    protected @NotNull Component getMenuItemName(@NotNull Integer value) {
        return Component.text(value.toString());
    }
}
