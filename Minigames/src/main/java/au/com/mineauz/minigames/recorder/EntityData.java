package au.com.mineauz.minigames.recorder;

import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * This class encodes the data of an entity had.
 * Therefore, it stores the data in case the entity dies or gets changed doing a minigame
 */
public class EntityData {
    //uuid of the entity
    private final @NotNull UUID uuid;
    //type of entity
    private final @NotNull EntityType entType;
    // data (not including location)
    private final @Nullable EntitySnapshot snapshot;
    //location the entity had when it was changed
    private final @NotNull Location entLocation;
    // the player who has changed the entity. If null, the entity doesn't get reset if the player left the minigame
    private final @Nullable MinigamePlayer player;
    // was this entity created and needs to removed or was it changed / killed?
    private final @NotNull ChangeType changeType;

    /**
     * @param entity   the entity to store data of
     * @param modifier the player who was the first one to change this entity,
     *                 might be null in case there was no player, or the minigame will be reset at the end,
     *                 regardless if players are joining / leaving it
     * @param changeType  if the entity was created and therefor has to be killed to reset the minigame
     *                 or if it was changed / killed
     */
    public EntityData(@NotNull Entity entity, @Nullable MinigamePlayer modifier, @NotNull ChangeType changeType) {
        this.uuid = entity.getUniqueId();
        this.snapshot = entity.createSnapshot();
        this.entType = entity.getType();
        this.entLocation = entity.getLocation().clone();
        this.player = modifier;
        this.changeType = changeType;
    }

    /**
     * get the entity on the server,
     * might be null if the entity was killed
     */
    public @Nullable Entity getEntity() {
        return Bukkit.getEntity(uuid);
    }

    /**
     * get the player who first changed the entity,
     * might be null if it wasn't a player,
     * or the minigame will be reset at the end,
     * regardless if players are joining / leaving it
     */
    public @Nullable MinigamePlayer getModifier() {
        return player;
    }

    public @NotNull ChangeType getChangeType() {
        return changeType;
    }

    /**
     * get the EntityType the entity had when it was recorded
     */
    public EntityType getEntityType() {
        return entType;
    }

    public @Nullable EntitySnapshot getSnapshot() {
        return snapshot;
    }

    /**
     * get the location the entity had when it was recorded
     */
    public Location getEntityLocation() {
        return entLocation;
    }

    public enum ChangeType {
        CREATED,
        REMOVED,
        CHANGED // todo record changed entities
    }
}
