package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class MenuItemComponent extends AMenuItem implements StringConsumer {
    private static final String DESCRIPTION_VALUE_TOKEN = "COMPONENT_VALUE_DESCRIPTION";
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final @NotNull Callback<@Nullable Component> callback;
    private boolean allowNull = false;

    public MenuItemComponent(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                             final @NotNull Callback<@NotNull Component> callback) {
        super(displayType, langKey);
        this.callback = callback;
        updateDescription();
    }

    public MenuItemComponent(final @Nullable ItemType displayType, final @Nullable Component name,
                             final @NotNull Callback<@NotNull Component> callback) {
        this(displayType, name, null, callback);
    }

    public MenuItemComponent(@Nullable ItemType displayType, @Nullable Component name,
                             @Nullable List<@NotNull Component> description, @NotNull Callback<Component> callback) {
        super(displayType, name, description);
        this.callback = callback;
        updateDescription();
    }

    public void setAllowNull(boolean allow) {
        allowNull = allow;
    }

    public void updateDescription() {
        @Nullable Component settingComp = callback.getValue();
        if (settingComp == null) {
            settingComp = MessageManager.getMessage(MgMenuLangKey.MENU_ELEMENTNOTSET);
        }

        // limit to a still readable size
        settingComp = MinigameUtils.limitIgnoreFormat(settingComp, 20);

        setDescriptionPart(DESCRIPTION_VALUE_TOKEN, List.of(settingComp));
    }

    @Override
    public @NotNull ItemStack onDoubleClick() {
        final @NotNull MinigamePlayer mgPlayer = getMenu().getIntendedViewer();

        final @NotNull Duration reopenTime = Duration.ofSeconds(20);
        MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_STRING_ENTERCHAT,
            Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()),
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));
        if (allowNull) {
            MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_STRING_ALLOWNULL,
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()));
        }
        getMenu().closeAndWaitForInput(reopenTime, this);

        return ItemStack.empty();
    }

    @Override
    public void acceptString(final @NotNull String string) {
        if (string.equals("null") && allowNull) {
            callback.setValue(null);
        } else {
            callback.setValue(miniMessage.deserialize(string));
        }

        updateDescription();
        getMenu().cancelWaitForInput();
        getMenu().displayMenu();
    }
}
