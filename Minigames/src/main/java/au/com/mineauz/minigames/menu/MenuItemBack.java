package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class MenuItemBack extends MenuItem {
    private final @NotNull Menu prev;

    public MenuItemBack(@NotNull Menu prev) {
        super(MenuUtility.pageBackType(), MgMenuLangKey.MENU_PAGE_BACK);
        this.prev = prev;
    }

    @Override
    public @NotNull ItemStack onClick() {
        prev.displayMenu();
        return ItemStack.empty();
    }
}
