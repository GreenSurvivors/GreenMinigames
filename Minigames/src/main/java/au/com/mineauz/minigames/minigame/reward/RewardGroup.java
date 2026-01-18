package au.com.mineauz.minigames.minigame.reward;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public class RewardGroup {
    private final String groupName;
    private final @NotNull List<@NotNull ARewardType> items = new ArrayList<>();
    private RewardRarity rarity;

    public RewardGroup(String groupName, RewardRarity rarity) {
        this.groupName = groupName;
        this.rarity = rarity;
    }

    public static @Nullable RewardGroup load(final @NotNull CommentedConfigurationNode config, final @NotNull String groupName, final @NotNull Rewards container) throws SerializationException {
        if (!config.virtual() && !config.isNull()) {
            final @NotNull RewardRarity rarity = RewardRarity.valueOf(config.node("rarity").getString());
            final @NotNull RewardGroup group = new RewardGroup(groupName, rarity);

            // Load contents
            for (final @NotNull CommentedConfigurationNode rewardEntry : config.childrenList()) {
                if (rewardEntry.key().toString().equals("rarity")) {
                    continue;
                }

                final @NotNull ARewardType type = RewardTypes.getRewardType(rewardEntry.node("type").getString(), container);
                type.loadReward(rewardEntry.node("data"));
                group.addItem(type);
            }

            return group;
        } else {
            return null;
        }
    }

    public String getName() {
        return groupName;
    }

    public void addItem(ARewardType item) {
        items.add(item);
    }

    public void removeItem(ARewardType item) {
        items.remove(item);
    }

    public @NotNull List<@NotNull ARewardType> getItems() {
        return items;
    }

    public RewardRarity getRarity() {
        return rarity;
    }

    public void setRarity(RewardRarity rarity) {
        this.rarity = rarity;
    }

    public void clearGroup() {
        items.clear();
    }

    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        int index = 0;
        for (final @NotNull ARewardType item : items) {
            final @NotNull CommentedConfigurationNode indexedNode = config.node(index++);

            indexedNode.node("type").raw(item.getName());
            item.saveReward(indexedNode.node("data"));
        }

        config.node("rarity").set(rarity);
    }
}
