package au.com.mineauz.minigames.minigame.reward;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Rewards {
    private final @NotNull List<@NotNull ARewardType> items = new ArrayList<>();
    private final @NotNull List<@NotNull RewardGroup> groups = new ArrayList<>();

    public boolean isEmpty() {
        return items.isEmpty() && groups.isEmpty();
    }

    public @Nullable List<@NotNull ARewardType> getReward() {
        double rand = ThreadLocalRandom.current().nextDouble();
        RewardRarity rarity;
        List<Object> itemsCopyList = new ArrayList<>();
        itemsCopyList.addAll(items);
        itemsCopyList.addAll(groups);
        Collections.shuffle(itemsCopyList);

        if (rand > RewardRarity.VERY_COMMON.getRarity()) {
            rarity = RewardRarity.VERY_COMMON;
        } else if (rand > RewardRarity.COMMON.getRarity()) {
            rarity = RewardRarity.COMMON;
        } else if (rand > RewardRarity.NORMAL.getRarity()) {
            rarity = RewardRarity.NORMAL;
        } else if (rand > RewardRarity.RARE.getRarity()) {
            rarity = RewardRarity.RARE;
        } else {
            rarity = RewardRarity.VERY_RARE;
        }

        if (!itemsCopyList.isEmpty()) {
            ARewardType item = null;
            RewardGroup group = null;
            final RewardRarity originalRarity = rarity;
            boolean up = false;

            while (item == null && group == null) {
                for (Object ritem : itemsCopyList) {
                    if (ritem instanceof ARewardType ri) {
                        if (ri.getRarity() == rarity) {
                            item = ri;
                            break;
                        }
                    } else {
                        RewardGroup rg = (RewardGroup) ritem;
                        if (rg.getRarity() == rarity) {
                            group = rg;
                            break;
                        }
                    }
                }

                // nothing in the list with the same rarity
                // only go up if there is no way further down
                if (rarity == RewardRarity.VERY_COMMON && !up) {
                    rarity = originalRarity;
                    up = true;
                }

                if (up) {
                    rarity = rarity.getHigherRarity();
                } else {
                    rarity = rarity.getLowerRarity();
                }
            }

            if (item != null) {
                return List.of(item);
            } else {
                return group.getItems();
            }
        }

        return null;
    }

    public void addReward(ARewardType reward) {
        items.add(reward);
    }

    public void removeReward(ARewardType item) {
        items.remove(item);
    }

    public @NotNull List<@NotNull ARewardType> getRewards() {
        return items;
    }

    public @NotNull RewardGroup addGroup(String groupName, RewardRarity rarity) {
        RewardGroup group = new RewardGroup(groupName, rarity);
        groups.add(group);
        return group;
    }

    public void removeGroup(@NotNull RewardGroup group) {
        groups.remove(group);
    }

    public @NotNull List<@NotNull RewardGroup> getGroups() {
        return groups;
    }

    public @NotNull Menu createMenu(@NotNull Component name, @NotNull MinigamePlayer player, @NotNull Menu parent) {
        Menu rewardMenu = new Menu(5, name, player);

        rewardMenu.setPreviousPage(parent);

        rewardMenu.addItem(new MenuItemRewardGroupAdd(MenuUtility.getCreateMaterial(),
                MgMenuLangKey.MENU_REWARD_GROUP_ADD_NAME, this), 42);
        rewardMenu.addItem(new MenuItemRewardAdd(MenuUtility.getCreateMaterial(), MgMenuLangKey.MENU_REWARD_ITEM_ADD_NAME, this), 43);
        rewardMenu.addItem(new MenuItemPage(MenuUtility.getSaveMaterial(),
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_SAVE_NAME,
                        Placeholder.component(MinigamePlaceHolderKey.REWARD.getKey(), name)),
                parent), 44);

        List<MenuItem> mi = new ArrayList<>();
        for (ARewardType item : items) {
            mi.add(item.getMenuItem());
        }

        List<Component> des = MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_EDIT_SHIFTLEFT);
        for (RewardGroup group : groups) {
            MenuItemRewardGroup rwg = new MenuItemRewardGroup(Material.CHEST,
                    MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_GROUP_NAME,
                            Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), group.getName())),
                    des, group, this);
            mi.add(rwg);
        }
        rewardMenu.addItems(mi);

        return rewardMenu;
    }

    public void save(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();
        int index = 0;
        for (ARewardType item : items) {
            config.set(path + configSeparator + index + configSeparator + "type", item.getName());
            config.set(path + configSeparator + index + configSeparator + "rarity", item.getRarity().name());
            item.saveReward(config, path + configSeparator + index + configSeparator + "data");
            index++;
        }

        for (RewardGroup group : groups) {
            group.save(config, path + configSeparator + group.getName());
        }
    }

    public void load(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();
        ConfigurationSection section = config.getConfigurationSection(path);

        if (section != null) {
            for (String key : section.getKeys(false)) {
                // Load reward item
                if (section.contains(key + configSeparator + "type")) {
                    final String rawRewardType = section.getString(key + configSeparator + "type", "");
                    ARewardType rewardType = RewardTypes.getRewardType(rawRewardType, this);
                    if (rewardType != null) {
                        rewardType.loadReward(config, path + configSeparator + key + configSeparator + "data");
                        rewardType.setRarity(RewardRarity.valueOf(config.getString(
                                path + configSeparator + key + configSeparator + "rarity")));
                        addReward(rewardType);
                    } else {
                        Minigames.getCmpnntLogger().warn("Could not load rewardType of '" +
                                path + configSeparator + key + configSeparator + "type' with value: '" + rawRewardType + "'! Ignoring.");
                    }
                } else { // Load reward group
                    groups.add(RewardGroup.load(config, path + configSeparator + key, this));
                }
            }
        }
    }
}
