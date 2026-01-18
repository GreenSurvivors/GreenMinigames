package au.com.mineauz.minigames.script;

import au.com.mineauz.minigames.objects.safelocation.SafeBlockLocation;
import au.com.mineauz.minigames.objects.safelocation.SafeFineLocation;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class ScriptWrapper {

    public static @NotNull ScriptObject wrap(final @NotNull SafeFullLocation object) {
        return new ScriptObject() {
            @Override
            public @NotNull Set<@NotNull String> getReferenceKeys() {
                return Set.of("x", "y", "z", "bx", "by", "bz", "world", "yaw", "pitch", "block");
            }

            @Override
            public @Nullable ScriptReference resolveReference(@NotNull String name) {
                if (name.equalsIgnoreCase("x")) {
                    return ScriptValue.of(object.x());
                } else if (name.equalsIgnoreCase("y")) {
                    return ScriptValue.of(object.y());
                } else if (name.equalsIgnoreCase("z")) {
                    return ScriptValue.of(object.z());
                } else if (name.equalsIgnoreCase("bx")) {
                    return ScriptValue.of(object.blockX());
                } else if (name.equalsIgnoreCase("by")) {
                    return ScriptValue.of(object.blockY());
                } else if (name.equalsIgnoreCase("bz")) {
                    return ScriptValue.of(object.blockZ());
                } else if (name.equalsIgnoreCase("world")) {
                    return wrap(object.getWorld());
                } else if (name.equalsIgnoreCase("yaw")) {
                    return ScriptValue.of(object.yaw());
                } else if (name.equalsIgnoreCase("pitch")) {
                    return ScriptValue.of(object.pitch());
                } else if (name.equalsIgnoreCase("block")) {
                    return wrap(object.getBlockAt());
                }
                return null;
            }

            @Override
            public @NotNull String getAsString() {
                return String.format("%.1f,%.1f,%.1f,%s", object.x(), object.y(), object.z(), object.getWorld().getName());
            }
        };
    }

    public static @NotNull ScriptObject wrap(final @NotNull SafeFineLocation object) {
        return new ScriptObject() {
            @Override
            public @NotNull Set<@NotNull String> getReferenceKeys() {
                return Set.of("x", "y", "z", "bx", "by", "bz", "world");
            }

            @Override
            public @Nullable ScriptReference resolveReference(@NotNull String name) {
                if (name.equalsIgnoreCase("x")) {
                    return ScriptValue.of(object.x());
                } else if (name.equalsIgnoreCase("y")) {
                    return ScriptValue.of(object.y());
                } else if (name.equalsIgnoreCase("z")) {
                    return ScriptValue.of(object.z());
                } else if (name.equalsIgnoreCase("bx")) {
                    return ScriptValue.of(object.blockX());
                } else if (name.equalsIgnoreCase("by")) {
                    return ScriptValue.of(object.blockY());
                } else if (name.equalsIgnoreCase("bz")) {
                    return ScriptValue.of(object.blockZ());
                } else if (name.equalsIgnoreCase("world")) {
                    return wrap(object.getWorld());
                }
                return null;
            }

            @Override
            public @NotNull String getAsString() {
                return String.format("%.1f,%.1f,%.1f,%s", object.x(), object.y(), object.z(), object.getWorldName());
            }
        };
    }

    public static @NotNull ScriptObject wrap(final @NotNull SafeBlockLocation object) {
        return new ScriptObject() {
            @Override
            public @NotNull Set<@NotNull String> getReferenceKeys() {
                return Set.of("x", "y", "z", "bx", "by", "bz", "world");
            }

            @Override
            public @Nullable ScriptReference resolveReference(@NotNull String name) {
                if (name.equalsIgnoreCase("x")) {
                    return ScriptValue.of(object.x());
                } else if (name.equalsIgnoreCase("y")) {
                    return ScriptValue.of(object.y());
                } else if (name.equalsIgnoreCase("z")) {
                    return ScriptValue.of(object.z());
                } else if (name.equalsIgnoreCase("bx")) {
                    return ScriptValue.of(object.blockX());
                } else if (name.equalsIgnoreCase("by")) {
                    return ScriptValue.of(object.blockY());
                } else if (name.equalsIgnoreCase("bz")) {
                    return ScriptValue.of(object.blockZ());
                } else if (name.equalsIgnoreCase("world")) {
                    return wrap(object.getWorld());
                }
                return null;
            }

            @Override
            public @NotNull String getAsString() {
                return String.format("%.1f,%.1f,%.1f,%s", object.x(), object.y(), object.z(), object.getWorldName());
            }
        };
    }

    public static @NotNull ScriptObject wrap(final @NotNull Location object) {
        return new ScriptObject() {
            @Override
            public @NotNull Set<@NotNull String> getReferenceKeys() {
                return Set.of("x", "y", "z", "bx", "by", "bz", "world", "yaw", "pitch", "block");
            }

            @Override
            public @Nullable ScriptReference resolveReference(@NotNull String name) {
                if (name.equalsIgnoreCase("x")) {
                    return ScriptValue.of(object.getX());
                } else if (name.equalsIgnoreCase("y")) {
                    return ScriptValue.of(object.getY());
                } else if (name.equalsIgnoreCase("z")) {
                    return ScriptValue.of(object.getZ());
                } else if (name.equalsIgnoreCase("bx")) {
                    return ScriptValue.of(object.getBlockX());
                } else if (name.equalsIgnoreCase("by")) {
                    return ScriptValue.of(object.getBlockY());
                } else if (name.equalsIgnoreCase("bz")) {
                    return ScriptValue.of(object.getBlockZ());
                } else if (name.equalsIgnoreCase("world")) {
                    return wrap(object.getWorld());
                } else if (name.equalsIgnoreCase("yaw")) {
                    return ScriptValue.of(object.getYaw());
                } else if (name.equalsIgnoreCase("pitch")) {
                    return ScriptValue.of(object.getPitch());
                } else if (name.equalsIgnoreCase("block")) {
                    return wrap(object.getBlock());
                }
                return null;
            }

            @Override
            public @NotNull String getAsString() {
                return String.format("%.1f,%.1f,%.1f,%s", object.getX(), object.getY(), object.getZ(), object.getWorld().getName());
            }
        };
    }

    public static @NotNull ScriptObject wrap(final @NotNull Block object) {
        return new ScriptObject() {
            @Override
            public @NotNull Set<@NotNull String> getReferenceKeys() {
                return Set.of("pos", "type", "data", "temperature", "light", "blocklight", "skylight", "redstone");
            }

            @Override
            public @Nullable ScriptReference resolveReference(@NotNull String name) {
                if (name.equalsIgnoreCase("pos")) {
                    return wrap(object.getLocation());
                } else if (name.equalsIgnoreCase("type")) {
                    return ScriptValue.of(object.getType());
                } else if (name.equalsIgnoreCase("data")) {
                    return ScriptValue.of(object.getBlockData());
                } else if (name.equalsIgnoreCase("temperature")) {
                    return ScriptValue.of(object.getTemperature());
                } else if (name.equalsIgnoreCase("light")) {
                    return ScriptValue.of(object.getLightLevel());
                } else if (name.equalsIgnoreCase("blocklight")) {
                    return ScriptValue.of(object.getLightFromBlocks());
                } else if (name.equalsIgnoreCase("skylight")) {
                    return ScriptValue.of(object.getLightFromSky());
                } else if (name.equalsIgnoreCase("redstone")) {
                    return ScriptValue.of(object.getBlockPower());
                }

                return null;
            }

            @Override
            public @NotNull String getAsString() {
                return String.format("%d,%d,%d,%s %s:%s", object.getX(), object.getY(), object.getZ(), object.getWorld().getName(), object.getType(), object.getBlockData().getAsString());
            }
        };
    }

    public static @NotNull ScriptObject wrap(final @NotNull World object) {
        return new ScriptObject() {
            @Override
            public @NotNull Set<@NotNull String> getReferenceKeys() {
                return Set.of("name", "time");
            }

            @Override
            public @Nullable ScriptReference resolveReference(@NotNull String name) {
                if (name.equalsIgnoreCase("name")) {
                    return ScriptValue.of(object.getName());
                } else if (name.equalsIgnoreCase("time")) {
                    return ScriptValue.of(object.getTime());
                }

                return null;
            }

            @Override
            public @NotNull String getAsString() {
                return object.getName();
            }
        };
    }
}
