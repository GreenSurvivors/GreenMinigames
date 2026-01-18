package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.minigame.Team;
import au.com.mineauz.minigames.minigame.TeamColor;
import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class TeamFlag extends AFlag<Team> {
    private final @NotNull Minigame mgm;

    @ApiStatus.Obsolete
    public TeamFlag(final @NotNull String colorName, final Team defaultVal, final @NotNull Minigame mgm) throws IllegalArgumentException {
        super(colorName, defaultVal);
        this.mgm = mgm;

        // todo this seams just bad design to me
        // sanityCheck
        Preconditions.checkArgument(TeamColor.matchColor(getName()) != null, "the name of the flag has to be the team color!");
    }

    public TeamFlag(final @NotNull TeamColor color, final Team value, final @NotNull Minigame mgm) {
        this(color.name(), value, mgm);
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            getFlag().save(config.node(getName()));
        }
    }

    @Override
    public void loadValue(@NotNull CommentedConfigurationNode config) throws SerializationException {
        if (config.hasChild(getName())) {
            final TeamColor color = TeamColor.matchColor(getName());
            @SuppressWarnings("DataFlowIssue") // null can't happen with our constructor
            final @NotNull Team team = new Team(color, mgm);
            team.load(config.node(getName()));
            setFlag(team);
        } else {
            setFlag(getDefaultFlag());
        }
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
        return null; //TODO: Menu Item
    }
}
