package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpectateMinigameEvent extends AbstractCancellableMinigameEvent {
    private final @NotNull MinigamePlayer player;

    public SpectateMinigameEvent(@NotNull MinigamePlayer player, @NotNull Minigame minigame) {
        super(minigame);
        this.player = player;
    }

    public @NotNull MinigamePlayer getMinigamePlayer() {
        return player;
    }

    public @NotNull Player getPlayer() {
        return player.getPlayer();
    }
}
