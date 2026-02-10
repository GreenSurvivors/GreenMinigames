package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.AMenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockType;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockTypeListFlag extends AFlag<List<@NotNull BlockType>> { // todo replace with GENERIC<T> list flag AFlag<List<AFlag<T>>>

    public BlockTypeListFlag(final @NotNull String name, final List<@NotNull BlockType> value) {
        super(name, new ArrayList<>(), value); // default value - saving tests if the flag is equal to their default
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().isEmpty() && !getFlag().equals(getDefaultFlag())) {
            final @NotNull List<@NotNull String> list = new ArrayList<>();
            for (BlockType blockType : getFlag()) {
                final @NotNull String minimalString = blockType.key().asMinimalString();
                list.add(minimalString);
            }

            config.node(getName()).setList(String.class, list);
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        if (config.hasChild(getName())) {
            final @NotNull CommentedConfigurationNode node = config.node(getName());

            final @NotNull List<@NotNull BlockType> types = new ArrayList<>();

            if (node.isList()) {
                for (final @NotNull String keyStr : node.getList(String.class, ArrayList::new)) {
                    final @Nullable NamespacedKey namespacedKey = NamespacedKey.fromString(keyStr);

                    if (namespacedKey != null) {
                        final @Nullable BlockType blockType = Registry.BLOCK.get(namespacedKey);

                        if (blockType != null) {
                            types.add(blockType);
                        } else {
                            throw new ConfigurateException(node, keyStr + " is not a valid block type.");
                        }
                    } else {
                        throw new ConfigurateException(node, keyStr + " is not a valid block type.");
                    }
                }
            } else { // datafixerupper
                for (final @NotNull Map.Entry<@NotNull Object, @NotNull CommentedConfigurationNode> entry : node.childrenMap().entrySet()) {
                    final int i = Integer.parseInt(entry.getKey().toString());
                    types.add(i, Material.matchMaterial(entry.getValue().getString()).asBlockType());
                }
            }

            setFlag(types);
        } else {
            setFlag(getDefaultFlag());
        }
    }

    @Override
    public @NotNull AMenuItem getMenuItem(final @Nullable ItemType displayType, final @Nullable Component name,
                                          final @Nullable List<@NotNull Component> description) {
        return null; // todo
    }
}
