package au.com.mineauz.minigamesregions.actions;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuDisplayTypes;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.menu.MenuItemAction;
import au.com.mineauz.minigamesregions.menu.MenuItemActionAdd;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ActionRegistry {
    private static final @NotNull Map<@NotNull Key, @NotNull ActionFactory> actions = new HashMap<>();
    private static final @NotNull Map<@NotNull String, Key> legacyMapping = new HashMap<>();

    static {
        for (ActionFactory factory : RegionActions.values()) {
            addAction(factory);
        }
    }

    public static void addAction(final @NotNull ActionFactory factory) {
        actions.put(factory.key(), factory);
    }

    @Deprecated
    public static void addActionLegacy(final @NotNull ActionFactory factory, final @NotNull String legacyName) {
        actions.put(factory.key(), factory);
        legacyMapping.put(legacyName, factory.key());
    }

    @Deprecated
    public static @Nullable IAction getActionByName(final @NotNull String name) {
        final Key legacyKey = legacyMapping.get(name);
        if (legacyKey != null) {
            return getActionByKey(legacyKey);
        }
        return null;
    }

    public static @Nullable IAction getActionByKey(final @NotNull Key key) {
        final @Nullable ActionFactory actionFactory = actions.get(key);
        if (actionFactory != null) {
            return actionFactory.makeNewAction();
        }
        return null;
    }

    public static @NotNull Set<@NotNull ActionFactory> getAllActionFactories() {
        return new HashSet<>(actions.values());
    }

    public static @NotNull Set<@NotNull Key> getAllActionKeys() {
        return actions.keySet();
    }

    public static boolean hasAction(final @NotNull Key key) {
        return actions.containsKey(key);
    }

    public static boolean hasLegacy(final @NotNull String name) {
        return legacyMapping.containsKey(name.toUpperCase(Locale.ROOT));
    }

    public static boolean removeAction(final @NotNull Key key) {
        boolean any = actions.remove(key) != null;
        legacyMapping.entrySet().removeIf(entry -> entry.getValue().equals(key));

        return any;
    }

    public static @NotNull Menu createMenu(final @NotNull ActionExecutor exec, final @NotNull Menu prev) {
        final @NotNull Menu menu = new Menu(3, MessageManager.getMessage(RegionLangKey.MENU_ACTIONS_NAME), prev.getIntendedViewer());
        menu.setPreviousPage(prev);
        for (final @NotNull IAction act : exec.getActions()) {
            menu.addItem(new MenuItemAction(ItemType.PAPER, act.getDisplayname(), exec, act));
        }
        menu.setItem(new MenuItemBack(prev), menu.getSize() - 9);
        menu.setItem(new MenuItemActionAdd(MenuDisplayTypes.createType(),
                MessageManager.getMessage(RegionLangKey.MENU_ACTIONS_ADD_NAME), exec), menu.getSize() - 1);
        return menu;
    }
}
