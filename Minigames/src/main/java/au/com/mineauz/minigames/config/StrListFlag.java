package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.AMenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public class StrListFlag extends AFlag<List<String>> { // todo replace with GENERIC<T> list flag AFlag<List<AFlag<T>>>

    public StrListFlag(final @NotNull String name, final List<String> value) {
        super(name, new ArrayList<>(), value); // default value - saving tests if the flag is equal to their default
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.node(getName()).setList(String.class, getFlag());
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        @Nullable List<String> result = config.node(getName()).getList(String.class);

        if (result == null || result.isEmpty()) {
            result = getDefaultFlag();
        }

        setFlag(result);
    }

    @Deprecated
    @Override
    public @NotNull AMenuItem getMenuItem(final @Nullable ItemType displayType, final @Nullable Component name) {
        return getMenuItem(displayType, name, null);
    }

    @Deprecated
    @Override
    public @NotNull AMenuItem getMenuItem(final @Nullable ItemType displayType, final @Nullable Component name,
                                          final @Nullable List<@NotNull Component> description) {
        return null; //todo
    }
}
