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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class MenuItemList<T> extends MenuItem implements StringConsumer { //todo add constructor with map T -> Component for display
    private final static @NotNull String DESCRIPTION_TOKEN = "List_description";
    private final @NotNull Callback<T> value;
    private final @NotNull List<T> options;

    public MenuItemList(@Nullable Material displayMat, @NotNull MinigameLangKey langKey, @NotNull Callback<@NotNull T> value,
                        @NotNull List<@NotNull T> options) {
        super(displayMat, langKey);
        this.value = value;
        this.options = options;
        updateDescription();
    }

    public MenuItemList(@Nullable Material displayMat, @Nullable Component name, @NotNull Callback<@NotNull T> value,
                        @NotNull List<@NotNull T> options) {
        this(displayMat, name, null, value, options);
    }

    public MenuItemList(@Nullable Material displayMat, @NotNull MinigameLangKey langKey,
                        @Nullable List<@NotNull Component> description, @NotNull Callback<@NotNull T> value,
                        @NotNull List<@NotNull T> options) {
        super(displayMat, langKey, description);
        this.value = value;
        this.options = options;
        updateDescription();
    }

    public MenuItemList(@Nullable Material displayMat, @Nullable Component name,
                        @Nullable List<@NotNull Component> description, @NotNull Callback<@NotNull T> value,
                        @NotNull List<@NotNull T> options) {
        super(displayMat, name, description);
        this.value = value;
        this.options = options;
        updateDescription();
    }

    public void updateDescription() {
        if (options.isEmpty()) {
            return;
        }

        int pos = options.indexOf(value.getValue());

        if (pos == -1) {
            setDescriptionPart(DESCRIPTION_TOKEN,
                    MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_ERROR_UNKNOWN));
        } else {
            List<Component> description = new ArrayList<>();

            int before = pos - 1;
            int after = pos + 1;
            if (before < 0) {
                before = options.size() - 1;
            }
            if (after == options.size()) {
                after = 0;
            }

            description.add(Component.text(options.get(before).toString(), NamedTextColor.GRAY));
            description.add(Component.text(options.get(pos).toString(), NamedTextColor.GREEN));
            description.add(Component.text(options.get(after).toString(), NamedTextColor.GRAY));

            setDescriptionPart(DESCRIPTION_TOKEN, description);
        }
    }

    @Override
    public @NotNull ItemStack onClick() {
        int ind = options.lastIndexOf(value.getValue());
        ind++;
        if (ind == options.size()) {
            ind = 0;
        }

        value.setValue(options.get(ind));
        updateDescription();

        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onRightClick() {
        int ind = options.lastIndexOf(value.getValue());
        ind--;
        if (ind == -1) {
            ind = options.size() - 1;
        }

        value.setValue(options.get(ind));
        updateDescription();

        return getDisplayItem();
    }

    @Override
    public @Nullable ItemStack onDoubleClick() {
        MinigamePlayer mgPlayer = getContainer().getViewer();

        mgPlayer.setNoClose(true);
        mgPlayer.getPlayer().closeInventory();
        int reopenSeconds = 10;
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_LIST_ENTERCHAT,
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()),
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(reopenSeconds))));

        String optionsStr = String.join(", ", options.stream().map(Object::toString).toList());
        if (optionsStr.length() > 8000) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMenuLangKey.MENU_LIST_ERROR_TOOLONG);
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_LIST_OPTION,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), optionsStr));
        }

        mgPlayer.setManualEntry(this);
        getContainer().startReopenTimer(reopenSeconds);

        return null;
    }

    @Override
    public void acceptString(@NotNull String string) {
        for (T opt : options) {
            if (opt.toString().equalsIgnoreCase(string)) {
                value.setValue(opt);
                updateDescription();

                getContainer().cancelReopenTimer();
                getContainer().displayMenu(getContainer().getViewer());
                return;
            }
        }
        getContainer().cancelReopenTimer();
        getContainer().displayMenu(getContainer().getViewer());

        MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_LIST_ERROR_INVALID);
    }
}
