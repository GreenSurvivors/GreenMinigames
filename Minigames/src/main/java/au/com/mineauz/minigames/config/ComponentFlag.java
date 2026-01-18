package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.Scalars;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class ComponentFlag extends AFlag<Component> {
    public ComponentFlag(final @NotNull String name, final @Nullable Component defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != getDefaultFlag()) {
            config.node(getName()).set(MiniMessage.miniMessage().serialize(getFlag()));
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) {
        final String confStr = Scalars.STRING.tryDeserialize(config.node(getName()).rawScalar());
        if (confStr != null) {
            setFlag(MiniMessage.miniMessage().deserialize(confStr));
        } else {
            setFlag(getDefaultFlag());
        }
    }

    @Override
    public @NotNull MenuItemComponent getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                                  @Nullable List<@NotNull Component> description) {
        return new MenuItemComponent(displayType, name, description, new Callback<>() {

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
