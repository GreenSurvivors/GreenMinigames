package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemBlockType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;
import java.util.Locale;

public class BlockTypeFlag extends AFlag<BlockType> {

    public BlockTypeFlag(final @NotNull String name, BlockType defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.node(getName()).set(getFlag().key().asMinimalString());
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        final @NotNull CommentedConfigurationNode node = config.node(getName());
        if (!node.virtual() && !node.isNull()) {
            final @Nullable String keyStr = node.getString();

            if (keyStr != null) {
                final @Nullable Key blockKey = NamespacedKey.fromString(keyStr.toLowerCase(Locale.ROOT));

                if (blockKey != null) {
                    final @Nullable BlockType flag = Registry.BLOCK.get(blockKey);

                    if (flag != null) {
                        setFlag(flag);
                        return;
                    }
                }

                throw new ConfigurateException(node, keyStr + " is not a valid block type.");
            }
        }

        setFlag(getDefaultFlag());
    }

    public @NotNull MenuItemBlockType getMenuItem(@Nullable Component name) {
        return getMenuItem(getFlag().getItemType(), name, null);
    }

    public @NotNull MenuItemBlockType getMenuItem(@Nullable Component name, @Nullable List<@NotNull Component> description) {
        return getMenuItem(getFlag().getItemType(), name, description);
    }

    @Deprecated
    @Override
    public @NotNull MenuItemBlockType getMenuItem(@Nullable ItemType displayType, @Nullable Component name) {
        return getMenuItem(displayType, name, null);
    }

    @Deprecated
    @Override
    public @NotNull MenuItemBlockType getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                                  @Nullable List<@NotNull Component> description) {
        return new MenuItemBlockType(displayType, name, description, new Callback<>() {
            @Override
            public BlockType getValue() {
                return getFlag();
            }

            @Override
            public void setValue(BlockType value) {
                setFlag(value);
            }
        });
    }
}
