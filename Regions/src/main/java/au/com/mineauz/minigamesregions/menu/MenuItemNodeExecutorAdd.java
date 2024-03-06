package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.triggers.Trigger;
import au.com.mineauz.minigamesregions.triggers.TriggerRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemNodeExecutorAdd extends MenuItem {
    private final @NotNull Node node;

    public MenuItemNodeExecutorAdd(@Nullable Material displayMat, @Nullable RegionLangKey langKey, @NotNull Node node) {
        super(displayMat, RegionMessageManager.getMessage(langKey));
        this.node = node;
    }

    public MenuItemNodeExecutorAdd(@Nullable Material displayMat, @Nullable Component name, @NotNull Node node) {
        super(displayMat, name);
        this.node = node;
    }

    public MenuItemNodeExecutorAdd(@Nullable Material displayMat, @Nullable Component name,
                                   @Nullable List<@NotNull Component> description, @NotNull Node node) {
        super(displayMat, name, description);
        this.node = node;
    }

    @Override
    public ItemStack onClick() {
        Menu menu = new Menu(6, RegionMessageManager.getMessage(RegionLangKey.MENU_EXECUTOR_ADD_NAME), getContainer().getViewer());

        for (Trigger trig : TriggerRegistry.getAllNodeTriggers()) {
            menu.addItem(new MenuItemTrigger(trig, node, getContainer()));
        }

        menu.addItem(new MenuItemBack(getContainer()), menu.getSize() - 9);
        menu.displayMenu(getContainer().getViewer());

        return null;
    }
}