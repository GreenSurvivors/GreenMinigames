package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.Minigames;
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
        super(name, value, value);
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

        final TeamColor color = TeamColor.matchColor(getName());
        if (color != null) {
            Team team = new Team(color, mgm);
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
        } else {
            Minigames.getCmpnntLogger().error("Could not load TeamColor '" + getName() + "' of path '" + path + configSeparator + getName() + "'. " +
                    "Throwing Exception to not overwriting the config!");
            throw new RuntimeException("Invalid TeamColor '" + getName() + "' of path '" + path + configSeparator + getName() + "'");
        }
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
        return null; //TODO: Menu Item
    }
}
