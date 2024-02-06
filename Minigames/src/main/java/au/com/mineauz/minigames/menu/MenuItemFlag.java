package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuItemFlag extends MenuItem {
    private final String flag;
    private final List<String> flags;

    public MenuItemFlag(Material displayMat, String flag, List<String> flags) {
        super(Component.text(flag), displayMat);
        this.flag = flag;
        this.flags = flags;
    }

    public MenuItemFlag(List<Component> description, Material displayMat, String flag, List<String> flags) {
        super(Component.text(flag), description, displayMat);
        this.flag = flag;
        this.flags = flags;
    }

    @Override
    public ItemStack onShiftRightClick() {
        getContainer().getViewer().sendMessage("Removed " + flag + " flag.", MinigameMessageType.ERROR);
        flags.remove(flag);

        getContainer().removeItem(getSlot());
        return null;
    }
}
