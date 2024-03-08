package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.TeamColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class TeamFlag extends AFlag<Team> {
    private final @NotNull Minigame mgm;

    public TeamFlag(Team value, @NotNull String name, @NotNull Minigame mgm) {
        setFlag(value);
        setDefaultFlag(value);
        setName(name);
        this.mgm = mgm;
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();

        config.set(path + configSeparator + getName() + configSeparator + "displayName", getFlag().getDisplayName());
        if (!getFlag().getStartLocations().isEmpty()) {
            for (int i = 0; i < getFlag().getStartLocations().size(); i++) {
                LocationFlag locf = new LocationFlag(null, "startpos" + configSeparator + i);
                locf.setFlag(getFlag().getStartLocations().get(i));
                locf.saveValue(config, path + configSeparator + getName());
            }
        }

        getFlag().save(config, path + configSeparator + getName());
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        char configSeparator = config.options().pathSeparator();

        Team team = new Team(TeamColor.matchColor(getName()), mgm);
        team.setDisplayName(config.getString(path + configSeparator + getName() + configSeparator + "displayName"));
        if (config.contains(path + configSeparator + getName() + configSeparator + "startpos")) {
            Set<String> locations = config.getConfigurationSection(path + configSeparator + getName() + configSeparator + "startpos").getKeys(false);
            for (String loc : locations) {
                LocationFlag locf = new LocationFlag(null, "startpos" + configSeparator + loc);
                locf.loadValue(config, path + configSeparator + getName());
                team.addStartLocation(locf.getFlag());
            }
        }

        team.load(config, path + configSeparator + getName());
        setFlag(team);
    }

    @Deprecated
    @Override
    public @Nullable MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name) {
        return null; //TODO: Menu Item
    }

    @Deprecated
    @Override
    public @Nullable MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                          @Nullable List<@NotNull Component> description) {
        return null;
    }
}
