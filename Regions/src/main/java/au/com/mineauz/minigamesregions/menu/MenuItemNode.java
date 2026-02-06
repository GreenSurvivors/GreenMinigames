package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuUtility;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.Node;
import au.com.mineauz.minigamesregions.RegionModule;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import au.com.mineauz.minigamesregions.language.RegionPlaceHolderKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MenuItemNode extends MenuItem { // todo merge with MenuItemRegion
    private final @NotNull Node node;
    private final @NotNull RegionModule rmod;

    public MenuItemNode(@Nullable ItemType displayType, @Nullable Component name, @NotNull Node node,
                        @NotNull RegionModule rmod) {
        super(displayType, name);
        this.node = node;
        this.rmod = rmod;
    }

    public MenuItemNode(@Nullable ItemType displayType, @Nullable Component name,
                        @Nullable List<@NotNull Component> description, @NotNull Node node, @NotNull RegionModule rmod) {
        super(displayType, name, description);
        this.node = node;
        this.rmod = rmod;
    }

    public static @NotNull Menu createMenu(@NotNull MinigamePlayer viewer, @Nullable Menu previousPage, @NotNull Node node) {
        Menu menu = new Menu(3, RegionMessageManager.getMessage(RegionLangKey.MENU_NODE_NAME,
                Placeholder.unparsed(RegionPlaceHolderKey.NODE.getKey(), node.getName())), viewer);
        menu.setPreviousPage(previousPage);
        List<MenuItem> items = new ArrayList<>();
        for (ActionExecutor ex : node.getExecutors()) {
            items.add(new MenuItemNodeExecutor(node, ex));
        }
        if (previousPage != null) {
            menu.addItem(new MenuItemBack(previousPage), menu.getSize() - 9);
        }
        menu.addItem(new MenuItemNodeExecutorAdd(MenuUtility.createType(),
                RegionLangKey.MENU_EXECUTOR_ADD_NAME, node), menu.getSize() - 1);
        menu.addItems(items);

        return menu;
    }

    @Override
    public @NonNull ItemStack onClick() {
        Menu m = createMenu(getContainer().getViewer(), getContainer(), node);
        m.displayMenu(getContainer().getViewer());
        return ItemStack.empty();
    }

    @Override
    public @NonNull ItemStack onRightClick() {
        rmod.removeNode(node.getName());
        getContainer().removeItem(getSlot());
        return ItemStack.empty();
    }
}
