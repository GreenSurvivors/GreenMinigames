package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class MenuItemEnum<T extends Enum<T>> extends MenuItemList<T> {
    public MenuItemEnum(final @Nullable ItemType displayType, final @Nullable Component name,
                        final @NotNull Callback<T> callback, final @NotNull Class<T> enumClass) {
        this(displayType, name, null, callback, enumClass);
    }

    public MenuItemEnum(final @Nullable ItemType displayType, final @Nullable Component name,
                        final @Nullable List<@NotNull Component> description,
                        final @NotNull Callback<T> callback, final @NotNull Class<T> enumClass) {
        super(displayType, name, description, callback, new ArrayList<>(EnumSet.allOf(enumClass)));
    }

    public MenuItemEnum(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                        final @NotNull Callback<T> callback, final @NotNull Class<T> enumClass) {
        super(displayType, langKey, callback, new ArrayList<>(EnumSet.allOf(enumClass)));
    }
}
