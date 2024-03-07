package au.com.mineauz.minigames.minigame.modules;

import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a custom loadout element.
 * This can be used to add things like disguises
 * or commands.
 */
public abstract class ALoadoutAddon {
    protected final String name;
    protected final PlayerLoadout loadout;

    public ALoadoutAddon(@NotNull String name, PlayerLoadout loadout) {
        this.name = name;
        this.loadout = loadout;
    }

    public @NotNull String getName() {
        return name;
    }

    public abstract void addMenuOptions(@NotNull Menu menu);

    public abstract void save(@NotNull FileConfiguration config, @NotNull String path);

    public abstract void load(@NotNull FileConfiguration config, @NotNull String path);

    public abstract void applyLoadout(@NotNull MinigamePlayer player);

    public abstract void clearLoadout(@NotNull MinigamePlayer player);
}
