package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemBoolean;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class BooleanFlag extends AFlag<Boolean> {

    public BooleanFlag(final @NotNull String name, final Boolean defaultVal) {
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
        setFlag(config.node(getName()).getBoolean(getDefaultFlag()));
    }

    @Override
    public @NotNull MenuItemBoolean getMenuItem(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey) {
        return new MenuItemBoolean(displayType, langKey, new Callback<>() {

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
    public @NotNull MenuItemBoolean getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                                @Nullable List<@NotNull Component> description) {
        return new MenuItemBoolean(displayType, name, description, new Callback<>() {

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
