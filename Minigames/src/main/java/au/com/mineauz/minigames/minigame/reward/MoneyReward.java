package au.com.mineauz.minigames.minigame.reward;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoneyReward extends ARewardType {
    private static final @NotNull Minigames PLUGIN = Minigames.getPlugin();
    private double money = 0d;

    public MoneyReward(final @NotNull Key key, final @NotNull Rewards rewards) {
        super(key, rewards);
    }

    public static @Nullable MoneyReward getMinigameReward(final @NotNull Rewards rewards) {
        return (MoneyReward) RewardTypes.getRewardType(RewardTypes.MgDefaultRewardType.MONEY.key(), rewards);
    }

    @Override
    public boolean isUsable() {
        return PLUGIN.getEconomy() != null;
    }

    @Override
    public void giveReward(final @NotNull MinigamePlayer mgPlayer) {
        final @Nullable Economy economy = PLUGIN.getEconomy();

        if (economy != null) {
            economy.depositPlayer(mgPlayer.getPlayer().getPlayer(), money);
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.WIN, MgMiscLangKey.REWARD_MONEY,
                Placeholder.unparsed(MinigamePlaceHolderKey.MONEY.getKey(), economy.format(money)));
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_WARNING_NOVAULT);
        }
    }

    @Override
    public @NotNull AMenuItem getMenuItem() {
        return new MenuItemReward(this);
    }

    @Override
    public void saveReward(final @NotNull CommentedConfigurationNode config) {
        config.raw(money);
    }

    @Override
    public void loadReward(final @NotNull CommentedConfigurationNode config) {
        money = config.getDouble();
    }

    public double getRewardMoney() {
        return money;
    }

    public void setRewardMoney(double amount) {
        money = amount;
    }

    private class MenuItemReward extends MenuItemList<@NotNull RewardRarity> {
        private static final @NotNull String DESCRIPTION_TOKEN = "Reward_description";
        private final @NotNull MoneyReward reward;

        public MenuItemReward(final @NotNull MoneyReward reward) {
            super(ItemType.PAPER, MinigameUtils.formatMoney(money), new Callback<>() {
                @Override
                public @NotNull RewardRarity getValue() {
                    return getRarity();
                }

                @Override
                public void setValue(final @NotNull RewardRarity value) {
                    setRarity(value);
                }
            }, Arrays.asList(RewardRarity.values()));
            this.reward = reward;

            final @NotNull List<@NotNull Component> description = new ArrayList<>();
            description.add(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_CHANGE_SHIFTCLICK).color(NamedTextColor.DARK_PURPLE));
            description.add(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK).color(NamedTextColor.DARK_PURPLE));

            setDescriptionPart(DESCRIPTION_TOKEN, description);
        }

        @Override
        public @NotNull ItemStack onShiftClick() {
            final @NotNull Menu menu = new Menu(3, MgMenuLangKey.MENU_MONEYREWARD_MENU_NAME, getMenu().getIntendedViewer());
            final @NotNull MenuItemDecimal dec = new MenuItemDecimal(ItemType.PAPER,
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_MONEYREWARD_ITEM_NAME),
                new Callback<>() {
                    @Override
                    public @NotNull Double getValue() {
                        return reward.money;
                    }

                    @Override
                    public void setValue(@NotNull Double value) {
                        reward.money = value;

                        ItemMeta meta = getDisplayItem().getItemMeta();
                        Economy economy = PLUGIN.getEconomy();
                        if (economy != null) {
                            meta.displayName(Component.text(economy.format(value)));
                        } else {
                            meta.displayName(MinigameUtils.formatMoney(value));
                        }

                        getDisplayItem().setItemMeta(meta);
                    }
                }, 50d, 100d, 1d, null);
            menu.addItem(dec);
            menu.setItem(new MenuItemBack(getMenu()), menu.getSize() - 9);
            menu.displayMenu();
            return ItemStack.empty();
        }

        @Override
        public @NotNull ItemStack onShiftRightClick() {
            getRewards().removeReward(reward);
            getMenu().removeItem(getSlot());
            return ItemStack.empty();
        }
    }
}
