package au.com.mineauz.minigames.script;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@SuppressWarnings("unused") // api
public interface ScriptObject extends ScriptReference {
    @Nullable ScriptReference resolveReference(final @NotNull String name);

    Set<String> getReferenceKeys();

    String getAsString();
}
