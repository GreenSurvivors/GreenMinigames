package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemEnum;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.Scalars;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class EnumFlag<T extends Enum<T>> extends AFlag<T> {
    private final @NotNull Class<T> enumClass;

    @SuppressWarnings("unchecked")
    public EnumFlag(@NotNull String name, @NotNull T defaultVal) {
        super(name, defaultVal);
        enumClass = (Class<T>) defaultVal.getClass();
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.node(getName()).set(getFlag());
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) {
        final @Nullable T loadedValue = (T) Scalars.ENUM.tryDeserialize(config.node(getName()).rawScalar());

        if (loadedValue == null) {
            setFlag(getDefaultFlag());
        } else {
            setFlag(loadedValue);
        }
    }

    /**
     * @param description will get ignored
     */
    @Override
    public @NotNull MenuItemEnum<T> getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                                @Nullable List<@NotNull Component> description) {
        return new MenuItemEnum<>(displayType, name, new Callback<>() {

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
