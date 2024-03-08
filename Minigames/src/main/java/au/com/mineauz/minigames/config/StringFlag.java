package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemString;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StringFlag extends AFlag<String> {

    public StringFlag(String value, @NotNull String name) {
        setFlag(value);
        setDefaultFlag(value);
        setName(name);
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.set(path + config.options().pathSeparator() + getName(), getFlag());
        } else {
            config.set(path + config.options().pathSeparator() + getName(), null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        if (config.contains(path + config.options().pathSeparator() + getName())) {
            setFlag(config.getString(path + config.options().pathSeparator() + getName()));
        } else {
            setFlag(getDefaultFlag());
        }
    }

    @Override
    public MenuItemString getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                      @Nullable List<@NotNull Component> description) {
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
