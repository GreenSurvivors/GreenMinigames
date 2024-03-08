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

public class LocationFlag extends AFlag<Location> {

    public LocationFlag(Location value, String name) {
        setFlag(value);
        setDefaultFlag(value);
        setName(name);
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        if (!getFlag().equals(getDefaultFlag())) {
            config.set(path + config.options().pathSeparator() + getName(), getFlag());
        } else {
            config.set(path + config.options().pathSeparator() + getName(), null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();
        Location result = config.getLocation(path + configSeparator + getName());

        if (result == null && config.contains(path + configSeparator + getName() + configSeparator + "world")) { // dataFixerUpper
            double x = config.getDouble(path + configSeparator + getName() + configSeparator + "x");
            double y = config.getDouble(path + configSeparator + getName() + configSeparator + "y");
            double z = config.getDouble(path + configSeparator + getName() + configSeparator + "z");
            float yaw = ((Double) config.getDouble(path + configSeparator + getName() + configSeparator + "yaw")).floatValue();
            float pitch = ((Double) config.getDouble(path + configSeparator + getName() + configSeparator + "pitch")).floatValue();
            String worldName = config.getString(path + configSeparator + getName() + configSeparator + "world", "not found!");
            World world = Bukkit.getWorld(worldName);

            if (world != null) {
                result = new Location(world, x, y, z, yaw, pitch);
            } else {
                Minigames.getCmpnntLogger().warn("Could not load legacy location flag at '" + path + configSeparator + getName() +
                        "' because World '" + worldName + "' is not a valid name!");
            }
        }

        if (result == null) {
            result = getDefaultFlag();
        }

        setFlag(result);
    }

    @Deprecated
    @Override
    public @Nullable MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name) {
        return getMenuItem(displayMat, name, null);
    }

    @Deprecated
    @Override
    public @Nullable MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                          @Nullable List<@Nullable Component> description) {
        return null;
    }
}
