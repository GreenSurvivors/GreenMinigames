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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MenuItemRewardGroup extends AMenuItem implements StringConsumer {
    private static final String DESCRIPTION_TOKEN = "RewardGroup_description";
    private static final @NotNull List<@NotNull RewardRarity> options = List.of(RewardRarity.values());
    private final @NotNull RewardGroup group;
    private final @NotNull Rewards rewards;

    public MenuItemRewardGroup(final @Nullable ItemType displayType, final @Nullable Component name,
                               final @NotNull RewardGroup group, final @NotNull Rewards rewards) {
        this(displayType, name, null, group, rewards);
    }

    public MenuItemRewardGroup(final @Nullable ItemType displayType, final @Nullable Component name,
                               final @Nullable List<@NotNull Component> description,
                               final @NotNull RewardGroup group, final @NotNull Rewards rewards) {
        super(displayType, name, description);
        this.group = group;
        this.rewards = rewards;
        updateDescription();
    }

    public void updateDescription() {
        int pos = options.indexOf(group.getRarity());
        int before = pos - 1;
        int after = pos + 1;
        if (before == -1) {
            before = options.size() - 1;
        }
        if (after == options.size()) {
            after = 0;
        }

        List<Component> description = new ArrayList<>(3);
        description.add(Component.text(options.get(before).toString(), NamedTextColor.GRAY));
        description.add(Component.text(group.getRarity().toString(), NamedTextColor.GREEN));
        description.add(Component.text(options.get(after).toString(), NamedTextColor.GRAY));

        setDescriptionPart(DESCRIPTION_TOKEN, description);
    }


    @Override
    public @NotNull ItemStack onClick() {
        int ind = options.lastIndexOf(group.getRarity());
        ind++;
        if (ind == options.size()) {
            ind = 0;
        }

        group.setRarity(options.get(ind));

        updateDescription();

        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onRightClick() {
        int ind = options.lastIndexOf(group.getRarity());
        ind--;
        if (ind == -1) {
            ind = options.size() - 1;
        }

        group.setRarity(options.get(ind));
        updateDescription();

        return getDisplayItem();
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
            MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_REWARD_ITEM_ADD_DESCRIPTION), group), 43);
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
}
