package au.com.mineauz.minigames.events;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class StartMinigameEvent extends AbstractMinigameEvent {
    private final boolean willTeleport;
    private final @NotNull List<@NotNull MinigamePlayer> players;

    public StartMinigameEvent(@NotNull List<MinigamePlayer> players, @NotNull Minigame minigame, boolean willTeleport) {
        super(minigame);
        this.willTeleport = willTeleport;
        this.players = players;
    }

    public boolean getWillTeleport() {
        return willTeleport;
    }

    public @NotNull List<@NotNull MinigamePlayer> getPlayers() {
        return new ArrayList<>(players);
    }
}
