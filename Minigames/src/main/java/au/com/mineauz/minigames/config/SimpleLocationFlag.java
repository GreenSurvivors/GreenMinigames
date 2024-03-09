package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.menu.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Like {@link LocationFlag} but does not save pitch nor yaw
 */
public class SimpleLocationFlag extends AFlag<Location> {

    public SimpleLocationFlag(Location value, String name) {
        setFlag(value);
        setDefaultFlag(value);
        setName(name);
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.set(path + configSeparator + getName() + configSeparator + "x", getFlag().getX());
            config.set(path + configSeparator + getName() + configSeparator + "y", getFlag().getY());
            config.set(path + configSeparator + getName() + configSeparator + "z", getFlag().getZ());
            config.set(path + configSeparator + getName() + configSeparator + "world", getFlag().getWorld().getName());
        } else {
            config.set(path + configSeparator + getName(), null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();
        Location result = null;

        if (config.contains(path + configSeparator + getName() + configSeparator + "world")) {
            double x = config.getDouble(path + configSeparator + getName() + configSeparator + "x");
            double y = config.getDouble(path + configSeparator + getName() + configSeparator + "y");
            double z = config.getDouble(path + configSeparator + getName() + configSeparator + "z");
            String worldName = config.getString(path + configSeparator + getName() + configSeparator + "world", "not found!");

            World world = Bukkit.getWorld(worldName);

            if (world != null) {
                result = new Location(world, x, y, z);
            } else {
                Minigames.getCmpnntLogger().warn("Could not simple location flag at '" + path + configSeparator + getName() +
                        "' because World '" + worldName + "' is not a valid name! Throwing error so the config don't get overwritten.");
                throw new RuntimeException("invalid worldName at '" + path + configSeparator + getName() + "'");
            }
        }

        if (result == null) {
            result = getDefaultFlag();
        }

        setFlag(result);
    }

    @Deprecated
    @Override
    public MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name) {
        return getMenuItem(displayMat, name, null);
    }

    @Deprecated
    @Override
    public MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                @Nullable List<@NotNull Component> description) {
        return null;
    }
}
