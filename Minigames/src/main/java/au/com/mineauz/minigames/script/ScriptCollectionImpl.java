package au.com.mineauz.minigames.script;

import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;

class ScriptCollectionImpl<E extends ScriptReference> extends ScriptCollection {
    private final @NotNull Collection<E> collection;

    public ScriptCollectionImpl(@NotNull Collection<@NotNull E> collection) {
        this.collection = collection;
    }

    @Override
    public @Nullable ScriptReference getValue(@NotNull String key) throws IllegalArgumentException, NoSuchElementException {
        int index = Integer.parseInt(key);

        if (index < 0 || index >= collection.size()) {
            throw new NoSuchElementException();
        }

        return Iterables.get(collection, index);
    }

    @Override
    public @NotNull Collection<@NotNull String> getKeys() {
        return Collections.emptyList();
    }
}
