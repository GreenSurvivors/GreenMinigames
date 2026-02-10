package au.com.mineauz.minigamesregions.menu;

import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MgRegion;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MenuItemRegenRegion extends AMenuItem {
    private final @NotNull MgRegion region;
    private final @NotNull Minigame minigame;

    public MenuItemRegenRegion(final @Nullable ItemType displayType, final @Nullable Component name,
                               final @NotNull MgRegion region, final @NotNull Minigame minigame) {
        this(displayType, name, null, region, minigame);
    }

    public MenuItemRegenRegion(final @Nullable ItemType displayType, final @Nullable Component name,
                               final @Nullable List<@NotNull Component> description,
                               final @NotNull MgRegion region, final @NotNull Minigame minigame) {
        super(displayType, name, description);
        this.region = region;
        this.minigame = minigame;
    }

    //there is nothing in need of configuration
    @Override
    public @NonNull ItemStack onClick() {
        return ItemStack.empty();
    }

    @Override
    public @NonNull ItemStack onRightClick() {
        minigame.removeRegenRegion(region.getName());
        getMenu().removeItem(getSlot());
        return ItemStack.empty();
    }
}
