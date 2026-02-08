package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuUtility;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.menu.MenuItemCondition;
import au.com.mineauz.minigamesregions.menu.MenuItemConditionAdd;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConditionRegistry {
    private static final @NotNull Map<@NotNull String, @NotNull ConditionFactory> conditions = new HashMap<>();

    static {
        for (ConditionFactory conditionFactory : RegionConditions.values()) {
            addCondition(conditionFactory);
        }
    }

    public static void addCondition(@NotNull ConditionFactory conditionFactory) {
        conditions.put(conditionFactory.getName(), conditionFactory);

        // datafixerupper
        if (conditionFactory.getOldName() != null) {
            conditions.put(conditionFactory.getOldName(), conditionFactory);
        }
    }

    public static @Nullable ACondition getConditionByName(@NotNull String name) {
        ConditionFactory factory = conditions.get(name.toUpperCase());
        return factory != null ? factory.makeNewCondition() : null;
    }

    public static @NotNull Set<@NotNull ACondition> getAllConditions() {
        return conditions.values().stream().map(ConditionFactory::makeNewCondition).collect(Collectors.toSet());
    }

    public static void displayMenu(final @NotNull ActionExecutor exec, final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITIONS_NAME), prev.getIntendedViewer());
        menu.setPreviousPage(prev);
        for (ACondition con : exec.getConditions()) {
            menu.addItem(new MenuItemCondition(ItemType.PAPER, con.getDisplayName(), exec, con));
        }
        menu.addItem(new MenuItemBack(prev), menu.getSize() - 9);
        menu.addItem(new MenuItemConditionAdd(MenuUtility.createType(), RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITIONS_ADD_NAME), exec), menu.getSize() - 1);
        menu.displayMenu();
    }
}
