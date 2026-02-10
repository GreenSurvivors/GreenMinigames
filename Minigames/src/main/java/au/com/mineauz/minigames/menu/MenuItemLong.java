package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.consumer.StringConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class MenuItemLong extends AMenuItem implements StringConsumer {
    protected static final @NotNull Pattern LONG_PATTERN = Pattern.compile("-?[0-9]+");
    private static final String DESCRIPTION_TOKEN = "Long_description";
    protected final @NotNull Callback<Long> value;
    protected final @Nullable Long min;
    protected final @Nullable Long max;

    public MenuItemLong(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                        final @NotNull Callback<Long> value,
                        final @Nullable Long min, final @Nullable Long max) {
        this(displayType, langKey, null, value, min, max);
    }

    public MenuItemLong(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                        final @Nullable List<Component> description,
                        final @NotNull Callback<Long> value,
                        final @Nullable Long min, final @Nullable Long max) {
        super(displayType, langKey, description);
        this.value = value;
        this.min = min;
        this.max = max;
        updateDescription();
    }

    public MenuItemLong(final @Nullable ItemType displayType, final @Nullable Component name,
                        final @NotNull Callback<Long> value,
                        final @Nullable Long min, final @Nullable Long max) {
        this(displayType, name, null, value, min, max);
    }

    public MenuItemLong(@Nullable ItemType displayType, @Nullable Component name, @Nullable List<Component> description,
                        @NotNull Callback<Long> value, @Nullable Long min, @Nullable Long max) {
        super(displayType, name, description);
        this.value = value;
        this.min = min;
        this.max = max;
        updateDescription();
    }

    public void updateDescription() {
        setDescriptionPart(DESCRIPTION_TOKEN, List.of(Component.text(value.getValue(), NamedTextColor.GREEN)));
    }

    @Override
    public @NotNull ItemStack onClick() {
        try {
            value.setValue(Math.addExact(value.getValue(), 10));
            if (max != null && value.getValue() < max) {
                value.setValue(max);
            }
        } catch (ArithmeticException ignored) {
            value.setValue(Objects.requireNonNullElse(max, Long.MAX_VALUE));
        }

        updateDescription();
        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onRightClick() {
        try {
            value.setValue(Math.subtractExact(value.getValue(), 1));
            if (min != null && value.getValue() < min) {
                value.setValue(min);
            }
        } catch (ArithmeticException ignored) {
            value.setValue(Objects.requireNonNullElse(min, Long.MIN_VALUE));
        }

        updateDescription();
        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onShiftClick() {
        try {
            value.setValue(Math.addExact(value.getValue(), 10L));
            if (max != null && value.getValue() < max) {
                value.setValue(max);
            }
        } catch (ArithmeticException ignored) {
            value.setValue(Objects.requireNonNullElse(max, Long.MAX_VALUE));
        }

        updateDescription();
        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onShiftRightClick() {
        try {
            value.setValue(Math.subtractExact(value.getValue(), 10));
            if (min != null && value.getValue() < min) {
                value.setValue(min);
            }
        } catch (ArithmeticException ignored) {
            value.setValue(Objects.requireNonNullElse(min, Long.MIN_VALUE));
        }

        updateDescription();
        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onDoubleClick() {
        final @NotNull Duration reopenTime = Duration.ofSeconds(10);
        MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.INFO, MgMenuLangKey.MENU_NUMBER_ENTERCHAT,
            Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()),
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)),
            Placeholder.unparsed(MinigamePlaceHolderKey.MIN.getKey(), this.min == null ? "N/A" : this.min.toString()),
            Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), this.max == null ? "N/A" : this.max.toString()));
        getMenu().closeAndWaitForInput(reopenTime, this);

        return ItemStack.empty();
    }

    @Override
    public void acceptString(final @NotNull String string) {
        if (LONG_PATTERN.matcher(string).matches()) {
            final long entryValue = Long.parseLong(string);
            if ((min == null || entryValue >= min) && (max == null || entryValue <= max)) {
                value.setValue(entryValue);
                updateDescription();

                getMenu().cancelWaitForInput();
                getMenu().displayMenu();
            }
        } else {
            getMenu().cancelWaitForInput();
            getMenu().displayMenu();

            MinigameMessageManager.sendMgMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR,
                MgCommandLangKey.COMMAND_ERROR_NOTNUMBER,
                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), string));
        }
    }
}
