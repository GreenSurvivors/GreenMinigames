package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.menu.MenuUtility;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionModule;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import au.com.mineauz.minigamesregions.language.RegionMessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MenuItemRegion extends MenuItem {
    private final @NotNull Region region;
    private final @NotNull RegionModule rmod;

    public MenuItemRegion(@Nullable ItemType displayType, @Nullable Component name, @NotNull Region region,
                          @NotNull RegionModule rmod) {
        super(displayType, name);
        this.region = region;
        this.rmod = rmod;
    }

    public MenuItemRegion(@Nullable ItemType displayType, @NotNull Component name,
                          @Nullable List<@NotNull Component> description, @NotNull Region region,
                          @NotNull RegionModule rmod) {
        super(displayType, name, description);
        this.region = region;
        this.rmod = rmod;
    }

    public static @NotNull Menu createMenu(@NotNull MinigamePlayer viewer, @Nullable Menu previousPage, @NotNull Region region) {
        Menu menu = new Menu(3, RegionMessageManager.getMessage(RegionLangKey.MENU_REGION_NAME,
                Placeholder.unparsed(MinigamePlaceHolderKey.REGION.getKey(), region.getName())), viewer);
        menu.setPreviousPage(previousPage);
        List<MenuItem> items = new ArrayList<>();
        for (ActionExecutor ex : region.getExecutors()) {
            items.add(new MenuItemRegionExecutor(region, ex));
        }
        if (previousPage != null) {
            menu.addItem(new MenuItemBack(previousPage), menu.getSize() - 9);
        }
        menu.addItem(new MenuItemRegionExecutorAdd(MenuUtility.createType(),
                RegionLangKey.MENU_EXECUTOR_ADD_NAME, region), menu.getSize() - 1);
        menu.addItems(items);

        return menu;
    }

    @Override
    public @NonNull ItemStack onClick() {
        Menu menu = createMenu(getContainer().getViewer(), getContainer(), region);
        menu.displayMenu(getContainer().getViewer());
        return ItemStack.empty();
    }

    @Override
    public @NonNull ItemStack onRightClick() {
        rmod.removeRegion(region.getName());
        getContainer().removeItem(getSlot());
        return ItemStack.empty();
    }
}
