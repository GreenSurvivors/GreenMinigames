package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemPage extends MenuItem {
    private final @NotNull Menu menu;

    public MenuItemPage(@Nullable ItemType displayType, @Nullable Component name, @NotNull Menu menu) {
        super(displayType, name);
        this.menu = menu;
    }

    public MenuItemPage(@Nullable ItemType displayType, @NotNull MinigameLangKey name, @NotNull Menu menu) {
        super(displayType, name);
        this.menu = menu;
    }

    public MenuItemPage(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey,
                        @Nullable List<@NotNull Component> description, @NotNull Menu menu) {
        super(displayType, langKey, description);
        this.menu = menu;
    }

    public MenuItemPage(@Nullable ItemType displayType, @Nullable Component name,
                        @Nullable List<@NotNull Component> description, @NotNull Menu menu) {
        super(displayType, name, description);
        this.menu = menu;
    }

    @Override
    public @NotNull ItemStack onClick() {
        menu.setPreviousPage(getMenu());
        menu.displayMenu();
        return ItemStack.empty();
    }
}
