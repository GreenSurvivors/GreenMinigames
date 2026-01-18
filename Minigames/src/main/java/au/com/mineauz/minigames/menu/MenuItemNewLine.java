package au.com.mineauz.minigames.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

public class MenuItemNewLine extends MenuItem {

    public MenuItemNewLine() {
        super((ItemType) null, Component.text("NL")); // since it will never be visible anyway we can hardcode the name
    }

    @Override
    public @NotNull ItemStack onClick() {
        return ItemStack.empty();
    }

    @Override
    public @NotNull ItemStack onRightClick() {
        return ItemStack.empty();
    }

    @Override
    public @NotNull ItemStack onShiftClick() {
        return ItemStack.empty();
    }

    @Override
    public @NotNull ItemStack onShiftRightClick() {
        return ItemStack.empty();
    }

    @Override
    public @NotNull ItemStack onDoubleClick() {
        return ItemStack.empty();
    }
}
