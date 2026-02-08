package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class QuitMinigameEvent extends AbstractCancellableMinigameEvent {
    private final @NotNull MinigamePlayer mgPlayer;
    private final boolean isForced;
    private final boolean isWinner;

    public QuitMinigameEvent(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, boolean forced, boolean isWinner) {
        super(minigame);
        this.mgPlayer = mgPlayer;
        this.isForced = forced;
        this.isWinner = isWinner;
    }

    public @NotNull MinigamePlayer getMinigamePlayer() {
        return mgPlayer;
    }

    @ApiStatus.Obsolete
    public Player getPlayer() {
        return mgPlayer.getPlayer();
    }

    public boolean isForced() {
        return isForced;
    }

    public boolean isWinner() {
        return isWinner;
    }
}
