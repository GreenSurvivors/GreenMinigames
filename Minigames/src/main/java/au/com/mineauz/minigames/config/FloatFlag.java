package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemDecimal;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class FloatFlag extends AFlag<Float> {

    public FloatFlag(final @NotNull String name, final Float defaultVal) {
        super(name, defaultVal);
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
        setFlag(config.node(getName()).getFloat(getDefaultFlag()));
    }

    @Override
    public @NotNull MenuItemDecimal getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                                @Nullable List<@NotNull Component> description) {
        return this.getMenuItem(displayType, name, description, 1d, 1d, 0d, Double.POSITIVE_INFINITY);
    }

    public @NotNull MenuItemDecimal getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                                double lowerinc, double upperinc, @Nullable Double min, @Nullable Double max) {
        return this.getMenuItem(displayType, name, null, lowerinc, upperinc, min, max);
    }

    public @NotNull MenuItemDecimal getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                                @Nullable List<@NotNull Component> description,
                                                double lowerinc, double upperinc, @Nullable Double min, @Nullable Double max) {
        return new MenuItemDecimal(displayType, name, description, new Callback<>() {

            @Override
            public @NotNull Double getValue() {
                return getFlag().doubleValue();
            }

            @Override
            public void setValue(@NotNull Double value) {
                setFlag(value.floatValue());
            }

        }, lowerinc, upperinc, min, max);
    }
}
