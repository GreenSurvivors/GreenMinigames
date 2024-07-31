package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JoinMinigameEvent extends AbstractCancellableMinigameEvent {

    private final @NotNull MinigamePlayer player;
    private final boolean betting;

    public JoinMinigameEvent(@NotNull MinigamePlayer player, @NotNull Minigame minigame) {
        this(player, minigame, false);
    }

    public JoinMinigameEvent(@NotNull MinigamePlayer player, @NotNull Minigame minigame, boolean betting) {
        super(minigame);
        this.player = player;
        this.betting = betting;
    }

    public @NotNull MinigamePlayer getMinigamePlayer() {
        return player;
    }

    public @NotNull Player getPlayer() {
        return player.getPlayer();
    }

    public boolean isBetting() {
        return betting;
    }
}
