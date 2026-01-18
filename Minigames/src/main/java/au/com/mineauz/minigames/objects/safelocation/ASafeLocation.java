package au.com.mineauz.minigames.objects.safelocation;

import io.papermc.paper.math.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * Bukkits Location has multible downfalls:
 * 1.) it is mutable, witch leads to programming mistakes more often then not
 * 2.) more important the world is stored as a WeakReference leading to unexpected null values and exceptions
 * 3.) It's not compatible with configurate
 */
public abstract class ASafeLocation implements Position {
    @Setting("world")
    private final @NotNull String worldName;
    protected transient @NotNull Reference<@Nullable World> worldReference = new WeakReference<>(null);

    protected ASafeLocation(final @NotNull String worldName) {
        this.worldName = worldName;
    }

    public @NotNull String getWorldName() {
        return worldName;
    }

    public @Nullable World getWorld() {
        final @Nullable World referencedWorld = worldReference.get();

        if (referencedWorld != null) {
            return referencedWorld;
        }

        final @Nullable World fetchedWorld = Bukkit.getWorld(getWorldName());
        worldReference = new WeakReference<>(fetchedWorld);

        return fetchedWorld;
    }

    public abstract @NotNull Location toLocation();

    public abstract double distanceSquared(final @NotNull Location other);

    public boolean isSameBlock(final @NotNull Location other) {
        final @Nullable String otherWorld = (other.getWorld() == null) ? null : other.getWorld().getName();
        return getWorldName().equals(otherWorld) &&
            this.blockX() == other.getBlockX() &&
            this.blockY() == other.getBlockY() &&
            this.blockZ() == other.getBlockZ();
    }

    public boolean isSameBlock(final @NotNull Block other) {
        return getWorldName().equals(other.getWorld().getName()) &&
            this.blockX() == other.getX() &&
            this.blockY() == other.getY() &&
            this.blockZ() == other.getZ();
    }

    @Override
    public abstract @NotNull SafeBlockLocation toBlock(); // note: this is a pretty bad name isn't it, considering it returns a BlockPosition, not a block

    public @Nullable Block getBlockAt() {
        final @Nullable World world = getWorld();

        if (world != null) {
            return world.getBlockAt(blockX(), blockY(), blockZ());
        } else {
            return null;
        }
    }
}
