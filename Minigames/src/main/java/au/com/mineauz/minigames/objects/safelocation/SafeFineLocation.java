package au.com.mineauz.minigames.objects.safelocation;

import io.papermc.paper.math.FinePosition;
import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage") // shutup Position
@ConfigSerializable
public class SafeFineLocation extends ASafeLocation implements FinePosition {
    protected final double x;
    protected final double y;
    protected final double z;

    public SafeFineLocation(final @NotNull String worldName,
                            final double x, final double y, final double z) {
        super(worldName);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public SafeFineLocation(final @NotNull String worldName, final @NotNull Position position) {
        super(worldName);
        this.x = position.x();
        this.y = position.y();
        this.z = position.z();
    }

    public SafeFineLocation(final @NotNull Location location) throws IllegalArgumentException {
        super(location.getWorld().getName());
        if (location.getWorld() == null) { // todo move above super once we update to javaversion that supports it!
            throw new IllegalArgumentException("No valid world!");
        }
        this.x = location.x();
        this.y = location.y();
        this.z = location.z();
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
        return "SafeLocation[world:" + getWorldName() + ", x:" + x + ", y:" + y + ", z:" + z + "]";
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public double z() {
        return z;
    }

    @Override
    public @NotNull SafeBlockLocation toBlock() {
        return new SafeBlockLocation(getWorldName(), blockX(), blockY(), blockZ());
    }

    @Override
    public @NotNull SafeFineLocation offset(final double x, final double y, final double z) {
        return x == 0.0 && y == 0.0 && z == 0.0 ? this : new SafeFineLocation(getWorldName(), x() + x, y() + y, z() + z);
    }

    public @NotNull SafeFineLocation offset(final Position other) {
        return other.x() == 0.0 && other.y() == 0.0 && other.z() == 0.0 ? this : new SafeFineLocation(getWorldName(), x() + other.x(), y() + other.y(), z() + other.z());
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
    public boolean equals(final @Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final @NotNull SafeFineLocation other = (SafeFineLocation) obj;
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
