package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MaterialListFlag extends AFlag<List<Material>> { // todo replace with GENERIC<T> list flag AFlag<List<AFlag<T>>>

    public MaterialListFlag(List<Material> value, String name) {
        setFlag(value);
        setDefaultFlag(new ArrayList<>()); //saving tests if the flag is equal to their default
        setName(name);
    }

    @Override
    public void saveValue(@NotNull FileConfiguration config, @NotNull String path) {
        if (!getFlag().isEmpty()) {
            MaterialFlag matflag;
            for (int i = 0; i < getFlag().size(); i++) {
                matflag = new MaterialFlag(null, getName() + "." + i);
                matflag.setFlag(getFlag().get(i));
                matflag.saveValue(config, path);
            }
        }
    }

    @Override
    public void loadValue(@NotNull FileConfiguration config, @NotNull String path) {
        List<Material> materials = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection(path + "." + getName());

        if (section != null) {
            Set<String> ids = section.getKeys(false);
            MaterialFlag matFlag;

            for (String id : ids) {
                matFlag = new MaterialFlag(null, getName() + "." + id);
                matFlag.loadValue(config, path);

                materials.add(matFlag.getFlag());
            }
        }
        setFlag(materials);
    }

    @Override
    public MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                @Nullable List<@NotNull Component> description) {
        return null;
    }

}
