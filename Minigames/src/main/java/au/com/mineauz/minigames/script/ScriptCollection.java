package au.com.mineauz.minigames.script;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class ScriptCollection implements ScriptReference {
    public static <E extends ScriptReference> @NotNull ScriptCollection of(@NotNull Collection<@NotNull E> collection) {
        return new ScriptCollectionImpl<>(collection);
    }

    public static <E extends ScriptReference> @NotNull ScriptCollection of(@NotNull Map<@NotNull String, @NotNull E> map) {
        return new ScriptCollectionMapImpl<>(map);
    }

    public abstract @Nullable ScriptReference getValue(@NotNull String key) throws IllegalArgumentException, NoSuchElementException;

    public abstract @NotNull Collection<@NotNull String> getKeys();
}
