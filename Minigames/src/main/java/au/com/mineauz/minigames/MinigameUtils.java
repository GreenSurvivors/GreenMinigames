package au.com.mineauz.minigames;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinigameUtils {
    private static final @NotNull Pattern PERIOD_PATTERN = Pattern.compile("(\\d+)\\s*((?:ms)|[tsmhdw])", Pattern.CASE_INSENSITIVE);
    private static final @NotNull Pattern LONG_PATTERN = Pattern.compile("-?[0-9]+");

    /**
     * Try to get a time period of a string.
     * using the same time unit more than once is permitted.
     * If no time unit follows a number, it gets treated as seconds.
     *
     * @return the parsed duration in milliseconds, or null if not possible
     */
    public static @Nullable Long parsePeriod(@NotNull String periodStr) {
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
    public static Component convertTime(Duration duration, boolean small) { //todo make reverse methode
        long weeks = duration.toDaysPart() / 7L;
        long days = duration.toDaysPart() % 7L;
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();

        Stack<Component> timeComponents = new Stack<>();

        if (small) {
            if (weeks != 0) {
                timeComponents.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TIME_WEEKS_SHORT,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(weeks))));
            }
            if (days != 0) {
                timeComponents.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TIME_DAYS_SHORT,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(days))));
            }
            if (hours != 0) {
                timeComponents.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TIME_HOURS_SHORT,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(hours))));
            }
            if (minutes != 0) {
                timeComponents.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TIME_MINUTES_SHORT,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(minutes))));
            }

            if (seconds != 0 || timeComponents.isEmpty()) {
                timeComponents.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TIME_SECONDS_SHORT,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(seconds))));
            }

            return Component.join(JoinConfiguration.separator(Component.text(":")), timeComponents);
        } else {
            if (weeks != 0) {
                timeComponents.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TIME_WEEKS_LONG,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(weeks))));
            }
            if (days != 0) {
                timeComponents.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TIME_DAYS_LONG,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(days))));
            }
            if (hours != 0) {
                timeComponents.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TIME_HOURS_LONG,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(hours))));
            }
            if (minutes != 0) {
                timeComponents.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TIME_MINUTES_LONG,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(minutes))));
            }
            if (seconds != 0 || timeComponents.isEmpty()) {
                timeComponents.add(MinigameMessageManager.getMgMessage(MgMiscLangKey.TIME_SECONDS_LONG,
                        Placeholder.unparsed(MinigamePlaceHolderKey.TIME.getKey(), String.valueOf(seconds))));
            }

            Component lastTimeComponent = timeComponents.pop();
            if (!timeComponents.isEmpty()) {
                return Component.join(JoinConfiguration.commas(true), timeComponents).
                        appendSpace().append(MinigameMessageManager.getMgMessage(MgMiscLangKey.AND)).appendSpace().
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
    public static Component convertTime(Duration duration) {
        return convertTime(duration, false);
    }

    /**
     * Creates a string ID to compare locations.
     *
     * @param location - The location to give an ID to.
     * @return The ID
     */
    public static String createLocationID(Location location) {
        return location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ() + ":" + location.getWorld().getName();
    }

    /**
     * Loads a short location (x, y, z, world) from a configuration section
     *
     * @param section The section that contains the fields
     * @return A location with the contents of that section, or null if the world is invalid
     */
    public static Location loadShortLocation(ConfigurationSection section) {
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");

        String worldName = section.getString("world");
        World world = Bukkit.getWorld(worldName);

        if (world != null) {
            return new Location(world, x, y, z);
        } else {
            return null;
        }
    }

    /**
     * Saves a short location (x, y, z, world) to a configuration section
     *
     * @param section  The ConfigurationSection to save into
     * @param location The location to save
     */
    public static void saveShortLocation(ConfigurationSection section, Location location) {
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("world", location.getWorld().getName());
    }

    /**
     * Limits the length of a Component ignoring all default formats and styles within it
     *
     * @param component The string to limit
     * @param maxLength The maximum number of characters to allow
     * @return The Component, where it's plain text part is never longer than maxLength
     */
    public static Component limitIgnoreFormat(Component component, int maxLength) {
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

    public static String sanitizeYamlString(String input) {
        final Pattern pattern = Pattern.compile("^[a-zA-Z\\d_]+$");
        if (!pattern.matcher(input).matches()) {
            return null;
        } else {
            return input;
        }
    }
}
