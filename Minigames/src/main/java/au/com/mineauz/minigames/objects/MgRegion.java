package au.com.mineauz.minigames.objects;

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This is the base class for all regions in the minigames plugin and its regions & nodes addon.
 * It is a cuboid region defined by 2 Positions, a World and a name
 */
@SuppressWarnings("UnstableApiUsage") // Position
public class MgRegion implements ConfigurationSerializable {
    private final @NotNull String name;
    private @NotNull World world;
    private @NotNull BlockPosition pos1;
    private @NotNull BlockPosition pos2;

    public MgRegion(@NotNull World world, @NotNull String name, @NotNull Position pos1, @NotNull Position pos2) {
        this.name = name;
        this.world = world;
        this.pos1 = pos1.toBlock();
        this.pos2 = pos2.toBlock();
    }

    public MgRegion(@NotNull String name, @NotNull Location loc1, @NotNull Location loc2) {
        this.name = name;
        this.world = loc1.getWorld();
        this.pos1 = Position.block(loc1);
        this.pos2 = Position.block(loc2);
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull World getWorld() {
        return world;
    }

    public void setWorld(@NotNull World world) {
        this.world = world;
    }

    public void setFirstPos(@NotNull Position pos1) {
        this.pos1 = pos1.toBlock();
    }

    public void setFirstPos(@NotNull Location loc1) {
        this.pos1 = Position.block(loc1);
        this.world = loc1.getWorld();
    }

    public void setSecondPos(@NotNull Position pos2) {
        this.pos2 = pos2.toBlock();
    }

    public void setSecondPos(@NotNull Location loc2) {
        this.pos2 = Position.block(loc2);
        this.world = loc2.getWorld();
    }

    public @NotNull Position getPos1() {
        return pos1;
    }

    public @NotNull Position getPos2() {
        return pos2;
    }

    public @NotNull Location getLocation1() {
        return pos1.toLocation(world);
    }

    public @NotNull Location getLocation2() {
        return pos2.toLocation(world);
    }

    public void updateRegion(Location loc1, Location loc2) {
        this.world = loc1.getWorld();

        this.pos1 = Position.block(loc1);
        this.pos2 = Position.block(loc2);
    }

    /**
     * sorts the 2 positions making up this region: pos1 will have all the smaller coordinates,
     * while pos2 will hold all the bigger coordinates
     */
    public void sortPositions() {
        //temporary storage to not overwrite the max values
        Position pos1 = Position.fine(getMinX(), getMinY(), getMinZ());

        this.pos2 = Position.fine(getMaxX(), getMaxY(), getMaxZ()).toBlock();
        this.pos1 = pos1.toBlock();
    }

    public double getMinX() {
        return Math.min(pos1.x(), pos2.x());
    }

    public double getMaxX() {
        return Math.max(pos1.x(), pos2.x());
    }

    public double getMinY() {
        return Math.min(pos1.y(), pos2.y());
    }

    public double getMaxY() {
        return Math.max(pos1.y(), pos2.y());
    }

    public double getMinZ() {
        return Math.min(pos1.z(), pos2.z());
    }

    public double getMaxZ() {
        return Math.max(pos1.z(), pos2.z());
    }

    public boolean isInRegen(Location location) {
        return location.getWorld().getUID() == world.getUID() &&
                location.getBlockX() >= getMinX() && location.getBlockX() <= getMaxX() &&
                location.getBlockY() >= getMinY() && location.getBlockY() <= getMaxY() &&
                location.getBlockZ() >= getMinZ() && location.getBlockZ() <= getMaxZ();
    }

    public double getBaseArea() {
        return (1 + Math.abs(pos1.x() - pos2.x())) * (1 + Math.abs(pos1.z() - pos2.z()));
    }

    public double getVolume() {
        return getBaseArea() * (1 + Math.abs(pos1.y() - pos2.y()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MgRegion) obj;

        return this.name.equals(that.name) &&
                Objects.equals(this.world, that.world) &&
                Objects.equals(this.pos1, that.pos1) &&
                Objects.equals(this.pos2, that.pos2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, pos1, pos2);
    }

    @Override
    public String toString() {
        return "MgRegion[" +
                "name=" + name + ", " +
                "world=" + world + ", " +
                "pos1=" + pos1 + ", " +
                "pos2=" + pos2 + ']';
    }

    /**
     * Creates a Map representation of Position.
     *
     * @return Map containing the current state of this class
     */
    private static @NotNull Map<String, Object> serializePosition(@NotNull Position pos) {
        HashMap<String, Object> result = new HashMap<>();

        result.put("x", pos.x());
        result.put("y", pos.y());
        result.put("z", pos.z());

        return result;
    }

    /**
     * Creates a Map representation of this class.
     *
     * @return Map containing the current state of this class
     */
    @Override
    public @NotNull Map<String, Object> serialize() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("name", name);
        result.put("world", world.getName());
        result.put("pos1", serializePosition(pos1));
        result.put("pos2", serializePosition(pos2));

        return result;
    }

    /**
     * tries to recreate a Position from a map representation
     *
     * @return the fitting position or null if it fails.
     */
    private static @Nullable Position deserializePosition(@Nullable Map<String, Object> map) {
        if (map != null) {
            Double x = null, y = null, z = null;

            if (map.get("x") instanceof Number numX) {
                x = numX.doubleValue();
            }
            if (map.get("y") instanceof Number numY) {
                y = numY.doubleValue();
            }
            if (map.get("z") instanceof Number numZ) {
                z = numZ.doubleValue();
            }

            if (x != null && y != null && z != null) {
                return Position.fine(x, y, z);
            }
        }

        return null;
    }

    /**
     * tries to recreate a MgRegion from a map representation
     *
     * @return the fitting MgRegion or null if it fails.
     * @see ConfigurationSerializable
     */
    @NotNull
    public static MgRegion deserialize(@NotNull Map<String, Object> map) {
        String name;
        World world;
        Position pos1, pos2;

        if (map.get("name") instanceof String temp) {
            name = temp;
        } else {
            throw new IllegalArgumentException("no name");
        }

        if (map.get("world") instanceof String worldName) {
            world = Bukkit.getWorld(worldName);

            if (world == null) {
                throw new IllegalArgumentException("unknown world");
            }
        } else {
            throw new IllegalArgumentException("no world");
        }

        if (map.get("pos1") instanceof Map<?, ?> posObjMap) {
            HashMap<String, Object> posStrMap = new HashMap<>();

            for (Map.Entry<?, ?> objEntry : posObjMap.entrySet()) {
                if (objEntry.getKey() instanceof String key) {
                    posStrMap.put(key, objEntry.getValue());
                }
            }

            pos1 = deserializePosition(posStrMap);

            if (pos1 == null) {
                throw new IllegalArgumentException("broken position 1");
            }
        } else {
            throw new IllegalArgumentException("no position 1");
        }

        if (map.get("pos2") instanceof Map<?, ?> posObjMap2) {
            HashMap<String, Object> posStrMap = new HashMap<>();

            for (Map.Entry<?, ?> objEntry : posObjMap2.entrySet()) {
                if (objEntry.getKey() instanceof String key) {
                    posStrMap.put(key, objEntry.getValue());
                }
            }

            pos2 = deserializePosition(posStrMap);

            if (pos2 == null) {
                throw new IllegalArgumentException("broken position 2");
            }
        } else {
            throw new IllegalArgumentException("no position 2");
        }

        return new MgRegion(world, name, pos1, pos2);
    }
}
