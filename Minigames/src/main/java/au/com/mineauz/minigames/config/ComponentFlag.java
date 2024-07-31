package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ComponentFlag extends AFlag<Component> {
    public ComponentFlag(@Nullable Component value, @NotNull String name) {
        super(name, value, value);
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        if (getFlag() != getDefaultFlag()) {
            config.set(path + config.options().pathSeparator() + getName(), MiniMessage.miniMessage().serialize(getFlag()));
        } else {
            config.set(path + config.options().pathSeparator() + getName(), null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        final String confStr = config.getString(path + config.options().pathSeparator() + getName());
        if (confStr != null) {
            setFlag(MiniMessage.miniMessage().deserialize(confStr));
        } else {
            setFlag(getDefaultFlag());
        }
    }

    @Override
    public @NotNull MenuItemComponent getMenuItem(@Nullable Material displayMat, @Nullable Component name,
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
