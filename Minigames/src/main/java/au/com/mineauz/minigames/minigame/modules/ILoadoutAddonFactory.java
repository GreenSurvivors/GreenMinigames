package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.PlayerLoadout;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface ILoadoutAddonFactory {
    @NotNull String getAddonName();

    @NotNull ALoadoutAddon makeNewLoadoutAddon(final @NotNull PlayerLoadout loadout);

    @NotNull Key getKey();
}
