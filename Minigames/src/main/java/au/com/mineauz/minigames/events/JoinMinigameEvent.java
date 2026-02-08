package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class JoinMinigameEvent extends AbstractCancellableMinigameEvent {
    private final @NotNull MinigamePlayer mgPlayer;
    private final boolean betting;

    public JoinMinigameEvent(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame) {
        this(mgPlayer, minigame, false);
    }

    public JoinMinigameEvent(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, boolean betting) {
        super(minigame);
        this.mgPlayer = mgPlayer;
        this.betting = betting;
    }

    public @NotNull MinigamePlayer getMinigamePlayer() {
        return mgPlayer;
    }

    @ApiStatus.Obsolete
    public Player getPlayer() {
        return mgPlayer.getPlayer();
    }

    public boolean isBetting() {
        return betting;
    }
}
