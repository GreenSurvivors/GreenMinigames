package au.com.mineauz.minigames.menu;

import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage") // shutup ItemType
public class MenuUtility {

    public static @NotNull ItemType getBackType() {
        return ItemType.REDSTONE_TORCH;
    }

    public static @NotNull ItemType getSaveType() {
        return ItemType.GREEN_BED;
    }

    public static @NotNull ItemType getCreateType() {
        return ItemType.ITEM_FRAME;
    }

    public static @NotNull ItemType getSlotFillerType() {
        return ItemType.RED_STAINED_GLASS_PANE;
    }

    public static @NotNull ItemType getUnknownDisplayType() {
        return ItemType.WHITE_STAINED_GLASS_PANE;
    }
}
