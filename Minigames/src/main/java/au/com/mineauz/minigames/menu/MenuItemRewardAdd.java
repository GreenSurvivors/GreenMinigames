package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.minigame.reward.ARewardType;
import au.com.mineauz.minigames.minigame.reward.RewardGroup;
import au.com.mineauz.minigames.minigame.reward.RewardTypes;
import au.com.mineauz.minigames.minigame.reward.Rewards;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemRewardAdd extends MenuItem {
    private @Nullable Rewards rewards = null;
    private @Nullable RewardGroup group = null;

    public MenuItemRewardAdd(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey, @NotNull Rewards rewards) {
        super(displayType, langKey);
        this.rewards = rewards;
    }

    public MenuItemRewardAdd(@Nullable ItemType displayType, @Nullable Component name, @NotNull Rewards rewards) {
        super(displayType, name);
        this.rewards = rewards;
    }

    public MenuItemRewardAdd(@Nullable ItemType displayType, @Nullable Component name,
                             @Nullable List<@NotNull Component> description, @NotNull Rewards rewards) {
        super(displayType, name, description);
        this.rewards = rewards;
    }

    public MenuItemRewardAdd(@Nullable ItemType displayType, @Nullable Component name, @NotNull RewardGroup group) {
        super(displayType, name);
        this.group = group;
    }

    public MenuItemRewardAdd(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey,
                             @Nullable List<@NotNull Component> description, @NotNull RewardGroup group) {
        super(displayType, langKey, description);
        this.group = group;
    }

    public MenuItemRewardAdd(@Nullable ItemType displayType, @Nullable Component name,
                             @Nullable List<@NotNull Component> description, @NotNull RewardGroup group) {
        super(displayType, name, description);
        this.group = group;
    }

    @Override
    public @NotNull ItemStack onClick() {
        final @NotNull Menu menu = new Menu(6, MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_SELECTTYPE_NAME), getContainer().getViewer());
        final Menu orig = getContainer();
        for (RewardTypes.RewardTypeFactory factory : RewardTypes.getRewardTypeFactories()) {
            final MenuItemCustom custom = new MenuItemCustom(ItemType.STONE, MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_TYPE_NAME));
            final ARewardType rewType = factory.makeNewType(rewards);

            if (rewType.isUsable()) {
                ItemMeta meta = custom.getDisplayItem().getItemMeta();
                meta.displayName(Component.text(factory.getName()));
                custom.getDisplayItem().setItemMeta(meta);
                custom.setDisplayItem(rewType.getMenuItem().getDisplayItem());
                custom.setClick(() -> {
                    if (rewards != null) {
                        rewards.addReward(rewType);
                    } else {
                        group.addItem(rewType);
                    }
                    orig.displayMenu(orig.getViewer());
                    orig.addItem(rewType.getMenuItem());
                    return ItemStack.empty();
                });
                menu.addItem(custom);
            }
        }
        menu.addItem(new MenuItemBack(orig), menu.getSize() - 9);
        menu.displayMenu(menu.getViewer());
        return ItemStack.empty();
    }
}
