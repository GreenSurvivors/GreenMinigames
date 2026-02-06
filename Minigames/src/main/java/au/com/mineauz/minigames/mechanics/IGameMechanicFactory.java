package au.com.mineauz.minigames.mechanics;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface IGameMechanicFactory {
    @NotNull AGameMechanic makeNewMechanic(final @NotNull Minigames plugin, final @NotNull Minigame minigame);

    @NotNull Key getKey();
}
