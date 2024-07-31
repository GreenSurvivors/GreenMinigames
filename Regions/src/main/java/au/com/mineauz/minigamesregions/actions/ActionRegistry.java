package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuUtility;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.executors.BaseExecutor;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.menu.MenuItemAction;
import au.com.mineauz.minigamesregions.menu.MenuItemActionAdd;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ActionRegistry {
    private static final @NotNull Map<@NotNull String, @NotNull ActionFactory> actions = new HashMap<>();

    static {
        for (ActionFactory factory : RegionActions.values()) {
            addAction(factory);
        }
    }

    public static void addAction(@NotNull ActionFactory factory) {
        actions.put(factory.getName(), factory);
    }

    @Nullable
    public static ActionInterface getActionByName(@NotNull String name) {
        if (actions.containsKey(name.toUpperCase())) {
            return actions.get(name.toUpperCase()).makeNewAction();
        }
        return null;
    }

    public static @NotNull Set<@NotNull ActionFactory> getAllActionFactorys() {
        return new HashSet<>(actions.values());
    }

    public static @NotNull Set<@NotNull String> getAllActionNames() {
        return actions.keySet();
    }

    public static boolean hasAction(@NotNull String name) {
        return actions.containsKey(name.toUpperCase());
    }

    public static void displayMenu(@NotNull MinigamePlayer player, @NotNull BaseExecutor exec, @NotNull Menu prev) {
        Menu menu = new Menu(3, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_NAME), player);
        menu.setPreviousPage(prev);
        for (ActionInterface act : exec.getActions()) {
            menu.addItem(new MenuItemAction(Material.PAPER, act.getDisplayname(), exec, act));
        }
        menu.addItem(new MenuItemBack(prev), menu.getSize() - 9);
        menu.addItem(new MenuItemActionAdd(MenuUtility.getCreateMaterial(),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_ADD_NAME), exec), menu.getSize() - 1);
        menu.displayMenu(player);
    }
}
