package au.com.mineauz.minigames.minigame.reward.scheme;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.config.BooleanFlag;
import au.com.mineauz.minigames.config.EnumFlag;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.reward.ARewardType;
import au.com.mineauz.minigames.minigame.reward.Rewards;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigames.stats.StoredGameStats;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public abstract class HierarchyRewardScheme<T extends @NotNull Comparable<T>> extends ARewardScheme {
    private final @NotNull EnumFlag<Comparison> comparisonType = new EnumFlag<>("comparison", Comparison.Greater);
    private final @NotNull BooleanFlag enableRewardsOnLoss = new BooleanFlag("loss-rewards", false);
    private final @NotNull BooleanFlag lossUsesSecondary = new BooleanFlag("loss-use-secondary", true);

    private final @NotNull TreeMap<T, @NotNull Rewards> primaryRewards = new TreeMap<>();
    private final @NotNull TreeMap<T, @NotNull Rewards> secondaryRewards = new TreeMap<>();

    public HierarchyRewardScheme(@NotNull String name) {
        super(name);
    }

    @Override
    public void addMenuItems(final @NotNull Menu menu) {
        menu.addItem(new MenuItemEnum<>(ItemType.COMPARATOR, MgMenuLangKey.MENU_REWARD_SCHEME_HIERARCHY_COMPARISON_NAME,
            getConfigurationTypeCallback(), Comparison.class));
        menu.addItem(enableRewardsOnLoss.getMenuItem(ItemType.LEVER, MgMenuLangKey.MENU_REWARD_SCHEME_HIERARCHY_LOSS_AWARD_NAME,
            MgMenuLangKey.MENUREWARD_SCHEME_HIERARCHY_LOSS_AWARD_DESCRIPTION));
        menu.addItem(lossUsesSecondary.getMenuItem(ItemType.LEVER, MgMenuLangKey.MENU_REWARD_SCHEME_HIERARCHY_LOSS_SECONDARY_NAME,
            MgMenuLangKey.MENUREWARD_SCHEME_HIERARCHY_LOSS_SECONDARY_DESCRIPTION));
        menu.addItem(new MenuItemNewLine());

        MenuItemCustom primary = new MenuItemCustom(ItemType.CHEST, MgMenuLangKey.MENU_REWARD_PRIMARY_NAME);
        primary.setClick(() -> {
            showRewardsMenu(primaryRewards, menu);
            return ItemStack.empty();
        });

        MenuItemCustom secondary = new MenuItemCustom(ItemType.CHEST, MgMenuLangKey.MENU_REWARD_SECONDARY_NAME);
        secondary.setClick(() -> {
            showRewardsMenu(secondaryRewards, menu);
            return ItemStack.empty();
        });

        menu.addItem(primary);
        menu.addItem(secondary);
    }

    private void showRewardsMenu(final @NotNull TreeMap<T, @NotNull Rewards> rewards, final @NotNull Menu parent) {
        Menu submenu = new Menu(6, MgMenuLangKey.MENU_REWARD_NAME, parent.getIntendedViewer());

        for (T key : rewards.keySet()) {
            submenu.addItem(new MenuItemRewardPair(ItemType.CHEST, rewards, key));
        }

        submenu.setItem(new MenuItemAddReward(MenuDisplayTypes.createType(), MgMenuLangKey.MENU_REWARD_SET_ADD_NAME, rewards), submenu.getSize() - 2);
        submenu.setItem(new MenuItemBack(parent), submenu.getSize() - 1);

        submenu.setPreviousPage(parent);

        submenu.displayMenu();
    }

    protected abstract T getValue(final @NotNull MinigamePlayer player, final @NotNull StoredGameStats data, final Minigame minigame);

    @Override
    public void awardPlayer(final @NotNull MinigamePlayer player, final @NotNull StoredGameStats data, final Minigame minigame,
                            final boolean firstCompletion) {
        final T value = getValue(player, data, minigame);
        @Nullable Rewards reward;

        final @NotNull TreeMap<T, @NotNull Rewards> rewards = (firstCompletion ? primaryRewards : secondaryRewards);

        // Calculate rewards
        switch (comparisonType.getFlag()) {
            case Equal -> reward = rewards.get(value);
            case Lesser -> {
                reward = null;
                for (final @NotNull Entry<T, @NotNull Rewards> entry : rewards.entrySet()) {
                    if (value.compareTo(entry.getKey()) < 0) {
                        reward = entry.getValue();
                        break;
                    }
                }
            }
            case Greater -> {
                reward = null;
                for (final @NotNull Entry<T, @NotNull Rewards> entry : rewards.descendingMap().entrySet()) {
                    if (value.compareTo(entry.getKey()) > 0) {
                        reward = entry.getValue();
                        break;
                    }
                }
            }
            default -> throw new AssertionError();
        }

        // Apply reward
        if (reward != null) {
            final @Nullable List<@NotNull ARewardType> rewardItems = reward.getReward();
            if (rewardItems != null) {
                for (final @NotNull ARewardType item : rewardItems) {
                    item.giveReward(player);
                }
            }
        }
    }

    @Override
    public void awardPlayerOnLoss(final @NotNull MinigamePlayer player, final @NotNull StoredGameStats data, final Minigame minigame) {
        if (enableRewardsOnLoss.getFlag())
            awardPlayer(player, data, minigame, lossUsesSecondary.getFlag());
    }

    @Override
    public void save(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        comparisonType.saveValue(config);
        enableRewardsOnLoss.saveValue(config);
        lossUsesSecondary.saveValue(config);

        save(primaryRewards, config.node("score-primary"));
        save(secondaryRewards, config.node("score-secondary"));
    }

    private void save(final @NotNull TreeMap<@NotNull T, @NotNull Rewards> map, final @NotNull CommentedConfigurationNode config) throws SerializationException {
        for (Entry<T, Rewards> entry : map.entrySet()) {
            entry.getValue().save(config.node(entry.getKey()));
        }
    }

    @Override
    public void load(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        comparisonType.loadValue(config);
        enableRewardsOnLoss.loadValue(config);
        lossUsesSecondary.loadValue(config);

        load(primaryRewards, config.node("score-primary"));
        load(secondaryRewards, config.node("score-secondary"));
    }

    protected abstract T loadKey(final @NotNull Object key);

    private void load(final @NotNull TreeMap<@NotNull T, @NotNull Rewards> map, final @NotNull CommentedConfigurationNode config) throws SerializationException {
        map.clear();

        if (!config.virtual() && !config.isNull()) {
            for (final @NotNull Map.Entry<@NotNull Object, @NotNull CommentedConfigurationNode> entry: config.childrenMap().entrySet()) {
                T value = loadKey(entry.getKey());

                final @NotNull Rewards reward = new Rewards();
                reward.load(entry.getValue());
                map.put(value, reward);
            }
        }
    }

    private @NotNull Callback<@NotNull Comparison> getConfigurationTypeCallback() {
        return new Callback<>() {
            @Override
            public @NotNull Comparison getValue() {
                return comparisonType.getFlag();
            }

            @Override
            public void setValue(@NotNull Comparison value) {
                comparisonType.setFlag(value);
            }
        };
    }

    protected abstract @NotNull Component getMenuItemName(T value);

    protected abstract @NotNull Component getMenuItemDescName(T value);

    protected abstract T increment(T value);

    protected abstract T decrement(T value);

    public enum Comparison {
        Greater,
        Equal,
        Lesser
    }

    private class MenuItemRewardPair extends AMenuItem implements StringConsumer {
        private static final String DESCRIPTION_TOKEN = "RewardPair_description";
        private final @NotNull Rewards reward;
        private final @NotNull TreeMap<@NotNull T, @NotNull Rewards> map;
        private @NotNull T value;

        public MenuItemRewardPair(@Nullable ItemType displayType, @NotNull TreeMap<@NotNull T, @NotNull Rewards> map,
                                  @NotNull T value) {
            super(displayType, getMenuItemName(value));

            this.map = map;
            this.value = value;
            this.reward = map.get(value);

            updateDescription();
        }

        private void updateDescription() {
            List<Component> description = List.of(
                getMenuItemDescName(value).color(NamedTextColor.GREEN),
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARDPAIR_EDIT),
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK)
            );

            setDescriptionPart(DESCRIPTION_TOKEN, description);

            // Update name
            ItemStack item = getDisplayItem();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(getMenuItemName(value));
                item.setItemMeta(meta);
            }

            setDisplayItem(item);
        }

        private void updateValue(@NotNull T newValue) {
            map.remove(value);
            value = newValue;
            map.put(value, reward);
        }

        @Override
        // Increase score
        public @NotNull ItemStack onClick() {
            T nextValue = increment(value);
            while (map.containsKey(nextValue)) {
                nextValue = increment(nextValue);
            }

            updateValue(nextValue);

            updateDescription();
            return getDisplayItem();
        }

        @Override
        // Decrease score
        public @NotNull ItemStack onRightClick() {
            T nextValue = decrement(value);
            while (map.containsKey(nextValue)) {
                nextValue = decrement(nextValue);
            }

            updateValue(nextValue);

            updateDescription();
            return getDisplayItem();
        }

        // Open editor
        @Override
        public @NotNull ItemStack onDoubleClick() {
            MinigamePlayer mgPlayer = getMenu().getIntendedViewer();
            final @NotNull Duration reopenTime = Duration.ofSeconds(10);
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_HIERARCHY_ENTERCHAT,
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));

            getMenu().closeAndWaitForInput(reopenTime, this);

            return ItemStack.empty();
        }

        @Override
        public void acceptString(final @NotNull String entry) {
            try {
                T value = loadKey(entry);
                if (map.containsKey(value)) {
                    MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMiscLangKey.REWARDSCHEME_ERROR_DUPLICATE);
                } else {
                    updateValue(value);
                    updateDescription();
                }
            } catch (IllegalArgumentException e) {
                MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMiscLangKey.REWARDSCHEME_ERROR_INVALID);
            }

            getMenu().cancelWaitForInput();
            getMenu().displayMenu();
        }

        @Override
        // Open rewards
        public @NotNull ItemStack onShiftClick() {
            final @NotNull Menu rewardMenu = reward.createMenu(getName(), getMenu());

            rewardMenu.displayMenu();
            return ItemStack.empty();
        }

        @Override
        // Remove
        public @NotNull ItemStack onShiftRightClick() {
            getMenu().removeItem(getSlot());
            map.remove(value);

            return getDisplayItem();
        }
    }

    public class MenuItemAddReward extends AMenuItem implements StringConsumer {
        private final @NotNull TreeMap<@NotNull T, @NotNull Rewards> map;

        public MenuItemAddReward(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey,
                                 @NotNull TreeMap<@NotNull T, @NotNull Rewards> map) {
            super(displayType, langKey);

            this.map = map;
        }

        public MenuItemAddReward(@Nullable ItemType displayType, @Nullable Component name,
                                 @NotNull TreeMap<@NotNull T, @NotNull Rewards> map) {
            super(displayType, name);

            this.map = map;
        }

        @Override
        public @NotNull ItemStack onClick() {
            MinigamePlayer mgPlayer = getMenu().getIntendedViewer();
            final @NotNull Duration reopenTime = Duration.ofSeconds(10);
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_HIERARCHY_ENTERCHAT,
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));

            getMenu().closeAndWaitForInput(reopenTime, this);

            return ItemStack.empty();
        }

        @Override
        public void acceptString(final @NotNull String entry) {
            boolean show = true;

            try {
                T value = loadKey(entry);
                Rewards reward = new Rewards();

                if (map.containsKey(value)) {
                    MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMiscLangKey.REWARDSCHEME_ERROR_DUPLICATE);
                } else {
                    map.put(value, reward);
                    showRewardsMenu(map, getMenu().getPreviousPage());
                    show = false;
                }
            } catch (IllegalArgumentException e) {
                MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMiscLangKey.REWARDSCHEME_ERROR_INVALID);
            }

            getMenu().cancelWaitForInput();
            if (show) {
                getMenu().displayMenu();
            }
        }
    }
}
