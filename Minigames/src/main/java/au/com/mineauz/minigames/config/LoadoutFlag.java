package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.menu.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LoadoutFlag extends AFlag<PlayerLoadout> {

    public LoadoutFlag(PlayerLoadout value, @NotNull String name) {
        setFlag(value);
        setDefaultFlag(null);
        setName(name);
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        if (getFlag() != null) {
            getFlag().save(config, path + config.options().pathSeparator() + getName());
        } else {
            config.set(path + config.options().pathSeparator() + getName(), null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        if (getFlag() != null) {
            getFlag().load(config, path + config.options().pathSeparator() + getName());
        } else {
            setFlag(getDefaultFlag());
        }
    }

    @Deprecated
    @Override
    public @Nullable MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name) {
        return getMenuItem(displayMat, name, null); //TODO: Menu item easy access for loadouts.
    }

    @Deprecated
    @Override
    public @Nullable MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                          @Nullable List<@NotNull Component> description) {
        return null;
    }
}
