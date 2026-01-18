package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.objects.safelocation.ASafeLocation;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class LocationFlag<T extends ASafeLocation> extends AFlag<T> {
    protected final @NotNull TypeToken<T> typeToken;

    public LocationFlag(@NotNull String name, T defaultVal, @NotNull Class<T> clazz) {
        super(name, defaultVal);

        typeToken = TypeToken.get(clazz);
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.node(getName()).set(getFlag());
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        setFlag(config.node(getName()).get(typeToken, getDefaultFlag()));

        if (getFlag() != null && getFlag().getWorld() == null) {
            Minigames.getPlugin().getComponentLogger().info("loaded location config flag at " + config.path() + " but the world was unloaded.");
        }

        // todo check if the above is backwards compatible
//        if (result == null && config.contains(path + configSeparator + getName() + configSeparator + "world")) {
//            double x = config.getDouble(path + configSeparator + getName() + configSeparator + "x");
//            double y = config.getDouble(path + configSeparator + getName() + configSeparator + "y");
//            double z = config.getDouble(path + configSeparator + getName() + configSeparator + "z");
//            float yaw = ((Double) config.getDouble(path + configSeparator + getName() + configSeparator + "yaw")).floatValue();
//            float pitch = ((Double) config.getDouble(path + configSeparator + getName() + configSeparator + "pitch")).floatValue();
//            String worldName = config.getString(path + configSeparator + getName() + configSeparator + "world", "not found!");
//            World world = Bukkit.getWorld(worldName);
//
//            if (world != null) {
//                result = new Location(world, x, y, z, yaw, pitch);
//            } else {
//                Minigames.getPlugin().getComponentLogger().warn("Could not load legacy location flag at '" + path + configSeparator + getName() +
//                        "' because World '" + worldName + "' is not a valid name! Throwing exception so the config don't get overwritten.");
//                throw new RuntimeException("invalid worldName at '" + path + configSeparator + getName() + "'");
//            }
//        }
//
//        if (result == null) {
//            result = getDefaultFlag();
//        }
//
//        setFlag(result);
    }

    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable ItemType displayType, @Nullable Component name) {
        return getMenuItem(displayType, name, null);
    }

    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                         @Nullable List<@Nullable Component> description) {
        return null; // todo
    }
}
