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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class MenuItemComponent extends MenuItem implements StringConsumer {
    private static final String DESCRIPTION_VALUE_TOKEN = "COMPONENT_VALUE_DESCRIPTION";
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final @NotNull Callback<Component> component;
    private boolean allowNull = false;

    public MenuItemComponent(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey, @NotNull Callback<Component> component) {
        super(displayType, langKey);
        this.component = component;
        updateDescription();
    }

    public MenuItemComponent(@Nullable ItemType displayType, @Nullable Component name, @NotNull Callback<Component> component) {
        super(displayType, name);
        this.component = component;
        updateDescription();
    }

    public MenuItemComponent(@Nullable ItemType displayType, @Nullable Component name,
                             @Nullable List<@NotNull Component> description, @NotNull Callback<Component> component) {
        super(displayType, name, description);
        this.component = component;
        updateDescription();
    }

    public void setAllowNull(boolean allow) {
        allowNull = allow;
    }

    public void updateDescription() {
        Component settingComp = component.getValue();
        if (settingComp == null) {
            settingComp = MinigameMessageManager.getMgMessage(MgMenuLangKey.MENU_ELEMENTNOTSET);
        }

        // limit to a still readable size
        settingComp = MinigameUtils.limitIgnoreFormat(settingComp, 20);

        setDescriptionPart(DESCRIPTION_VALUE_TOKEN, List.of(settingComp));
    }

    @Override
    public @NotNull ItemStack onDoubleClick() {
        MinigamePlayer mgPlayer = getMenu().getIntendedViewer();

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
    public void acceptString(final @NotNull String string) {
        if (string.equals("null") && allowNull) {
            component.setValue(null);
        } else {
            component.setValue(miniMessage.deserialize(string));
        }

        updateDescription();
        getMenu().cancelWaitForInput();
        getMenu().displayMenu();
    }
}
