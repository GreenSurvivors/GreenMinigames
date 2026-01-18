package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused") // api
public class MenuItemCustom extends MenuItem {
    private @Nullable Supplier<@NotNull ItemStack> click = null;
    private @Nullable Function<ItemStack, @NotNull ItemStack> clickItem = null;
    private @Nullable Supplier<@NotNull ItemStack> rightClick = null;
    private @Nullable Supplier<@NotNull ItemStack> shiftClick = null;
    private @Nullable Supplier<@NotNull ItemStack> shiftRightClick = null;
    private @Nullable Supplier<@NotNull ItemStack> doubleClick = null;

    public MenuItemCustom(@Nullable ItemType displayType, @Nullable Component name) {
        super(displayType, name);
    }

    public MenuItemCustom(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey) {
        super(displayType, langKey);
    }

    public MenuItemCustom(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey,
                          @Nullable List<@NotNull Component> description) {
        super(displayType, langKey, description);
    }

    public MenuItemCustom(@Nullable ItemType displayType, @Nullable Component name,
                          @Nullable List<@NotNull Component> description) {
        super(displayType, name, description);
    }

    @Override
    public @NotNull ItemStack onClick() {
        if (click != null) {
            return click.get();
        }
        return getDisplayItem();
    }

    public void setClick(@Nullable Supplier<@NotNull ItemStack> sup) {
        click = sup;
    }

    @Override
    public @NotNull ItemStack onClickWithItem(@NotNull ItemStack item) {
        if (clickItem != null)
            return clickItem.apply(item);
        return getDisplayItem();
    }

    public void setClickItem(@Nullable Function<ItemStack, @NotNull ItemStack> func) {
        clickItem = func;
    }

    @Override
    public @NotNull ItemStack onRightClick() {
        if (rightClick != null) {
            return rightClick.get();
        }
        return getDisplayItem();
    }

    public void setRightClick(@Nullable Supplier<@NotNull ItemStack> sup) {
        rightClick = sup;
    }

    @Override
    public @NotNull ItemStack onShiftClick() {
        if (shiftClick != null) {
            return shiftClick.get();
        }
        return getDisplayItem();
    }

    public void setShiftClick(@Nullable Supplier<@NotNull ItemStack> sup) {
        shiftClick = sup;
    }

    @Override
    public @NotNull ItemStack onShiftRightClick() {
        if (shiftRightClick != null) {
            return shiftRightClick.get();
        }
        return getDisplayItem();
    }

    public void setShiftRightClick(@Nullable Supplier<@NotNull ItemStack> sup) {
        shiftRightClick = sup;
    }

    @Override
    public @NotNull ItemStack onDoubleClick() {
        if (doubleClick != null) {
            return doubleClick.get();
        }
        return getDisplayItem();
    }

    public void setDoubleClick(@Nullable Supplier<@NotNull ItemStack> sup) {
        doubleClick = sup;
    }
}
