package au.com.mineauz.minigames.objects;

import au.com.mineauz.minigames.Minigames;
import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.OptionalInt;

/**
 * this is an almost 1 to 1 copy of {@link org.bukkit.NamespacedKey},
 * with the exception, that our default namespace is {@code minigames}, not {@code minecraft}.
 * I would be happily expanding NamespacedKey, and only ever overwrite the necessary parts, but sadly the class is final.
 */
public class MinigamesKey implements Key {
    private final @NotNull String namespace;
    private final @NotNull String key;

    /**
     * Create a key in a specific namespace.
     * <p>
     * For most plugin related code, you should prefer using the
     * {@link MinigamesKey#MinigamesKey(Plugin, String)} constructor.
     *
     * @param namespace namespace
     * @param key key
     * @see #MinigamesKey(Plugin, String)
     */
    public MinigamesKey(@NotNull String namespace, @NotNull String key) {
        this.namespace = namespace;
        this.key = key;

        this.validate();
    }

    /**
     * Create a key in the plugin's namespace.
     * <p>
     * Namespaces may only contain lowercase alphanumeric characters, periods,
     * underscores, and hyphens.
     * <p>
     * Keys may only contain lowercase alphanumeric characters, periods,
     * underscores, hyphens, and forward slashes.
     *
     * @param plugin the plugin to use for the namespace
     * @param key the key to create
     */
    public MinigamesKey(@NotNull Plugin plugin, @NotNull String key) {
        this.namespace = plugin.namespace();
        this.key = key.toLowerCase(Locale.ROOT);

        // Check validity after normalization
        this.validate();
    }

    private void validate() {
        Preconditions.checkArgument(this.namespace.length() + 1 + this.key.length() <= Short.MAX_VALUE, "MinigamesKey must be less than 32768 characters");
        checkError("[a-z0-9_-.]", "namespace", this.namespace, Key.checkNamespace(this.namespace));
        checkError("[a-z0-9_-./]", "key", this.key, Key.checkValue(this.key));
    }

    private static void checkError(String pattern, String name, String value, OptionalInt index) {
        index.ifPresent(indexValue -> {
            char character = value.charAt(indexValue);
            throw new IllegalArgumentException(String.format("Non %s character in %s '%s' at index %d ('%s', bytes: %s)", pattern, name, value, indexValue, character, Arrays.toString(String.valueOf(character).getBytes(StandardCharsets.UTF_8))));
        });
    }

    @Override
    public int hashCode() {
        int result = this.namespace.hashCode();
        result = (31 * result) + this.key.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Key key)) return false;
        return this.namespace.equals(key.namespace()) && this.key.equals(key.value());
    }

    @Override
    public String toString() {
        return this.namespace + ':' + this.key;
    }

    /**
     * Get a key in the Minecraft namespace.
     *
     * @param key the key to use
     * @return new key in the Minecraft namespace
     */
    @NotNull
    public static MinigamesKey minigames(@NotNull String key) {
        return new MinigamesKey(Minigames.getPlugin(), key);
    }

    /**
     * Get a MinigamesKey from the supplied string with a default namespace if
     * a namespace is not defined. This is a utility method meant to fetch a
     * MinigamesKey from user input. Please note that casing does matter and
     * any instance of uppercase characters will be considered invalid. The
     * input contract is as follows:
     * <pre>
     * fromString("foo", plugin) -{@literal >} "plugin:foo"
     * fromString("foo:bar", plugin) -{@literal >} "foo:bar"
     * fromString(":foo", null) -{@literal >} "minecraft:foo"
     * fromString("foo", null) -{@literal >} "minecraft:foo"
     * fromString("Foo", plugin) -{@literal >} null
     * fromString(":Foo", plugin) -{@literal >} null
     * fromString("foo:bar:bazz", plugin) -{@literal >} null
     * fromString("", plugin) -{@literal >} null
     * </pre>
     *
     * @param string the string to convert to a MinigamesKey
     * @param defaultNamespace the default namespace to use if none was
     * supplied. If null, the {@code minigames} namespace
     * ({@link #minigames(String)}) will be used
     * @return the created MinigamesKey. null if invalid key
     * @see #fromString(String)
     */
    @Nullable
    public static MinigamesKey fromString(@NotNull String string, @Nullable Plugin defaultNamespace) {
        if (string.isEmpty() || string.length() > Short.MAX_VALUE) return null;

        String[] components = string.split(":", 3);
        if (components.length > 2) {
            return null;
        }

        String key = (components.length == 2) ? components[1] : "";
        if (components.length == 1) {
            String value = components[0];
            if (value.isEmpty() || !Key.parseableValue(value)) {
                return null;
            }

            return (defaultNamespace != null) ? new MinigamesKey(defaultNamespace, value) : minigames(value);
        } else if (components.length == 2 && !Key.parseableValue(key)) {
            return null;
        }

        String namespace = components[0];
        if (namespace.isEmpty()) {
            return (defaultNamespace != null) ? new MinigamesKey(defaultNamespace, key) : minigames(key);
        }

        if (!Key.parseableNamespace(namespace)) {
            return null;
        }

        return new MinigamesKey(namespace, key);
    }

    /**
     * Get a MinigamesKey from the supplied string.
     * <p>
     * The default namespace will be Minigames's (i.e.
     * {@link #minigames(String)}).
     *
     * @param key the key to convert to a MinigamesKey
     * @return the created MinigamesKey. null if invalid
     * @see #fromString(String, Plugin)
     */
    @Nullable
    public static MinigamesKey fromString(@NotNull String key) {
        return fromString(key, null);
    }

    @KeyPattern.Namespace
    @NotNull
    @Override
    public String namespace() {
        return this.namespace;
    }

    @KeyPattern.Value
    @NotNull
    @Override
    public String value() {
        return this.key;
    }

    @NotNull
    @Override
    public String asString() {
        return this.toString();
    }
}
