package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemPage extends MenuItem {
    private final @NotNull Menu menu;

    public MenuItemPage(@Nullable Material displayMat, @Nullable Component name, @NotNull Menu menu) {
        super(displayMat, name);
        this.menu = menu;
    }

    public MenuItemPage(@Nullable Material displayMat, @NotNull MinigameLangKey name, @NotNull Menu menu) {
        super(displayMat, name);
        this.menu = menu;
    }

    public MenuItemPage(@Nullable Material displayMat, @NotNull MinigameLangKey langKey,
                        @Nullable List<@NotNull Component> description, @NotNull Menu menu) {
        super(displayMat, langKey, description);
        this.menu = menu;
    }

    public MenuItemPage(@Nullable Material displayMat, @Nullable Component name,
                        @Nullable List<@NotNull Component> description, @NotNull Menu menu) {
        super(displayMat, name, description);
        this.menu = menu;
    }

    @Override
    public @Nullable ItemStack onClick() {
        menu.setPreviousPage(getContainer());
        menu.displayMenu(getContainer().getViewer());
        return null;
    }
}
