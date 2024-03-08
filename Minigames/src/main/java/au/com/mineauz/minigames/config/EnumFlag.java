package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemEnum;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnumFlag<T extends Enum<T>> extends AFlag<T> {
    private final Class<T> enumClass;

    @SuppressWarnings("unchecked")
    public EnumFlag(T value, @NotNull String name) {
        setFlag(value);
        setDefaultFlag(value);
        setName(name);
        enumClass = (Class<T>) value.getClass();
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
        String configStr = config.getString(path + config.options().pathSeparator() + getName());
        boolean notFound = true;

        // case-insensitive loading
        for (T value : enumClass.getEnumConstants()) {
            if (value.name().equalsIgnoreCase(configStr)) {
                notFound = false;
                setFlag(value);
                break;
            }
        }

        if (notFound) {
            setFlag(getDefaultFlag());
        }
    }

    /**
     * @param description will get ignored
     */
    @Override
    public MenuItemEnum<T> getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                       @Nullable List<@NotNull Component> description) {
        return new MenuItemEnum<>(displayMat, name, new Callback<>() {

            @Override
            public T getValue() {
                return getFlag();
            }

            @Override
            public void setValue(T value) {
                setFlag(value);
            }
        }, enumClass);
    }
}
