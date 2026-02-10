package au.com.mineauz.minigames.minigame.reward.scheme;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.stats.StoredGameStats;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * RewardSchemes allow more flexibility for reward handling.
 * A simple Primary/Secondary reward system is under {@link StandardRewardScheme}
 */
public abstract class ARewardScheme implements Keyed {
    protected final @NotNull Key key;

    public ARewardScheme(final @NotNull Key key) {
        this.key = key;
    }

    public @NotNull Key key() {
        return key;
    }

    /**
     * Adds menu items to the /mg edit menu for this scheme. These are added under a sub menu
     *
     * @param menu The menu to add into.
     */
    public abstract void addMenuItems(final @NotNull Menu menu);

    /**
     * Awards the player with the rewards specified in this scheme.
     *
     * @param player          The player to be awarded. <b>NOTE:</b> None of the stats will be set at this point. Use {@code data} to get that info
     * @param data            The SQLData for the minigame.
     * @param minigame        The minigame they were playing
     * @param firstCompletion True if this is the first time they are completing the minigame
     */
    public abstract void awardPlayer(final @NotNull MinigamePlayer player, final @NotNull StoredGameStats data, final Minigame minigame, final boolean firstCompletion);

    /**
     * Awards the player with the rewards specified in this scheme.
     * This may not do anything if no lose rewards are available by this scheme
     *
     * @param player   The player to be awarded. <b>NOTE:</b> None of the stats will be set at this point. Use {@code data} to get that info
     * @param data     The SQLData for the minigame.
     * @param minigame The minigame they were playing
     */
    public abstract void awardPlayerOnLoss(final @NotNull MinigamePlayer player, final @NotNull StoredGameStats data, final Minigame minigame);

    /**
     * Saves any extra info for this scheme. Flags will be saved elsewhere
     *
     * @param config The config to write into
     */
    public abstract void save(final @NotNull CommentedConfigurationNode config) throws SerializationException;

    /**
     * Loads any extra info for this scheme. Flags will be loaded elsewhere
     *
     * @param config The config to read from
     */
    public abstract void load(final @NotNull CommentedConfigurationNode config) throws SerializationException;
}
