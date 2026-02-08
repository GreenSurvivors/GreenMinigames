package au.com.mineauz.minigames.display;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigames.objects.safelocation.ASafeLocation;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import io.papermc.paper.math.Position;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage") // shut up position
public class DisplayManager {
    private final @NotNull Minigames plugin;
    private final @NotNull Map<@NotNull INonPersistentDisplay, @NotNull Integer> nextTickDelay = new IdentityHashMap<>();
    private final @NotNull SetMultimap<@NotNull UUID, @NotNull AbstractDisplayObject> playerDisplays = HashMultimap.create();
    private final @NotNull SetMultimap<@NotNull World, @NotNull AbstractDisplayObject> worldDisplays = HashMultimap.create();
    private @Nullable BukkitTask refreshTask;

    public DisplayManager(final @NotNull Minigames plugin) {
        this.plugin = plugin;
    }

    public @NotNull DisplayCuboid displayCuboid(@NotNull Player player, @NotNull ASafeLocation corner1, @NotNull ASafeLocation corner2) {
        Validate.isTrue(corner1.getWorld() == corner2.getWorld(), "Both corners must be in the same world");

        double minX = Math.min(corner1.x(), corner2.x());
        double maxX = Math.max(corner1.x(), corner2.x());
        double minY = Math.min(corner1.y(), corner2.y());
        double maxY = Math.max(corner1.y(), corner2.y());
        double minZ = Math.min(corner1.z(), corner2.z());
        double maxZ = Math.max(corner1.z(), corner2.z());

        return displayCuboid(player, minX, minY, minZ, maxX, maxY, maxZ);
    }

    @ApiStatus.Obsolete
    public @NotNull DisplayCuboid displayCuboid(@NotNull Location corner1, @NotNull Location corner2) {
        Validate.isTrue(corner1.getWorld() == corner2.getWorld(), "Both corners must be in the same world");

        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());

        return displayCuboid(corner1.getWorld(), minX, minY, minZ, maxX, maxY, maxZ);
    }

    public @NotNull DisplayCuboid displayCuboid(@NotNull Player player, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new DisplayCuboid(this, player, Position.fine(minX, minY, minZ), Position.fine(maxX, maxY, maxZ));
    }

    public @NotNull DisplayCuboid displayCuboid(final @NotNull Player player, final @NotNull MgRegion region) {
        return new DisplayCuboid(this, player, region.getMin(), region.getMax().offset(1.0, 1.0, 1.0));
    }

    public @NotNull DisplayCuboid displayCuboid(@NotNull World world, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new DisplayCuboid(this, world, Position.fine(minX, minY, minZ), Position.fine(maxX, maxY, maxZ));
    }

    public @NotNull DisplayCuboid displayCuboid(@NotNull World world, @NotNull MgRegion region) {
        return new DisplayCuboid(this, world, region.getMin(), region.getMax());
    }

    @ApiStatus.Obsolete
    public @NotNull DisplayPoint displayPoint(@NotNull Player player, @NotNull Location location, boolean showDirection) {
        return displayPoint(player, location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), showDirection);
    }

    public @NotNull DisplayPoint displayPoint(@NotNull Player player, @NotNull SafeFullLocation location, boolean showDirection) {
        return displayPoint(player, location.x(), location.y(), location.z(), location.yaw(), location.pitch(), showDirection);
    }

    public @NotNull DisplayPoint displayPoint(@NotNull Location location, boolean showDirection) {
        return displayPoint(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), showDirection);
    }

    public @NotNull DisplayPoint displayPoint(@NotNull Player player, double x, double y, double z, float yaw, float pitch, boolean showDirection) {
        return new DisplayPoint(this, player, Position.fine(x, y, z), yaw, pitch, showDirection);
    }

    public @NotNull DisplayPoint displayPoint(@NotNull World world, double x, double y, double z, float yaw, float pitch, boolean showDirection) {
        return new DisplayPoint(this, world, Position.fine(x, y, z), yaw, pitch, showDirection);
    }

    public void removeAll(final @NotNull Player player) {
        for (IDisplayObject display : playerDisplays.removeAll(player.getUniqueId())) {
            display.hide();
        }
    }

    public void removeAll(@NotNull World world) {
        for (IDisplayObject display : worldDisplays.removeAll(world)) {
            display.hide();
        }
    }

    public void removeAll() {
        for (IDisplayObject display : playerDisplays.values()) {
            display.hide();
        }

        for (IDisplayObject display : worldDisplays.values()) {
            display.hide();
        }

        playerDisplays.clear();
        worldDisplays.clear();
    }

    protected void onShow(@NotNull IDisplayObject object) {
        if (object instanceof INonPersistentDisplay display) {
            nextTickDelay.put(display, display.getRefreshInterval());

            enableRefreshTask();
        }
    }

    protected void onHide(@NotNull IDisplayObject object) {
        if (object instanceof INonPersistentDisplay display) {
            nextTickDelay.remove(display);

            disableRefreshTask();
        }
    }

    protected void onRemove(final @NotNull IDisplayObject object) {
        if (object.isPlayerDisplay()) {
            playerDisplays.remove(object.getPlayer(), object);
        } else {
            worldDisplays.remove(object.getWorld(), object);
        }
    }

    private void doRefreshAll() {
        for (Entry<INonPersistentDisplay, Integer> next : nextTickDelay.entrySet()) {
            if (next.getValue() <= 0) {
                next.setValue(next.getKey().getRefreshInterval());
                next.getKey().refresh();
            } else {
                next.setValue(next.getValue() - 1);
            }
        }
    }

    private void enableRefreshTask() {
        if (refreshTask == null) {
            refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, this::doRefreshAll, 1, 1);
        }
    }

    private void disableRefreshTask() {
        if (refreshTask != null && nextTickDelay.isEmpty()) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }
}
