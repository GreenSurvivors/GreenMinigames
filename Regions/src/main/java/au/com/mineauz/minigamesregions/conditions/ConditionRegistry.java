package au.com.mineauz.minigamesregions.conditions;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuUtility;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.executors.BaseExecutor;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.menu.MenuItemCondition;
import au.com.mineauz.minigamesregions.menu.MenuItemConditionAdd;
import org.bukkit.Material;
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
    }

    public static @Nullable ACondition getConditionByName(@NotNull String name) {
        ConditionFactory factory = conditions.get(name.toUpperCase());
        return factory != null ? factory.makeNewCondition() : null;
    }

    public static @NotNull Set<@NotNull ACondition> getAllConditions() {
        return conditions.values().stream().map(ConditionFactory::makeNewCondition).collect(Collectors.toSet());
    }

    public static void displayMenu(@NotNull MinigamePlayer mgPlayer, @NotNull BaseExecutor exec, @NotNull Menu prev) {
        Menu m = new Menu(3, RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITIONS_NAME), mgPlayer);
        m.setPreviousPage(prev);
        for (ACondition con : exec.getConditions()) {
            m.addItem(new MenuItemCondition(Material.PAPER, con.getDisplayName(), exec, con));
        }
        m.addItem(new MenuItemBack(prev), m.getSize() - 9);
        m.addItem(new MenuItemConditionAdd(MenuUtility.getCreateMaterial(), RegionMessageManager.getMessage(RegionLangKey.MENU_CONDITIONS_ADD_NAME), exec), m.getSize() - 1);
        m.displayMenu(mgPlayer);
    }
}
