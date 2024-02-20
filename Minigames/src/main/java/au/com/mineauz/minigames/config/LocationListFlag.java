package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LocationListFlag extends AFlag<List<Location>> {

    public LocationListFlag(List<Location> value, String name) {
        setFlag(value);
        setDefaultFlag(new ArrayList<>()); //saving tests if the flag is equal to their default
        setName(name);
    }

    @Override
    public void saveValue(@NotNull FileConfiguration config, @NotNull String path) {
        if (!getFlag().isEmpty()) {
            LocationFlag locf;
            for (int i = 0; i < getFlag().size(); i++) {
                locf = new LocationFlag(null, getName() + "." + i);
                locf.setFlag(getFlag().get(i));
                locf.saveValue(config, path);
            }
        }
    }

    @Override
    public void loadValue(@NotNull FileConfiguration config, @NotNull String path) {
        List<Location> locs = new ArrayList<>();
        Set<String> ids = config.getConfigurationSection(path + "." + getName()).getKeys(false);
        LocationFlag locf;

        for (int i = 0; i < ids.size(); i++) {
            locf = new LocationFlag(null, getName() + "." + i);
            locf.loadValue(config, path);
            locs.add(locf.getFlag());
        }
        setFlag(locs);
    }

    @Deprecated
    @Override
    public @Nullable MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name) {
        return getMenuItem(displayMat, name, null);
    }

    @Deprecated
    @Override
    public @Nullable MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                          @Nullable List<@NotNull Component> description) {
        return null;
    }
}
