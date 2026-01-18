package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.MenuItemTime;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class TimeFlag extends AFlag<Long> {

    public TimeFlag(@NotNull String name, Long defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.node(getName()).set(getFlag());
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) {
        setFlag(config.node(getName()).getLong(getDefaultFlag()));
    }

    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable ItemType displayType, @Nullable Component name) {
        return getMenuItem(displayType, name, null);
    }

    public @NotNull MenuItemTime getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                             @Nullable Long min, @Nullable Long max) {
        return getMenuItem(displayType, name, null, min, max);
    }

    public MenuItemTime getMenuItem(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey,
                                    @Nullable Long min, @Nullable Long max) {
        return getMenuItem(displayType, langKey, null, min, max);
    }

    @Deprecated
    @Override
    public @NotNull MenuItemTime getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                             @Nullable List<@NotNull Component> description) {
        return getMenuItem(displayType, name, description, 0L, null);
    }

    public @NotNull MenuItemTime getMenuItem(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey,
                                             @Nullable List<@NotNull Component> description, @Nullable Long min, @Nullable Long max) {
        return getMenuItem(displayType, MinigameMessageManager.getMgMessage(langKey), description, min, max);
    }

    public @NotNull MenuItemTime getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                             @Nullable List<@NotNull Component> description, @Nullable Long min, @Nullable Long max) {
        return new MenuItemTime(displayType, name, description, new Callback<>() {

            @Override
            public Long getValue() {
                return getFlag();
            }

            @Override
            public void setValue(Long value) {
                setFlag(value);
            }

        }, min, max);
    }
}
