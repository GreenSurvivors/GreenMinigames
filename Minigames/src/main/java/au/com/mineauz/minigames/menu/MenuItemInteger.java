package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
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
import java.util.Objects;
import java.util.regex.Pattern;

/// Does Not roll over, i.e.
/// may set the value to {@link Integer#MAX_VALUE} / {@link Integer#MIN_VALUE} if it would overflow / underflow
public class MenuItemInteger extends AMenuItem implements StringConsumer {
    private static final @NotNull String DESCRIPTION_TOKEN = "Integer_description";
    protected static final @NotNull Pattern INT_PATTERN = Pattern.compile("-?[0-9]+");
    private final @NotNull Callback<@NotNull Integer> value;
    private final @Nullable Integer min; // inclusive
    private final @Nullable Integer max; // inclusive

    public MenuItemInteger(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                           final @NotNull Callback<@NotNull Integer> value,
                           final @Nullable Integer min, final @Nullable Integer max) {
        this(displayType, langKey, null, value, min, max);
    }

    public MenuItemInteger(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                           final @Nullable List<Component> description,
                           final @NotNull Callback<@NotNull Integer> value,
                           final @Nullable Integer min, final @Nullable Integer max) {
        super(displayType, langKey, description);
        this.value = value;
        this.min = min;
        this.max = max;
        updateDescription();
    }

    public MenuItemInteger(final @Nullable ItemType displayType, final @Nullable Component name,
                           final @NotNull Callback<@NotNull Integer> value,
                           final @Nullable Integer min, final @Nullable Integer max) {
        this(displayType, name, null, value, min, max);
    }

    public MenuItemInteger(final @Nullable ItemType displayType, final @Nullable Component name,
                           final @Nullable List<Component> description,
                           final @NotNull Callback<@NotNull Integer> value,
                           final @Nullable Integer min, final @Nullable Integer max) {
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
            value.setValue(Math.addExact(value.getValue(), 1));
            if (max != null && value.getValue() < max) {
                value.setValue(max);
            }
        } catch (ArithmeticException ignored) {
            value.setValue(Objects.requireNonNullElse(max, Integer.MAX_VALUE));
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
        } catch (final @NotNull ArithmeticException ignored) {
            value.setValue(Objects.requireNonNullElse(min, Integer.MIN_VALUE));
        }

        updateDescription();
        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onShiftClick() {
        try {
            value.setValue(Math.addExact(value.getValue(), 10));
            if (max != null && value.getValue() < max) {
                value.setValue(max);
            }
        } catch (final @NotNull ArithmeticException ignored) {
            value.setValue(Objects.requireNonNullElse(max, Integer.MAX_VALUE));
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
            value.setValue(Objects.requireNonNullElse(min, Integer.MIN_VALUE));
        }

        updateDescription();
        return getDisplayItem();
    }

    @Override
    public @NotNull ItemStack onDoubleClick() {
        final @NotNull MinigamePlayer mgPlayer = getMenu().getIntendedViewer();

        final @NotNull Duration reopenTime = Duration.ofSeconds(10);
        MessageManager.sendMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_NUMBER_ENTERCHAT,
            Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()),
            Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(reopenTime)),
            Placeholder.unparsed(MinigamePlaceHolderKey.MIN.getKey(), this.min == null ? "N/A" : this.min.toString()), //todo don't hardcode N/A
            Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), this.max == null ? "N/A" : this.max.toString()));

        getMenu().closeAndWaitForInput(reopenTime, this);

        return ItemStack.empty();
    }

    @Override
    public void acceptString(final @NotNull String string) {
        if (INT_PATTERN.matcher(string).matches()) {
            int entryValue = Integer.parseInt(string);
            if ((min == null || entryValue >= min) && (max == null || entryValue <= max)) {
                value.setValue(entryValue);
                updateDescription();

            } else {
                MessageManager.sendMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR,
                    MgCommandLangKey.COMMAND_ERROR_OUTOFBOUNDS,
                    Placeholder.unparsed(MinigamePlaceHolderKey.MIN.getKey(), this.min == null ? "N/A" : this.min.toString()),
                    Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), this.max == null ? "N/A" : this.max.toString()));
            }
        } else {
            MessageManager.sendMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR,
                MgCommandLangKey.COMMAND_ERROR_NOTNUMBER,
                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), string));
        }

        getMenu().cancelWaitForInput();
        getMenu().displayMenu();
    }
}
