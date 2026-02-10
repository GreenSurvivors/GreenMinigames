package au.com.mineauz.minigames.minigame.reward;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CommandReward extends ARewardType {
    private static final String DESCRIPTION_TOKEN = "CommandReward_description";
    private String command = "say Hello World!";

    public CommandReward(final @NotNull Rewards rewards) {
        super(rewards);
    }

    public static @Nullable CommandReward getMinigameReward(final @NotNull Rewards rewards) {
        return (CommandReward) RewardTypes.getRewardType(RewardTypes.MgDefaultRewardType.COMMAND.getName(), rewards);
    }

    @Override
    public @NotNull String getName() {
        return "COMMAND";
    }

    @Override
    public boolean isUsable() {
        return true;
    }

    @Override
    public void giveReward(@NotNull MinigamePlayer mgPlayer) {
        String finalCommand = command.replace("%player%", mgPlayer.getName());
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
    }

    @Override
    public @NotNull AMenuItem getMenuItem() {
        return new CommandRewardItem(this);
    }

    @Override
    public void saveReward(@NotNull CommentedConfigurationNode config) throws SerializationException {
        config.set(command);
    }

    @Override
    public void loadReward(@NotNull CommentedConfigurationNode config) {
        command = config.getString();
    }

    private class CommandRewardItem extends AMenuItem implements StringConsumer {
        private static final @NotNull List<@NotNull RewardRarity> options = List.of(RewardRarity.values());
        private final @NotNull CommandReward reward;

        public CommandRewardItem(final @NotNull CommandReward reward) {
            super(ItemType.COMMAND_BLOCK, Component.text("/" + command));

            this.reward = reward;
            updateDescription();
        }

        public void updateName(@NotNull String newName) {
            ItemMeta meta = getDisplayItem().getItemMeta();
            if (newName.length() > 16) {
                newName = newName.substring(0, 15);
                newName += "...";
            }
            meta.displayName(Component.text(newName));
            getDisplayItem().setItemMeta(meta);
        }

        public void updateDescription() {
            int pos = options.indexOf(getRarity());
            int before = pos - 1;
            int after = pos + 1;
            if (before == -1) {
                before = options.size() - 1;
            }
            if (after == options.size()) {
                after = 0;
            }

            List<Component> description = new ArrayList<>(5);
            description.add(options.get(before).getDisplayName().color(NamedTextColor.GRAY));
            description.add(getRarity().getDisplayName().color(NamedTextColor.GREEN));
            description.add(options.get(after).getDisplayName().color(NamedTextColor.GRAY));
            description.add(MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_EDIT_SHIFTLEFT).color(NamedTextColor.DARK_PURPLE));
            description.addAll(MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_DELETE_SHIFTRIGHTCLICK));

            setDescriptionPart(DESCRIPTION_TOKEN, description);
        }

        @Override
        public @NotNull ItemStack onClick() {
            int ind = options.lastIndexOf(getRarity());
            ind++;
            if (ind == options.size()) {
                ind = 0;
            }

            setRarity(options.get(ind));
            updateDescription();

            return getDisplayItem();
        }

        @Override
        public @NotNull ItemStack onRightClick() {
            int ind = options.lastIndexOf(getRarity());
            ind--;
            if (ind == -1) {
                ind = options.size() - 1;
            }

            setRarity(options.get(ind));
            updateDescription();

            return getDisplayItem();
        }

        @Override
        public @NotNull ItemStack onShiftRightClick() {
            getRewards().removeReward(reward);
            getMenu().removeItem(getSlot());
            return ItemStack.empty();
        }

        @Override
        public @NotNull ItemStack onShiftClick() {
            MinigamePlayer mgPlayer = getMenu().getIntendedViewer();
            final @NotNull Duration reopenTime = Duration.ofSeconds(40);
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_STRING_ENTERCHAT,
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()),
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));

            getMenu().closeAndWaitForInput(reopenTime, this);

            return ItemStack.empty();
        }

        @Override
        public void acceptString(@NotNull String input) {
            if (input.startsWith("./")) {
                input = input.replace("./", "/");
            }
            command = input;

            updateDescription();
            getMenu().cancelWaitForInput();
            getMenu().displayMenu();

            updateName(input);
        }
    }
}
