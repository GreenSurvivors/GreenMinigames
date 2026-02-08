package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.CTFFlag;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TakeCTFFlagEvent extends AbstractCancellableMinigameEvent {
    private final @Nullable CTFFlag flag;
    private final @NotNull MinigamePlayer player;
    private boolean displayMessage = true;

    public TakeCTFFlagEvent(final @NotNull Minigame minigame, final @NotNull MinigamePlayer player, final @Nullable CTFFlag flag) {
        super(minigame);
        this.flag = flag;
        this.player = player;
    }

    public @Nullable CTFFlag getFlag() {
        return flag;
    }

    public boolean shouldDisplayMessage() {
        return displayMessage;
    }

    public void setShouldDisplayMessage(final boolean shouldDisplayMessage) {
        displayMessage = shouldDisplayMessage;
    }

    public @NotNull MinigamePlayer getPlayer() {
        return player;
    }
}
