package au.com.mineauz.minigames.stats;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import net.kyori.adventure.text.Component;

/**
 * These are the components of {@link StatFormat} indicating what
 * fields are available
 */
public enum StatisticValueField {
    Last("", MgMiscLangKey.STATISTIC_TITLE_LAST),
    Min("_min", MgMiscLangKey.STATISTIC_TITLE_MIN),
    Max("_max", MgMiscLangKey.STATISTIC_TITLE_MAX),
    Total("_total", MgMiscLangKey.STATISTIC_TITLE_TOTAL);

    private final MinigameLangKey titleLangKey;
    private final String suffix;

    StatisticValueField(String suffix, MinigameLangKey titleLangKey) {
        this.suffix = suffix;
        this.titleLangKey = titleLangKey;
    }

    public Component getTitle() {
        return MinigameMessageManager.getMgMessage(titleLangKey);
    }

    public String getSuffix() {
        return suffix;
    }

    /**
     * Can be used to apply this fields function
     *
     * @param currentValue The current value of this stat before update
     * @param newValue     The value being applied to this stat
     * @return The resulting value that should be used for this field
     */
    public long apply(long currentValue, long newValue) {
        return switch (this) {
            case Last -> newValue;
            case Min -> Math.min(currentValue, newValue);
            case Max -> Math.max(currentValue, newValue);
            case Total -> currentValue + newValue;
        };
    }
}
