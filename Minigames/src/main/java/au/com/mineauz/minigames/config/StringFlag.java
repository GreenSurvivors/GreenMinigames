package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemString;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class StringFlag extends AFlag<String> {

    public StringFlag(final @NotNull String name, final String defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.node(getName()).raw(getFlag());
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) {
        setFlag(config.node(getFlag()).getString(getDefaultFlag()));
    }

    @Override
    public @NotNull MenuItemString getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                               @Nullable List<@NotNull Component> description) {
        return new MenuItemString(displayType, name, description, new Callback<>() {

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
