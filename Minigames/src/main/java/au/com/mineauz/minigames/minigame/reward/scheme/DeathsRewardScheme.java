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

public class DeathsRewardScheme extends HierarchyRewardScheme<Integer> {

    protected DeathsRewardScheme(@NotNull String name) {
        super(name);
    }

    @Override
    protected @NotNull Integer decrement(Integer value) {
        return value - 1;
    }

    @Override
    protected @NotNull Integer increment(Integer value) {
        return value + 1;
    }

    @Override
    protected @NotNull Integer loadKey(@NotNull String key) {
        return Integer.valueOf(key);
    }

    @Override
    protected @NotNull Component getMenuItemDescName(Integer value) {
        return MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_DEATHS_DESCRIPTION,
                Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(value)));
    }

    @Override
    protected @NotNull Integer getValue(MinigamePlayer player, @NotNull StoredGameStats data, Minigame minigame) {
        return (int) data.getStat(MinigameStatistics.Deaths);
    }

    @Override
    protected @NotNull Component getMenuItemName(@NotNull Integer value) {
        return Component.text(value.toString());
    }
}
