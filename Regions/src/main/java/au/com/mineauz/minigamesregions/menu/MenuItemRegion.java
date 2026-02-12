package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.menu.MenuDisplayTypes;
import au.com.mineauz.minigames.menu.MenuItemBack;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.ActionExecutor;
import au.com.mineauz.minigamesregions.Region;
import au.com.mineauz.minigamesregions.RegionModule;
import au.com.mineauz.minigamesregions.language.RegionLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MenuItemRegion extends AMenuItem {
    private final @NotNull Region region;
    private final @NotNull RegionModule rmod;

    public MenuItemRegion(final @Nullable ItemType displayType, final @Nullable Component name,
                          final @NotNull Region region, final @NotNull RegionModule rmod) {
        this(displayType, name, null, region, rmod);
    }

    public MenuItemRegion(final @Nullable ItemType displayType, @Nullable Component name,
                          final @Nullable List<@NotNull Component> description,
                          final @NotNull Region region, final @NotNull RegionModule rmod) {
        super(displayType, name, description);
        this.region = region;
        this.rmod = rmod;
    }


    public static @NotNull Menu createMenu(final @NotNull MinigamePlayer viewer, final @NotNull Region region) {
        return createMenu(viewer, null, region);
    }

    public static @NotNull Menu createMenu(final @NotNull Menu previousPage, final @NotNull Region region) {
        return createMenu(previousPage.getIntendedViewer(), previousPage, region);
    }

    @ApiStatus.Obsolete // use one of the ones above, since you only need one - a page or a viewer
    protected static @NotNull Menu createMenu(final @NotNull MinigamePlayer viewer,
                                              final @Nullable Menu previousPage, final @NotNull Region region) {
        final @NotNull Menu menu = new Menu(3, MessageManager.getMessage(RegionLangKey.MENU_REGION_NAME,
                Placeholder.unparsed(MinigamePlaceHolderKey.REGION.getKey(), region.getName())), viewer);
        menu.setPreviousPage(previousPage);
        final @NotNull List<@NotNull AMenuItem> items = new ArrayList<>();
        for (final @NotNull ActionExecutor ex : region.getExecutors()) {
            items.add(new MenuItemActionExecutor(region, ex));
        }
        if (previousPage != null) {
            menu.setItem(new MenuItemBack(previousPage), menu.getSize() - 9);
        }
        menu.setItem(new MenuItemActionExecutorAdd(MenuDisplayTypes.createType(),
                RegionLangKey.MENU_EXECUTOR_ADD_NAME, region), menu.getSize() - 1);
        menu.addItems(items);

        return menu;
    }

    @Override
    public @NonNull ItemStack onClick() {
        final @NotNull Menu menu = createMenu(getMenu(), region);
        menu.displayMenu();
        return ItemStack.empty();
    }

    @Override
    public @NonNull ItemStack onRightClick() {
        rmod.removeRegion(region.getName());
        getMenu().removeItem(getSlot());
        return ItemStack.empty();
    }
}
