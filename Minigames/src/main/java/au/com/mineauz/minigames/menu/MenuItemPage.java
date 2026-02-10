package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// also see {@link MenuDisplayTypes#genericSubMenu()}
public class MenuItemPage extends AMenuItem {
    private final @NotNull Menu menu;

    public MenuItemPage(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                        final @NotNull Menu menu) {
        this(displayType, langKey, null, menu);
    }

    public MenuItemPage(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                        final @Nullable List<@NotNull Component> description,
                        final @NotNull Menu menu) {
        super(displayType, langKey, description);
        this.menu = menu;
    }

    public MenuItemPage(final @Nullable ItemType displayType, final @Nullable Component name,
                        final @NotNull Menu menu) {
        this(displayType, name, null, menu);
    }

    public MenuItemPage(final @Nullable ItemType displayType, final @Nullable Component name,
                        final @Nullable List<@NotNull Component> description,
                        final @NotNull Menu menu) {
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
