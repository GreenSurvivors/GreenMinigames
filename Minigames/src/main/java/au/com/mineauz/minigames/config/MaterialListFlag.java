package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MaterialListFlag extends AFlag<List<Material>> { // todo replace with GENERIC<T> list flag AFlag<List<AFlag<T>>>

    public MaterialListFlag(List<Material> value, @NotNull String name) {
        super(name, new ArrayList<>(), value); // default value - saving tests if the flag is equal to their default
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        if (!getFlag().isEmpty()) {
            char configSeparator = config.options().pathSeparator();
            MaterialFlag matflag;
            for (int i = 0; i < getFlag().size(); i++) {
                matflag = new MaterialFlag(null, getName() + configSeparator + i);
                matflag.setFlag(getFlag().get(i));
                matflag.saveValue(config, path);
            }
        } else {
            config.set(path + config.options().pathSeparator() + getName(), null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();
        List<Material> materials = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection(path + configSeparator + getName());

        if (section != null) {
            Set<String> ids = section.getKeys(false);
            MaterialFlag matFlag;

            for (String id : ids) {
                matFlag = new MaterialFlag(null, getName() + configSeparator + id);
                matFlag.loadValue(config, path);

                materials.add(matFlag.getFlag());
            }
        }
        setFlag(materials);
    }

    @Override
    public @NotNull MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                         @Nullable List<@NotNull Component> description) {
        return null; // todo
    }
}
