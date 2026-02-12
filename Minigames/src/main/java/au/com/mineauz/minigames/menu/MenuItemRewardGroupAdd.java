package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
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
import java.util.List;

public class MenuItemRewardGroupAdd extends AMenuItem implements StringConsumer {
    private final @NotNull Rewards rewards;

    public MenuItemRewardGroupAdd(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                                  final @NotNull Rewards rewards) {
        super(displayType, langKey);
        this.rewards = rewards;
    }

    public MenuItemRewardGroupAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                                  final @NotNull Rewards rewards) {
        this(displayType, name, null, rewards);
    }

    public MenuItemRewardGroupAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                                  final @Nullable List<@NotNull Component> description,
                                  final @NotNull Rewards rewards) {
        super(displayType, name, description);
        this.rewards = rewards;
    }

    @Override
    public @NotNull ItemStack onClick() {
        final @NotNull MinigamePlayer mgPlayer = getMenu().getIntendedViewer();
        final @NotNull Duration reopenTime = Duration.ofSeconds(30);
        MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_REWARD_ENTERCHAT,
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));

        getMenu().closeAndWaitForInput(reopenTime, this);
        return ItemStack.empty();
    }

    @Override
    public void acceptString(final @NotNull String string) {
        @Nullable RewardGroup group = rewards.getGroupByName(string.replace(" ", "_"));

        if (group != null) {
            MessageManager.sendMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR,
                MgMenuLangKey.MENU_REWARD_ERROR_GROUPEXISTS,
                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), string));
        } else {
            group = rewards.addNewGroup(string, RewardRarity.NORMAL);

            final @NotNull MenuItemRewardGroup menuItemRewardGroup = new MenuItemRewardGroup(ItemType.BUNDLE,
                MessageManager.getMessage(MgMenuLangKey.MENU_REWARD_GROUP_NAME,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), string)), group, rewards);
            getMenu().addItem(menuItemRewardGroup);
        }

        getMenu().cancelWaitForInput();
        getMenu().displayMenu();
    }
}
