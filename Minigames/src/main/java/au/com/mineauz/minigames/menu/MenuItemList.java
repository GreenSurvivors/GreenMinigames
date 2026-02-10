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
import org.apache.commons.text.WordUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MenuItemList<T> extends AMenuItem implements StringConsumer {
    private static final @NotNull String DESCRIPTION_TOKEN = "List_description";
    protected final @NotNull Callback<T> callback;
    protected final @NotNull List<T> options;
    protected final @NotNull Function<T, @NotNull String> displayFunction;

    /// uses the value#toString() to display it
    public MenuItemList(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                        final @NotNull Callback<@NotNull T> callback, final @NotNull List<@NotNull T> options) {
        this(displayType, langKey, null, callback, options);
    }

    /// uses the value#toString() to display it
    public MenuItemList(final @Nullable ItemType displayType, final @Nullable Component name,
                        final @NotNull Callback<@NotNull T> callback, final @NotNull List<@NotNull T> options) {
        this(displayType, name, null, callback, options);
    }

    /// uses the value#toString() to display it
    public MenuItemList(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                        final @Nullable List<@NotNull Component> description,
                        final @NotNull Callback<@NotNull T> callback, final @NotNull List<@NotNull T> options) {
        this(displayType, langKey, description,
            value -> WordUtils.capitalizeFully(value.toString().replace('_', ' ')),
            callback, options);
    }

    /// uses the value#toString() to display it
    public MenuItemList(final @Nullable ItemType displayType, final @Nullable Component name,
                        final @Nullable List<@NotNull Component> description,
                        final @NotNull Callback<@NotNull T> callback, final @NotNull List<@NotNull T> options) {
        this(displayType, name, description,
            value -> WordUtils.capitalizeFully(value.toString().replace('_', ' ')),
            callback, options);
    }

    /// the display function should return a minimessage formatted String
    public MenuItemList(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                        final @Nullable List<@NotNull Component> description, final @NotNull Function<T, @NotNull String> displayFunction,
                        final @NotNull Callback<@NotNull T> callback, final @NotNull List<@NotNull T> options) {
        super(displayType, langKey, description);
        this.callback = callback;
        this.options = options;
        this.displayFunction = displayFunction;
        updateDescription();
    }

    /// the display function should return a minimessage formatted String
    public MenuItemList(final @Nullable ItemType displayType, final @Nullable Component name,
                        final @Nullable List<@NotNull Component> description, final @NotNull Function<T, @NotNull String> displayFunction,
                        final @NotNull Callback<@NotNull T> callback, final @NotNull List<@NotNull T> options) {
        super(displayType, name, description);
        this.callback = callback;
        this.options = options;
        this.displayFunction = displayFunction;
        updateDescription();
    }

    public void updateDescription() {
        if (options.isEmpty()) {
            return;
        }

        final int pos = options.indexOf(callback.getValue());
        if (pos == -1) {
            setDescriptionPart(DESCRIPTION_TOKEN, MinigameMessageManager.getMgMessageList(MgMenuLangKey.MENU_ERROR_UNKNOWN));
        } else {
            final @NotNull List<@NotNull Component> description = new ArrayList<>(3);

            int before = pos - 1;
            int next = pos + 1;
            if (before < 0) {
                before = options.size() - 1;
            }
            if (next >= options.size()) {
                next = 0;
            }

            final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

            description.add(miniMessage.deserialize(displayFunction.apply(options.get(before))).color(NamedTextColor.GRAY));
            description.add(miniMessage.deserialize(displayFunction.apply(options.get(pos))).color(NamedTextColor.GREEN));
            description.add(miniMessage.deserialize(displayFunction.apply(options.get(next))).color(NamedTextColor.GRAY));

            setDescriptionPart(DESCRIPTION_TOKEN, description);
        }
    }

    @Override
    public @NotNull ItemStack onClick() {
        T oldValue = callback.getValue();
        T newValue = increaseValue(oldValue, false);
        callback.setValue(newValue);

        updateDescription();

        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onShiftClick() {
        final T oldValue = callback.getValue();
        final T newValue = increaseValue(oldValue, true);
        callback.setValue(newValue);

        updateDescription();

        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onRightClick() {
        final T oldValue = callback.getValue();
        final T newValue = decreaseValue(oldValue, false);
        callback.setValue(newValue);

        updateDescription();

        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onShiftRightClick() {
        final T oldValue = callback.getValue();
        final T newValue = decreaseValue(oldValue, true);
        callback.setValue(newValue);

        updateDescription();

        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onDoubleClick() {
        MinigamePlayer mgPlayer = getMenu().getIntendedViewer();

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

        getMenu().closeAndWaitForInput(reopenTime, this);

        return ItemStack.empty();
    }

    @Override
    public void acceptString(final @NotNull String string) {
        final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

        for (final @NotNull T opt : options) {
            if (miniMessage.stripTags(displayFunction.apply(opt)).equalsIgnoreCase(string)) {
                callback.setValue(opt);
                updateDescription();

                getMenu().cancelWaitForInput();
                getMenu().displayMenu();
                return;
            }
        }
        getMenu().cancelWaitForInput();
        getMenu().displayMenu();

        MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR, MgMenuLangKey.MENU_LIST_ERROR_INVALID);
    }

    protected @Nullable T increaseValue(T current, boolean shift) {
        if (options.isEmpty()) {
            return null;
        }

        int index = options.indexOf(current);
        if (index == -1) {
            return options.getFirst();
        }

        ++index;
        if (index >= options.size()) {
            index = 0;
        }

        return options.get(index);
    }

    protected @Nullable T decreaseValue(T current, boolean shift) {
        if (options.isEmpty()) {
            return null;
        }

        int index = options.indexOf(current);
        if (index == -1) {
            return options.getFirst();
        }

        --index;
        if (index < 0) {
            index = options.size() - 1;
        }

        return options.get(index);
    }
}
