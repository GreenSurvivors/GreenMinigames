package au.com.mineauz.minigames.minigame.reward;

import au.com.mineauz.minigames.objects.MinigamesKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class RewardTypes {
    private static final @NotNull Map<@NotNull Key, @NotNull RewardTypeFactory> REGISTERED_TYPES = new HashMap<>();

    static {
        for (final @NotNull MgDefaultRewardType factory : MgDefaultRewardType.values()) {
            registerRewardType(factory);
        }
    }

    public static void registerRewardType(final @NotNull RewardTypeFactory factory) {
        if (REGISTERED_TYPES.containsKey(factory.key())) {
            throw new InvalidRewardTypeException("A reward type already exists by that name");
        } else {
            REGISTERED_TYPES.put(factory.key(), factory);
        }
    }

    public static @Nullable ARewardType getRewardType(final @NotNull Key key, final @NotNull Rewards rewards) {
        if (REGISTERED_TYPES.containsKey(key)) {
            return REGISTERED_TYPES.get(key).makeNewType(rewards);
        }
        return null;
    }

    /// while the returned list is in the current implementation modifiable, no guarantees are made
    /// it stays this way in the future.
    /// Changing the list does not change the
    public static @NotNull List<@NotNull RewardTypeFactory> getRewardTypeFactories() {
        return new ArrayList<>(REGISTERED_TYPES.values());
    }

    public interface RewardTypeFactory extends Keyed {
        @NotNull ARewardType makeNewType(final @NotNull Rewards rewards);
    }

    public enum MgDefaultRewardType implements RewardTypeFactory {
        COMMAND("command", CommandReward::new),
        ITEM("item", ItemReward::new),
        MONEY("money", MoneyReward::new);

        final @NotNull BiFunction<@NotNull Key, @NotNull Rewards, ? extends @NotNull ARewardType> init;
        final @NotNull Key key;

        MgDefaultRewardType(final @NotNull String name, final @NotNull BiFunction<@NotNull Key, @NotNull Rewards, ? extends @NotNull ARewardType> init) {
            this.init = init;
            this.key = MinigamesKey.minigames(name);
        }

        @Override
        public @NotNull ARewardType makeNewType(final @NotNull Rewards rewards) {
            return init.apply(key, rewards);
        }

        @Override
        public @NotNull Key key() {
            return key;
        }
    }
}
