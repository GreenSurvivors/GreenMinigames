package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StartGlobalMinigameEvent extends AbstractCancellableMinigameEvent {
    private final @Nullable MinigamePlayer caller;

    public StartGlobalMinigameEvent(final @NotNull Minigame mgm, final @Nullable MinigamePlayer caller) {
        super(mgm);
        this.caller = caller;
    }

    public @Nullable MinigamePlayer getCaller() {
        return caller;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {
        throw new UnsupportedOperationException("Cannot cancel a  Global Minigame Star Event");
    }
}
