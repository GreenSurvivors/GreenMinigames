package au.com.mineauz.minigames.menu;

import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage") // shutup ItemType
public class MenuUtility {

    public static @NotNull ItemType backType() {
        return ItemType.REDSTONE_TORCH;
    }

    public static @NotNull ItemType saveType() {
        return ItemType.GREEN_BED;
    }

    public static @NotNull ItemType createType() {
        return ItemType.ITEM_FRAME;
    }

    public static @NotNull ItemType slotFillerType() {
        return ItemType.RED_STAINED_GLASS_PANE;
    }

    public static @NotNull ItemType timeType() {
        return ItemType.CLOCK;
    }

    public static @NotNull ItemType unknownType() {
        return ItemType.WHITE_STAINED_GLASS_PANE;
    }
}
