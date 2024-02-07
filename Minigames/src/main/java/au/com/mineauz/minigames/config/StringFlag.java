package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemString;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StringFlag extends Flag<String> {

    public StringFlag(String value, String name) {
        setFlag(value);
        setDefaultFlag(value);
        setName(name);
    }

    @Override
    public void saveValue(String path, FileConfiguration config) {
        config.set(path + "." + getName(), getFlag());
    }

    @Override
    public void loadValue(String path, FileConfiguration config) {
        if (config.contains(path + "." + getName())) {
            setFlag(config.getString(path + "." + getName()));
        } else {
            setFlag(getDefaultFlag());
        }
    }

    @Override
    public MenuItemString getMenuItem(@Nullable Component name, @Nullable Material displayMat) {
        return getMenuItem(name, displayMat, null);
    }

    @Override
    public MenuItemString getMenuItem(@Nullable Component name, @Nullable Material displayMat,
                                      @NotNull List<@NotNull Component> description) {
        return new MenuItemString(displayMat, name, description, new Callback<>() {

            @Override
            public String getValue() {
                return getFlag();
            }

            @Override
            public void setValue(String value) {
                setFlag(value);
            }
        });
    }
}
