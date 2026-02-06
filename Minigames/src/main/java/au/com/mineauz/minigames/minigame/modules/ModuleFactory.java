package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface ModuleFactory {
    @NotNull AMinigameModule makeNewModule(final @NotNull Minigame minigame);

    @NotNull Key getKey();
}
