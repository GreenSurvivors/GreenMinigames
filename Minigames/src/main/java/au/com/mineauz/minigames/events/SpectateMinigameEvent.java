package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class SpectateMinigameEvent extends AbstractCancellableMinigameEvent {
    private final @NotNull MinigamePlayer mgPlayer;

    public SpectateMinigameEvent(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame) {
        super(minigame);
        this.mgPlayer = mgPlayer;
    }

    public @NotNull MinigamePlayer getMinigamePlayer() {
        return mgPlayer;
    }

    @ApiStatus.Obsolete
    public @NotNull Player getPlayer() {
        return mgPlayer.getPlayer();
    }
}
