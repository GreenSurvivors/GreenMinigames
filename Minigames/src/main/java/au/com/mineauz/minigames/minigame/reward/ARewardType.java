package au.com.mineauz.minigames.minigame.reward;

import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public abstract class ARewardType implements Keyed {
    private final @NotNull Key key;
    private final @NotNull Rewards rewards;
    private @NotNull RewardRarity rarity = RewardRarity.NORMAL;

    public ARewardType(final @NotNull Key key, final @NotNull Rewards rewards) {
        this.key = key;
        this.rewards = rewards;
    }

    public @NotNull Key key() {
        return key;
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

    public abstract boolean isUsable();

    public abstract void giveReward(@NotNull MinigamePlayer mgPlayer);

    public abstract @NotNull AMenuItem getMenuItem();

    public abstract void saveReward(final @NotNull CommentedConfigurationNode config) throws SerializationException;

    public abstract void loadReward(final @NotNull CommentedConfigurationNode config) throws SerializationException;
}
