package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemBoolean;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BooleanFlag extends AFlag<Boolean> {

    public BooleanFlag(boolean value, @NotNull String name) {
        setFlag(value);
        setName(name);
        setDefaultFlag(value);
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
            setFlag(config.getBoolean(path + config.options().pathSeparator() + getName()));
        } else {
            setFlag(getDefaultFlag());
        }
    }

    @Override
    public MenuItemBoolean getMenuItem(@Nullable Material displayMaterial, @NotNull MinigameLangKey langKey) {
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
    public MenuItemBoolean getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                       @Nullable List<@NotNull Component> description) {
        return new MenuItemBoolean(displayMat, name, description, new Callback<>() {

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
