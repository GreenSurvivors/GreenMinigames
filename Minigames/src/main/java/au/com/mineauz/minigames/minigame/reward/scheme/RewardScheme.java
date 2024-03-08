package au.com.mineauz.minigames.minigame.reward.scheme;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.stats.StoredGameStats;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;

/**
 * RewardSchemes allow more flexibility for reward handling.
 * The previous simple Primary/Secondary reward system is under {@link StandardRewardScheme}
 */
public abstract class RewardScheme {
    protected final @NotNull String name;

    public RewardScheme(@NotNull String name) {
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }

    /**
     * Adds menu items to the /mg edit menu for this scheme. These are added under a sub menu
     *
     * @param menu The menu to add into.
     */
    public abstract void addMenuItems(Menu menu);

    /**
     * Awards the player with the rewards specified in this scheme.
     *
     * @param player          The player to be awarded. <b>NOTE:</b> None of the stats will be set at this point. Use {@code data} to get that info
     * @param data            The SQLData for the minigame.
     * @param minigame        The minigame they were playing
     * @param firstCompletion True if this is the first time they are completing the minigame
     */
    public abstract void awardPlayer(MinigamePlayer player, StoredGameStats data, Minigame minigame, boolean firstCompletion);

    /**
     * Awards the player with the rewards specified in this scheme.
     * This may not do anything if no lose rewards are available by this scheme
     *
     * @param player   The player to be awarded. <b>NOTE:</b> None of the stats will be set at this point. Use {@code data} to get that info
     * @param data     The SQLData for the minigame.
     * @param minigame The minigame they were playing
     */
    public abstract void awardPlayerOnLoss(MinigamePlayer player, StoredGameStats data, Minigame minigame);

    /**
     * Saves any extra info for this scheme. Flags will be saved elsewhere
     *
     * @param config The config to write into
     */
    public abstract void save(@NotNull Configuration config, @NotNull String path);

    /**
     * Loads any extra info for this scheme. Flags will be loaded elsewhere
     *
     * @param config The config to read from
     */
    public abstract void load(@NotNull Configuration config, @NotNull String path);
}
