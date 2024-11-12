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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class MenuItemComponent extends MenuItem implements StringConsumer {
    private final static String DESCRIPTION_VALUE_TOKEN = "COMPONENT_VALUE_DESCRIPTION";
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final @NotNull Callback<Component> component;
    private boolean allowNull = false;

    public MenuItemComponent(@Nullable Material displayMat, @NotNull MinigameLangKey langKey, @NotNull Callback<Component> component) {
        super(displayMat, langKey);
        this.component = component;
        updateDescription();
    }

    public MenuItemComponent(@Nullable Material displayMat, @Nullable Component name, @NotNull Callback<Component> component) {
        super(displayMat, name);
        this.component = component;
        updateDescription();
    }

    public MenuItemComponent(@Nullable Material displayMat, @Nullable Component name,
                             @Nullable List<@NotNull Component> description, @NotNull Callback<Component> component) {
        super(displayMat, name, description);
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
    public @Nullable ItemStack onDoubleClick() {
        MinigamePlayer mgPlayer = getContainer().getViewer();
        mgPlayer.setNoClose(true);
        mgPlayer.getPlayer().closeInventory();

        final int reopenSeconds = 20;
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_STRING_ENTERCHAT,
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()),
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(reopenSeconds))));
        if (allowNull) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_STRING_ALLOWNULL,
                    Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()));
        }
        mgPlayer.setManualEntry(this);
        getContainer().startReopenTimer(reopenSeconds);

        return null;
    }

    @Override
    public void acceptString(@NotNull String string) {
        if (string.equals("null") && allowNull) {
            component.setValue(null);
        } else {
            component.setValue(miniMessage.deserialize(string));
        }

        updateDescription();
        getContainer().cancelReopenTimer();
        getContainer().displayMenu(getContainer().getViewer());
    }
}
