package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.objects.safelocation.ASafeLocation;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public class LocationListFlag<T extends @NotNull ASafeLocation> extends AFlag<List<T>> {// todo replace with GENERIC<T> list flag AFlag<List<AFlag<T>>>
    protected final @NotNull TypeToken<T> typeToken;

    public LocationListFlag(final @NotNull String name, final List<T> value, final @NotNull Class<T> clazz) {
        super(name, new ArrayList<>(), value); // default flag - saving tests if the flag is equal to their default
        typeToken = TypeToken.get(clazz);
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.node(getName()).setList(typeToken, getFlag());
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        setFlag(config.node(getName()).getList(typeToken, getDefaultFlag()));
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
        return null; // todo
    }
}
