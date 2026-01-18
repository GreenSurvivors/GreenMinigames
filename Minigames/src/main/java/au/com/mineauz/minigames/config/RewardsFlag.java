package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItemDisplayRewards;
import au.com.mineauz.minigames.minigame.reward.Rewards;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class RewardsFlag extends AFlag<Rewards> {

    public RewardsFlag(final @NotNull String name, final Rewards defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (!getFlag().isEmpty()) {
            getFlag().save(config.node(getName()));
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        getFlag().load(config.node(getName()));
    }

    @Override
    public @NotNull MenuItemDisplayRewards getMenuItem(final @Nullable ItemType displayType, final @Nullable Component name,
                                                       @Nullable List<@NotNull Component> description) {
        return new MenuItemDisplayRewards(displayType, name, description, getFlag());
    }
}
