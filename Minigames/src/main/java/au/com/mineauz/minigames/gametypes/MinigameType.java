package au.com.mineauz.minigames.gametypes;

import org.jetbrains.annotations.NotNull;

public enum MinigameType {
    SINGLEPLAYER("Singleplayer"),
    MULTIPLAYER("Multiplayer"),
    GLOBAL("Global");

    private final @NotNull String name;

    MinigameType(final @NotNull String name) {
        this.name = name;
    }

    public static boolean hasValue(String value) {
        for (MinigameType type : values()) {
            if (type.toString().equalsIgnoreCase(value))
                return true;
        }
        return false;
    }

    public @NotNull String getName() {
        return name;
    }
}
