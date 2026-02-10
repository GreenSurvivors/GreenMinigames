package au.com.mineauz.minigames.minigame.reward;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemReward extends ARewardType {
    private @NotNull ItemStack item = ItemType.DIAMOND.createItemStack();

    public ItemReward(final @NotNull Rewards rewards) {
        super(rewards);
    }

    public static ItemReward getMinigameReward(final @NotNull Rewards rewards) {
        return (ItemReward) RewardTypes.getRewardType(RewardTypes.MgDefaultRewardType.ITEM.getName(), rewards);
    }

    @Override
    public @NotNull String getName() {
        return "ITEM";
    }

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public void giveReward(final @NotNull MinigamePlayer mgPlayer) {
        if (mgPlayer.isInMinigame()) {
            mgPlayer.addRewardItem(item);
        } else {
            mgPlayer.getPlayer().give(item);

            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.WIN, MgMiscLangKey.REWARD_ITEM,
                Placeholder.unparsed(MinigamePlaceHolderKey.NUMBER.getKey(), String.valueOf(item.getAmount())),
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), item.displayName()));
        }
    }

    @Override
    public @NotNull AMenuItem getMenuItem() {
        return new MenuItemReward(this);
    }

    @Override
    public void saveReward(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.set(item.serializeAsBytes());
    }

    @Override
    public void loadReward(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        if (config.isMap()) {
            // datafixerupper
            item = ItemStack.deserialize((Map<String, Object>) config.get(TypeFactory.parameterizedClass(Map.class, String.class, Object.class)));
        } else {
            item = ItemStack.deserializeBytes(config.get(TypeToken.get(byte[].class)));
        }
    }

    public ItemStack getRewardItem() {
        return item;
    }

    public void setRewardItem(ItemStack item) {
        this.item = item;
    }

    // note: does not extend MenuItemList by design: This displays a complete ItemStack instead of just an ItemType
    private class MenuItemReward extends AMenuItem {
        private static final @NotNull String DESCRIPTION_REWARD_TOKEN = "Reward_description";
        private final @NotNull ItemReward reward;
        private final @NotNull List<RewardRarity> rarities;

        public MenuItemReward(final @NotNull ItemReward reward) {
            super(reward.item.clone(), reward.item.getItemMeta().displayName());

            rarities = List.of(RewardRarity.values());
            this.reward = reward;

            updateDescriptionRarity();
        }

        @Override
        public @NotNull ItemStack onClickWithItem(final @NotNull ItemStack item) {
            setRewardItem(item.clone());
            setDisplayItem(item);

            // update lore
            updateDescriptionRarity();

            // update display name
            ItemMeta meta = super.getDisplayItem().getItemMeta();
            meta.displayName(item.getItemMeta().displayName());
            super.getDisplayItem().setItemMeta(meta);

            return super.getDisplayItem();
        }

        public void updateDescriptionRarity() {
            final int pos = rarities.indexOf(getRarity());
            int before = pos - 1;
            int after = pos + 1;
            if (before == -1) {
                before = rarities.size() - 1;
            }
            if (after == rarities.size()) {
                after = 0;
            }

            final @NotNull List<@NotNull Component> description = new ArrayList<>();
            description.add(rarities.get(before).getDisplayName().color(NamedTextColor.GRAY));
            description.add(getRarity().getDisplayName().color(NamedTextColor.GREEN));
            description.add(rarities.get(after).getDisplayName().color(NamedTextColor.GRAY));
            description.add(MinigameMessageManager.getMgMessage(
                MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK).color(NamedTextColor.DARK_PURPLE));

            setDescriptionPartAtIndex(DESCRIPTION_REWARD_TOKEN, 0, description);
        }

        @Override
        public @NotNull ItemStack onClick() {
            int ind = rarities.lastIndexOf(getRarity());
            ind++;
            if (ind == rarities.size()) {
                ind = 0;
            }

            setRarity(rarities.get(ind));
            updateDescriptionRarity();

            return getDisplayItem();
        }

        @Override
        public @NotNull ItemStack onRightClick() {
            int ind = rarities.lastIndexOf(getRarity());
            ind--;
            if (ind == -1) {
                ind = rarities.size() - 1;
            }

            setRarity(rarities.get(ind));
            updateDescriptionRarity();

            return getDisplayItem();
        }

        @Override
        public @NotNull ItemStack onShiftRightClick() {
            getRewards().removeReward(reward);
            getMenu().removeItem(getSlot());
            return ItemStack.empty();
        }
    }
}
