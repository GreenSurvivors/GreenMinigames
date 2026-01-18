package au.com.mineauz.minigames;

import au.com.mineauz.minigames.config.MinigameSave;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.objects.safelocation.SafeFullLocation;
import io.leangen.geantyref.TypeToken;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StoredPlayerCheckpoints {
    private final @NotNull UUID uuid;
    private final @NotNull Map<@NotNull String, @NotNull SafeFullLocation> checkpoints;
//    private final @NotNull Map<@NotNull String, @NotNull List<@NotNull String>> singlePlayerFlags; // the whole singleplayer flag system is unused.
    private final @NotNull Map<@NotNull String, @NotNull Long> storedTime;
    private final @NotNull Map<@NotNull String, @NotNull Integer> storedDeaths;
    private final @NotNull Map<@NotNull String, @NotNull Integer> storedReverts;
    private @Nullable SafeFullLocation globalCheckpoint;

    public StoredPlayerCheckpoints(final @NotNull UUID uuid) {
        this.uuid = uuid;
        checkpoints = new HashMap<>();
//        singlePlayerFlags = new HashMap<>();
        storedTime = new HashMap<>();
        storedDeaths = new HashMap<>();
        storedReverts = new HashMap<>();
    }

    public void addCheckpoint(@NotNull String minigame, @NotNull SafeFullLocation checkpoint) {
        checkpoints.put(minigame, checkpoint);
    }

    public void removeCheckpoint(@NotNull String minigame) {
        checkpoints.remove(minigame);
    }

    public boolean hasCheckpoint(@NotNull String minigame) {
        return checkpoints.containsKey(minigame);
    }

    public @Nullable SafeFullLocation getCheckpoint(@NotNull String minigame) {
        return checkpoints.get(minigame);
    }

    public boolean hasGlobalCheckpoint() {
        return globalCheckpoint != null;
    }

    public @Nullable SafeFullLocation getGlobalCheckpoint() {
        return globalCheckpoint;
    }

    public void setGlobalCheckpoint(@Nullable SafeFullLocation checkpoint) {
        globalCheckpoint = checkpoint;
    }

    public boolean hasNoCheckpoints() {
        return checkpoints.isEmpty();
    }

//    public boolean hasSinglePlayerFlags(@NotNull String minigame) {
//        return singlePlayerFlags.containsKey(minigame);
//    }

//    public void addSinglePlayerFlags(@NotNull String minigame, @NotNull List<String> flagList) {
//        singlePlayerFlags.put(minigame, new ArrayList<>(flagList));
//    }
//
//    public @NotNull List<@NotNull String> getSinglePlayerFlags(@NotNull String minigame) {
//        return singlePlayerFlags.get(minigame);
//    }

//    public void removeSinglePlayerFlags(@NotNull String minigame) {
//        singlePlayerFlags.remove(minigame);
//    }

    public void addTime(@NotNull String minigame, long time) {
        storedTime.put(minigame, time);
    }

    public @Nullable Long getTime(final @NotNull String minigame) {
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

    public @Nullable Integer getDeaths(@NotNull String minigame) {
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

    public @Nullable Integer getReverts(@NotNull String minigame) {
        return storedReverts.get(minigame);
    }

    public boolean hasReverts(@NotNull String minigame) {
        return storedReverts.containsKey(minigame);
    }

    public void removeReverts(@NotNull String minigame) {
        storedReverts.remove(minigame);
    }

    public void saveCheckpoints() throws IOException {
        final @NotNull MinigameSave save = MinigameSave.forPlayerData(uuid, Path.of("checkpoints"));
        save.deleteFile();
        if (hasNoCheckpoints()) return;

        final @NotNull CommentedConfigurationNode rootNode = save.getConfigRoot();

        for (final @NotNull Map.Entry<@NotNull String, @NotNull SafeFullLocation> entry : checkpoints.entrySet()) {
            final @NotNull String minigameName = entry.getKey();
            MinigameMessageManager.debugMessage("Attempting to save checkpoint for " + minigameName + "...");
            final @NotNull CommentedConfigurationNode minigameCheckpointNode = rootNode.node(minigameName);

            final @NotNull SafeFullLocation location = entry.getValue();
            minigameCheckpointNode.node("x").raw(location.x());
            minigameCheckpointNode.node("y").raw(location.y());
            minigameCheckpointNode.node("z").raw(location.z());
            minigameCheckpointNode.node("yaw").raw(location.yaw());
            minigameCheckpointNode.node("pitch").raw(location.pitch());
            minigameCheckpointNode.node("world").raw(location.getWorldName());

//            minigameCheckpointNode.node("flags").setList(String.class, getSinglePlayerFlags(minigameName));
            minigameCheckpointNode.node("time").raw(getTime(minigameName));
            minigameCheckpointNode.node("deaths").raw(getDeaths(minigameName));
            minigameCheckpointNode.node("reverts").raw(getReverts(minigameName));
        }

        if (globalCheckpoint != null) {
            rootNode.node("globalcheckpoint").set(globalCheckpoint);
        }
        save.saveConfig();
    }

    public void loadCheckpoints() throws ConfigurateException {
        final @NotNull CommentedConfigurationNode rootNode = MinigameSave.forPlayerData(uuid, Path.of("checkpoints")).getConfigRoot();
        for (final @NotNull Map.Entry<@NotNull Object, @NotNull CommentedConfigurationNode> entry : rootNode.childrenMap().entrySet()) {
            final @NotNull String nodeKey = entry.getKey().toString();
            final @NotNull CommentedConfigurationNode checkpointNode = entry.getValue();

            if (nodeKey.equals("globalcheckpoint")) {
                globalCheckpoint = checkpointNode.get(TypeToken.get(SafeFullLocation.class));
            } else {
                final @NotNull String minigameName = checkpointNode.key().toString();

                MinigameMessageManager.debugMessage("Attempting to load checkpoint for " + minigameName + "...");
                final double x = checkpointNode.node("x").getDouble();
                final double y = checkpointNode.node("y").getDouble();
                final double z = checkpointNode.node("z").getDouble();
                final float yaw = checkpointNode.node("yaw").getFloat();
                final float pitch = checkpointNode.node("pitch").getFloat();
                final String worldName = checkpointNode.node("world").getString("");

                final @Nullable World world = Minigames.getPlugin().getServer().getWorld(worldName);
                if (world == null) {
                    Minigames.getPlugin().getComponentLogger().warn("Invalid world \"" + worldName + "\" found in checkpoint for " + minigameName + "!");
                }

                checkpoints.put(minigameName, new SafeFullLocation(worldName, x, y, z, yaw, pitch));

//                if (checkpointNode.hasChild("flags")) {
//                    singlePlayerFlags.put(minigameName, checkpointNode.node("flags").getList(String.class));
//                }

                if (rootNode.hasChild("time")) {
                    storedTime.put(minigameName, rootNode.node("time").getLong());
                }

                if (rootNode.hasChild("deaths")) {
                    storedDeaths.put(minigameName, rootNode.node("deaths").getInt());
                }

                if (rootNode.hasChild("reverts")) {
                    storedReverts.put(minigameName, rootNode.node("reverts").getInt());
                }
            }
        }
    }
}
