package au.com.mineauz.minigames.minigame.reward;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RewardTypes {
    private static final @NotNull Map<@NotNull String, @NotNull RewardTypeFactory> REGISTERED_TYPES = new HashMap<>();

    static {
        for (final @NotNull MgDefaultRewardType factory : MgDefaultRewardType.values()) {
            registerRewardType(factory);
        }
    }

    public static void registerRewardType(final @NotNull RewardTypeFactory factory) {
        if (REGISTERED_TYPES.containsKey(factory.getName())) {
            throw new InvalidRewardTypeException("A reward type already exists by that name");
        } else {
            REGISTERED_TYPES.put(factory.getName(), factory);
        }
    }

    public static @Nullable ARewardType getRewardType(final @NotNull String name, final @NotNull Rewards rewards) {
        if (REGISTERED_TYPES.containsKey(name.toUpperCase())) {
            return REGISTERED_TYPES.get(name.toUpperCase()).makeNewType(rewards);
        }
        return null;
    }

    public static @NotNull List<@NotNull RewardTypeFactory> getRewardTypeFactories() {
        return new ArrayList<>(REGISTERED_TYPES.values());
    }

    public interface RewardTypeFactory {
        @NotNull ARewardType makeNewType(@NotNull Rewards rewards);

        @NotNull String getName();
    }

    public enum MgDefaultRewardType implements RewardTypeFactory {
        COMMAND(CommandReward::new),
        ITEM(ItemReward::new),
        MONEY(MoneyReward::new);

        final @NotNull Function<@NotNull Rewards, ? extends @NotNull ARewardType> init;

        MgDefaultRewardType(@NotNull Function<@NotNull Rewards, ? extends @NotNull ARewardType> init) {
            this.init = init;
        }

        @Override
        public @NotNull ARewardType makeNewType(final @NotNull Rewards rewards) {
            return init.apply(rewards);
        }

        @Override
        public @NotNull String getName() {
            return toString();
        }
    }
}
