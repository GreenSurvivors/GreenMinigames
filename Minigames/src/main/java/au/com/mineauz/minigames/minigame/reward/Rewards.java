package au.com.mineauz.minigames.minigame.reward;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamesKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Rewards {
    private final @NotNull List<@NotNull ARewardType> rewardTypes = new ArrayList<>();
    private final @NotNull Map<@NotNull String, @NotNull RewardGroup> groups = new LinkedHashMap<>();

    public boolean isEmpty() {
        return rewardTypes.isEmpty() && groups.isEmpty();
    }

    public @Nullable List<@NotNull ARewardType> getReward() {
        final double rand = ThreadLocalRandom.current().nextDouble();
        @NotNull RewardRarity rarity;
        final @NotNull List<Object> itemsCopyList = new ArrayList<>();
        itemsCopyList.addAll(rewardTypes);
        itemsCopyList.addAll(groups.values());
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
            @Nullable ARewardType item = null;
            @Nullable RewardGroup group = null;
            final RewardRarity originalRarity = rarity;
            boolean up = false;

            while (item == null && group == null) {
                for (final @NotNull Object ritem : itemsCopyList) {
                    if (ritem instanceof final @NotNull ARewardType ri) {
                        if (ri.getRarity() == rarity) {
                            item = ri;
                            break;
                        }
                    } else {
                        final @NotNull RewardGroup rg = (RewardGroup) ritem;
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

    public void addReward(final @NotNull ARewardType reward) {
        rewardTypes.add(reward);
    }

    public void removeReward(final @NotNull ARewardType rewardType) {
        rewardTypes.remove(rewardType);
    }

    public @NotNull List<@NotNull ARewardType> getRewards() {
        return rewardTypes;
    }

    public @NotNull RewardGroup addNewGroup(final @NotNull String groupName, final @NotNull RewardRarity rarity) {
        final @NotNull RewardGroup group = new RewardGroup(groupName, rarity);
        groups.put(groupName, group);
        return group;
    }

    public boolean removeGroup(final @NotNull RewardGroup group) {
        return groups.remove(group.getName()) != null;
    }

    public @Nullable RewardGroup removeGroupByName(final @NotNull String groupName) {
        return groups.remove(groupName);
    }

    public @Nullable RewardGroup getGroupByName(final @NotNull String groupName) {
        return groups.get(groupName);
    }

    public @NotNull Collection<@NotNull RewardGroup> getGroups() {
        return groups.values();
    }

    @NotNull
    public Menu createMenu(final @NotNull Component name, final @NotNull Menu parent) {
        final @NotNull Menu rewardMenu = new Menu(5, name, parent.getIntendedViewer());

        rewardMenu.setPreviousPage(parent);

        rewardMenu.setItem(new MenuItemRewardGroupAdd(MenuDisplayTypes.createType(),
            MgMenuLangKey.MENU_REWARD_GROUP_ADD_NAME, this), 42);
        rewardMenu.setItem(new MenuItemRewardAdd(MenuDisplayTypes.createType(), MgMenuLangKey.MENU_REWARD_ITEM_ADD_NAME, this), 43);
        rewardMenu.setItem(new MenuItemPage(MenuDisplayTypes.saveType(),
            MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_SAVE_NAME,
                Placeholder.component(MinigamePlaceHolderKey.REWARD.getKey(), name)),
            parent), 44);

        final @NotNull List<@NotNull AMenuItem> mi = new ArrayList<>();
        for (final @NotNull ARewardType item : rewardTypes) {
            mi.add(item.getMenuItem());
        }

        final @NotNull List<@NotNull Component> des = MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_EDIT_SHIFTLEFT);
        for (final @NotNull RewardGroup group : getGroups()) {
            final @NotNull MenuItemRewardGroup rwg = new MenuItemRewardGroup(ItemType.BUNDLE,
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_GROUP_NAME,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), group.getName())),
                des, group, this);
            mi.add(rwg);
        }
        rewardMenu.addItems(mi);

        return rewardMenu;
    }

    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        int index = 0;
        for (final @NotNull ARewardType item : rewardTypes) {
            final @NotNull CommentedConfigurationNode indexedNode = config.node(index++);

            indexedNode.node("type").raw(item.key().asMinimalString());
            indexedNode.node("rarity").raw(item.getRarity().name());
            item.saveReward(indexedNode.node("data"));
        }

        if (!groups.isEmpty()) {
            final @NotNull CommentedConfigurationNode groupNode = config.node("groups");

            for (final @NotNull RewardGroup group : getGroups()) {
                group.save(groupNode.node(group.getName()));
            }
        }
    }

    public void load(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        for (final @NotNull CommentedConfigurationNode rewardEntryNode : config.childrenList()) {
            // Load reward item
            if (rewardEntryNode.hasChild("type")) {
                final @Nullable String rawRewardType = rewardEntryNode.node("type").getString();

                @Nullable ARewardType rewardType = null;
                if (rawRewardType != null) {
                    final @Nullable Key rewardTypeKey = MinigamesKey.fromString(rawRewardType);

                    if (rewardTypeKey != null) {
                        rewardType = RewardTypes.getRewardType(rewardTypeKey, this);
                    }
                }

                if (rewardType != null) {
                    rewardType.loadReward(rewardEntryNode.node("data"));
                    rewardType.setRarity(RewardRarity.valueOf(rewardEntryNode.node("rarity").getString()));
                    addReward(rewardType);
                } else {
                    Minigames.getPlugin().getComponentLogger().warn("Could not load rewardType of '" + rewardEntryNode.path() + "type' with value: '" + rawRewardType + "'! Ignoring.");
                }
            } else if (rewardEntryNode.key().equals("groups")) { // Load reward groups
                for (final @NotNull CommentedConfigurationNode groupEntryNode : config.childrenList()) {
                    final String groupName = groupEntryNode.key().toString();
                    groups.put(groupName, RewardGroup.load(groupEntryNode, groupName, this));
                }
            } else { // datafixerupper
                final String groupName = rewardEntryNode.key().toString();
                groups.put(groupName, RewardGroup.load(rewardEntryNode, groupName, this));
            }
        }
    }
}
