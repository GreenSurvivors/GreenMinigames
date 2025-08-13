package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItem;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnumFlag<T extends Enum<T>> extends Flag<T> {

    private final Class<T> enumClass;

    @SuppressWarnings("unchecked")
    public EnumFlag(T value, String name) {
        setFlag(value);
        setDefaultFlag(value);
        setName(name);
        enumClass = (Class<T>) value.getClass();
    }

    @Override
    public void saveValue(String path, FileConfiguration config) {
        config.set(path + "." + getName(), getFlag().name());
    }

    @Override
    public void loadValue(String path, FileConfiguration config) {
        if (config.contains(path + "." + getName())) {final @Nullable String configValue = config.getString(path + "." + getName());

            // because of implementation specifics of FileConfiguration, this should never be null.
            // However, theoretically setting a value explicit as null in config is a different state from not being defined in the config at all.
            if (configValue != null) {
                setFlag(T.valueOf(enumClass, configValue));
            } else {
                setFlag(null);
            }
        } else {
            setFlag(getDefaultFlag());
        }
    }

    @Override
    public MenuItem getMenuItem(String name, Material displayItem) {
        return null;
    }

    @Override
    public MenuItem getMenuItem(String name, Material displayItem,
                                List<String> description) {
        return null;
    }

}
