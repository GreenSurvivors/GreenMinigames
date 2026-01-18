package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigames.objects.safelocation.SafeFineLocation;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class RegionFlag extends AFlag<MgRegion> {
    private final @Nullable String legacyFistPointLabel, legacySecondPointLabel;

    public RegionFlag(final @NotNull MgRegion defaultVal,
                      final @Nullable String legacyFirstPoint, final @Nullable String legacySecondPoint) {
        super(defaultVal.getName(), defaultVal);
        this.legacyFistPointLabel = legacyFirstPoint;
        this.legacySecondPointLabel = legacySecondPoint;
    }

    /// the given name HAS TO Match with the name of the MgRegion!
    public RegionFlag(final @NotNull String name, final MgRegion defaultVal,
                      final @Nullable String legacyFirstPoint, final @Nullable String legacySecondPoint) {
        super(name, defaultVal);
        this.legacyFistPointLabel = legacyFirstPoint;
        this.legacySecondPointLabel = legacySecondPoint;
    }

    public RegionFlag(final @NotNull String name, final MgRegion defaultVal) {
        super(name, defaultVal);
        this.legacyFistPointLabel = null;
        this.legacySecondPointLabel = null;
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());
        if (legacyFistPointLabel != null) {
            config.removeChild(legacyFistPointLabel);
        }
        if (legacySecondPointLabel != null) {
            config.removeChild(legacySecondPointLabel);
        }

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.node(getName()).set(getFlag());
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        MgRegion result = null;

        if (legacyFistPointLabel == null || !config.hasChild(legacyFistPointLabel)) {
            setFlag(config.node(getName()).get(TypeToken.get(MgRegion.class)));
        } else { // datafixerupper
            //import legacy regions from before region object existed
            if (legacySecondPointLabel != null) {
                final @NotNull LocationFlag<SafeFineLocation> locFlag1 = new LocationFlag<>(legacyFistPointLabel, null, SafeFineLocation.class);
                final @NotNull LocationFlag<SafeFineLocation> locFlag2 = new LocationFlag<>(legacySecondPointLabel, null, SafeFineLocation.class);

                if (locFlag1.getFlag() != null && locFlag2.getFlag() != null) {
                    result = new MgRegion("legacy" + System.nanoTime(), locFlag1.getFlag(), locFlag2.getFlag());
                }
            }
        }

        if (result == null) {
            result = getDefaultFlag();
        }

        setFlag(result);
    }

    /// the name of the region HAS TO match the name of the node!
    @Override
    public void setFlag (MgRegion region) {
        super.setFlag(region);
    }

    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable ItemType displayType, @Nullable Component name) {
        return getMenuItem(displayType, name, null);
    }

    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                         @Nullable List<@NotNull Component> description) {
        return null; // todo
    }
}
