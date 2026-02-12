package au.com.mineauz.minigames;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.objects.safelocation.ASafeLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinigameUtils {
    private static final @NotNull Pattern PERIOD_PATTERN = Pattern.compile("(\\d+)\\s*((?:ms)|[tsmhdw])", Pattern.CASE_INSENSITIVE);
    private static final @NotNull Pattern LONG_PATTERN = Pattern.compile("-?[0-9]+");

    private static final Pattern BET_MONEY_PATTERN = Pattern.compile("\\$\\s*?(?<amount>[+-]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)");
    private static final DecimalFormat FALLBACK_BET_MONEY_FORMAT = new DecimalFormat("$#0.00");

    /**
     * Try to get a time period of a string.
     * using the same time unit more than once is permitted.
     * If no time unit follows a number, it gets treated as seconds.
     *
     * @return the parsed duration in milliseconds, or null if not possible
     */
    public static @Nullable Long parsePeriod(@NotNull String periodStr) { // todo DateTimeFormatter
        Matcher matcher = PERIOD_PATTERN.matcher(periodStr);
        Long millis = null;

        if (LONG_PATTERN.matcher(periodStr).matches()) {
            return TimeUnit.SECONDS.toMillis(Long.parseLong(periodStr));
        }

        while (matcher.find()) {
            // we got a match.
            if (millis == null) {
                millis = 0L;
            }

            try {
                long num = Long.parseLong(matcher.group(1));

                if (matcher.groupCount() > 1) {
                    String typ = matcher.group(2);
                    millis += switch (typ) { // from periodPattern
                        case "ms" -> num;
                        case "t" -> TimeUnit.SECONDS.toMillis(20L * num); // ticks
                        case "s" -> TimeUnit.SECONDS.toMillis(num);
                        case "m" -> TimeUnit.MINUTES.toMillis(num);
                        case "h" -> TimeUnit.HOURS.toMillis(num);
                        case "d" -> TimeUnit.DAYS.toMillis(num);
                        case "w" -> TimeUnit.DAYS.toMillis(num * 7);
                        default -> 0; // should never get reached because of pattern
                    };
                } else {
                    millis += TimeUnit.SECONDS.toMillis(num);
                }
            } catch (NumberFormatException e) {
                Minigames.getPlugin().getComponentLogger().warn("Couldn't get time period for " + periodStr, e);
            }
        }
        return millis;
    }

    /**
     * Converts seconds into weeks, days, hours, minutes and seconds to be neatly
     * displayed.
     *
     * @param duration - The duration to be converted
     * @param small    - If the time should be shortened to: hh:mm:ss
     * @return A message with a neat time
     */
    public static @NotNull Component convertTime(@NotNull Duration duration, boolean small) { //todo make reverse methode
        long weeks = duration.toDaysPart() / 7L;
        long days = duration.toDaysPart() % 7L;
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();

        Stack<Component> timeComponents = new Stack<>();

        if (small) {
            if (weeks != 0) {
                timeComponents.add(MessageManager.getMessage(MgMiscLangKey.TIME_WEEKS_SHORT,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(weeks))));
            }
            if (days != 0) {
                timeComponents.add(MessageManager.getMessage(MgMiscLangKey.TIME_DAYS_SHORT,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(days))));
            }
            if (hours != 0) {
                timeComponents.add(MessageManager.getMessage(MgMiscLangKey.TIME_HOURS_SHORT,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(hours))));
            }
            if (minutes != 0) {
                timeComponents.add(MessageManager.getMessage(MgMiscLangKey.TIME_MINUTES_SHORT,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(minutes))));
            }

            if (seconds != 0 || timeComponents.isEmpty()) {
                timeComponents.add(MessageManager.getMessage(MgMiscLangKey.TIME_SECONDS_SHORT,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(seconds))));
            }

            return Component.join(JoinConfiguration.separator(Component.text(":")), timeComponents);
        } else {
            if (weeks != 0) {
                timeComponents.add(MessageManager.getMessage(MgMiscLangKey.TIME_WEEKS_LONG,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(weeks))));
            }
            if (days != 0) {
                timeComponents.add(MessageManager.getMessage(MgMiscLangKey.TIME_DAYS_LONG,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(days))));
            }
            if (hours != 0) {
                timeComponents.add(MessageManager.getMessage(MgMiscLangKey.TIME_HOURS_LONG,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(hours))));
            }
            if (minutes != 0) {
                timeComponents.add(MessageManager.getMessage(MgMiscLangKey.TIME_MINUTES_LONG,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(minutes))));
            }
            if (seconds != 0 || timeComponents.isEmpty()) {
                timeComponents.add(MessageManager.getMessage(MgMiscLangKey.TIME_SECONDS_LONG,
                    Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(seconds))));
            }

            Component lastTimeComponent = timeComponents.pop();
            if (!timeComponents.isEmpty()) {
                return Component.join(JoinConfiguration.commas(true), timeComponents).
                    appendSpace().append(MessageManager.getMessage(MgMiscLangKey.AND)).appendSpace().
                    append(lastTimeComponent);
            } else {
                return lastTimeComponent;
            }
        }
    }

    /**
     * Converts seconds into weeks, days, hours, minutes and seconds to be neatly
     * displayed.
     *
     * @param duration - The time to be converted
     * @return A message with a neat time
     */
    public static @NotNull Component convertTime(@NotNull Duration duration) {
        return convertTime(duration, false);
    }

    /**
     * Creates a string ID to compare locations.
     *
     * @param location - The location to give an ID to.
     * @return The ID
     */
    public static @NotNull String createBlockLocationID(@NotNull Location location) {
        return location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ() + ":" + location.getWorld().getName();
    }

    public static @NotNull String createBlockLocationID(@NotNull ASafeLocation location) {
        return location.blockX() + ":" + location.blockY() + ":" + location.blockZ() + ":" + location.getWorld().getName();
    }

    /**
     * Limits the length of a Component ignoring all default formats and styles within it
     *
     * @param component The string to limit
     * @param maxLength The maximum number of characters to allow
     * @return The Component, where it's plain text part is never longer than maxLength
     */
    public static @NotNull Component limitIgnoreFormat(@NotNull Component component, int maxLength) {
        String formatted = MiniMessage.miniMessage().serialize(component);
        String unformatted = MiniMessage.miniMessage().stripTags(formatted);

        if (maxLength >= unformatted.length()) {
            return component;
        }

        StringBuilder result = new StringBuilder();

        int unformattedIndex = 0;
        for (int formattedIndex = 0; formattedIndex < formatted.length(); formattedIndex++) {
            char formattedChar = formatted.charAt(formattedIndex);

            if (formattedChar == unformatted.charAt(unformattedIndex)) {
                unformattedIndex++;

                if (unformattedIndex > maxLength) {
                    break;
                }
            }

            result.append(formattedChar);
        }

        // reassemble to component
        return MiniMessage.miniMessage().deserialize(result.toString());
    }

    @Nullable
    public static String sanitizeYamlString(@NotNull String input) {
        final Pattern pattern = Pattern.compile("^[a-zA-Z\\d_]+$");
        if (!pattern.matcher(input).matches()) {
            return null;
        } else {
            return input;
        }
    }

    public static @Nullable Double getMoneyFromString(final @NotNull String line3, final boolean acceptPlainNumber) {
        final Matcher fallbackMatcher = BET_MONEY_PATTERN.matcher(line3);
        if (fallbackMatcher.matches()) {
            return Double.parseDouble(fallbackMatcher.group("amount"));
        } else if (acceptPlainNumber && NumberUtils.isParsable(line3)) {
            return Double.parseDouble(line3);
        } else {
            return null;
        }
    }

    public static @NotNull Component formatMoney(final double amount) {
        if (Minigames.getPlugin().hasEconomy()) {
            return Component.text(Minigames.getPlugin().getEconomy().format(amount));
        } else {
            return Component.text(FALLBACK_BET_MONEY_FORMAT.format(amount));
        }
    }
}
