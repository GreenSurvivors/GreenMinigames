package au.com.mineauz.minigames.minigame.scoreboard;

import au.com.mineauz.minigames.MinigameUtils;
import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.minigame.Minigame;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.HashMap;
import java.util.Map;

// todo figure out configuration node how to get direct children
//  do something about the whole Minigames.getPlugin mess
//  handle unload of worlds in Locations
//  war against Location and World without WeakReference, as well as still ChatColor
//
//  todo somewhere here when updating the signs or smth always put the ROOT into it

public class ScoreboardDisplayManger {
    protected static final @NotNull NamespacedKey DISPLAY_ROOT_KEY = new NamespacedKey(Minigames.getPlugin(), "displayRootKey");
    private final @NotNull Map<@NotNull String, @NotNull ScoreboardDisplay> displays = new HashMap<>();

    public ScoreboardDisplayManger() {
    }

    public @Nullable ScoreboardDisplay getDisplay(final @NotNull Sign sign) {
        return displays.get(sign.getPersistentDataContainer().get(DISPLAY_ROOT_KEY, PersistentDataType.STRING));
    }

    public void addDisplay(final @NotNull ScoreboardDisplay display) {
        displays.put(MinigameUtils.createBlockLocationID(display.getRoot()), display);
    }

    public void removeDisplay(final @NotNull Block block) {
        final @Nullable ScoreboardDisplay display = displays.remove(block);
        if (display != null) {
            display.deleteSigns();
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

    /**
     * Makes each scoreboard update its signs with their current data. This does not update the scoreboard data.
     */
    public void refreshDisplays() {
        for (final @NotNull ScoreboardDisplay display : displays.values()) {
            display.updateSigns();
        }
    }

    public void saveDisplays(final @NotNull CommentedConfigurationNode node) throws SerializationException {
        final @NotNull CommentedConfigurationNode scoreboardsNode = node.node("scoreboards");

        int index = 0;
        for (ScoreboardDisplay display : displays.values()) {
            display.save(scoreboardsNode.node(index++));
        }
    }

    public void loadDisplays(final @NotNull CommentedConfigurationNode config, final @NotNull Minigame minigame) throws SerializationException {
        final @NotNull CommentedConfigurationNode scoreboardsNode = config.node("scoreboards");

        if (scoreboardsNode.virtual() || scoreboardsNode.isNull()) {
            return;
        }

        for (final @NotNull CommentedConfigurationNode node : scoreboardsNode.childrenList()) {
            ScoreboardDisplay display = ScoreboardDisplay.load(minigame, node);
            if (display != null) {
                addDisplay(display);
            }
        }
    }
}
