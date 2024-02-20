package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.PlayerLoadout;
import au.com.mineauz.minigames.menu.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoadoutSetFlag extends AFlag<Map<String, PlayerLoadout>> {
    public LoadoutSetFlag(Map<String, PlayerLoadout> value, String name) {
        setFlag(value);
        setDefaultFlag(null);
        setName(name);
    }

    @Override
    public void saveValue(@NotNull FileConfiguration config, @NotNull String path) {
        LoadoutFlag lf;
        for (String loadout : getFlag().keySet()) {
            lf = new LoadoutFlag(getFlag().get(loadout), loadout);
            lf.saveValue(config, path + "." + getName());
        }
    }

    @Override
    public void loadValue(@NotNull FileConfiguration config, @NotNull String path) {
        Set<String> keys = config.getConfigurationSection(path + "." + getName()).getKeys(false);
        LoadoutFlag lf;
        for (String loadout : keys) {
            lf = new LoadoutFlag(new PlayerLoadout(loadout), loadout);
            if (loadout.equals("default"))
                lf.getFlag().setDeletable(false);
            lf.loadValue(config, path + "." + getName());
            getFlag().put(lf.getName(), lf.getFlag());
        }
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
        return null; //todo
    }
}
