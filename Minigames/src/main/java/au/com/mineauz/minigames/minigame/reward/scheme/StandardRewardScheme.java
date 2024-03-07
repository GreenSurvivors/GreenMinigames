package au.com.mineauz.minigames.minigame.reward.scheme;

import au.com.mineauz.minigames.config.RewardsFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemDisplayRewards;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.reward.RewardType;
import au.com.mineauz.minigames.minigame.reward.Rewards;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.stats.StoredGameStats;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The standard reward scheme handles the previous reward behaviour.
 * It provides rewards only on victory and has a primary and secondary
 * reward. The primary reward is acquired on the first completion only.
 */
public class StandardRewardScheme extends RewardScheme {
    private final RewardsFlag primaryRewardFlag = new RewardsFlag(new Rewards(), "reward");
    private final RewardsFlag secondaryRewardFlag = new RewardsFlag(new Rewards(), "reward2");

    protected StandardRewardScheme(@NotNull String name) {
        super(name);
    }

    public Rewards getPrimaryReward() {
        return primaryRewardFlag.getFlag();
    }

    public Rewards getSecondaryReward() {
        return secondaryRewardFlag.getFlag();
    }

    @Override
    public void awardPlayer(MinigamePlayer player, StoredGameStats data, Minigame minigame, boolean firstCompletion) {
        List<RewardType> rewards = primaryRewardFlag.getFlag().getReward();

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
    public void awardPlayerOnLoss(MinigamePlayer player, StoredGameStats data, Minigame minigame) {
        // No lose awards
    }

    private void giveRewards(List<RewardType> rewards, MinigamePlayer player) {
        for (RewardType reward : rewards) {
            if (reward != null) {
                MinigameMessageManager.debugMessage("Giving " + player.getName() + " " + reward.getName() + " reward type.");
                reward.giveReward(player);
            }
        }
    }

    @Override
    public void load(@NotNull FileConfiguration config, @NotNull String path) {
        primaryRewardFlag.loadValue(config, path);
        secondaryRewardFlag.loadValue(config, path);
    }

    @Override
    public void save(@NotNull FileConfiguration config, @NotNull String path) {
        primaryRewardFlag.loadValue(config, path);
        secondaryRewardFlag.loadValue(config, path);
    }

    @Override
    public void addMenuItems(Menu menu) {
        menu.addItem(new MenuItemDisplayRewards(Material.CHEST, MgMenuLangKey.MENU_REWARD_PRIMARY_NAME, primaryRewardFlag.getFlag()));
        menu.addItem(new MenuItemDisplayRewards(Material.CHEST, MgMenuLangKey.MENU_REWARD_SECONDARY_NAME, secondaryRewardFlag.getFlag()));
    }
}
