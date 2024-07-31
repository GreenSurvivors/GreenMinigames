package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.objects.MgRegion;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegionMapFlag extends AFlag<Map<String, MgRegion>> { // todo move this logic into Minigames or redo this into a List
    // dataFixerUpper
    private final @Nullable String legacyFistPointLabel, legacySecondPointLabel;

    public RegionMapFlag(@NotNull Map<String, MgRegion> value, @NotNull String name, @Nullable String legacyFirstPoint, @Nullable String legacySecondPoint) {
        super(name, new HashMap<>(), value); //default value - saving tests if the flag is equal to their default
        this.legacyFistPointLabel = legacyFirstPoint;
        this.legacySecondPointLabel = legacySecondPoint;
    }

    public RegionMapFlag(@NotNull Map<String, MgRegion> value, @NotNull String name) {
        super(name, new HashMap<>(), value); //default value - saving tests if the flag is equal to their default

        this.legacyFistPointLabel = null;
        this.legacySecondPointLabel = null;
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();

        if (!getFlag().isEmpty()) {
            RegionFlag regionFlag;

            for (MgRegion region : getFlag().values()) {
                regionFlag = new RegionFlag(region, getName() + configSeparator + region.getName());
                regionFlag.saveValue(config, path + configSeparator + region.getName());
            }
        } else {
            config.set(path + configSeparator + getName(), null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();
        Map<String, MgRegion> regions = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection(path + configSeparator + getName());
        if (section != null) {
            Set<String> regionNames = section.getKeys(false);
            RegionFlag regionFlag;

            for (String regionName : regionNames) {
                regionFlag = new RegionFlag(null, getName() + configSeparator + regionName);
                regionFlag.loadValue(config, path + configSeparator + getName() + configSeparator + regionName);
                if (regionFlag.getFlag() != null) {
                    regions.put(regionFlag.getFlag().getName(), regionFlag.getFlag());
                } else {
                    Minigames.getCmpnntLogger().error("Could not add MgRegion into RegionMapFlag because it failed to load. " +
                            "('" + path + configSeparator + getName() + configSeparator + regionName + "') Throwing new exception to not overwriting it!");
                    throw new RuntimeException("invalid MgRegion at '" + path + configSeparator + getName() + configSeparator + regionName + "'");
                }
            }
        }

        //dataFixerUpper - import legacy regions from before regions existed
        if (legacyFistPointLabel != null && legacySecondPointLabel != null) {
            SimpleLocationFlag locFlag1 = new SimpleLocationFlag(null, legacyFistPointLabel);
            SimpleLocationFlag locFlag2 = new SimpleLocationFlag(null, legacySecondPointLabel);

            if (locFlag1.getFlag() != null && locFlag2.getFlag() != null) {
                regions.put("legacy", new MgRegion("legacy", locFlag1.getFlag(), locFlag2.getFlag()));
            }
        }

        if (regions.isEmpty()) {
            regions = getDefaultFlag();
        }

        setFlag(regions);
    }

    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name) {
        return getMenuItem(displayMat, name, null);
    }

    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                         @Nullable List<@NotNull Component> description) {
        return null; // todo
    }
}
