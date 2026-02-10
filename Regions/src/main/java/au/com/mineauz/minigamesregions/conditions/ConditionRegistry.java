package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuDisplayTypes;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.menu.MenuItemCondition;
import au.com.mineauz.minigamesregions.menu.MenuItemConditionAdd;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConditionRegistry {
    private static final @NotNull Map<@NotNull Key, @NotNull ConditionFactory> conditions = new HashMap<>();
    private static final @NotNull Map<@NotNull String, @NotNull ConditionFactory> conditionsByName = new HashMap<>(); // legacy

    static {
        for (ConditionFactory conditionFactory : RegionDefaultConditions.values()) {
            addCondition(conditionFactory);
        }
    }

    public static void addCondition(final @NotNull ConditionFactory conditionFactory) {
        conditions.put(conditionFactory.key(), conditionFactory);
        conditionsByName.put(conditionFactory.getName(), conditionFactory);

        // datafixerupper
        if (conditionFactory.getOldName() != null) {
            conditionsByName.put(conditionFactory.getOldName(), conditionFactory);
        }
    }

    public static @Nullable ACondition getConditionByKey(final @NotNull Key key) {
        ConditionFactory factory = conditions.get(key);
        return factory != null ? factory.makeNewCondition() : null;
    }

    @Deprecated
    public static @Nullable ACondition getConditionByName(final @NotNull String name) {
        ConditionFactory factory = conditionsByName.get(name.toUpperCase());
        return factory != null ? factory.makeNewCondition() : null;
    }

    public static @NotNull Set<@NotNull ACondition> getAllConditions() {
        return conditions.values().stream().map(ConditionFactory::makeNewCondition).collect(Collectors.toSet());
    }

    public static void displayMenu(final @NotNull ActionExecutor exec, final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITIONS_NAME), prev.getIntendedViewer());
        menu.setPreviousPage(prev);
        for (final @NotNull ACondition con : exec.getConditions()) {
            menu.addItem(new MenuItemCondition(ItemType.PAPER, con.getDisplayName(), exec, con));
        }
        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);
        menu.setItem(new MenuItemConditionAdd(MenuDisplayTypes.createType(), RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITIONS_ADD_NAME), exec), menu.getSize() - 1);
        menu.displayMenu();
    }
}
