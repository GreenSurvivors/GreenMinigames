package au.com.mineauz.minigames.minigame.reward;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RewardGroup {
    private final String groupName;
    private final List<ARewardType> items = new ArrayList<>();
    private RewardRarity rarity;

    public RewardGroup(String groupName, RewardRarity rarity) {
        this.groupName = groupName;
        this.rarity = rarity;
    }

    public static @Nullable RewardGroup load(@NotNull Configuration config, @NotNull String path, @NotNull Rewards container) {
        char configSeparator = config.options().pathSeparator();
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section != null) {
            RewardRarity rarity = RewardRarity.valueOf(config.getString(path + configSeparator + "rarity"));

            int index = path.lastIndexOf(configSeparator);
            String groupName;
            if (index > 0) {
                groupName = path.substring(index + 1);
            } else {
                groupName = path;
            }
            RewardGroup group = new RewardGroup(groupName, rarity);

            // Load contents
            for (String key : section.getKeys(false)) {
                if (key.equals("rarity")) {
                    continue;
                }

                ARewardType rew = RewardTypes.getRewardType(config.getString(path + configSeparator + key + "type"), container);
                rew.loadReward(config, path + key + configSeparator + "data");
                group.addItem(rew);
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

    public List<ARewardType> getItems() {
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

    public void save(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();
        int index = 0;
        for (ARewardType item : items) {
            config.set(path + configSeparator + index + configSeparator + "type", item.getName());
            item.saveReward(config, path + configSeparator + index + configSeparator + "data");
            index++;
        }

        config.set(path + configSeparator + "rarity", rarity.name());
    }
}
