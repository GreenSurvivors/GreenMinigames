package au.com.mineauz.minigames.display;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * The type Abstract display object.
 */
public abstract class AbstractDisplayObject implements IDisplayObject {
    private final @NotNull DisplayManager manager;
    private final @NotNull WeakReference<World> worldReference;
    protected final @Nullable UUID playerUUID;

    /**
     * Instantiates a new Abstract display object.
     *
     * @param manager the manager
     * @param world   the world
     */
    public AbstractDisplayObject(final @NotNull DisplayManager manager, final @NotNull World world) {
        this.manager = manager;
        this.worldReference = new WeakReference<>(world);
        this.playerUUID = null;
    }

    /**
     * Instantiates a new Abstract display object.
     *
     * @param manager the manager
     * @param player  the player
     */
    public AbstractDisplayObject(final @NotNull DisplayManager manager, final @NotNull Player player) {
        this.manager = manager;
        this.worldReference = new WeakReference<>(player.getWorld());
        this.playerUUID = player.getUniqueId();
    }

    @Override
    public boolean isPlayerDisplay() {
        return playerUUID != null;
    }

    @Override
    public @Nullable Player getPlayer() {
        return playerUUID == null ? null : Bukkit.getPlayer(playerUUID);
    }

    @Override
    public @Nullable UUID getPayerUUID() {
        return playerUUID;
    }

    @Override
    public @Nullable World getWorld() {
        return worldReference.get();
    }

    @Override
    public void show() {
        manager.onShow(this);
    }

    @Override
    public void hide() {
        manager.onHide(this);
    }

    @Override
    public void remove() {
        hide();
        manager.onRemove(this);
    }
}
