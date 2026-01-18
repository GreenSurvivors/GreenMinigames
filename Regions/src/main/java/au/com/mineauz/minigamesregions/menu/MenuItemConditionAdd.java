package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.conditions.ACondition;
import au.com.mineauz.minigamesregions.conditions.ConditionRegistry;
import au.com.mineauz.minigamesregions.conditions.IConditionCategory;
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

public class MenuItemConditionAdd extends MenuItem {
    private final @NotNull ActionExecutor exec;

    public MenuItemConditionAdd(@Nullable ItemType displayType, @NotNull Component name, @NotNull ActionExecutor exec) {
        super(displayType, name);
        this.exec = exec;
    }

    @Override
    public @NonNull ItemStack onClick() {
        Menu menu = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITIONS_NAME), getContainer().getViewer());
        menu.setPreviousPage(getContainer());
        Map<IConditionCategory, Menu> cats = new HashMap<>();
        List<ACondition> cons = new ArrayList<>(ConditionRegistry.getAllConditions());
        for (ACondition condition : cons) {
            if (condition.useInNodes() || condition.useInRegions()) {
                if (!exec.getTrigger().triggerOnPlayerAvailable()) {
                    if (condition.playerNeeded()) {
                        continue;
                    }
                }

                IConditionCategory category = condition.getCategory();
                Menu catMenu;
                if (!cats.containsKey(category)) {
                    catMenu = new Menu(6, category.getDisplayName(), getContainer().getViewer());
                    cats.put(category, catMenu);
                    menu.addItem(new MenuItemPage(ItemType.CHEST, category.getDisplayName(), catMenu));
                    catMenu.addItem(new MenuItemBack(menu), catMenu.getSize() - 9);
                } else {
                    catMenu = cats.get(category);
                }
                MenuItemCustom menuItemCustom = new MenuItemCustom(ItemType.PAPER, condition.getDisplayName());

                menuItemCustom.setClick(() -> {
                    exec.addCondition(condition);
                    getContainer().addItem(new MenuItemCondition(ItemType.PAPER, condition.getDisplayName(), exec, condition));
                    getContainer().displayMenu(getContainer().getViewer());
                    return ItemStack.empty();
                });
                catMenu.addItem(menuItemCustom);
            }
        }
        menu.addItem(new MenuItemBack(getContainer()), menu.getSize() - 9);
        menu.displayMenu(getContainer().getViewer());
        return ItemStack.empty();
    }
}
