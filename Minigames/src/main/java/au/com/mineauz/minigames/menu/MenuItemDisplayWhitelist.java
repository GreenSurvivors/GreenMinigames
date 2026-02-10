package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MenuItemDisplayWhitelist extends AMenuItem {
    private final @NotNull List<@NotNull BlockType> whitelist;
    private final @NotNull Callback<Boolean> whitelistMode;
    private final @NotNull List<@NotNull Component> modeDescription;

    public MenuItemDisplayWhitelist(final @Nullable ItemType displayType, final @Nullable Component name,
                                    final @NotNull List<@NotNull BlockType> whitelist, final @NotNull Callback<Boolean> whitelistMode,
                                    final @NotNull List<@NotNull Component> modeDescription) {
        this(displayType, name, null, whitelist, whitelistMode, modeDescription);
    }

    public MenuItemDisplayWhitelist(final @Nullable ItemType displayType, final @Nullable Component name,
                                    final @Nullable List<@NotNull Component> mainDescription,
                                    final @NotNull List<@NotNull BlockType> whitelist, final @NotNull Callback<Boolean> whitelistMode,
                                    final @NotNull List<@NotNull Component> modeDescription) {
        super(displayType, name, mainDescription);
        this.whitelist = whitelist;
        this.whitelistMode = whitelistMode;
        this.modeDescription = modeDescription;
    }

    @Override
    public @NotNull ItemStack onClick() {
        final @NotNull Menu menu = new Menu(6, MgMenuLangKey.MENU_WHITELIST_BLOCK_NAME, getMenu().getIntendedViewer());
        final @NotNull List<@NotNull AMenuItem> items = new ArrayList<>();
        for (final @NotNull BlockType blockType : whitelist) {
            if (blockType.hasItemType()) {
                items.add(new MenuItemWhitelistBlock(blockType.getItemType(), whitelist));
            } else {
                // todo create a placeholder item
            }
        }
        menu.setItem(new MenuItemBack(getMenu()), menu.getSize() - 9);
        menu.setItem(new MenuItemAddWhitelistBlock(MgMenuLangKey.MENU_WHITELIST_ADDBLOCKTYPE_NAME, whitelist), menu.getSize() - 1);
        menu.setItem(new MenuItemBoolean(ItemType.ENDER_PEARL, MgMenuLangKey.MENU_WHITELIST_MODE, modeDescription,
            whitelistMode), menu.getSize() - 2);
        menu.addItems(items);
        menu.displayMenu();
        return ItemStack.empty();
    }
}
