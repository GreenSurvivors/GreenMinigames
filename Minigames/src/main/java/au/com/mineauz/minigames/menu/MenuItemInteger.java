package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
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
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class MenuItemInteger extends MenuItem implements StringConsumer {
    private final static String DESCRIPTION_TOKEN = "Integer_description";
    protected final static @NotNull Pattern INT_PATTERN = Pattern.compile("-?[0-9]+");
    private final @NotNull Callback<Integer> value;
    private final @Nullable Integer min; // inclusive
    private final @Nullable Integer max; // inclusive

    public MenuItemInteger(@Nullable Material displayMat, @NotNull MinigameLangKey langKey, @NotNull Callback<Integer> value,
                           @Nullable Integer min, @Nullable Integer max) {
        super(displayMat, langKey);
        this.value = value;
        this.min = min;
        this.max = max;
        updateDescription();
    }

    public MenuItemInteger(@Nullable Material displayMat, @Nullable Component name, @NotNull Callback<Integer> value,
                           @Nullable Integer min, @Nullable Integer max) {
        super(displayMat, name);
        this.value = value;
        this.min = min;
        this.max = max;
        updateDescription();
    }

    public MenuItemInteger(@Nullable Material displayMat, @NotNull MinigameLangKey langKey, @Nullable List<Component> description,
                           @NotNull Callback<Integer> value, @Nullable Integer min, @Nullable Integer max) {
        super(displayMat, langKey, description);
        this.value = value;
        this.min = min;
        this.max = max;
        updateDescription();
    }

    public MenuItemInteger(@Nullable Material displayMat, @Nullable Component name, @Nullable List<Component> description,
                           @NotNull Callback<Integer> value, @Nullable Integer min, @Nullable Integer max) {
        super(displayMat, name, description);
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
        } catch (ArithmeticException ignored) {
            value.setValue(Objects.requireNonNullElse(min, Integer.MAX_VALUE));
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
        } catch (ArithmeticException ignored) {
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
    public @Nullable ItemStack onDoubleClick() {
        MinigamePlayer mgPlayer = getContainer().getViewer();
        mgPlayer.setNoClose(true);
        mgPlayer.getPlayer().closeInventory();

        final int reopenSeconds = 10;
        MinigameMessageManager.sendMgMessage(mgPlayer, MinigameMessageType.INFO, MgMenuLangKey.MENU_NUMBER_ENTERCHAT,
                Placeholder.component(MinigamePlaceHolderKey.TYPE.getKey(), getName()),
                Placeholder.component(MinigamePlaceHolderKey.TIME.getKey(), MinigameUtils.convertTime(Duration.ofSeconds(reopenSeconds))),
                Placeholder.unparsed(MinigamePlaceHolderKey.MIN.getKey(), this.min == null ? "N/A" : this.min.toString()), //todo don't hardcode N/A
                Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), this.max == null ? "N/A" : this.max.toString()));

        mgPlayer.setManualEntry(this);
        getContainer().startReopenTimer(reopenSeconds);

        return null;
    }

    @Override
    public void acceptString(@NotNull String string) {
        if (INT_PATTERN.matcher(string).matches()) {
            int entryValue = Integer.parseInt(string);
            if ((min == null || entryValue >= min) && (max == null || entryValue <= max)) {
                value.setValue(entryValue);
                updateDescription();

            } else {
                MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR,
                        MgCommandLangKey.COMMAND_ERROR_OUTOFBOUNDS,
                        Placeholder.unparsed(MinigamePlaceHolderKey.MIN.getKey(), this.min == null ? "N/A" : this.min.toString()),
                        Placeholder.unparsed(MinigamePlaceHolderKey.MAX.getKey(), this.max == null ? "N/A" : this.max.toString()));
            }
        } else {
            MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR,
                    MgCommandLangKey.COMMAND_ERROR_NOTNUMBER,
                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), string));
        }

        getContainer().cancelReopenTimer();
        getContainer().displayMenu(getContainer().getViewer());
    }
}
