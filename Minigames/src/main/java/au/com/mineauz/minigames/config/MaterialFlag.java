package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemMaterial;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MaterialFlag extends AFlag<Material> {

    public MaterialFlag(Material mat, @NotNull String name) {
        super(name, mat, mat);
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.set(path + config.options().pathSeparator() + getName(), getFlag().name());
        } else {
            config.set(path + config.options().pathSeparator() + getName(), null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        if (config.contains(path + config.options().pathSeparator() + getName())) {
            Material flag = Material.matchMaterial(config.getString(path + config.options().pathSeparator() + getName(), ""));
            if (flag == null) {
                flag = getDefaultFlag();
                Minigames.getCmpnntLogger().warn("Failed to load Material from config at :" + path + config.options().pathSeparator() + getName() +
                        " Value: " + config.getString(path + config.options().pathSeparator() + getName()));
            }
            setFlag(flag);
        } else {
            setFlag(getDefaultFlag());
        }
    }

    public @NotNull MenuItemMaterial getMenuItem(@Nullable Component name) {
        return getMenuItem(getFlag(), name, null);
    }

    public @NotNull MenuItemMaterial getMenuItem(@Nullable Component name, @Nullable List<@NotNull Component> description) {
        return getMenuItem(getFlag(), name, description);
    }

    @Deprecated
    @Override
    public @NotNull MenuItemMaterial getMenuItem(@Nullable Material displayMat, @Nullable Component name) {
        return getMenuItem(displayMat, name, null);
    }

    @Deprecated
    @Override
    public @NotNull MenuItemMaterial getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                        @Nullable List<@NotNull Component> description) {
        return new MenuItemMaterial(displayMat, name, description, new Callback<>() {
            @Override
            public Material getValue() {
                return getFlag();
            }

            @Override
            public void setValue(Material value) {
                setFlag(value);
            }
        });
    }
}
