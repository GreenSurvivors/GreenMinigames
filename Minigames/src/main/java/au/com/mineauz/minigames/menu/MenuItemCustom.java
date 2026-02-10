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
public class MenuItemCustom extends AMenuItem {
    private @Nullable Supplier<@NotNull ItemStack> click = null;
    private @Nullable Function<@NotNull ItemStack, @NotNull ItemStack> clickItem = null;
    private @Nullable Supplier<@NotNull ItemStack> rightClick = null;
    private @Nullable Supplier<@NotNull ItemStack> shiftClick = null;
    private @Nullable Supplier<@NotNull ItemStack> shiftRightClick = null;
    private @Nullable Supplier<@NotNull ItemStack> doubleClick = null;

    public MenuItemCustom(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey) {
        this(displayType, langKey, null);
    }

    public MenuItemCustom(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                          final @Nullable List<@NotNull Component> description) {
        super(displayType, langKey, description);
    }


    public MenuItemCustom(final @Nullable ItemType displayType, final @Nullable Component name) {
        super(displayType, name);
    }
    public MenuItemCustom(final @Nullable ItemType displayType, final @Nullable Component name,
                          final @Nullable List<@NotNull Component> description) {
        super(displayType, name, description);
    }

    @Override
    public @NotNull ItemStack onClick() {
        if (click != null) {
            return click.get();
        }
        return getDisplayItem();
    }

    public void setClick(final @Nullable Supplier<@NotNull ItemStack> sup) {
        click = sup;
    }

    @Override
    public @NotNull ItemStack onClickWithItem(final @NotNull ItemStack item) {
        if (clickItem != null)
            return clickItem.apply(item);
        return getDisplayItem();
    }

    public void setClickItem(final @Nullable Function<@NotNull ItemStack, @NotNull ItemStack> func) {
        clickItem = func;
    }

    @Override
    public @NotNull ItemStack onRightClick() {
        if (rightClick != null) {
            return rightClick.get();
        }
        return getDisplayItem();
    }

    public void setRightClick(final @Nullable Supplier<@NotNull ItemStack> sup) {
        rightClick = sup;
    }

    @Override
    public @NotNull ItemStack onShiftClick() {
        if (shiftClick != null) {
            return shiftClick.get();
        }
        return getDisplayItem();
    }

    public void setShiftClick(final @Nullable Supplier<@NotNull ItemStack> sup) {
        shiftClick = sup;
    }

    @Override
    public @NotNull ItemStack onShiftRightClick() {
        if (shiftRightClick != null) {
            return shiftRightClick.get();
        }
        return getDisplayItem();
    }

    public void setShiftRightClick(final @Nullable Supplier<@NotNull ItemStack> sup) {
        shiftRightClick = sup;
    }

    @Override
    public @NotNull ItemStack onDoubleClick() {
        if (doubleClick != null) {
            return doubleClick.get();
        }
        return getDisplayItem();
    }

    public void setDoubleClick(final @Nullable Supplier<@NotNull ItemStack> sup) {
        doubleClick = sup;
    }
}
