package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.minigame.reward.ARewardType;
import au.com.mineauz.minigames.minigame.reward.RewardGroup;
import au.com.mineauz.minigames.minigame.reward.RewardRarity;
import au.com.mineauz.minigames.minigame.reward.Rewards;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MenuItemRewardGroup extends MenuItemList<@NotNull RewardRarity> implements StringConsumer {
    private final @NotNull RewardGroup group;
    private final @NotNull Rewards rewards;

    public MenuItemRewardGroup(final @Nullable ItemType displayType, final @Nullable Component name,
                               final @NotNull RewardGroup group, final @NotNull Rewards rewards) {
        this(displayType, name, null, group, rewards);
    }

    public MenuItemRewardGroup(final @Nullable ItemType displayType, final @Nullable Component name,
                               final @Nullable List<@NotNull Component> description,
                               final @NotNull RewardGroup group, final @NotNull Rewards rewards) {
        super(displayType, name, description, new Callback<>() {
            @Override
            public @NotNull RewardRarity getValue() {
                return group.getRarity();
            }

            @Override
            public void setValue(final @NotNull RewardRarity value) {
                group.setRarity(value);
            }
        }, List.of(RewardRarity.values()));
        this.group = group;
        this.rewards = rewards;
    }

    @Override
    public @NotNull ItemStack onShiftRightClick() {
        final @NotNull MinigamePlayer mgPlayer = getMenu().getIntendedViewer();

        final @NotNull Duration reopenTime = Duration.ofSeconds(10);
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_REWARD_GROUP_ENTERCHAT,
            Placeholder.unparsed(MinigamePlaceHolderKey.TYPE.getKey(), group.getName()),
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));

        getMenu().closeAndWaitForInput(reopenTime, this);
        return ItemStack.empty();
    }

    @Override
    public @NotNull ItemStack onShiftClick() {
        final @NotNull Menu rewardMenu = new Menu(5, getName(), getMenu().getIntendedViewer());
        rewardMenu.setPreviousPage(getMenu());

        rewardMenu.setItem(new MenuItemRewardAdd(MenuDisplayTypes.createType(), MgMenuLangKey.MENU_REWARD_ITEM_ADD_NAME,
            MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_REWARD_ITEM_ADD_DESCRIPTION), rewards, group), 43);
        rewardMenu.setItem(new MenuItemPage(MenuDisplayTypes.saveType(),
            MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_REWARD_SAVE_NAME,
                Placeholder.component(MinigamePlaceHolderKey.REWARD.getKey(), getName())), rewardMenu.getPreviousPage()), 44);

        final @NotNull List<@NotNull AMenuItem> menuItems = new ArrayList<>(group.getItems().size());
        for (final @NotNull ARewardType item : group.getItems()) {
            menuItems.add(item.getMenuItem());
        }

        rewardMenu.addItems(menuItems);
        rewardMenu.displayMenu();
        return ItemStack.empty();
    }

    @Override
    public void acceptString(final @NotNull String string) {
        getMenu().cancelWaitForInput();

        if (string.equalsIgnoreCase("yes")) { // todo?
            rewards.removeGroup(group);
            getMenu().removeItem(this.getSlot());

            getMenu().displayMenu();
        } else {
            MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_REWARD_NOTREMOVED);

            getMenu().displayMenu();
        }
    }
}
