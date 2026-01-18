package au.com.mineauz.minigames.objects.safelocation;

import io.papermc.paper.math.FinePosition;
import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage") // shutup Position
@ConfigSerializable
public class SafeFullLocation extends SafeFineLocation implements FinePosition {
    private final float yaw;
    private final float pitch;

    public SafeFullLocation(final @NotNull String worldName,
                            final double x, final double y, final double z,
                            final float yaw, final float pitch) {
        super(worldName, x, y, z);
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public SafeFullLocation(final @NotNull Location location) throws IllegalArgumentException {
        super(location);
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    /// Note: The returned Location may have no (null) World!!
    @Override
    public @NotNull Location toLocation() {
        return new Location(
            getWorld(),
            this.x,
            this.y,
            this.z,
            this.yaw,
            this.pitch
        );
    }

    @Override
    public @NotNull String toString() {
        return "SafeLocation[world:" + getWorldName() + ", x:" + x + ", y:" + y + ", z:" + z + ", yaw:" + yaw + ", pitch:" + pitch + "]";
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    @Override
    public @NotNull SafeFullLocation offset(final double x, final double y, final double z) {
        return x == 0.0 && y == 0.0 && z == 0.0 ? this : new SafeFullLocation(getWorldName(), x() + x, y() + y, z() + z, yaw(), pitch());
    }

    public @NotNull SafeFullLocation offset(final Position other) {
        return other.x() == 0.0 && other.y() == 0.0 && other.z() == 0.0 ? this : new SafeFullLocation(getWorldName(), x() + other.x(), y() + other.y(), z() + other.z(), yaw(), pitch());
    }

    public @NotNull SafeFullLocation plusRotation(final float yaw, final float pitch) {
        return yaw == 0.0f && pitch == 0.0f ? this : new SafeFullLocation(getWorldName(), x() + x, y() + y, z() + z, yaw() + yaw, pitch() + pitch);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final @NotNull SafeFullLocation other = (SafeFullLocation) obj;
        return getWorldName().equals(other.getWorldName()) &&
            this.x == other.x &&
            this.y == other.y &&
            this.z == other.z &&
            this.yaw == other.yaw &&
            this.pitch == other.pitch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWorldName(), x, y, z, yaw, pitch);
    }
}
