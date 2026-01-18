package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
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

public class MenuItemAddWhitelistBlock extends MenuItem implements StringConsumer, BlockDataConsumer, BlockTypeConsumer {
    protected final @NotNull List<@NotNull BlockType> whitelist;

    public MenuItemAddWhitelistBlock(final @NotNull MinigameLangKey langKey, final @NotNull List<@NotNull BlockType> whitelist) {
        this(MinigameMessageManager.getMgMessage(langKey), whitelist);
    }

    public MenuItemAddWhitelistBlock(final @NotNull Component name, final @NotNull List<@NotNull BlockType> whitelist) {
        super(MenuUtility.getCreateType(), name,
            MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_WHITELIST_INTERACT));
        this.whitelist = whitelist;
    }

    @Override
    public @NotNull ItemStack onClickWithItem(final @NotNull ItemStack item) {
        final @Nullable BlockType blockType = item.getType().asBlockType();

        if (blockType != null) {
            if (!whitelist.contains(blockType)) {
                whitelist.add(blockType);
                getContainer().addItem(new MenuItemWhitelistBlock(item.getType().asItemType(), whitelist));
            } else {
                MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_WHITELIST_ERROR_CONTAINS);
            }
        } else {
            // todo
        }
        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onClick() {
        MinigamePlayer mgPlayer = getContainer().getViewer();
        mgPlayer.setNoClose(true);
        mgPlayer.getPlayer().closeInventory();
        final @NotNull Duration reopenTime = Duration.ofSeconds(30);
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_WHITELIST_ENTERCHAT,
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));
        mgPlayer.setManualEntry(this);

        getContainer().startReopenTimer(reopenTime);
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
        getContainer().cancelReopenTimer();
        getContainer().displayMenu(getContainer().getViewer());

        MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTBLOCKTYPE,
            Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), string));

        /* cancel automatic reopening and reopen {@link MenuItemDisplayWhitelist}*/
        getContainer().cancelReopenTimer();
        getContainer().displayMenu(getContainer().getViewer());
    }

    @Override
    public void acceptBlockType(final @NotNull BlockType blockType) {
        if (!whitelist.contains(blockType)) {
            // intern
            whitelist.add(blockType);

            // visual
            if (blockType.hasItemType()) {
                getContainer().addItem(new MenuItemWhitelistBlock(blockType.getItemType(), whitelist));
            } else {
                // todo add placeholder here
            }
        } else {
            MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_WHITELIST_ERROR_CONTAINS);
        }

        /* cancel automatic reopening and reopen {@link MenuItemDisplayWhitelist}*/
        getContainer().cancelReopenTimer();
        getContainer().displayMenu(getContainer().getViewer());
    }
}
