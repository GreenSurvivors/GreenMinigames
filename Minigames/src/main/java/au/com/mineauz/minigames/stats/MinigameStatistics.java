package au.com.mineauz.minigames.stats;

import au.com.mineauz.minigames.managers.language.langkeys.MgMenuLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.menu.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class allows you to register stats that are usable in scoreboards
 */
public final class MinigameStatistics {
    public static final @NotNull MinigameStat Wins = new BasicMinigameStat("wins", MgMiscLangKey.STATISTIC_WINS_NAME, StatFormat.Total);
    public static final @NotNull MinigameStat Losses = new BasicMinigameStat("losses", MgMiscLangKey.STATISTIC_LOSSES_NAME, StatFormat.Total); // Fake stat
    public static final @NotNull MinigameStat Attempts = new BasicMinigameStat("attempts", MgMiscLangKey.STATISTIC_ATTEMPTS_NAME, StatFormat.Total);
    // in milliseconds
    public static final @NotNull MinigameStat CompletionTime = new BasicMinigameStat("time", MgMiscLangKey.STATISTIC_TIME_NAME, StatFormat.MinMaxAndTotal);

    public static final @NotNull MinigameStat Kills = new BasicMinigameStat("kills", MgMiscLangKey.STATISTIC_KILLS_NAME, StatFormat.MaxAndTotal);
    public static final @NotNull MinigameStat Deaths = new BasicMinigameStat("deaths", MgMiscLangKey.STATISTIC_DEATHS_NAME, StatFormat.MinAndTotal);
    public static final @NotNull MinigameStat Score = new BasicMinigameStat("score", MgMiscLangKey.STATISTIC_SCORE_NAME, StatFormat.MaxAndTotal);
    public static final @NotNull MinigameStat Reverts = new BasicMinigameStat("reverts", MgMiscLangKey.STATISTIC_REVERTS_NAME, StatFormat.MinAndTotal);

    private static final @NotNull Map<@NotNull String, @NotNull MinigameStat> stats = new HashMap<>();

    static {
        registerStatIntern(Wins);
        registerStatIntern(Losses);
        registerStatIntern(Attempts);
        registerStatIntern(CompletionTime);
        registerStatIntern(Kills);
        registerStatIntern(Deaths);
        registerStatIntern(Score);
        registerStatIntern(Reverts);
    }

    private MinigameStatistics() {
    }

    /**
     * Registers a new stat that is automatically saved and made available to scoreboards
     *
     * @param stat The stat to add. The name of the stat must be unique and must only contain only letters and numbers
     * @throws IllegalArgumentException Thrown if the stat name is not unique or contains invalid characters
     */
    public static void registerStat(final @NotNull DynamicMinigameStat stat) throws IllegalArgumentException {
        registerStatIntern(stat);
    }

    private static void registerStatIntern(final @NotNull MinigameStat stat) throws IllegalArgumentException {
        final @NotNull String name = stat.getName().toLowerCase();

        // Validity tests
        if (!isNameValid(name)) {
            throw new IllegalArgumentException("Invalid name '" + stat.getName() + "' for stat.");
        }

        if (stats.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate stat '" + stat.getName() + "'");
        }

        // Add the stat
        stats.put(name, stat);
    }

    private static boolean isNameValid(final @NotNull String name) {
        for (final char c : name.toCharArray()) {
            if (!Character.isDigit(c) && !Character.isLetter(c)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets a stat by name
     *
     * @param name The name of the stat case-insensitive
     * @return The found stat or null
     */
    public static @Nullable MinigameStat getStat(final @NotNull String name) {
        return stats.get(name.toLowerCase());
    }

    /**
     * Checks if a stat exists
     *
     * @param name The name of the stat case-insensitive
     * @return True if it exists
     */
    public static boolean hasStat(final @NotNull String name) {
        return stats.containsKey(name.toLowerCase());
    }

    /**
     * Removes a previously registered stat. This can remove
     * any stat added through {@link #registerStat(DynamicMinigameStat)}
     *
     * @param name The name of the stat case-insensitive
     * @return True if a stat was removed
     */
    public static boolean removeStat(final @NotNull String name) {
        MinigameStat stat = stats.get(name.toLowerCase());

        if (stat instanceof DynamicMinigameStat) {
            return stats.remove(name.toLowerCase()) != null;
        } else {
            return false;
        }
    }

    /**
     * @return Returns an unmodifiable map of all registered stats
     */
    public static @NotNull @Unmodifiable Map<@NotNull String, @NotNull MinigameStat> getAllStats() {
        return Collections.unmodifiableMap(stats);
    }

    /**
     * @return Returns all dynamic stats
     */
    public static @NotNull @Unmodifiable Iterable<@NotNull DynamicMinigameStat> getDynamicStats() {
        return stats.values().stream()
            .filter(DynamicMinigameStat.class::isInstance)
            .map(DynamicMinigameStat.class::cast)
            .toList();
    }

    /**
     * Creates a menu that allows you to select a statistic
     *
     * @param parent       The parent menu
     * @param statCallback The callback to be invoked when the statistic is chosen. Note: only the setValue() method will be called.
     * @return The menu to display
     */
    public static @NotNull Menu createStatSelectMenu(final @NotNull Menu parent, final @NotNull Callback<MinigameStat> statCallback) {
        final @NotNull Menu submenu = new Menu(6, MgMenuLangKey.MENU_STAT_SELECT_NAME, parent.getIntendedViewer());

        for (final @NotNull MinigameStat stat : getAllStats().values()) {
            final @NotNull MenuItemCustom item = new MenuItemCustom(MenuDisplayTypes.statistics(), stat.getDisplayName());
            item.setClick(() -> {
                statCallback.setValue(stat);
                parent.displayMenu();
                return ItemStack.empty();
            });

            submenu.addItem(item);
        }

        submenu.setItem(new MenuItemBack(parent), submenu.getSize() - 9);
        return submenu;
    }

    /**
     * Creates a menu that allows you to select a statistic field
     *
     * @param parent   The parent menu
     * @param format   The format to define the fields available
     * @param callback The callback to be invoked when the field is chosen. Note: only the setValue() method will be called.
     * @return The menu to display
     */
    @NotNull
    public static Menu createStatFieldSelectMenu(final @NotNull Menu parent, final @NotNull StatFormat format, final @NotNull Callback<StatisticValueField> callback) {
        final @NotNull Menu submenu = new Menu(6, MgMenuLangKey.MENU_STAT_SELECT_FIELD_NAME, parent.getIntendedViewer());

        for (final @NotNull StatisticValueField field : format.getFields()) {
            final @NotNull MenuItemCustom item = new MenuItemCustom(ItemType.PAPER, field.getTitle());
            item.setClick(() -> {
                callback.setValue(field);
                parent.displayMenu();
                return ItemStack.empty();
            });

            submenu.addItem(item);
        }

        submenu.setItem(new MenuItemBack(parent), submenu.getSize() - 9);
        return submenu;
    }
}
