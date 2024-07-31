package au.com.mineauz.minigamesregions.events;

import au.com.mineauz.minigames.objects.MinigamePlayer;
import au.com.mineauz.minigamesregions.Region;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EnterRegionEvent extends Event {
    private static final @NotNull HandlerList handlers = new HandlerList();

    private final @NotNull MinigamePlayer player;
    private final @NotNull Region region;

    public EnterRegionEvent(@NotNull MinigamePlayer player, @NotNull Region region) {
        this.player = player;
        this.region = region;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }

    public @NotNull MinigamePlayer getMinigamePlayer() {
        return player;
    }

    public @NotNull Region getRegion() {
        return region;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

}
