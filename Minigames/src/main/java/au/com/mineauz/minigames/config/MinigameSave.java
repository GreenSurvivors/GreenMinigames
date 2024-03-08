package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.Minigames;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public class MinigameSave {
    private final @NotNull String path;
    private final @Nullable String minigame;
    private FileConfiguration minigameSave = null;
    private File minigameSaveFile = null;

    public MinigameSave(@NotNull String path) {
        this.path = path;
        this.minigame = null;
        reloadFile();
        saveConfig();
    }

    public MinigameSave(@NotNull String minigame, @NotNull String path) {
        this.minigame = minigame;
        this.path = path;
        reloadFile();
        saveConfig();
    }

    public void reloadFile() {
        if (minigame != null) {
            if (minigameSaveFile == null) {
                minigameSaveFile = new File(Minigames.getPlugin().getDataFolder() + File.separator + "minigames" +
                        File.separator + minigame + File.separator, path + ".yml");
            }
        } else {
            if (minigameSaveFile == null) {
                minigameSaveFile = new File(Minigames.getPlugin().getDataFolder() + File.separator, path + ".yml");
            }
        }
        minigameSave = YamlConfiguration.loadConfiguration(minigameSaveFile);
    }

    public FileConfiguration getConfig() {
        if (minigameSave == null) {
            reloadFile();
        }
        return minigameSave;
    }

    public void saveConfig() {
        if (minigameSave == null || minigameSaveFile == null) {
            if (minigame != null) {
                Minigames.getCmpnntLogger().info("Could not save " + minigame + File.separator + path + " config file!");
            } else {
                Minigames.getCmpnntLogger().info("Could not save " + path + " config file!");
            }
            return;
        }
        try {
            minigameSave.save(minigameSaveFile);
        } catch (IOException ex) {
            if (minigame != null) {
                Minigames.getCmpnntLogger().error("Could not save " + minigame + File.separator + path + " config file!");
            } else {
                Minigames.getCmpnntLogger().error("Could not save " + path + " config file!");
            }
        }
    }

    public void deleteFile() {
        if (minigameSave == null) {
            reloadFile();
        }
        File delfile = new File(minigameSaveFile.getPath());
        delfile.delete();
        minigameSaveFile = null;
    }
}
