package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ComponentFlag extends AFlag<Component> {
    public ComponentFlag(Component value, String name) {
        setFlag(value);
        setDefaultFlag(value);
        setName(name);
    }

    @Override
    public void saveValue(@NotNull FileConfiguration config, @NotNull String path) {
        config.set(path + "." + getName(), MiniMessage.miniMessage().serialize(getFlag()));
    }

    @Override
    public void loadValue(@NotNull FileConfiguration config, @NotNull String path) {
        final String confStr = config.getString(path + "." + getName());
        if (confStr != null) {
            setFlag(MiniMessage.miniMessage().deserialize(confStr));
        } else {
            setFlag(getDefaultFlag());
        }
    }

    @Override
    public MenuItemComponent getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                         @Nullable List<@NotNull Component> description) {
        return new MenuItemComponent(displayMat, name, description, new Callback<>() {

            @Override
            public Component getValue() {
                return getFlag();
            }

            @Override
            public void setValue(Component value) {
                setFlag(value);
            }
        });
    }
}