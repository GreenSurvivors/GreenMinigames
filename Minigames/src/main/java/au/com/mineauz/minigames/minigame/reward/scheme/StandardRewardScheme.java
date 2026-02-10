package au.com.mineauz.minigames.minigame.reward.scheme;

import au.com.mineauz.minigames.config.RewardsFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemDisplayRewards;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.reward.ARewardType;
import au.com.mineauz.minigames.minigame.reward.Rewards;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.stats.StoredGameStats;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

/**
 * The standard reward scheme handles the previous reward behaviour.
 * It provides rewards only on victory and has a primary and secondary
 * reward. The primary reward is acquired on the first completion only.
 */
public class StandardRewardScheme extends ARewardScheme {
    private final @NotNull RewardsFlag primaryRewardFlag = new RewardsFlag("reward", new Rewards());
    private final @NotNull RewardsFlag secondaryRewardFlag = new RewardsFlag("reward2", new Rewards());

    protected StandardRewardScheme(final @NotNull String name) {
        super(name);
    }

    public Rewards getPrimaryReward() {
        return primaryRewardFlag.getFlag();
    }

    public Rewards getSecondaryReward() {
        return secondaryRewardFlag.getFlag();
    }

    @Override
    public void awardPlayer(final @NotNull MinigamePlayer player, final @Nullable StoredGameStats data, final @Nullable Minigame minigame, boolean firstCompletion) {
        @Nullable List<@NotNull ARewardType> rewards = primaryRewardFlag.getFlag().getReward();

        if (firstCompletion && rewards != null) {
            MinigameMessageManager.debugMessage("Issue Primary Reward for " + player.getName());
            giveRewards(rewards, player);
        } else {
            rewards = secondaryRewardFlag.getFlag().getReward();
            if (rewards != null) {
                MinigameMessageManager.debugMessage("Issue Secondary Reward for " + player.getName());
                giveRewards(rewards, player);
            }
        }

        player.updateInventory();
    }

    @Override
    public void awardPlayerOnLoss(final @Nullable MinigamePlayer player, final @Nullable StoredGameStats data, final @Nullable Minigame minigame) {
        // No lose awards
    }

    private void giveRewards(final @NotNull List<@Nullable ARewardType> rewards, final @NotNull MinigamePlayer player) {
        for (ARewardType reward : rewards) {
            if (reward != null) {
                MinigameMessageManager.debugMessage("Giving " + player.getName() + " " + reward.getName() + " reward type.");
                reward.giveReward(player);
            }
        }
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        primaryRewardFlag.loadValue(config);
        secondaryRewardFlag.loadValue(config);
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        primaryRewardFlag.loadValue(config);
        secondaryRewardFlag.loadValue(config);
    }

    @Override
    public void addMenuItems(final @NotNull Menu menu) {
        menu.addItem(new MenuItemDisplayRewards(ItemType.CHEST, MgMenuLangKey.MENU_REWARD_PRIMARY_NAME, primaryRewardFlag.getFlag()));
        menu.addItem(new MenuItemDisplayRewards(ItemType.CHEST, MgMenuLangKey.MENU_REWARD_SECONDARY_NAME, secondaryRewardFlag.getFlag()));
    }
}
