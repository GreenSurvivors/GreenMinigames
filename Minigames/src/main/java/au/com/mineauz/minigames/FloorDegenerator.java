package au.com.mineauz.minigames;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigames.objects.safelocation.SafeBlockLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockType;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

@SuppressWarnings({"LoggingSimilarMessage", // it's fine. we don't need to know the code line to know the world was unloaded...
    "UnstableApiUsage"}) // shutup Position!
public class FloorDegenerator {
    private static final Minigames plugin = Minigames.getPlugin();
    private final @NotNull SafeBlockLocation topCorner;
    private final @NotNull SafeBlockLocation bottomCorner;
    private @NotNull SafeBlockLocation xSideNeg1;
    private @NotNull SafeBlockLocation xSidePos1;
    private @NotNull SafeBlockLocation zSideNeg1;
    private @NotNull SafeBlockLocation zSidePos1;
    private @NotNull SafeBlockLocation xSideNeg2;
    private @NotNull SafeBlockLocation xSidePos2;
    private @NotNull SafeBlockLocation zSideNeg2;
    private @NotNull SafeBlockLocation zSidePos2;
    private final @NotNull Minigame mgm;
    private final long timeDelay;
    private int taskID = -1;

    private int radiusModifier = 0;

    public FloorDegenerator(@NotNull MgRegion region, @NotNull Minigame mgm) {
        timeDelay = mgm.getFloorDegenTime();
        this.mgm = mgm;

        topCorner = new SafeBlockLocation(region.getWorldName(), region.getMax());
        bottomCorner = new SafeBlockLocation(region.getWorldName(), region.getMin());

        xSideNeg1 = new SafeBlockLocation(region.getWorldName(), region.getMin());
        xSideNeg2 = new SafeBlockLocation(region.getWorldName(), NumberConversions.floor(region.getMaxX()), NumberConversions.floor(region.getMaxY()), NumberConversions.floor(region.getMinZ()));
        zSideNeg1 = new SafeBlockLocation(region.getWorldName(), NumberConversions.floor(region.getMinX()), NumberConversions.floor(region.getMinY()), NumberConversions.floor(region.getMinZ()));
        zSideNeg2 = new SafeBlockLocation(region.getWorldName(), NumberConversions.floor(region.getMinX()), NumberConversions.floor(region.getMaxY()), NumberConversions.floor(region.getMaxZ()));
        xSidePos1 = new SafeBlockLocation(region.getWorldName(), NumberConversions.floor(region.getMinX()), NumberConversions.floor(region.getMinY()), NumberConversions.floor(region.getMaxZ()));
        xSidePos2 = new SafeBlockLocation(region.getWorldName(), NumberConversions.floor(region.getMaxX()), NumberConversions.floor(region.getMaxY()), NumberConversions.floor(region.getMaxZ()));
        zSidePos1 = new SafeBlockLocation(region.getWorldName(), NumberConversions.floor(region.getMaxX()), NumberConversions.floor(region.getMinY()), NumberConversions.floor(region.getMinZ()));
        zSidePos2 = new SafeBlockLocation(region.getWorldName(), region.getMax());
    }

    public void startDegeneration() {
        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            switch (mgm.getDegenType()) {
                case INWARD -> {
                    degenerateSide(xSideNeg1, xSideNeg2);
                    degenerateSide(xSidePos1, xSidePos2);
                    degenerateSide(zSideNeg1, zSideNeg2);
                    degenerateSide(zSidePos1, zSidePos2);
                    incrementSide();
                    if (xSideNeg1.z() >= xSidePos1.z() || zSideNeg1.x() >= zSidePos1.x()) {
                        stopDegenerator();
                    }
                }
                case RANDOM -> degenerateRandom(bottomCorner, topCorner, mgm.getDegenRandomChance());
                case CIRCLE -> degenerateCircle(bottomCorner, topCorner);
            }
        }, timeDelay * 20L, timeDelay * 20L);
    }

    private void incrementSide() {
        xSideNeg1 = xSideNeg1.offset(0, 0, xSideNeg1.blockZ() + 1);
        xSideNeg2 = xSideNeg2.offset(0, 0, xSideNeg2.blockZ() + 1);
        xSidePos1 = xSidePos1.offset(0, 0, xSidePos1.blockZ() - 1);
        xSidePos2 = xSidePos2.offset(0, 0, xSidePos2.blockZ() - 1);
        zSideNeg1 = zSideNeg1.offset(zSideNeg1.blockX() + 1, 0, 0);
        zSideNeg2 = zSideNeg2.offset(zSideNeg2.blockX() + 1, 0, 0);
        zSidePos1 = zSidePos1.offset(zSidePos1.blockX() - 1, 0, 0);
        zSidePos2 = zSidePos2.offset(zSidePos2.blockX() - 1, 0, 0);
    }

    private void degenerateSide(final @NotNull SafeBlockLocation loc1, final @NotNull SafeBlockLocation loc2) {
        if (loc1.getWorld() == null) {
            plugin.getComponentLogger().error("Couldn't degenenerate block for minigame " + mgm.getName() + " in world " + loc1.getWorldName() + ", because the world wasn't loaded! Stopping degen process.");
            stopDegenerator();
            return;
        }

        final @NotNull Location curblock = loc1.toLocation();
        final int x = curblock.getBlockX();
        final int z = curblock.getBlockZ();
        int y = curblock.getBlockY();
        do {
            curblock.setZ(z);
            curblock.setX(x);
            curblock.setY(y);
            for (int i = loc1.blockX(); i <= loc2.blockX() + 1; i++) {
                for (int k = loc1.blockZ(); k <= loc2.blockZ() + 1; k++) {
                    if (curblock.getBlock().getType().isAir()) {
                        mgm.getRecorderData().addBlock(curblock.getBlock(), null);
                        curblock.getBlock().setBlockData(BlockType.AIR.createBlockData());
                    }
                    curblock.setZ(k);
                }
                curblock.setX(i);
                curblock.setZ(z);
            }
            y++;
        } while (y <= loc2.blockZ());
    }

    private void degenerateRandom(final @NotNull SafeBlockLocation lowest, final @NotNull SafeBlockLocation highest, final int chance) {
        if (lowest.getWorld() == null) {
            plugin.getComponentLogger().error("Couldn't degenenerate block for minigame " + mgm.getName() + " in world " + lowest.getWorldName() + ", because the world wasn't loaded! Stopping degen process.");
            stopDegenerator();
            return;
        }

        final @NotNull Location curblock = lowest.toLocation();
        final int x = curblock.getBlockX();
        final int z = curblock.getBlockZ();
        int y = curblock.getBlockY();
        final @NotNull Random random = new Random();
        do {
            curblock.setZ(z);
            curblock.setX(x);
            curblock.setY(y);
            for (int i = lowest.blockX(); i <= highest.blockX() + 1; i++) {
                for (int k = lowest.blockZ(); k <= highest.blockZ() + 1; k++) {
                    if (curblock.getBlock().getType().isAir() && random.nextInt(100) < chance) {
                        mgm.getRecorderData().addBlock(curblock.getBlock(), null);
                        curblock.getBlock().setBlockData(BlockType.AIR.createBlockData());
                    }
                    curblock.setZ(k);
                }
                curblock.setX(i);
                curblock.setZ(z);
            }
            y++;
        } while (y <= highest.blockY());
    }

    private void degenerateCircle(final @NotNull SafeBlockLocation lowest, final @NotNull SafeBlockLocation highest) {
        if (lowest.getWorld() == null) {
            plugin.getComponentLogger().error("Couldn't degenenerate block for minigame " + mgm.getName() + " in world " + lowest.getWorldName() + ", because the world wasn't loaded! Stopping degen process.");
            stopDegenerator();
            return;
        }

        final int middledist = (int) Math.abs(Math.floor((double) (highest.blockX() - lowest.blockX()) / 2));
        final int radius = middledist - radiusModifier;
        Location centerBlock = lowest.toLocation();
        centerBlock.setX(centerBlock.getX() + middledist);
        centerBlock.setZ(centerBlock.getZ() + middledist);
        Location curBlock = centerBlock.clone();

        final int size = (int) Math.pow(radius, 3) + 8;

        for (int i = 0; i < size; i++) {
            final double cirPoint = 2 * Math.PI * i / size;
            final double cx = centerBlock.getX() - 0.5 + Math.round(radius * Math.cos(cirPoint));
            final double cz = centerBlock.getZ() - 0.5 + Math.round(radius * Math.sin(cirPoint));
            curBlock.setX(cx);
            curBlock.setZ(cz);
            for (int k = lowest.blockY(); k <= highest.blockY(); k++) {
                curBlock.setY(k);
                mgm.getRecorderData().addBlock(curBlock.getBlock(), null);
                curBlock.getBlock().setBlockData(BlockType.AIR.createBlockData());
            }
        }

        radiusModifier++;

        if (middledist == radiusModifier) {
            stopDegenerator();
        }
    }

    public void stopDegenerator() {
        if (taskID != -1) {
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }

    public enum DegeneratorType {
        INWARD,
        RANDOM,
        CIRCLE;

        public static @Nullable DegeneratorType matchType(final @NotNull String str) {
            for (final @NotNull DegeneratorType value : DegeneratorType.values()) {
                if (value.name().equalsIgnoreCase(str)) {
                    return value;
                }
            }

            return null;
        }
    }
}
