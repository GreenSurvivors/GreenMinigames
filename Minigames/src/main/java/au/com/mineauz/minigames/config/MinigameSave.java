package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class MinigameSave {
    private final @MonotonicNonNull Minigames plugin = Minigames.getPlugin();
    protected final @NotNull Path path;
    protected final @NotNull YamlConfigurationLoader loader;
    protected @Nullable CommentedConfigurationNode root;

    protected MinigameSave(final @NotNull Path path) {
        this.path = plugin.getDataPath().resolve(path.resolveSibling(path.getFileName() + ".yml"));
        this.loader = YamlConfigurationLoader.builder().path(path).build();
    }

    public static MinigameSave forGlobalData(final @NotNull Path subPath) {
        return new MinigameSave(subPath);
    }

    public static MinigameSave forPlayerData(final @NotNull UUID playerUUID, final @NotNull Path subPath) {
        return new MinigameSave(Path.of("playerdata").resolve(subPath).resolve(playerUUID.toString()));
    }

    public static @NotNull MinigameSave forMinigameData(final @NotNull Minigame minigame, final @NotNull Path subPath) {
        return new MinigameSave(Path.of("minigames", minigame.getName()).resolve(subPath));
    }

    public boolean existsOnDisk() {
        return Files.exists(path);
    }

    public void createBackup() throws IOException {
        if (existsOnDisk()) {
            Files.copy(path, path.resolveSibling(path.getFileName().toString() + System.currentTimeMillis() + ".backup"));
        }
    }

    public @NotNull CommentedConfigurationNode getConfigRoot() throws ConfigurateException {
        if (root == null) {
            root = loader.load();
        }

        return root;
    }

    public void saveConfig() throws IOException {
        if (root == null) {
            plugin.getComponentLogger().info("Could not save config file" + path + "!");
            return;
        }

        loader.save(root);
    }

    public void deleteFile() {
        if (existsOnDisk()) {
            try {
                Files.delete(path);
            } catch (final @NotNull IOException e) {
                plugin.getComponentLogger().error("Could not delete config file " + path + "!", e);
            }
        }

        root = null;
    }
}
