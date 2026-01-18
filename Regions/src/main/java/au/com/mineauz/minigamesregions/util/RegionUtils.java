package au.com.mineauz.minigamesregions.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;
import java.util.SequencedCollection;
import java.util.random.RandomGenerator;
import java.util.regex.Pattern;

public class RegionUtils {
    public static void createWildcardPattern(final @NotNull String value, final @NotNull StringBuffer buffer) {
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

    // stolen from Collections.shuffle, but adapted to work with all SequencedCollection's
    public static <T> void shuffle(final @NotNull SequencedCollection<T> collection, final @NotNull RandomGenerator rnd) {
        final int size = collection.size();
        if ((size < 5 || collection instanceof RandomAccess) && collection instanceof List<T> list) {
            for (int i = size; i > 1; i--) {
                Collections.swap(list, i-1, rnd.nextInt(i));
            }
        } else {
            Object[] arr = collection.toArray();

            // Shuffle array
            for (int i = size; i > 1; i--) {
                swap(arr, i - 1, rnd.nextInt(i));
            }

            // Dump array back into collection
            collection.clear();
            for (Object e : arr) {
                collection.addLast((T)e);
            }
        }
    }

    public static void swap(final @UnknownNullability Object @NotNull[] arr, final int i, final int j) {
        Object tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
}
