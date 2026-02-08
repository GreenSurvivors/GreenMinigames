package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigamesregions.ActionExecutorHolder;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.triggers.Trigger;
import au.com.mineauz.minigamesregions.triggers.TriggerRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MenuItemActionExecutorAdd extends MenuItem {
    private final @NotNull ActionExecutorHolder actionExecutorHolder;

    public MenuItemActionExecutorAdd(final @Nullable ItemType displayType, final @Nullable RegionLangKey langKey,
                                     final @NotNull ActionExecutorHolder actionExecutorHolder) {
        super(displayType, RegionMessageManager.getMessage(langKey));
        this.actionExecutorHolder = actionExecutorHolder;
    }

    public MenuItemActionExecutorAdd(final @Nullable ItemType displayType,
                                     final @Nullable Component name,
                                     final @NotNull ActionExecutorHolder actionExecutorHolder) {
        super(displayType, name);
        this.actionExecutorHolder = actionExecutorHolder;
    }

    public MenuItemActionExecutorAdd(final @Nullable ItemType displayType, final @Nullable Component name,
                                     final @Nullable List<@NotNull Component> description,
                                     final @NotNull ActionExecutorHolder actionExecutorHolder) {
        super(displayType, name, description);
        this.actionExecutorHolder = actionExecutorHolder;
    }

    @Override
    public @NonNull ItemStack onClick() {
        final @NotNull Menu menu = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_REGIONEXECUTOR_ADD_TRIGGER_NAME),
            getMenu().getIntendedViewer());

        for (final @NotNull Trigger trig : TriggerRegistry.getAllRegionTriggers()) {
            menu.addItem(new MenuItemTrigger(trig, actionExecutorHolder, getMenu()));
        }

        menu.addItem(new MenuItemBack(getMenu()), menu.getSize() - 9);
        menu.displayMenu();

        return ItemStack.empty();
    }
}
