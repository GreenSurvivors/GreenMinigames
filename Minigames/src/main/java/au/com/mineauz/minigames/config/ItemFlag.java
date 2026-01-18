package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemItemNbt;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Map;

public class ItemFlag extends AFlag<ItemStack> {

    public ItemFlag(@NotNull String name, ItemStack defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.node(getName()).set(getFlag().serializeAsBytes());
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        if (config.hasChild(getName())) {

            final ItemStack item;
            if (config.isMap()) {
                // datafixerupper
                item = ItemStack.deserialize((Map<String, Object>) config.get(TypeFactory.parameterizedClass(Map.class, String.class, Object.class)));
            } else {
                item = ItemStack.deserializeBytes(config.get(TypeToken.get(byte[].class)));
            }

            setFlag(item);
        } else {
            setFlag(getDefaultFlag());
        }
    }

    public @NotNull MenuItemItemNbt getMenuItem(@NotNull Component name) {
        return new MenuItemItemNbt(getFlagOrDefault(), name, new Callback<>() {
            @Override
            public ItemStack getValue() {
                return getFlag();
            }

            @Override
            public void setValue(ItemStack value) {
                setFlag(value);
            }
        });
    }

    @Override
    public @NotNull MenuItemItemNbt getMenuItem(@Nullable ItemType displayType, @Nullable Component name) {
        return getMenuItem(displayType, name, null);
    }

    @Override
    public @NotNull MenuItemItemNbt getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                                @Nullable List<@NotNull Component> description) {
        return new MenuItemItemNbt(displayType, name, description, new Callback<>() {
            @Override
            public ItemStack getValue() {
                return getFlag();
            }

            @Override
            public void setValue(ItemStack value) {
                setFlag(value);
            }
        });
    }
}
