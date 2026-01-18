package au.com.mineauz.minigames.display;

import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage") // shut up position
public class DisplayPoint extends AbstractDisplayObject implements INonPersistentDisplay, IDisplayObject {
    private final @NotNull Position position;
    private final boolean showDirection;
    private final float yaw;
    private final float pitch;

    public DisplayPoint(final @NotNull DisplayManager manager, final @NotNull World world,
                        final @NotNull Position position,
                        final float yaw, final float pitch, final boolean showDirection) {
        super(manager, world);
        this.position = position;
        this.showDirection = showDirection;
        this.yaw = yaw;
        this.pitch = pitch;
    }


    public DisplayPoint(final @NotNull DisplayManager manager, final @NotNull Player player,
                        final @NotNull Position position,
                        final float yaw, final float pitch, final boolean showDirection) {
        super(manager, player);
        this.position = position;
        this.showDirection = showDirection;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public void show() {
        refresh();
        super.show();
    }

    @Override
    public int getRefreshInterval() {
        return 10;
    }

    @Override
    public void refresh() {
        if (player != null && player.getWorld() != getWorld()) {
            return;
        }

        double dist = 0.25;

        placeEffect(position.x() - dist, position.y() - dist, position.z() - dist);
        placeEffect(position.x() + dist, position.y() - dist, position.z() - dist);
        placeEffect(position.x() - dist, position.y() + dist, position.z() - dist);
        placeEffect(position.x() + dist, position.y() + dist, position.z() - dist);
        placeEffect(position.x() - dist, position.y() - dist, position.z() + dist);
        placeEffect(position.x() + dist, position.y() - dist, position.z() + dist);
        placeEffect(position.x() - dist, position.y() + dist, position.z() + dist);
        placeEffect(position.x() + dist, position.y() + dist, position.z() + dist);

        if (showDirection) {
            double xz = Math.cos(Math.toRadians(pitch));

            double x = -xz * Math.sin(Math.toRadians(yaw));
            double y = -Math.sin(Math.toRadians(pitch));
            double z = xz * Math.cos(Math.toRadians(yaw));

            double length = Math.sqrt(NumberConversions.square(x) + NumberConversions.square(y) + NumberConversions.square(z));

            x /= length;
            y /= length;
            z /= length;

            for (double p = 0; p <= 1; p += 0.25) {
                Position point = position.offset(x * p, y * p, z * p);
                placeEffect(point.x(), point.y(), point.z());
            }
        }
    }

    private void placeEffect(final double x, final double y, final double z) {
        if (getWorld() != null) {
            final @NotNull Location temp = new Location(getWorld(), x, y, z);

            if (player != null) {
                player.spawnParticle(Particle.FLAME, temp, 1);
            } else {
                getWorld().spawnParticle(Particle.FLAME, temp, 1);
            }
        } else {
            remove();
        }
    }
}
