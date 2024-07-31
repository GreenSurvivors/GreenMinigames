package au.com.mineauz.minigames.script;

import org.jetbrains.annotations.NotNull;

public record ScriptValue<T>(T value) implements ScriptReference {

    public static @NotNull <T> ScriptValue<T> of(T value) {
        return new ScriptValue<>(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
