package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.menu.*;
import au.com.mineauz.minigamesregions.conditions.ACondition;
import au.com.mineauz.minigamesregions.conditions.ConditionRegistry;
import au.com.mineauz.minigamesregions.conditions.IConditionCategory;
import au.com.mineauz.minigamesregions.executors.BaseExecutor;
import au.com.mineauz.minigamesregions.executors.NodeExecutor;
import au.com.mineauz.minigamesregions.executors.RegionExecutor;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuItemConditionAdd extends MenuItem {
    private final @NotNull BaseExecutor exec;

    public MenuItemConditionAdd(@Nullable Material displayMat, @NotNull Component name, @NotNull BaseExecutor exec) {
        super(displayMat, name);
        this.exec = exec;
    }

    @Override
    public @Nullable ItemStack onClick() {
        Menu menu = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITIONS_NAME), getContainer().getViewer());
        menu.setPreviousPage(getContainer());
        Map<IConditionCategory, Menu> cats = new HashMap<>();
        List<ACondition> cons = new ArrayList<>(ConditionRegistry.getAllConditions());
        for (ACondition condition : cons) {
            if ((condition.useInNodes() && exec instanceof NodeExecutor) || (condition.useInRegions() && exec instanceof RegionExecutor)) {
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
                    menu.addItem(new MenuItemPage(Material.CHEST, category.getDisplayName(), catMenu));
                    catMenu.addItem(new MenuItemBack(menu), catMenu.getSize() - 9);
                } else {
                    catMenu = cats.get(category);
                }
                MenuItemCustom menuItemCustom = new MenuItemCustom(Material.PAPER, condition.getDisplayName());

                menuItemCustom.setClick(() -> {
                    exec.addCondition(condition);
                    getContainer().addItem(new MenuItemCondition(Material.PAPER, condition.getDisplayName(), exec, condition));
                    getContainer().displayMenu(getContainer().getViewer());
                    return null;
                });
                catMenu.addItem(menuItemCustom);
            }
        }
        menu.addItem(new MenuItemBack(getContainer()), menu.getSize() - 9);
        menu.displayMenu(getContainer().getViewer());
        return null;
    }
}
