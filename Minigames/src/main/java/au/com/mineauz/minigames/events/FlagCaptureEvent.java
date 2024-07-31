package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.CTFFlag;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.jetbrains.annotations.NotNull;

public class FlagCaptureEvent extends AbstractCancellableMinigameEvent {

    private final @NotNull MinigamePlayer player;
    private final @NotNull CTFFlag flag;
    private boolean displayMessage = true; // todo

    public FlagCaptureEvent(@NotNull Minigame minigame, @NotNull MinigamePlayer player, @NotNull CTFFlag flag) {
        super(minigame);
        this.player = player;
        this.flag = flag;
    }

    public @NotNull MinigamePlayer getPlayer() {
        return player;
    }


    public @NotNull CTFFlag getFlag() {
        return flag;
    }

    public boolean shouldDisplayMessage() {
        return displayMessage;
    }

    public void setShouldDisplayMessage(boolean arg0) {
        displayMessage = arg0;
    }
}
