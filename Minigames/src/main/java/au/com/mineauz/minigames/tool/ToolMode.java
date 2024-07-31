package au.com.mineauz.minigames.tool;

import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ToolMode {

    @NotNull String getName();

    @NotNull Component getDisplayName();

    /**
     * Returns the description of the tool mode with lines separated into list elements,
     * so it is compatible with lore of a (menu) item
     */
    @NotNull List<@NotNull Component> getDescription();

    @NotNull Material getIcon();

    void onSetMode(@NotNull MinigamePlayer player, @NotNull MinigameTool tool);

    void onUnsetMode(@NotNull MinigamePlayer mgPlayer, @NotNull MinigameTool tool);

    void onLeftClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team, @NotNull PlayerInteractEvent event);

    void onRightClick(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team, @NotNull PlayerInteractEvent event);

    void select(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team);

    void deselect(@NotNull MinigamePlayer mgPlayer, @NotNull Minigame minigame, @Nullable Team team);
}
