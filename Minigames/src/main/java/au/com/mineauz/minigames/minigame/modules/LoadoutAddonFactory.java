package au.com.mineauz.minigames.minigame.modules;

import org.jetbrains.annotations.NotNull;

public interface LoadoutAddonFactory {
    @NotNull String getAddonName();

    @NotNull ALoadoutAddon makeNewLoadoutAddon();
}
