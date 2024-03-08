package au.com.mineauz.minigames.minigame;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.MinigameSave;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ScoreboardData {
    private final Map<Block, ScoreboardDisplay> displays = new HashMap<>();

    public ScoreboardData() {
    }

    public ScoreboardDisplay getDisplay(Block block) {
        return displays.get(block);
    }

    public void addDisplay(ScoreboardDisplay display) {
        displays.put(display.getRoot().getBlock(), display);
    }

    public void removeDisplay(Block block) {
        ScoreboardDisplay display = displays.remove(block);
        if (display != null) {
            display.deleteSigns();

            block.removeMetadata("MGScoreboardSign", Minigames.getPlugin());
            block.removeMetadata("Minigame", Minigames.getPlugin());
        }
    }

    /**
     * Makes async queries to the database loading the data for each scoreboard display
     */
    public void reload() {
        for (ScoreboardDisplay display : displays.values()) {
            display.reload();
        }
    }

    public void reload(Block block) {
        ScoreboardDisplay display = getDisplay(block);
        if (display != null) {
            display.reload();
        }
    }

    /**
     * Makes each scoreboard update its signs with their current data. This does not update the scoreboard data.
     */
    public void refreshDisplays() {
        for (ScoreboardDisplay display : displays.values()) {
            display.updateSigns();
        }
    }

    public void saveDisplays(MinigameSave save, String name) {
        FileConfiguration config = save.getConfig();

        int index = 0;
        for (ScoreboardDisplay display : displays.values()) {
            display.save(config, name + config.options().pathSeparator() + "scoreboards" + config.options().pathSeparator() + index++);
        }
    }

    public void loadDisplays(MinigameSave save, Minigame mgm) {
        FileConfiguration config = save.getConfig();
        ConfigurationSection root = config.getConfigurationSection(mgm.getName() + config.options().pathSeparator() + "scoreboards");

        if (root == null) {
            return;
        }

        for (String key : root.getKeys(false)) {
            ScoreboardDisplay display = ScoreboardDisplay.load(mgm, config, mgm.getName() + config.options().pathSeparator() +
                    "scoreboards" + config.options().pathSeparator() + key);
            if (display != null) {
                addDisplay(display);
            }
        }
    }
}
