package au.com.mineauz.minigames.objects;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.objects.safelocation.ASafeLocation;
import au.com.mineauz.minigames.objects.safelocation.SafeFineLocation;
import io.papermc.paper.math.FinePosition;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.NodeKey;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * This is the base class for all regions in the minigames plugin and its regions & nodes addon.
 * It is a cuboid region defined by 2 Positions, a World and a name
 */
@ConfigSerializable
@SuppressWarnings("UnstableApiUsage") // Position
public class MgRegion {
    private transient @NotNull WeakReference<@NotNull World> worldReference = new WeakReference<>(null);
    private @NotNull FinePosition pos1;
    private @NotNull FinePosition pos2;
    @NodeKey
    private final @NotNull String name;
    @Setting("world")
    private @NotNull String worldName;

    public MgRegion(@NotNull String worldName, @NotNull String name, @NotNull FinePosition pos1, @NotNull FinePosition pos2) {
        this.name = name;
        this.worldName = worldName;
        this.pos1 = pos1;
        this.pos2 = pos2;
    }

    public MgRegion(@NotNull String name, @NotNull SafeFineLocation loc1, @NotNull SafeFineLocation loc2) {
        this.name = name;
        this.worldName = loc1.getWorldName();
        this.pos1 = loc1;
        this.pos2 = loc2;
    }

    public MgRegion(@NotNull String name, @NotNull Location loc1, @NotNull Location loc2) {
        this.name = name;
        this.worldName = loc1.getWorld().getName();
        this.pos1 = Position.fine(loc1);
        this.pos2 = Position.fine(loc2);
    }

    public @NotNull String getName() {
        return name;
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

    public void setWorld(final @NotNull World world) {
        this.worldName = world.getName();
        this.worldReference = new WeakReference<>(world);
    }

    public void setFirstPos(final @NotNull FinePosition pos1) {
        this.pos1 = pos1;
    }

    /// note: this also may change the world of the region.
    /// If you don't want this, use {@link #setFirstPos(FinePosition)} instead
    public void setFirstPos(final @NotNull Location loc1) {
        this.pos1 = Position.fine(loc1);
        this.worldName = loc1.getWorld().getName();
        this.worldReference = new WeakReference<>(loc1.getWorld());
    }

    public void setSecondPos(final @NotNull FinePosition pos2) {
        this.pos2 = pos2;
    }

    /// note: this also may change the world of the region.
    /// If you don't want this, use {@link #setSecondPos(FinePosition)} instead
    public void setSecondPos(final @NotNull Location loc2) {
        this.pos2 = Position.fine(loc2);
        this.worldName = loc2.getWorld().getName();
        this.worldReference = new WeakReference<>(loc2.getWorld());
    }

    public @NotNull FinePosition getPos1() {
        return pos1;
    }

    public @NotNull FinePosition getPos2() {
        return pos2;
    }

    public @NotNull SafeFineLocation getFirstPoint() {
        return new SafeFineLocation(worldName, pos1);
    }

    public @NotNull SafeFineLocation getSecondPoint() {
        return new SafeFineLocation(worldName, pos2);
    }

    public void updateRegion(@NotNull SafeFineLocation loc1, @NotNull SafeFineLocation loc2) {
        this.worldName = loc1.getWorldName();

        this.pos1 = loc1;
        this.pos2 = loc2;
    }

    /**
     * sorts the 2 positions making up this region: pos1 will have all the smaller coordinates,
     * while pos2 will hold all the bigger coordinates
     */
    public void sortPositions() {
        //temporary storage to not overwrite the max values
        final FinePosition pos1 = getMin();

        this.pos2 = getMax();
        this.pos1 = pos1;
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

    public boolean isInRegen(@NotNull Location location) {
        return location.getWorld().getName().equals(worldName) &&
            location.getBlockX() >= getMinX() && location.getBlockX() <= getMaxX() &&
            location.getBlockY() >= getMinY() && location.getBlockY() <= getMaxY() &&
            location.getBlockZ() >= getMinZ() && location.getBlockZ() <= getMaxZ();
    }

    public boolean isInRegen(@NotNull ASafeLocation location) {
        return location.getWorld().getName().equals(worldName) &&
            location.blockX() >= getMinX() && location.blockX() <= getMaxX() &&
            location.blockY() >= getMinY() && location.blockY() <= getMaxY() &&
            location.blockZ() >= getMinZ() && location.blockZ() <= getMaxZ();
    }

    public double getBaseArea() {
        return (1 + Math.abs(pos1.x() - pos2.x())) * (1 + Math.abs(pos1.z() - pos2.z()));
    }

    public double getVolume() {
        return getBaseArea() * (1 + Math.abs(pos1.y() - pos2.y()));
    }

    public double getHeight() {
        return Math.abs(pos1.y() - pos2.y());
    }

    public double getWidthX() {
        return Math.abs(pos1.x() - pos2.x());
    }

    public double getWidthZ() {
        return Math.abs(pos1.z() - pos2.z());
    }

    public @NotNull BoundingBox getBoundingBox() {
        return new BoundingBox(pos1.x(), pos1.y(), pos1.z(), pos2.x(), pos2.y(), pos2.z());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MgRegion) obj;

        return this.name.equals(that.name) &&
            Objects.equals(this.worldName, that.worldName) &&
            Objects.equals(this.pos1, that.pos1) &&
            Objects.equals(this.pos2, that.pos2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, pos1, pos2);
    }

    @Override
    public @NotNull String toString() {
        return "MgRegion[" +
            "name=" + name + ", " +
            "world=" + worldName + ", " +
            "pos1=" + pos1 + ", " +
            "pos2=" + pos2 + ']';
    }

    public @NotNull FinePosition getMin() {
        return Position.fine(getMinX(), getMinY(), getMinZ());
    }

    public @NotNull FinePosition getMax() {
        return Position.fine(getMaxX(), getMaxY(), getMaxZ());
    }

    public @NotNull Component describe() {
        return MinigameMessageManager.getMgMessage(MgMiscLangKey.REGION_DESCRIBE,
            Placeholder.component(MinigamePlaceHolderKey.POSITION_1.getKey(),
                MinigameMessageManager.getMgMessage(MgMiscLangKey.POSITION,
                    Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_X.getKey(), String.valueOf(getMinX())),
                    Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_Y.getKey(), String.valueOf(getMinY())),
                    Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_Z.getKey(), String.valueOf(getMinZ())))),
            Placeholder.component(MinigamePlaceHolderKey.POSITION_2.getKey(),
                MinigameMessageManager.getMgMessage(MgMiscLangKey.POSITION,
                    Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_X.getKey(), String.valueOf(getMaxX())),
                    Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_Y.getKey(), String.valueOf(getMaxY())),
                    Placeholder.unparsed(MinigamePlaceHolderKey.COORDINATE_Z.getKey(), String.valueOf(getMaxZ())))));
    }
}
