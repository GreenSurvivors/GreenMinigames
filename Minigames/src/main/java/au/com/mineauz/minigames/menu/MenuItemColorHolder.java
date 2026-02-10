package au.com.mineauz.minigames.menu;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * This MenuItem just echos it's set color when clicked
 */
@SuppressWarnings("UnstableApiUsage") // shutup ItemType
public class MenuItemColorHolder extends AMenuItem {
    // called whenever the item was clicked
    protected final @NotNull Consumer<@NotNull TextColor> colorCallback;
    // the state of this item
    protected @NotNull TextColor color; // don't let the IDE fool you. it's getting set via this.setColor(color) in constructor.

    /**
     * @param displayType should be one of {@link ItemType#LEATHER_HELMET}, {@link ItemType#LEATHER_CHESTPLATE}, {@link ItemType#LEATHER_LEGGINGS}, {@link ItemType#LEATHER_BOOTS} {@link ItemType#LEATHER_HORSE_ARMOR} or {@link ItemType#WOLF_ARMOR}
     *                   the result will be a leather item colored with the given color. If no valid item type is given, this will return a colored {@link ItemType#LEATHER_CHESTPLATE}
     */
    public MenuItemColorHolder(final @Nullable ItemType displayType, final @NotNull TextColor color,
                               final @NotNull Consumer<@NotNull TextColor> colorCallback) {
        super(checkDisplayType(displayType), Component.text(color.asHexString()).color(color));

        this.setColor(color);
        this.colorCallback = colorCallback;
    }

    private static @NotNull ItemType checkDisplayType(final @Nullable ItemType displayType) {
        // note: for whatever reason ColorableArmorMeta extends LeatherArmorMeta and not the other way around, like one would expect....
        if (displayType == null || !LeatherArmorMeta.class.isAssignableFrom(displayType.getItemMetaClass())) {
            return ItemType.LEATHER_CHESTPLATE;
        }

        return displayType;
    }

    /**
     * Accepts the current color
     */
    @Override
    public @NotNull ItemStack onClick() {
        this.colorCallback.accept(this.color);

        return super.onClick();
    }

    /**
     * Accepts the current color
     */
    @Override
    public @NotNull ItemStack onClickWithItem(final @NotNull ItemStack item) {
        this.colorCallback.accept(this.color);

        return super.onClickWithItem(item);
    }

    /**
     * Accepts the current color
     */
    public @NotNull ItemStack onDoubleClick() {
        this.colorCallback.accept(this.color);

        return super.onDoubleClick();
    }

    /**
     * get the color this item displays
     */
    public @NotNull TextColor getColor() {
        return color;
    }

    /**
     * set the color this item displays
     */
    public void setColor(final @NotNull TextColor color) {
        super.getDisplayItem().editMeta(LeatherArmorMeta.class, meta ->
            meta.setColor(Color.fromRGB(color.red(), color.green(), color.blue())));

        this.color = color;
    }
}
