package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuItemBoolean extends AMenuItem {
    private static final String DESCRIPTION_TOKEN = "Boolean_description";
    private final @NotNull Callback<@NotNull Boolean> toggle;

    public MenuItemBoolean(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                           final @NotNull Callback<@NotNull Boolean> toggle) {
        this(displayType, langKey, null, toggle);
    }

    public MenuItemBoolean(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                           final @Nullable List<@NotNull Component> description,
                           final @NotNull Callback<@NotNull Boolean> toggle) {
        super(displayType, langKey, description);
        this.toggle = toggle;
        update();
    }

    public MenuItemBoolean(final @Nullable ItemType displayType, final @Nullable Component name,
                           final @NotNull Callback<@NotNull Boolean> toggle) {
        this(displayType, name, null, toggle);
    }

    public MenuItemBoolean(final @Nullable ItemType displayType, final @Nullable Component name,
                           final @Nullable List<@NotNull Component> description,
                           final @NotNull Callback<@NotNull Boolean> toggle) {
        super(displayType, name, description);
        this.toggle = toggle;
        update();
    }

    @Override
    public void update() {
        MinigameLangKey boolKey = toggle.getValue() ? MgMiscLangKey.BOOL_TRUE : MgMiscLangKey.BOOL_FALSE;
        setDescriptionPart(DESCRIPTION_TOKEN, MinigameMessageManager.getMgMessageList(boolKey));
    }

    @Override
    public @NotNull ItemStack onClick() {
        if (toggle.getValue()) {
            toggle.setValue(false);
        } else {
            toggle.setValue(true);
        }

        update();
        return getDisplayItem();
    }
}
