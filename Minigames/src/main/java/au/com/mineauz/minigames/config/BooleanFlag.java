package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.managers.language.langkeys.LangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.MenuItemBoolean;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BooleanFlag extends Flag<Boolean> {
    public BooleanFlag(boolean value, String name) {
        setFlag(value);
        setName(name);
        setDefaultFlag(value);
    }

    @Override
    public void saveValue(String path, FileConfiguration config) {
        config.set(path + "." + getName(), getFlag());
    }

    @Override
    public void loadValue(String path, FileConfiguration config) {
        if (config.contains(path + "." + getName())) {
            setFlag(config.getBoolean(path + "." + getName()));
        } else {
            setFlag(getDefaultFlag());
        }
    }

    @Override
    public MenuItem getMenuItem(@NotNull LangKey langKey, @Nullable Material displayMaterial) {
        return new MenuItemBoolean(displayMaterial, langKey, new Callback<>() {

            @Override
            public Boolean getValue() {
                return getFlag();
            }

            @Override
            public void setValue(Boolean value) {
                setFlag(value);
            }
        });
    }

    @Override
    public MenuItem getMenuItem(@Nullable Component name, @Nullable Material displayMat,
                                @Nullable List<@NotNull Component> description) {
        return new MenuItemBoolean(name, description, displayMat, new Callback<>() {

            @Override
            public Boolean getValue() {
                return getFlag();
            }

            @Override
            public void setValue(Boolean value) {
                setFlag(value);
            }
        });
    }
}
