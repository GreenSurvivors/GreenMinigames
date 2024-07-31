package au.com.mineauz.minigames.minigame.reward;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoneyReward extends ARewardType {
    private final static Minigames PLUGIN = Minigames.getPlugin();
    private final static String DESCRIPTION_TOKEN = "Reward_description";
    private double money = 0d;

    public MoneyReward(@NotNull Rewards rewards) {
        super(rewards);
    }

    public static @Nullable MoneyReward getMinigameReward(@NotNull Rewards rewards) {
        return (MoneyReward) RewardTypes.getRewardType(RewardTypes.MgRewardType.MONEY.getName(), rewards);
    }

    @Override
    public @NotNull String getName() {
        return "MONEY";
    }

    @Override
    public boolean isUsable() {
        return Minigames.getPlugin().getEconomy() != null;
    }

    @Override
    public void giveReward(@NotNull MinigamePlayer mgPlayer) {
        Economy economy = PLUGIN.getEconomy();

        if (economy != null) {
            economy.depositPlayer(mgPlayer.getPlayer().getPlayer(), money);
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.WIN, MgMiscLangKey.REWARD_MONEY,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MONEY.getKey(), economy.format(money)));
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMiscLangKey.MINIGAME_WARNING_NOVAULT);
        }
    }

    @Override
    public @NotNull MenuItem getMenuItem() {
        return new MenuItemReward(this);
    }

    @Override
    public void saveReward(@NotNull Configuration config, @NotNull String path) {
        config.set(path, money);
    }

    @Override
    public void loadReward(@NotNull Configuration config, @NotNull String path) {
        money = config.getDouble(path);
    }

    public double getRewardMoney() {
        return money;
    }

    public void setRewardMoney(double amount) {
        money = amount;
    }

    private class MenuItemReward extends MenuItem {
        private final @NotNull MoneyReward reward;
        private final @NotNull List<@NotNull RewardRarity> options = new ArrayList<>();

        public MenuItemReward(@NotNull MoneyReward reward) {
            super(Material.PAPER, Component.text("$" + money));
            options.addAll(Arrays.asList(RewardRarity.values()));
            this.reward = reward;
            updateDescription();
        }

        public void updateDescription() {
            List<Component> description;
            int pos = options.indexOf(getRarity());
            int before = pos - 1;
            int after = pos + 1;
            if (before <= -1) {
                before = options.size() - 1;
            }
            if (after >= options.size()) {
                after = 0;
            }

            description = new ArrayList<>();
            description.add(options.get(before).getDisplayName().color(NamedTextColor.GRAY));
            description.add(getRarity().getDisplayName().color(NamedTextColor.GREEN));
            description.add(options.get(after).getDisplayName().color(NamedTextColor.GRAY));
            description.add(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_CHANGE_SHIFTCLICK).color(NamedTextColor.DARK_PURPLE));
            description.add(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK).color(NamedTextColor.DARK_PURPLE));

            setDescriptionPart(DESCRIPTION_TOKEN, description);
        }

        @Override
        public @NotNull ItemStack onClick() {
            int ind = options.lastIndexOf(getRarity());
            ind++;
            if (ind == options.size())
                ind = 0;

            setRarity(options.get(ind));
            updateDescription();

            return getDisplayItem();
        }

        @Override
        public @NotNull ItemStack onRightClick() {
            int ind = options.lastIndexOf(getRarity());
            ind--;
            if (ind == -1)
                ind = options.size() - 1;

            setRarity(options.get(ind));
            updateDescription();

            return getDisplayItem();
        }

        @Override
        public @Nullable ItemStack onShiftClick() {
            Menu m = new Menu(3, MgMenuLangKey.MENU_MONEYREWARD_MENU_NAME, getContainer().getViewer());
            MenuItemDecimal dec = new MenuItemDecimal(Material.PAPER,
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
                                meta.displayName(Component.text("$" + value));
                            }

                            getDisplayItem().setItemMeta(meta);
                        }
                    }, 50d, 100d, 1d, null);
            m.addItem(dec);
            m.addItem(new MenuItemBack(getContainer()), m.getSize() - 9);
            m.displayMenu(getContainer().getViewer());
            return null;
        }

        @Override
        public @Nullable ItemStack onShiftRightClick() {
            getRewards().removeReward(reward);
            getContainer().removeItem(getSlot());
            return null;
        }
    }
}
