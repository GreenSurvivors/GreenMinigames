package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.objects.MgRegion;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RegionFlag extends AFlag<MgRegion> {
    private final @Nullable String legacyFistPointLabel, legacySecondPointLabel;

    public RegionFlag(MgRegion value, @NotNull String name,
                      @Nullable String legacyFirstPoint, @Nullable String legacySecondPoint) {
        this.legacyFistPointLabel = legacyFirstPoint;
        this.legacySecondPointLabel = legacySecondPoint;

        setFlag(value);
        setDefaultFlag(value);
        setName(name);
    }

    public RegionFlag(MgRegion value, @NotNull String name) {
        this.legacyFistPointLabel = null;
        this.legacySecondPointLabel = null;

        setFlag(value);
        setDefaultFlag(value);
        setName(name);
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();

        if (getFlag() != null) {
            config.set(path + configSeparator + "name", getFlag().getName());
            config.set(path + configSeparator + "world", getFlag().getWorld().getName());
            config.set(path + configSeparator + "pos1", getFlag().getPos1().x() + ":" + getFlag().getPos1().y() + ":" + getFlag().getPos1().z());
            config.set(path + configSeparator + "pos2", getFlag().getPos2().x() + ":" + getFlag().getPos2().y() + ":" + getFlag().getPos2().z());
        } else {
            config.set(path, null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();
        String name = config.getString(path + configSeparator + "name");
        MgRegion result = null;

        if (name != null) {
            String worldName = config.getString(path + configSeparator + "world", "not found!");

            String[] slitPos1 = config.getString(path + configSeparator + "pos1").split(":");
            String[] slitPos2 = config.getString(path + configSeparator + "pos2").split(":");

            double x1 = Double.parseDouble(slitPos1[0]);
            double y1 = Double.parseDouble(slitPos1[1]);
            double z1 = Double.parseDouble(slitPos1[2]);

            double x2 = Double.parseDouble(slitPos2[0]);
            double y2 = Double.parseDouble(slitPos2[1]);
            double z2 = Double.parseDouble(slitPos2[2]);

            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                result = new MgRegion(world, name, Position.fine(x1, y1, z1), Position.fine(x2, y2, z2));
            } else {
                Minigames.getCmpnntLogger().warn("Could not load Region because world '" + worldName + "' was invalid. " +
                        "Throwing exception so the config don't get overwritten.");
                throw new RuntimeException("invalid worldName at '" + path + configSeparator + getName() + "'");
            }
        } else {
            //import legacy regions from before regions existed
            if (legacyFistPointLabel != null && legacySecondPointLabel != null) {
                SimpleLocationFlag locFlag1 = new SimpleLocationFlag(null, legacyFistPointLabel);
                SimpleLocationFlag locFlag2 = new SimpleLocationFlag(null, legacySecondPointLabel);

                if (locFlag1.getFlag() != null && locFlag2.getFlag() != null) {
                    result = new MgRegion("legacy", locFlag1.getFlag(), locFlag2.getFlag());
                }
            }
        }

        if (result == null) {
            result = getDefaultFlag();
        }

        setFlag(result);
    }

    @Deprecated
    @Override
    public @Nullable MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name) {
        return null;
    }

    @Deprecated
    @Override
    public @Nullable MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                          @Nullable List<@NotNull Component> description) {
        return null;
    }
}
