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

public class MenuItemBoolean extends MenuItem {
    private static final String DESCRIPTION_TOKEN = "Boolean_description";
    private final @NotNull Callback<@NotNull Boolean> toggle;

    public MenuItemBoolean(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey,
                           @NotNull Callback<@NotNull Boolean> toggle) {
        super(displayType, langKey);
        this.toggle = toggle;
        update();
    }

    public MenuItemBoolean(@Nullable ItemType displayType, @Nullable Component name,
                           @NotNull Callback<@NotNull Boolean> toggle) {
        super(displayType, name);
        this.toggle = toggle;
        update();
    }

    public MenuItemBoolean(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey, @Nullable List<@NotNull Component> description,
                           @NotNull Callback<@NotNull Boolean> toggle) {
        super(displayType, langKey, description);
        this.toggle = toggle;
        update();
    }

    public MenuItemBoolean(@Nullable ItemType displayType, @Nullable Component name, @Nullable List<@NotNull Component> description,
                           @NotNull Callback<@NotNull Boolean> toggle) {
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
