package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.minigame.modules.loadout.PlayerLoadout;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class LoadoutFlag extends AFlag<PlayerLoadout> {

    public LoadoutFlag(final @NotNull String name, final PlayerLoadout value) {
        super(name, value);
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            getFlag().save(config.node(getName()));
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        if (getFlag() != null) {
            getFlag().load(config.node(getName()));
        } else {
            setFlag(getDefaultFlag());
        }
    }

    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable ItemType displayType, @Nullable Component name) {
        return getMenuItem(displayType, name, null);
    }

    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                         @Nullable List<@NotNull Component> description) {
        return null; //TODO: Menu item easy access for loadouts.
    }
}
