package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItemDisplayRewards;
import au.com.mineauz.minigames.minigame.reward.Rewards;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RewardsFlag extends AFlag<Rewards> {

    public RewardsFlag(Rewards value, @NotNull String name) {
        super(name, value, value);
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        if (!getFlag().isEmpty()) {
            getFlag().save(config, path + config.options().pathSeparator() + getName());
        } else {
            config.set(path + config.options().pathSeparator() + getName(), null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        getFlag().load(config, path + config.options().pathSeparator() + getName());
    }

    @Override
    public @NotNull MenuItemDisplayRewards getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                              @Nullable List<@NotNull Component> description) {
        return new MenuItemDisplayRewards(displayMat, name, description, getFlag());
    }
}
