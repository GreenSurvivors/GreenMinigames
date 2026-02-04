package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public abstract class AMechanicProvidingModule extends AMinigameModule {
    protected AMechanicProvidingModule(@NotNull Minigame mgm, @NotNull Key moduleKey) {
        super(mgm, moduleKey);
    }

    public abstract boolean displayMechanicSettings(final @NotNull Menu previous);
}
