package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MenuItemWhitelistBlock extends MenuItem {
    private final @NotNull List<BlockType> whitelist;

    public MenuItemWhitelistBlock(final @NotNull ItemType displayType, final @NotNull List<@NotNull BlockType> whitelist) {
        super(displayType, Component.translatable(displayType.translationKey()),
            MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_DELETE_RIGHTCLICK));
        this.whitelist = whitelist;
    }

    @Override
    public @NotNull ItemStack onRightClick() {
        final @NotNull ItemType itemTypeNow = getDisplayItem().getType().asItemType();

        if (itemTypeNow.hasBlockType()) { // pure sanity check, our whitelist should never allow anything other than a block to become the current item
            whitelist.remove(itemTypeNow.getBlockType());
        }
        getMenu().removeItem(getSlot());
        return ItemStack.empty();
    }
}
