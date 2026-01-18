package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuUtility;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.menu.MenuItemAction;
import au.com.mineauz.minigamesregions.menu.MenuItemActionAdd;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ActionRegistry {
    private static final @NotNull Map<@NotNull NamespacedKey, @NotNull ActionFactory> actions = new HashMap<>();
    private static final @NotNull Map<@NotNull String, NamespacedKey> legacyMapping = new HashMap<>();

    static {
        for (ActionFactory factory : RegionActions.values()) {
            addAction(factory);
        }
    }

    public static void addAction(final @NotNull ActionFactory factory) {
        actions.put(factory.getKey(), factory);
    }

    @Deprecated
    public static void addActionLegacy(final @NotNull ActionFactory factory, final @NotNull String legacyName) {
        actions.put(factory.getKey(), factory);
        legacyMapping.put(legacyName, factory.getKey());
    }

    @Deprecated
    public static @Nullable IAction getActionByName(final @NotNull String name) {
        final NamespacedKey legacyKey = legacyMapping.get(name);
        if (legacyKey != null) {
            return getActionByKey(legacyKey);
        }
        return null;
    }

    public static @Nullable IAction getActionByKey(final @NotNull NamespacedKey key) {
        final @Nullable ActionFactory actionFactory = actions.get(key);
        if (actionFactory != null) {
            return actionFactory.makeNewAction();
        }
        return null;
    }

    public static @NotNull Set<@NotNull ActionFactory> getAllActionFactories() {
        return new HashSet<>(actions.values());
    }

    public static @NotNull Set<@NotNull NamespacedKey> getAllActionKeys() {
        return actions.keySet();
    }

    public static boolean hasAction(final @NotNull NamespacedKey key) {
        return actions.containsKey(key);
    }

    public static boolean hasLegacy(final @NotNull String name) {
        return legacyMapping.containsKey(name.toUpperCase(Locale.ROOT));
    }

    public static boolean removeAction(final @NotNull NamespacedKey key) {
        boolean any = actions.remove(key) != null;
        legacyMapping.entrySet().removeIf(entry -> entry.getValue().equals(key));

        return any;
    }

    public static void displayMenu(final @NotNull MinigamePlayer player, final @NotNull ActionExecutor exec, final @NotNull Menu prev) {
        Menu menu = new Menu(3, RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_NAME), player);
        menu.setPreviousPage(prev);
        for (IAction act : exec.getActions()) {
            menu.addItem(new MenuItemAction(ItemType.PAPER, act.getDisplayname(), exec, act));
        }
        menu.addItem(new MenuItemBack(prev), menu.getSize() - 9);
        menu.addItem(new MenuItemActionAdd(MenuUtility.getCreateType(),
                RegionMessageManager.getMessage(RegionLangKey.MENU_ACTIONS_ADD_NAME), exec), menu.getSize() - 1);
        menu.displayMenu(player);
    }
}
