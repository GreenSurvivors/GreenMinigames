package au.com.mineauz.minigames.menu;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.managers.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.LangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgCommandLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class MenuItemTime extends MenuItemLong {
    private final static String DESCRIPTION_TOKEN = "Time_description";

    public MenuItemTime(@Nullable Material displayMat, @Nullable Component name, @NotNull Callback<Long> value,
                        @Nullable Long min, @Nullable Long max) {
        super(displayMat, name, value, min, max);
    }

    public MenuItemTime(@Nullable Material displayMat, @NotNull LangKey langKey, @Nullable List<@NotNull Component> description,
                        @NotNull Callback<Long> value, @Nullable Long min, @Nullable Long max) {
        super(displayMat, langKey, description, value, min, max);
    }

    public MenuItemTime(@Nullable Material displayMat, @Nullable Component name, @Nullable List<@NotNull Component> description,
                        @NotNull Callback<Long> value, @Nullable Long min, @Nullable Long max) {
        super(displayMat, name, description, value, min, max);
    }

    @Override
    public void updateDescription() {
        Component timeComponent = MinigameUtils.convertTime(Duration.ofMillis(value.getValue()), true).color(NamedTextColor.GREEN);
        setDescriptionPart(DESCRIPTION_TOKEN, List.of(timeComponent));
    }

    @Override
    public void checkValidEntry(String entry) {
        MinigameUtils.parsePeriod(entry);

        if (entry.matches("-?[0-9]+")) {
            long entryValue = Long.parseLong(entry);
            if ((min == null || entryValue >= min) && (max == null || entryValue <= max)) {
                value.setValue(entryValue);
                updateDescription();

                getContainer().cancelReopenTimer();
                getContainer().displayMenu(getContainer().getViewer());
            }
        } else {
            getContainer().cancelReopenTimer();
            getContainer().displayMenu(getContainer().getViewer());

            MinigameMessageManager.sendMgMessage(getContainer().getViewer(), MinigameMessageType.ERROR,
                    MgCommandLangKey.COMMAND_ERROR_NOTNUMBER,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TEXT.getKey(), entry));
        }
    }
}
