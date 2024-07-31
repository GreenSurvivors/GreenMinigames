package au.com.mineauz.minigames.script;

import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ScriptObject extends ScriptReference {
    @Nullable ScriptReference get(String name);

    Set<String> getKeys();

    String getAsString();
}
