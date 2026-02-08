package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigamesregions.RegionModule;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MenuItemRegenRegion extends MenuItem {
    private final @NotNull MgRegion region;
    private final @NotNull RegionModule rmod;

    public MenuItemRegenRegion(@Nullable ItemType displayType, @Nullable Component name, @NotNull MgRegion region,
                               @NotNull RegionModule rmod) {
        super(displayType, name);
        this.region = region;
        this.rmod = rmod;
    }

    public MenuItemRegenRegion(@Nullable ItemType displayType, @Nullable Component name,
                               @Nullable List<@NotNull Component> description, @NotNull MgRegion region,
                               @NotNull RegionModule rmod) {
        super(displayType, name, description);
        this.region = region;
        this.rmod = rmod;
    }

    //there is nothing in need of configuration
    @Override
    public @NonNull ItemStack onClick() {
        return ItemStack.empty();
    }

    @Override
    public @NonNull ItemStack onRightClick() {
        rmod.getMinigame().removeRegenRegion(region.getName());
        getMenu().removeItem(getSlot());
        return ItemStack.empty();
    }
}
