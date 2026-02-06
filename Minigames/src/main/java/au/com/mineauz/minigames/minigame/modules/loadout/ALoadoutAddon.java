package au.com.mineauz.minigames.minigame.modules.loadout;

import au.com.mineauz.minigames.menu.Menu;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

/**
 * Represents a custom loadout element.
 * This can be used to add things like disguises
 * or commands.
 */
public abstract class ALoadoutAddon {
    protected final @NotNull Key key;
    protected final @NotNull PlayerLoadout loadout;

    public ALoadoutAddon(final @NotNull Key key, final @NotNull PlayerLoadout loadout) {
        this.key = key;
        this.loadout = loadout;
    }

    public @NotNull Key getKey() {
        return key;
    }

    public abstract void addMenuOptions(@NotNull Menu menu);

    public abstract void save(final @NotNull CommentedConfigurationNode config) throws SerializationException;

    public abstract void load(final @NotNull CommentedConfigurationNode config) throws ConfigurateException;

    public abstract void applyLoadout(@NotNull MinigamePlayer player);

    public abstract void clearLoadout(@NotNull MinigamePlayer player);
}
