package au.com.mineauz.minigames.script;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

class ScriptCollectionMapImpl<E extends ScriptReference> extends ScriptCollection {
    private final @NotNull Map<@NotNull String, @NotNull E> map;

    public ScriptCollectionMapImpl(@NotNull Map<@NotNull String, @NotNull E> map) {
        this.map = map;
    }

    @Override
    public @NotNull ScriptReference getValue(@NotNull String key) throws IllegalArgumentException, NoSuchElementException {
        ScriptReference ref = map.get(key);
        if (ref == null) {
            throw new NoSuchElementException();
        }

        return ref;
    }

    @Override
    public @NotNull Collection<@NotNull String> getKeys() {
        return map.keySet();
    }
}
