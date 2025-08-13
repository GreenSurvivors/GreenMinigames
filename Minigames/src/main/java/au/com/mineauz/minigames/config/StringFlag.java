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

    public StringFlag(@NotNull String name, String value) {
        super(name, value);
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
        setFlag(config.getString(path + config.options().pathSeparator() + getName(), getDefaultFlag()));
    }

    @Override
    public @NotNull MenuItemString getMenuItem(@Nullable Material displayMat, @Nullable Component name,
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
