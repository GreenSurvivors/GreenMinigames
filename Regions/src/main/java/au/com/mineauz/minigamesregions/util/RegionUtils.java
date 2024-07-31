package au.com.mineauz.minigamesregions.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class RegionUtils {
    public static void createWildcardPattern(@NotNull String value, @NotNull StringBuffer buffer) {
        int start = 0;
        int index;
        while (true) {
            index = value.indexOf('%', start);
            // End of input, append the rest
            if (index == -1) {
                buffer.append(Pattern.quote(value.substring(start)));
                break;
            }

            // Append the start
            buffer.append(Pattern.quote(value.substring(start, index)));

            // Append the wildcard code
            buffer.append(".*?");

            // Move to next position
            start = index + 1;
        }
    }
}
