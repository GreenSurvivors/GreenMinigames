package au.com.mineauz.minigames.backend.sqlite;

import au.com.mineauz.minigames.backend.BackendImportCallback;
import au.com.mineauz.minigames.backend.Notifier;
import au.com.mineauz.minigames.config.MinigameSave;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class FlatFileExporter {
    private final @NotNull MinigameSave save;
    private final @NotNull Notifier notifier;
    private final @NotNull BackendImportCallback callback;
    private final @NotNull Map<@NotNull String, @NotNull Integer> minigameIds = new HashMap<>();
    private SetMultimap<String, UUID> completions;
    private int nextMinigameId;

    private String notifyState;
    private int notifyCount;
    private long notifyTime;

    public FlatFileExporter(final @NotNull Path subPath, @NotNull BackendImportCallback callback, @NotNull Notifier notifier) {
        this.callback = callback;
        this.notifier = notifier;
        this.save = MinigameSave.forGlobalData(subPath);
    }

    public boolean doExport() {
        try {
            callback.begin();
            loadCompletions();

            exportPlayers();
            exportMinigames();
            exportStats();

            notifyNext("Done");

            callback.end();
            notifier.onComplete();

            return true;
        } catch (final @NotNull IOException | IllegalArgumentException e) {
            notifier.onError(e, notifyState, notifyCount);
            return false;
        }
    }

    private void loadCompletions() throws IOException, IllegalArgumentException {
        completions = HashMultimap.create();

        final @NotNull CommentedConfigurationNode root = save.getConfigRoot();

        for (final @NotNull Map.Entry<@NotNull Object, @NotNull CommentedConfigurationNode> entry : root.childrenMap().entrySet()) {
            final @Nullable List<@NotNull String> rawIds = entry.getValue().getList(String.class);

            if (rawIds != null) {
                for (final @NotNull String rawPlayerId : rawIds) {
                    final @NotNull UUID playerId = UUID.fromString(rawPlayerId.replace('_', '-'));
                    final String minigameName = entry.getKey().toString();
                    completions.put(minigameName, playerId);
                }
            }
        }
    }

    private void exportPlayers() {
        notifyNext("Exporting players...");
        Set<UUID> uniquePlayers = new HashSet<>(completions.values());

        for (UUID playerId : uniquePlayers) {
            // Attempt to get information about this player
            OfflinePlayer player = Bukkit.getPlayer(playerId);
            if (player != null) {
                callback.acceptPlayer(playerId, player.getName(), player.getPlayer() != null ? player.getPlayer().displayName() : Component.text(player.getName()));
            } else {
                callback.acceptPlayer(playerId, "Unknown", Component.text("Unknown"));
            }

            ++notifyCount;
            notifyProgress();
        }
    }

    private void exportMinigames() {
        notifyNext("Exporting minigames...");

        for (final @NotNull String minigame : completions.keySet()) {
            int id = nextMinigameId++;
            minigameIds.put(minigame, id);

            callback.acceptMinigame(id, minigame);

            ++notifyCount;
            notifyProgress();
        }
    }

    private void exportStats() {
        notifyNext("Exporting stats...");

        for (final @NotNull String minigame : completions.keySet()) {
            int id = minigameIds.get(minigame);

            for (UUID playerId : completions.get(minigame)) {
                callback.acceptStat(playerId, id, "wins", 1);
                callback.acceptStat(playerId, id, "attempts", 1);

                ++notifyCount;
                notifyProgress();
            }
        }
    }

    private void notifyProgress() {
        if (System.currentTimeMillis() - notifyTime >= 2000) {
            notifier.onProgress(notifyState, notifyCount);
            notifyTime = System.currentTimeMillis();
        }
    }

    private void notifyNext(@NotNull String state) {
        if (notifyCount != 0) {
            notifier.onProgress(notifyState, notifyCount);
        }

        notifyTime = System.currentTimeMillis();
        notifyCount = 0;
        notifyState = state;

        notifier.onProgress(state, 0);
    }
}
