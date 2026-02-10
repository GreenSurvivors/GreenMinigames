package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.actions.ActionFactory;
import au.com.mineauz.minigamesregions.actions.ActionRegistry;
import au.com.mineauz.minigamesregions.actions.IAction;
import au.com.mineauz.minigamesregions.actions.IActionCategory;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuItemActionAdd extends AMenuItem {
    private final @NotNull ActionExecutor exec;

    public MenuItemActionAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                             final @NotNull ActionExecutor exec) {
        super(displayType, name);
        this.exec = exec;
    }

    @Override
    public @NonNull ItemStack onClick() {
        final @NotNull Menu menu = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_NAME), getMenu().getIntendedViewer());
        menu.setPreviousPage(getMenu());
        final @NotNull Map<@NotNull IActionCategory, @NotNull Menu> cats = new HashMap<>();
        final @NotNull List<@NotNull ActionFactory> acts = new ArrayList<>(ActionRegistry.getAllActionFactories());
        for (final @NotNull ActionFactory factory : acts) {
            final @NotNull IAction action = factory.makeNewAction();
            if (action.useInNodes() || action.useInRegions()) {
                final @NotNull IActionCategory category = action.getCategory();
                final @NotNull Menu menuCat;
                if (!cats.containsKey(category)) {
                    menuCat = new Menu(6, category.getDisplayName(), getMenu().getIntendedViewer());
                    cats.put(category, menuCat);
                    menu.addItem(new MenuItemPage(MenuDisplayTypes.genericSubMenu(), category.getDisplayName(), menuCat));
                    menuCat.setItem(new MenuItemBack(menu), menuCat.getSize() - 9);
                } else {
                    menuCat = cats.get(category);
                }

                // I myself use a bit of string to add action to my cat.
                final @NotNull MenuItemCustom addActionToCatMenuItem = new MenuItemCustom(ItemType.PAPER, action.getDisplayname());
                addActionToCatMenuItem.setClick(() -> {
                    exec.addAction(action);
                    getMenu().addItem(new MenuItemAction(ItemType.PAPER, action.getDisplayname(), exec, action));
                    getMenu().displayMenu();
                    return ItemStack.empty();
                });
                menuCat.addItem(addActionToCatMenuItem);
            }
        }
        menu.setItem(new MenuItemBack(getMenu()), menu.getSize() - 9);
        menu.displayMenu();
        return ItemStack.empty();
    }
}
