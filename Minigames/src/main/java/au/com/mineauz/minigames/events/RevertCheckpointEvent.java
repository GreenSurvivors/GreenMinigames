package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RevertCheckpointEvent extends AbstractCancellableMinigameEvent {
    private final @NotNull MinigamePlayer player;

    public RevertCheckpointEvent(@NotNull MinigamePlayer player) {
        super(player.getMinigame());
        this.player = player;
    }

    public MinigamePlayer getMinigamePlayer() {
        return player;
    }

    public @NotNull Player getPlayer() {
        return player.getPlayer();
    }
}
