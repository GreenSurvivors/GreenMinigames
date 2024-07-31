package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class MenuItemAddWhitelistBlock extends MenuItem {
    protected final @NotNull List<@NotNull Material> whitelist;

    public MenuItemAddWhitelistBlock(@NotNull MinigameLangKey langKey, @NotNull List<@NotNull Material> whitelist) {
        this(MinigameMessageManager.getMgMessage(langKey), whitelist);
    }

    public MenuItemAddWhitelistBlock(@NotNull Component name, @NotNull List<@NotNull Material> whitelist) {
        super(MenuUtility.getCreateMaterial(), name,
                MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_WHITELIST_INTERACT));
        this.whitelist = whitelist;
    }

    @Override
    public @NotNull ItemStack onClickWithItem(@NotNull ItemStack item) {
        if (!whitelist.contains(item.getType())) {
            whitelist.add(item.getType());
            getContainer().addItem(new MenuItemWhitelistBlock(item.getType(), whitelist));
        } else {
            MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_WHITELIST_ERROR_CONTAINS);
        }
        return getDisplayItem();
    }

    @Override
    public @Nullable ItemStack onClick() {
        MinigamePlayer mgPlayer = getContainer().getViewer();
        mgPlayer.setNoClose(true);
        mgPlayer.getPlayer().closeInventory();
        int reopenSeconds = 30;
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_WHITELIST_ENTERCHAT,
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(reopenSeconds))));
        mgPlayer.setManualEntry(this);

        getContainer().startReopenTimer(reopenSeconds);
        return null;
    }

    @Override
    public void checkValidEntry(@NotNull String entry) {
        // try a direct match in case of a chat input
        Material mat = Material.matchMaterial(entry);

        if (mat == null) {
            // didn't work, now try the input as a block data, as we get when a block was clicked
            try {
                mat = Bukkit.createBlockData(entry).getMaterial();
            } catch (IllegalArgumentException ignored) {
            }
        }

        if (mat != null) {
            if (!whitelist.contains(mat)) {
                // intern
                whitelist.add(mat);

                // visual
                getContainer().addItem(new MenuItemWhitelistBlock(mat, whitelist));
            } else {
                MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_WHITELIST_ERROR_CONTAINS);
            }
        } else {
            // still didn't work.
            getContainer().cancelReopenTimer();
            getContainer().displayMenu(getContainer().getViewer());

            MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR, MgCommandLangKey.COMMAND_ERROR_NOTMATERIAL,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), entry));
        }

        /* cancel automatic reopening and reopen {@link MenuItemDisplayWhitelist}*/
        getContainer().cancelReopenTimer();
        getContainer().displayMenu(getContainer().getViewer());
    }
}
