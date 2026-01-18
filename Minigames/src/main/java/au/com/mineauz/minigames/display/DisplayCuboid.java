package au.com.mineauz.minigames.display;

import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage") // shut up Position
public class DisplayCuboid extends AbstractDisplayObject implements INonPersistentDisplay, IDisplayObject {
    private final @NotNull Position minCorner;
    private final @NotNull Position maxCorner;

    private int lastBarrier = 41;

    public DisplayCuboid(final @NotNull DisplayManager manager, @NotNull World world, @NotNull Position minCorner, @NotNull Position maxCorner) {
        super(manager, world);
        this.minCorner = minCorner;
        this.maxCorner = maxCorner;
    }

    public DisplayCuboid(final @NotNull DisplayManager manager, final @NotNull Player player,
                         final @NotNull Position minCorner, final @NotNull Position maxCorner) {
        super(manager, player);
        this.minCorner = minCorner;
        this.maxCorner = maxCorner;
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
        // Don't display effect if they cant see it
        if (player != null && player.getWorld() != getWorld()) {
            return;
        }
        double step = 0.5;

        // X axis
        for (double x = minCorner.x(); x <= maxCorner.x(); x += step) {
            placeEffectAt(x, minCorner.y(), minCorner.z());
            placeEffectAt(x, maxCorner.y(), minCorner.z());
            placeEffectAt(x, minCorner.y(), maxCorner.z());
            placeEffectAt(x, maxCorner.y(), maxCorner.z());
        }

        // Y axis
        for (double y = minCorner.y(); y <= maxCorner.y(); y += step) {
            placeEffectAt(minCorner.x(), y, minCorner.z());
            placeEffectAt(maxCorner.x(), y, minCorner.z());
            placeEffectAt(minCorner.x(), y, maxCorner.z());
            placeEffectAt(maxCorner.x(), y, maxCorner.z());
        }

        // Z axis
        for (double z = minCorner.z(); z <= maxCorner.z(); z += step) {
            placeEffectAt(minCorner.x(), minCorner.y(), z);
            placeEffectAt(maxCorner.x(), minCorner.y(), z);
            placeEffectAt(minCorner.x(), maxCorner.y(), z);
            placeEffectAt(maxCorner.x(), maxCorner.y(), z);
        }
    }

    private void placeEffectAt(final double x, final double y, final double z) {
        if (getWorld() != null) {
            lastBarrier++;
            if (lastBarrier < 41) {
                return;
            }
            lastBarrier = 0;
            final @NotNull Location temp = new Location(getWorld(), x, y, z);

            if (player == null) {
                getWorld().spawnParticle(Particle.BLOCK_MARKER, temp, 1, BlockType.BARRIER.createBlockData());
            } else {
                player.spawnParticle(Particle.BLOCK_MARKER, temp, 1, BlockType.BARRIER.createBlockData());
            }
        } else {
            remove();
        }
    }
}
