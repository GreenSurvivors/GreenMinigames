package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.MinigameTimer;
import au.com.mineauz.minigames.minigame.Minigame;
import org.jetbrains.annotations.NotNull;

public class MinigameTimerTickEvent extends AbstractCancellableMinigameEvent {
    private final @NotNull MinigameTimer timer;

    public MinigameTimerTickEvent(@NotNull Minigame minigame, @NotNull MinigameTimer timer) {
        super(minigame);
        this.timer = timer;
    }

    public long getTimeLeft() {
        return timer.getTimeLeft();
    }

    public void setTimeLeft(int time) {
        timer.setTimeLeft(time);
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean b) {
        throw new UnsupportedOperationException("Cannot cancel a  Minigames tick Event");
    }
}
