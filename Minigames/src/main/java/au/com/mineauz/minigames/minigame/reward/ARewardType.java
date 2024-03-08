package au.com.mineauz.minigames.minigame.reward;

import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

public abstract class ARewardType {
    private final @NotNull Rewards rewards;
    private @NotNull RewardRarity rarity = RewardRarity.NORMAL;

    public ARewardType(@NotNull Rewards rewards) {
        this.rewards = rewards;
    }

    public @NotNull RewardRarity getRarity() {
        return rarity;
    }

    public void setRarity(@NotNull RewardRarity rarity) {
        this.rarity = rarity;
    }

    public @NotNull Rewards getRewards() {
        return rewards;
    }

    public abstract @NotNull String getName();

    public abstract boolean isUsable();

    public abstract void giveReward(@NotNull MinigamePlayer mgPlayer);

    public abstract @NotNull MenuItem getMenuItem();

    public abstract void saveReward(@NotNull Configuration config, @NotNull String path);

    public abstract void loadReward(@NotNull Configuration config, @NotNull String path);
}
