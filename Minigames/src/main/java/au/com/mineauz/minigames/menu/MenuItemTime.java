package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class MenuItemTime extends MenuItemLong {
    private static final String DESCRIPTION_TOKEN = "Time_description";

    public MenuItemTime(@Nullable ItemType displayType, @Nullable Component name, @NotNull Callback<Long> value,
                        @Nullable Long min, @Nullable Long max) {
        super(displayType, name, value, min, max);
    }

    public MenuItemTime(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey, @Nullable List<@NotNull Component> description,
                        @NotNull Callback<Long> value, @Nullable Long min, @Nullable Long max) {
        super(displayType, langKey, description, value, min, max);
    }

    public MenuItemTime(@Nullable ItemType displayType, @Nullable Component name, @Nullable List<@NotNull Component> description,
                        @NotNull Callback<Long> value, @Nullable Long min, @Nullable Long max) {
        super(displayType, name, description, value, min, max);
    }

    @Override
    public void updateDescription() {
        Component timeComponent = MinigameUtils.convertTime(Duration.ofMillis(value.getValue()), true).color(NamedTextColor.GREEN);
        setDescriptionPart(DESCRIPTION_TOKEN, List.of(timeComponent));
    }

    @Override
    public void acceptString(@NotNull String string) {
        MinigameUtils.parsePeriod(string); // todo use

        if (LONG_PATTERN.matcher(string).matches()) {
            long entryValue = Long.parseLong(string);
            if ((min == null || entryValue >= min) && (max == null || entryValue <= max)) {
                value.setValue(entryValue);
                updateDescription();

                getMenu().cancelWaitForInput();
                getMenu().displayMenu();
            }
        } else {
            getMenu().cancelWaitForInput();
            getMenu().displayMenu();

            MessageManager.sendMessage(getMenu().getIntendedViewer(), MinigameMessageType.ERROR,
                MgCommandLangKey.COMMAND_ERROR_NOTNUMBER,
                Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), string));
        }
    }
}
