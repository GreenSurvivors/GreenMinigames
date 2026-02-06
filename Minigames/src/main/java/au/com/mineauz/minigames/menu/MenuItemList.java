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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MenuItemList<T> extends MenuItem implements StringConsumer {
    private static final @NotNull String DESCRIPTION_TOKEN = "List_description";
    private final @NotNull Callback<T> valueCallback;
    private final @NotNull List<T> options;
    private final @NotNull Function<T, @NotNull String> displayFunction;

    /// uses the value#toString() to display it
    public MenuItemList(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                        final @NotNull Callback<@NotNull T> valueCallback, final @NotNull List<@NotNull T> options) {
        this(displayType, langKey, null, valueCallback, options);
    }

    /// uses the value#toString() to display it
    public MenuItemList(final @Nullable ItemType displayType, final @Nullable Component name,
                        final @NotNull Callback<@NotNull T> valueCallback, final @NotNull List<@NotNull T> options) {
        this(displayType, name, null, valueCallback, options);
    }

    /// uses the value#toString() to display it
    public MenuItemList(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                        final @Nullable List<@NotNull Component> description,
                        final @NotNull Callback<@NotNull T> valueCallback, final @NotNull List<@NotNull T> options) {
        this(displayType, langKey, description, Object::toString, valueCallback, options);
    }

    /// uses the value#toString() to display it
    public MenuItemList(final @Nullable ItemType displayType, final @Nullable Component name,
                        final @Nullable List<@NotNull Component> description,
                        final @NotNull Callback<@NotNull T> valueCallback, final @NotNull List<@NotNull T> options) {
        this(displayType, name, description, Object::toString, valueCallback, options);
    }

    /// the display function should return a minimessage formatted String
    public MenuItemList(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                        final @Nullable List<@NotNull Component> description, final @NotNull Function<T, @NotNull String> displayFunction,
                        final @NotNull Callback<@NotNull T> valueCallback, final @NotNull List<@NotNull T> options) {
        super(displayType, langKey, description);
        this.valueCallback = valueCallback;
        this.options = options;
        this.displayFunction = displayFunction;
        updateDescription();
    }

    /// the display function should return a minimessage formatted String
    public MenuItemList(final @Nullable ItemType displayType, final @Nullable Component name,
                        final @Nullable List<@NotNull Component> description, final @NotNull Function<T, @NotNull String> displayFunction,
                        final @NotNull Callback<@NotNull T> valueCallback, final @NotNull List<@NotNull T> options) {
        super(displayType, name, description);
        this.valueCallback = valueCallback;
        this.options = options;
        this.displayFunction = displayFunction;
        updateDescription();
    }

    public void updateDescription() {
        if (options.isEmpty()) {
            return;
        }

        final int pos = options.indexOf(valueCallback.getValue());

        if (pos == -1) {
            setDescriptionPart(DESCRIPTION_TOKEN, MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_ERROR_UNKNOWN));
        } else {
            final @NotNull List<@NotNull Component> description = new ArrayList<>();

            int before = pos - 1;
            int after = pos + 1;
            if (before < 0) {
                before = options.size() - 1;
            }
            if (after >= options.size()) {
                after = 0;
            }

            final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

            description.add(miniMessage.deserialize(displayFunction.apply(options.get(before))).color(NamedTextColor.GRAY));
            description.add(miniMessage.deserialize(displayFunction.apply(options.get(pos))).color(NamedTextColor.GREEN));
            description.add(miniMessage.deserialize(displayFunction.apply(options.get(after))).color(NamedTextColor.GRAY));

            setDescriptionPart(DESCRIPTION_TOKEN, description);
        }
    }

    @Override
    public @NotNull ItemStack onClick() {
        int ind = options.lastIndexOf(valueCallback.getValue());
        ind++;
        if (ind == options.size()) {
            ind = 0;
        }

        valueCallback.setValue(options.get(ind));
        updateDescription();

        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onRightClick() {
        int ind = options.lastIndexOf(valueCallback.getValue());
        ind--;
        if (ind == -1) {
            ind = options.size() - 1;
        }

        valueCallback.setValue(options.get(ind));
        updateDescription();

        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onDoubleClick() {
        MinigamePlayer mgPlayer = getContainer().getViewer();

        mgPlayer.setNoClose(true);
        mgPlayer.getPlayer().closeInventory();
        final @NotNull Duration reopenTime = Duration.ofSeconds(10);
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_LIST_ENTERCHAT,
            Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()),
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)));

        String optionsStr = String.join(", ", options.stream().map(displayFunction).toList());
        if (MiniMessage.miniMessage().stripTags(optionsStr).length() > 8000) {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.ERROR, MgMenuLangKey.MENU_LIST_ERROR_TOOLONG);
        } else {
            MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_LIST_OPTION,
                Placeholder.parsed(MinigamePlaceHolderKey.TEXT.getKey(), optionsStr));
        }

        mgPlayer.setManualEntry(this);
        getContainer().startReopenTimer(reopenTime);

        return ItemStack.empty();
    }

    @Override
    public void acceptString(final @NotNull String string) {
        final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

        for (final @NotNull T opt : options) {
            if (miniMessage.stripTags(displayFunction.apply(opt)).equalsIgnoreCase(string)) {
                valueCallback.setValue(opt);
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
