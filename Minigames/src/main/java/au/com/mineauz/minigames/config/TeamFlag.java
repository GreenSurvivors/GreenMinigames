package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.TeamColor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class TeamFlag extends AFlag<Team> {
    private final Minigame mgm;

    public TeamFlag(Team value, String name, Minigame mgm) {
        setFlag(value);
        setDefaultFlag(value);
        setName(name);
        this.mgm = mgm;
    }

    @Override
    public void saveValue(@NotNull FileConfiguration config, @NotNull String path) {
        config.set(path + "." + getName() + ".displayName", getFlag().getDisplayName());
        if (!getFlag().getStartLocations().isEmpty()) {
            for (int i = 0; i < getFlag().getStartLocations().size(); i++) {
                LocationFlag locf = new LocationFlag(null, "startpos." + i);
                locf.setFlag(getFlag().getStartLocations().get(i));
                locf.saveValue(config, path + "." + getName());
            }
        }

        getFlag().save(config, path + "." + getName());
    }

    @Override
    public void loadValue(@NotNull FileConfiguration config, @NotNull String path) {
        Team team = new Team(TeamColor.valueOf(getName()), mgm);
        team.setDisplayName(config.getString(path + "." + getName() + ".displayName"));
        if (config.contains(path + "." + getName() + ".startpos")) {
            Set<String> locations = config.getConfigurationSection(path + "." + getName() + ".startpos").getKeys(false);
            for (String loc : locations) {
                LocationFlag locf = new LocationFlag(null, "startpos." + loc);
                locf.loadValue(config, path + "." + getName());
                team.addStartLocation(locf.getFlag());
            }
        }

        team.load(config, path + "." + getName());
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
