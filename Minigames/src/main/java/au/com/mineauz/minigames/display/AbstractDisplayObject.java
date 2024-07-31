package au.com.mineauz.minigames.display;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The type Abstract display object.
 */
public abstract class AbstractDisplayObject implements IDisplayObject {
    private final @NotNull World world;
    private final @NotNull DisplayManager manager;
    protected @Nullable Player player;

    /**
     * Instantiates a new Abstract display object.
     *
     * @param manager the manager
     * @param world the world
     */
    public AbstractDisplayObject(final @NotNull DisplayManager manager, final @NotNull World world) {
        this.manager = manager;
        this.world = world;
    }

    /**
     * Instantiates a new Abstract display object.
     *
     * @param manager the manager
     * @param player the player
     */
    public AbstractDisplayObject(final @NotNull DisplayManager manager, final @NotNull Player player) {
        this.manager = manager;
        this.world = player.getWorld();
        this.player = player;
    }

    /**
     * True if player display.
     *
     * @return boolean
     */
    @Override
    public boolean isPlayerDisplay() {
        return player != null;
    }

    /**
     * Get the player.
     *
     * @return the player
     */
    @Override
    public @Nullable Player getPlayer() {
        return player;
    }

    /**
     * @return the world
     */
    @Override
    public @NotNull World getWorld() {
        return world;
    }

    /**
     * Show the Display.
     */
    @Override
    public void show() {
        manager.onShow(this);
    }

    /**
     * Hide the display.
     */
    @Override
    public void hide() {
        manager.onHide(this);
    }

    /**
     * remove the display.
     */
    @Override
    public void remove() {
        hide();
        manager.onRemove(this);
    }
}
