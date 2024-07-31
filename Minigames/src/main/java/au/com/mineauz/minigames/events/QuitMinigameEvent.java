package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class QuitMinigameEvent extends AbstractCancellableMinigameEvent {
    private final @NotNull MinigamePlayer player;
    private final boolean isForced;
    private final boolean isWinner;

    public QuitMinigameEvent(@NotNull MinigamePlayer player, @NotNull Minigame minigame, boolean forced, boolean isWinner) {
        super(minigame);
        this.player = player;
        isForced = forced;
        this.isWinner = isWinner;
    }

    public @NotNull MinigamePlayer getMinigamePlayer() {
        return player;
    }

    public @NotNull Player getPlayer() {
        return player.getPlayer();
    }


    public boolean isForced() {
        return isForced;
    }

    public boolean isWinner() {
        return isWinner;
    }
}
