package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import org.jetbrains.annotations.NotNull;

/**
 * this event indicates the minigame has definitely ended now and is in the process of clearing up
 */
public class EndedMinigameEvent extends AbstractMinigameEvent {
    public EndedMinigameEvent(@NotNull Minigame minigame) {
        super(minigame);
    }
}