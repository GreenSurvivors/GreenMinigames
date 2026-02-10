package au.com.mineauz.minigames.menu;

import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage") // shutup ItemType
public class MenuDisplayTypes {

    public static @NotNull ItemType pageBackType() {
        return ItemType.REDSTONE_TORCH;
    }

    public static @NotNull ItemType pageNextType() {
        return ItemType.COPPER_TORCH;
    }

    public static @NotNull ItemType saveType() {
        return ItemType.GREEN_BED;
    }

    public static @NotNull ItemType createType() {
        return ItemType.ITEM_FRAME;
    }

    public static @NotNull ItemType timeType() {
        return ItemType.CLOCK;
    }

    public static @NotNull ItemType playerType() {
        return ItemType.PLAYER_HEAD;
    }

    public static @NotNull ItemType nameType() {
        return ItemType.NAME_TAG;
    }

    public static @NotNull ItemType potionEffectType() {
        return ItemType.POTION;
    }

    public static @NotNull ItemType genericSubMenu() {
        return ItemType.CHEST;
    }

    public static @NotNull ItemType statistics() {
        return ItemType.WRITABLE_BOOK;
    }

    public static @NotNull ItemType slotFillerType() {
        return ItemType.RED_STAINED_GLASS_PANE;
    }

    /// used, when we don't know what to use to display. Is not supposed whatever we are trying to display is unknown.
    public static @NotNull ItemType unknownType() {
        return ItemType.WHITE_STAINED_GLASS_PANE;
    }
}
