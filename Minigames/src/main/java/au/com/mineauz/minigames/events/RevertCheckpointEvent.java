package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class RevertCheckpointEvent extends AbstractCancellableMinigameEvent {
    private final @NotNull MinigamePlayer mgPlayer;

    public RevertCheckpointEvent(final @NotNull MinigamePlayer mgPlayer) {
        super(mgPlayer.getMinigame());
        this.mgPlayer = mgPlayer;
    }

    public @NotNull MinigamePlayer getMinigamePlayer() {
        return mgPlayer;
    }

    @ApiStatus.Obsolete
    public Player getPlayer() {
        return mgPlayer.getPlayer();
    }
}
