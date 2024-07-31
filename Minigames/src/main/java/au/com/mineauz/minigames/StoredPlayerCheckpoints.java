package au.com.mineauz.minigames;

import au.com.mineauz.minigames.config.MinigameSave;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class StoredPlayerCheckpoints {
    private final @NotNull String uuid;
    private final @NotNull Map<@NotNull String, @NotNull Location> checkpoints;
    private final @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> singlePlayerFlags;
    private final @NotNull Map<@NotNull String, @NotNull Long> storedTime;
    private final @NotNull Map<@NotNull String, @NotNull Integer> storedDeaths;
    private final @NotNull Map<@NotNull String, @NotNull Integer> storedReverts;
    private @Nullable Location globalCheckpoint;

    public StoredPlayerCheckpoints(@NotNull String uuid) {
        this.uuid = uuid;
        checkpoints = new HashMap<>();
        singlePlayerFlags = new HashMap<>();
        storedTime = new HashMap<>();
        storedDeaths = new HashMap<>();
        storedReverts = new HashMap<>();
    }

    public void addCheckpoint(@NotNull String minigame, @NotNull Location checkpoint) {
        checkpoints.put(minigame, checkpoint);
    }

    public void removeCheckpoint(@NotNull String minigame) {
        checkpoints.remove(minigame);
    }

    public boolean hasCheckpoint(@NotNull String minigame) {
        return checkpoints.containsKey(minigame);
    }

    public Location getCheckpoint(@NotNull String minigame) {
        return checkpoints.get(minigame);
    }

    public boolean hasGlobalCheckpoint() {
        return globalCheckpoint != null;
    }

    public @Nullable Location getGlobalCheckpoint() {
        return globalCheckpoint;
    }

    public void setGlobalCheckpoint(Location checkpoint) {
        globalCheckpoint = checkpoint;
    }

    public boolean hasNoCheckpoints() {
        return checkpoints.isEmpty();
    }

    public boolean hasSinglePlayerFlags(@NotNull String minigame) {
        return singlePlayerFlags.containsKey(minigame);
    }

    public void addSinglePlayerFlags(@NotNull String minigame, @NotNull List<String> flagList) {
        singlePlayerFlags.put(minigame, new ArrayList<>(flagList));
    }

    public @NotNull List<@NotNull String> getSinglePlayerFlags(@NotNull String minigame) {
        return singlePlayerFlags.get(minigame);
    }

    public void removeSinglePlayerFlags(@NotNull String minigame) {
        singlePlayerFlags.remove(minigame);
    }

    public void addTime(@NotNull String minigame, long time) {
        storedTime.put(minigame, time);
    }

    public Long getTime(@NotNull String minigame) {
        return storedTime.get(minigame);
    }

    public boolean hasTime(@NotNull String minigame) {
        return storedTime.containsKey(minigame);
    }

    public void removeTime(@NotNull String minigame) {
        storedTime.remove(minigame);
    }

    public void addDeaths(@NotNull String minigame, int deaths) {
        storedDeaths.put(minigame, deaths);
    }

    public Integer getDeaths(@NotNull String minigame) {
        return storedDeaths.get(minigame);
    }

    public boolean hasDeaths(@NotNull String minigame) {
        return storedDeaths.containsKey(minigame);
    }

    public void removeDeaths(@NotNull String minigame) {
        storedDeaths.remove(minigame);
    }

    public void addReverts(@NotNull String minigame, int reverts) {
        storedReverts.put(minigame, reverts);
    }

    public Integer getReverts(@NotNull String minigame) {
        return storedReverts.get(minigame);
    }

    public boolean hasReverts(@NotNull String minigame) {
        return storedReverts.containsKey(minigame);
    }

    public void removeReverts(@NotNull String minigame) {
        storedReverts.remove(minigame);
    }

    public void saveCheckpoints() {
        MinigameSave save = new MinigameSave("playerdata" + File.separator + "checkpoints" + File.separator + uuid);
        save.deleteFile();
        if (hasNoCheckpoints()) return;

        save = new MinigameSave("playerdata" + File.separator + "checkpoints" + File.separator + uuid);
        Configuration config = save.getConfig();
        char configSeparator = config.options().pathSeparator();
        for (String mgm : checkpoints.keySet()) {
            MinigameMessageManager.debugMessage("Attempting to save checkpoint for " + mgm + "...");
            try {
                config.set(mgm, null);
                config.set(mgm + configSeparator + "x", checkpoints.get(mgm).getX());
                config.set(mgm + configSeparator + "y", checkpoints.get(mgm).getY());
                config.set(mgm + configSeparator + "z", checkpoints.get(mgm).getZ());
                config.set(mgm + configSeparator + "yaw", checkpoints.get(mgm).getYaw());
                config.set(mgm + configSeparator + "pitch", checkpoints.get(mgm).getPitch());
                config.set(mgm + configSeparator + "world", checkpoints.get(mgm).getWorld().getName());

                if (singlePlayerFlags.containsKey(mgm)) {
                    config.set(mgm + configSeparator + "flags", getSinglePlayerFlags(mgm));
                }

                if (storedTime.containsKey(mgm)) {
                    config.set(mgm + configSeparator + "time", getTime(mgm));
                }

                if (storedDeaths.containsKey(mgm)) {
                    config.set(mgm + configSeparator + "deaths", getDeaths(mgm));
                }

                if (storedReverts.containsKey(mgm)) {
                    config.set(mgm + configSeparator + "reverts", getReverts(mgm));
                }
            } catch (Exception e) {
                // When an error is detected, remove the stored erroneous checkpoint
                Minigames.getCmpnntLogger().warn("Unable to save checkpoint for " + mgm + "! It has been been removed.");
                Minigames.getCmpnntLogger().error("", e);

                // Remove the checkpoint from memory, so it doesn't cause an error again
                config.set(mgm, null);
                checkpoints.remove(mgm);
                singlePlayerFlags.remove(mgm);
                storedTime.remove(mgm);
                storedDeaths.remove(mgm);
                storedReverts.remove(mgm);
            }
        }

        if (hasGlobalCheckpoint()) {
            try {
                config.set("globalcheckpoint" + configSeparator + "x", globalCheckpoint.getX());
                config.set("globalcheckpoint" + configSeparator + "y", globalCheckpoint.getY());
                config.set("globalcheckpoint" + configSeparator + "z", globalCheckpoint.getZ());
                config.set("globalcheckpoint" + configSeparator + "yaw", globalCheckpoint.getYaw());
                config.set("globalcheckpoint" + configSeparator + "pitch", globalCheckpoint.getPitch());
                config.set("globalcheckpoint" + configSeparator + "world", globalCheckpoint.getWorld().getName());
            } catch (Exception e) {
                // When an error is detected, remove the global checkpoint
                config.set("globalcheckpoint", null);
                Minigames.getCmpnntLogger().warn("Unable to save global checkpoint!", e);
            }
        }
        save.saveConfig();
    }

    public void loadCheckpoints() {
        MinigameSave save = new MinigameSave("playerdata" + File.separator + "checkpoints" + File.separator + uuid);
        Configuration config = save.getConfig();
        char configSeparator = config.options().pathSeparator();
        Set<String> mgms = config.getKeys(false);
        for (String mgm : mgms) {
            if (!mgm.equals("globalcheckpoint")) {
                MinigameMessageManager.debugMessage("Attempting to load checkpoint for " + mgm + "...");
                try {
                    double locx = config.getDouble(mgm + configSeparator + "x");
                    double locy = config.getDouble(mgm + configSeparator + "y");
                    double locz = config.getDouble(mgm + configSeparator + "z");
                    float yaw = (float) config.getDouble(mgm + configSeparator + "yaw");
                    float pitch = (float) config.getDouble(mgm + configSeparator + "pitch");
                    String world = (String) config.get(mgm + configSeparator + "world");

                    World w = Minigames.getPlugin().getServer().getWorld(world);
                    if (w == null) {
                        Minigames.getCmpnntLogger().warn("WARNING: Invalid world \"" + world + "\" found in checkpoint for " + mgm + "! Checkpoint has been removed.");
                        continue;
                    }

                    Location loc = new Location(w, locx, locy, locz, yaw, pitch);
                    checkpoints.put(mgm, loc);
                } catch (ClassCastException e) {
                    MinigameMessageManager.debugMessage("Checkpoint could not be loaded ... " + mgm + " xyz not double");
                } catch (NullPointerException e) {
                    Minigames.getCmpnntLogger().error("", e);
                }
                if (config.contains(mgm + configSeparator + "flags")) {
                    singlePlayerFlags.put(mgm, config.getStringList(mgm + configSeparator + "flags"));
                }

                if (config.contains(mgm + configSeparator + "time")) {
                    storedTime.put(mgm, config.getLong(mgm + configSeparator + "time"));
                }

                if (config.contains(mgm + configSeparator + "deaths")) {
                    storedDeaths.put(mgm, config.getInt(mgm + configSeparator + "deaths"));
                }

                if (config.contains(mgm + configSeparator + "reverts")) {
                    storedReverts.put(mgm, config.getInt(mgm + configSeparator + "reverts"));
                }
            }
        }

        if (config.contains("globalcheckpoint")) {
            double x = config.getDouble("globalcheckpoint" + configSeparator + "x");
            double y = config.getDouble("globalcheckpoint" + configSeparator + "y");
            double z = config.getDouble("globalcheckpoint" + configSeparator + "z");
            float yaw = (float) config.getDouble("globalcheckpoint" + configSeparator + "yaw");
            float pitch = (float) config.getDouble("globalcheckpoint" + configSeparator + "pitch");
            String world = config.getString("globalcheckpoint" + configSeparator + "world");

            World w = Minigames.getPlugin().getServer().getWorld(world);
            if (w == null) {
                Minigames.getCmpnntLogger().warn("WARNING: Invalid world \"" + world + "\" found in global checkpoint! Checkpoint has been removed.");
            } else {
                globalCheckpoint = new Location(Minigames.getPlugin().getServer().getWorld(world), x, y, z, yaw, pitch);
            }
        }
    }
}
