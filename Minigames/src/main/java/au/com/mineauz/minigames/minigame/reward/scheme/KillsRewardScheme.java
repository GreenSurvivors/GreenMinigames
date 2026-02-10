package au.com.mineauz.minigames.minigame.reward.scheme;

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

public class KillsRewardScheme extends HierarchyRewardScheme<@NotNull Integer> {

    protected KillsRewardScheme(final @NotNull Key key) {
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
    protected @NotNull Integer loadKey(final @NotNull Object key) {
        if (key instanceof Number number) {
            return number.intValue();
        }

        return Integer.valueOf(key.toString());
    }

    @Override
    protected @NotNull Component getMenuItemDescName(final @NotNull Integer value) {
        return MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_KILLS_DESCRIPTION,
            Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(value)));
    }

    @Override
    protected @NotNull Integer getValue(final @Nullable MinigamePlayer player,
                                        final @NotNull StoredGameStats data,
                                        final @Nullable Minigame minigame) {
        return (int) data.getStat(MinigameStatistics.Kills);
    }

    @Override
    protected @NotNull Component getMenuItemName(final @NotNull Integer value) {
        return Component.text(value.toString());
    }
}
