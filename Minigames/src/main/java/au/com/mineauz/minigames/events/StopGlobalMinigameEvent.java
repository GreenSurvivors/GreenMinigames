package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public class StopGlobalMinigameEvent extends AbstractCancellableMinigameEvent {
    private final @NotNull Audience caller;

    public StopGlobalMinigameEvent(final @NotNull Minigame mgm, final @NotNull Audience caller) {
        super(mgm);
        this.caller = caller;
    }

    public @NotNull Audience getCaller() {
        return caller;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {
        throw new UnsupportedOperationException("Cannot cancel a  Minigames Broadcast Event");
    }
}
