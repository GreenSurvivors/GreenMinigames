package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.objects.MgRegion;
import au.com.mineauz.minigames.objects.safelocation.SafeFineLocation;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.ArrayList;
import java.util.List;

public class RegionListFlag extends AFlag<List<MgRegion>> {
    // dataFixerUpper
    private final @Nullable String legacyFistPointLabel, legacySecondPointLabel;

    public RegionListFlag(final @NotNull String name, final @NotNull List<MgRegion> value) {
        this(name, value, null, null);
    }

    public RegionListFlag(final @NotNull String name, final @NotNull List<MgRegion> value,
                          final @Nullable String legacyFirstPoint, final @Nullable String legacySecondPoint) {
        super(name, new ArrayList<>(), value); //default value - saving tests if the flag is equal to their default
        this.legacyFistPointLabel = legacyFirstPoint;
        this.legacySecondPointLabel = legacySecondPoint;
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            RegionFlag regionFlag;

            final @NotNull CommentedConfigurationNode node = config.node(getName());
            for (final @NotNull MgRegion region : getFlag()) {
                regionFlag = new RegionFlag(region.getName(), region);
                regionFlag.saveValue(node);
            }
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) throws ConfigurateException {
        @NotNull List<@NotNull MgRegion> flag = new ArrayList<>();

        for (final @NotNull CommentedConfigurationNode regionNode : config.node(getName()).childrenList()) {
            final @NotNull RegionFlag regionFlag = new RegionFlag(regionNode.key().toString(), null);
            regionFlag.loadValue(regionNode);

            if (regionFlag.getFlag() != null) {
                flag.add(regionFlag.getFlag());
            } else {
                Minigames.getPlugin().getComponentLogger().error("Could not add MgRegion into RegionMapFlag because it failed to load. " +
                    "('" + regionNode.key() + "') Throwing new exception to not overwriting it!");
                throw new ConfigurateException(regionNode, "invalid MgRegion");
            }
        }

        //dataFixerUpper - import legacy regions from before regions existed
        if (legacyFistPointLabel != null && legacySecondPointLabel != null) {
            final @NotNull LocationFlag<SafeFineLocation> locFlag1 = new LocationFlag<>(legacyFistPointLabel, null, SafeFineLocation.class);
            final @NotNull LocationFlag<SafeFineLocation> locFlag2 = new LocationFlag<>(legacySecondPointLabel, null, SafeFineLocation.class);
            locFlag1.loadValue(config);
            locFlag2.loadValue(config);

            if (locFlag1.getFlag() != null && locFlag2.getFlag() != null) {
                flag.add(new MgRegion("legacy", locFlag1.getFlag(), locFlag2.getFlag()));
            }
        }

        if (flag.isEmpty()) {
            flag = getDefaultFlag();
        }

        setFlag(flag);
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
