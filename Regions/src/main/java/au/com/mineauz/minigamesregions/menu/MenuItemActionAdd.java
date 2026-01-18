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

public class MenuItemActionAdd extends MenuItem {
    private final @NotNull ActionExecutor exec;

    public MenuItemActionAdd(@Nullable ItemType displayType, @Nullable Component name, @NotNull ActionExecutor exec) {
        super(displayType, name);
        this.exec = exec;
    }

    @Override
    public @NonNull ItemStack onClick() { // miau
        Menu m = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_NAME), getContainer().getViewer());
        m.setPreviousPage(getContainer());
        Map<IActionCategory, Menu> cats = new HashMap<>();
        List<ActionFactory> acts = new ArrayList<>(ActionRegistry.getAllActionFactories());
        for (ActionFactory factory : acts) {
            final IAction action = factory.makeNewAction();
            if (action.useInNodes() || action.useInRegions()) {
                IActionCategory category = action.getCategory();
                Menu menuCat;
                if (!cats.containsKey(category)) {
                    menuCat = new Menu(6, category.getDisplayName(), getContainer().getViewer());
                    cats.put(category, menuCat);
                    m.addItem(new MenuItemPage(ItemType.CHEST, category.getDisplayName(), menuCat));
                    menuCat.addItem(new MenuItemBack(m), menuCat.getSize() - 9);
                } else {
                    menuCat = cats.get(category);
                }

                MenuItemCustom menuItemCustom = new MenuItemCustom(ItemType.PAPER, action.getDisplayname());
                menuItemCustom.setClick(() -> {
                    exec.addAction(action);
                    getContainer().addItem(new MenuItemAction(ItemType.PAPER, action.getDisplayname(), exec, action));
                    getContainer().displayMenu(getContainer().getViewer());
                    return ItemStack.empty();
                });
                menuCat.addItem(menuItemCustom);
            }
        }
        m.addItem(new MenuItemBack(getContainer()), m.getSize() - 9);
        m.displayMenu(getContainer().getViewer());
        return ItemStack.empty();
    }
}
