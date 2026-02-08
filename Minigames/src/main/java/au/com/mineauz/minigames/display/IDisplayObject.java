package au.com.mineauz.minigames.display;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IDisplayObject {

    /**
     * True if player display.
     *
     * @return boolean
     */
    boolean isPlayerDisplay();

    /**
     * Get the player.
     *
     * @return the player
     */
    @Nullable Player getPlayer();

    /**
     * Get the players uuid.
     *
     * @return the players uuid
     */
    @Nullable UUID getPayerUUID();

    /**
     * @return the world
     */
    World getWorld();

    /**
     * Show the Display.
     */
    void show();

    /**
     * Hide the display.
     */
    void hide();

    /**
     * remove the display.
     */
    void remove();
}
