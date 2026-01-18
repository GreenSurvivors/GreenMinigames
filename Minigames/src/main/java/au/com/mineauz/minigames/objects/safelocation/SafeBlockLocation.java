package au.com.mineauz.minigames.objects.safelocation;

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage") // shutup Position
@ConfigSerializable
public class SafeBlockLocation extends ASafeLocation implements BlockPosition {
    protected final int x;
    protected final int y;
    protected final int z;

    public SafeBlockLocation(final @NotNull String worldName, final int x, final int y, final int z) {
        super(worldName);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SafeBlockLocation(final @NotNull String worldName, final @NotNull Position position) {
        this(worldName, position.blockX(), position.blockY(), position.blockZ());
    }

    public SafeBlockLocation(final @NotNull Location location) throws IllegalArgumentException {
        super(location.getWorld().getName());
        if (location.getWorld() == null) { // todo move above super once we can update to a java version that supports it!
            throw new IllegalArgumentException("No valid world!");
        }
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    /// Note: The returned Location may have no (null) World!!
    @Override
    public @NotNull Location toLocation() {
        return new Location(
            getWorld(),
            this.x,
            this.y,
            this.z
        );
    }

    @Override
    public @NotNull String toString() {
        return "SafeBlockLocation[world:" + getWorldName() + ", x:" + x + ", y:" + y + ", z:" + z + "]";
    }

    @Override
    public int blockX() {
        return x;
    }

    @Override
    public int blockY() {
        return y;
    }

    @Override
    public int blockZ() {
        return z;
    }

    @Override
    public double distanceSquared(final @NotNull Location other) {
        if (other.getWorld() == null) {
            throw new IllegalArgumentException("Cannot measure distance to a null world");
        } else if (!other.getWorld().getName().equals(getWorldName())) {
            throw new IllegalArgumentException("Cannot measure distance between " + getWorldName() + " and " + other.getWorld().getName());
        }

        return NumberConversions.square(x - other.getX()) +
            NumberConversions.square(y - other.getY()) +
            NumberConversions.square(z - other.getZ());
    }

    @Override
    public @NotNull SafeBlockLocation toBlock() {
        return this;
    }

    @Override
    public @NotNull SafeBlockLocation offset(final int x, final int y, final int z) {
        return x == 0 && y == 0 && z == 0 ? this : new SafeBlockLocation(this.getWorldName(), this.blockX() + x, this.blockY() + y, this.blockZ() + z);
    }

    @Override
    public @NotNull SafeBlockLocation offset(final @NotNull BlockFace blockFace, final int amount) {
        return amount == 0 || blockFace == BlockFace.SELF ? this : new SafeBlockLocation(getWorldName(), blockX() + (blockFace.getModX() * amount), blockY() + (blockFace.getModY() * amount), blockZ() + (blockFace.getModZ() * amount));
    }

    @Override
    public @NotNull SafeBlockLocation offset(final @NotNull Axis axis, final int amount) {
        return amount == 0 ? this : switch (axis) {
            case X -> new SafeBlockLocation(getWorldName(), blockX() + amount, blockY(), blockZ());
            case Y -> new SafeBlockLocation(getWorldName(), blockX(), blockY() + amount, blockZ());
            case Z -> new SafeBlockLocation(getWorldName(), blockX(), blockY(), blockZ() + amount);
        };
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final @NotNull SafeBlockLocation other = (SafeBlockLocation) obj;
        return getWorldName().equals(other.getWorldName()) &&
            this.x == other.x &&
            this.y == other.y &&
            this.z == other.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWorldName(), x, y, z);
    }
}
