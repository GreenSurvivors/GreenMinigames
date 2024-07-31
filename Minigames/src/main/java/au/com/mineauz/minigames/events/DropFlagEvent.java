package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.CTFFlag;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.jetbrains.annotations.NotNull;

public class DropFlagEvent extends AbstractCancellableMinigameEvent {
    private final @NotNull CTFFlag flag;
    private final @NotNull MinigamePlayer player;
    private boolean displayMessage = true;// todo

    public DropFlagEvent(@NotNull Minigame mgm, @NotNull CTFFlag flag, @NotNull MinigamePlayer player) {
        super(mgm);
        this.flag = flag;
        this.player = player;
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
