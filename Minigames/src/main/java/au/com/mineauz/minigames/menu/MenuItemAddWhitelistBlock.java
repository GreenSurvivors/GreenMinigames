package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.consumer.BlockDataConsumer;
import au.com.mineauz.minigames.menu.consumer.BlockTypeConsumer;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class MenuItemAddWhitelistBlock extends AMenuItem implements StringConsumer, BlockDataConsumer, BlockTypeConsumer {
    protected final @NotNull List<@NotNull BlockType> whitelist;

    public MenuItemAddWhitelistBlock(final @NotNull MinigameLangKey langKey, final @NotNull List<@NotNull BlockType> whitelist) {
        this(MessageManager.getMessage(langKey), whitelist);
    }

    public MenuItemAddWhitelistBlock(final @NotNull Component name, final @NotNull List<@NotNull BlockType> whitelist) {
        super(MenuDisplayTypes.createType(), name,
            MessageManager.getMessageList(MgMenuLangKey.MENU_WHITELIST_INTERACT));
        this.whitelist = whitelist;
    }

    @Override
    public @NotNull ItemStack onClickWithItem(final @NotNull ItemStack item) {
        final @Nullable BlockType blockType = item.getType().asBlockType();

        if (blockType != null) {
            if (!whitelist.contains(blockType)) {
                whitelist.add(blockType);
                getMenu().addItem(new MenuItemWhitelistBlock(item.getType().asItemType(), whitelist));
            } else {
                MessageManager.sendMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_WHITELIST_ERROR_CONTAINS);
            }
        } else {
            // todo
        }
        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onClick() {
        MinigamePlayer mgPlayer = getMenu().getIntendedViewer();
        final @NotNull Duration reopenTime = Duration.ofSeconds(30);
        MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_WHITELIST_ENTERCHAT,
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));

        getMenu().closeAndWaitForInput(reopenTime, this);
        return ItemStack.empty();
    }

    @Override
    public void acceptBlockData(final @NotNull BlockData data) {
        acceptBlockType(data.getMaterial().asBlockType());
    }

    @Override
    public void acceptString(final @NotNull String string) {
        final @Nullable NamespacedKey key = NamespacedKey.fromString(string.toLowerCase(Locale.ROOT));

        if (key != null) {
            final @Nullable BlockType blockType = Registry.BLOCK.get(key);

            if (blockType != null) {
                acceptBlockType(blockType);
                return;
            }
        }
        // didn't work.
        getMenu().cancelWaitForInput();
        getMenu().displayMenu();

        MessageManager.sendMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTBLOCKTYPE,
            Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), string));

        /* cancel automatic reopening and reopen {@link MenuItemDisplayWhitelist}*/
        getMenu().cancelWaitForInput();
        getMenu().displayMenu();
    }

    @Override
    public void acceptBlockType(final @NotNull BlockType blockType) {
        if (!whitelist.contains(blockType)) {
            // intern
            whitelist.add(blockType);

            // visual
            if (blockType.hasItemType()) {
                getMenu().addItem(new MenuItemWhitelistBlock(blockType.getItemType(), whitelist));
            } else {
                // todo add placeholder here
            }
        } else {
            MessageManager.sendMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_WHITELIST_ERROR_CONTAINS);
        }

        /* cancel automatic reopening and reopen {@link MenuItemDisplayWhitelist}*/
        getMenu().cancelWaitForInput();
        getMenu().displayMenu();
    }
}
