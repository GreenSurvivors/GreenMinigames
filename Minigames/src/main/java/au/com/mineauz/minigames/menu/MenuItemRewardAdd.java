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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemRewardAdd extends AMenuItem {
    private final @NotNull Rewards rewards;
    private final @Nullable RewardGroup group;

    public MenuItemRewardAdd(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                             final @NotNull Rewards rewards) {
        this(displayType, langKey, null, rewards, null);
    }

    public MenuItemRewardAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                             final @NotNull Rewards rewards) {
        this(displayType, name, null, rewards);
    }

    public MenuItemRewardAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                             final @Nullable List<@NotNull Component> description,
                             final @NotNull Rewards rewards) {
        this(displayType, name, description, rewards, null);
    }

    public MenuItemRewardAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                             final @NotNull Rewards rewards, final @Nullable RewardGroup group) {
        this(displayType, name, null, rewards, group);
    }

    public MenuItemRewardAdd(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                             final @Nullable List<@NotNull Component> description,
                             final @NotNull Rewards rewards, final @Nullable RewardGroup group) {
        super(displayType, langKey, description);
        this.rewards = rewards;
        this.group = group;
    }

    public MenuItemRewardAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                             final @Nullable List<@NotNull Component> description,
                             final @NotNull Rewards rewards, final @Nullable RewardGroup group) {
        super(displayType, name, description);
        this.rewards = rewards;
        this.group = group;
    }

    @Override
    public @NotNull ItemStack onClick() {
        final @NotNull Menu menu = new Menu(6, MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_SELECTTYPE_NAME), getMenu().getIntendedViewer());
        for (final @NotNull RewardTypes.RewardTypeFactory factory : RewardTypes.getRewardTypeFactories()) {
            final @NotNull MenuItemCustom custom = new MenuItemCustom(ItemType.STONE, MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_TYPE_NAME));
            final @NotNull ARewardType rewType = factory.makeNewType(rewards);

            if (rewType.isUsable()) {
                custom.setDisplayItem(rewType.getMenuItem().getDisplayItem().clone());
                custom.getDisplayItem().editMeta(meta ->
                    meta.displayName(Component.text(factory.key().asMinimalString())));

                custom.setClick(() -> {
                    if (group != null) {
                        group.addItem(rewType);
                    } else {
                        rewards.addReward(rewType);
                    }
                    getMenu().displayMenu();
                    getMenu().addItem(rewType.getMenuItem());
                    return ItemStack.empty();
                });
                menu.addItem(custom);
            }
        }
        menu.setItem(new MenuItemBack(getMenu()), menu.getSize() - 9);
        menu.displayMenu();
        return ItemStack.empty();
    }
}
