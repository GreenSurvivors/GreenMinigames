package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.CTFFlag;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TakeCTFFlagEvent extends AbstractCancellableMinigameEvent {
    private final @Nullable CTFFlag flag;
    private final @NotNull MinigamePlayer player;
    //private boolean displayMessage = true; //unused; todo

    public TakeCTFFlagEvent(@NotNull Minigame minigame, @NotNull MinigamePlayer player, @Nullable CTFFlag flag) {
        super(minigame);
        this.flag = flag;
        this.player = player;
    }

    public boolean isCTFFlag() {
        return flag != null;
    }

    public @Nullable CTFFlag getFlag() {
        return flag;
    }

    /*public boolean shouldDisplayMessage() {
        return displayMessage;
    }*/

    /*public void setShouldDisplayMessage(boolean arg0) {
        displayMessage = arg0;
    }*/

    public @NotNull MinigamePlayer getPlayer() {
        return player;
    }
}
