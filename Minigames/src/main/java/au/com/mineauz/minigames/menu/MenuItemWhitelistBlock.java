package au.com.mineauz.minigames.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MenuItemWhitelistBlock extends MenuItem {
    private final List<Material> whitelist;

    public MenuItemWhitelistBlock(@NotNull Material displayMat, List<Material> whitelist) {
        super(Component.translatable(displayItem.translationKey()), displayMat);
        setDescription(List.of("Right Click to remove"));
        this.whitelist = whitelist;
    }

    @Override
    public ItemStack onRightClick() {
        whitelist.remove(getDisplayItem().getType());
        getContainer().removeItem(getSlot());
        return null;
    }
}
