package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class MenuItemString extends MenuItem implements StringConsumer {
    private static final String DESCRIPTION_TOKEN = "String_description";
    private final @NotNull Callback<String> stringCallback;
    private boolean allowNull = false;

    public MenuItemString(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey, @NotNull Callback<String> stringCallback) {
        super(displayType, langKey);
        this.stringCallback = stringCallback;
        updateDescription();
    }

    public MenuItemString(@Nullable ItemType displayType, @Nullable Component name, @NotNull Callback<String> stringCallback) {
        super(displayType, name);
        this.stringCallback = stringCallback;
        updateDescription();
    }

    public MenuItemString(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey,
                          @Nullable List<@NotNull Component> description, @NotNull Callback<String> str) {
        super(displayType, langKey, description);
        this.stringCallback = str;
        updateDescription();
    }

    public MenuItemString(@Nullable ItemType displayType, @Nullable Component name,
                          @Nullable List<@NotNull Component> description, @NotNull Callback<String> stringCallback) {
        super(displayType, name, description);
        this.stringCallback = stringCallback;
        updateDescription();
    }

    public void setAllowNull(boolean allow) {
        allowNull = allow;
    }

    public void updateDescription() {
        String setting = stringCallback.getValue();
        if (setting == null) {
            setDescriptionPart(DESCRIPTION_TOKEN, List.of(
                MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_ELEMENTNOTSET).color(NamedTextColor.GRAY)));
        } else if (setting.length() > 20) {
            setting = setting.substring(0, 17) + "...";
            setDescriptionPart(DESCRIPTION_TOKEN, List.of(Component.text(setting, NamedTextColor.GREEN)));
        }
    }

    @Override
    public @NotNull ItemStack onDoubleClick() {
        final @NotNull MinigamePlayer mgPlayer = getMenu().getIntendedViewer();
        final @NotNull Duration reopenTime = Duration.ofSeconds(20);
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_STRING_ENTERCHAT,
            Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()),
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));
        if (allowNull) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_STRING_ALLOWNULL,
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()));
        }
        getMenu().closeAndWaitForInput(reopenTime, this);

        return ItemStack.empty();
    }

    @Override
    public void acceptString(@NotNull String string) {
        if (string.equals("null") && allowNull) {
            stringCallback.setValue(null);
        } else {
            stringCallback.setValue(string);
        }

        updateDescription();
        getMenu().cancelWaitForInput();
        getMenu().displayMenu();
    }
}
