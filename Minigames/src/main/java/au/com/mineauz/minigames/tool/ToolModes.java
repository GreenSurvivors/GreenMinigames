package au.com.mineauz.minigames.tool;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolModes {
    private static final @NotNull Map<@NotNull String, @NotNull ToolMode> modes = new HashMap<>();

    static {
        addToolMode(new StartLocationMode());
        addToolMode(new SpectatorLocationMode());
        addToolMode(new QuitLocationMode());
        addToolMode(new EndLocationMode());
        addToolMode(new LobbyLocationMode());
        addToolMode(new RegenAreaMode());
        addToolMode(new DegenAreaMode());
    }

    public static void addToolMode(@NotNull ToolMode mode) {
        if (modes.containsKey(mode.getName().toUpperCase()))
            throw new InvalidToolModeException("A tool mode already exists by this name!");
        else
            modes.put(mode.getName().toUpperCase(), mode);
    }

    public static @NotNull List<@NotNull ToolMode> getToolModes() {
        return new ArrayList<>(modes.values());
    }

    public static void removeToolMode(@NotNull String name) {
        modes.remove(name.toUpperCase());
    }

    public static @Nullable ToolMode getToolMode(@NotNull String name) {
        if (modes.containsKey(name.toUpperCase()))
            return modes.get(name.toUpperCase());
        return null;
    }
}
