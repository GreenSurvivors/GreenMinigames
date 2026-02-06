package au.com.mineauz.minigames.minigame.modules.loadout;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface ILoadoutAddonFactory {
    @NotNull String getAddonName();

    @NotNull ALoadoutAddon makeNewLoadoutAddon(final @NotNull PlayerLoadout loadout);

    @NotNull Key getKey();
}
