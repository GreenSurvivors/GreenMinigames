package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemTime;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class TimeFlag extends AFlag<Long> {

    public TimeFlag(final @NotNull String name, final Long defaultVal) {
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
    public @NotNull AMenuItem getMenuItem(final @Nullable ItemType displayType, final @Nullable Component name) {
        return getMenuItem(displayType, name, null);
    }

    public @NotNull MenuItemTime getMenuItem(final @Nullable ItemType displayType, final @Nullable Component name,
                                             final @Nullable Long min, final @Nullable Long max) {
        return getMenuItem(displayType, name, null, min, max);
    }

    public MenuItemTime getMenuItem(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                                    final @Nullable Long min, final @Nullable Long max) {
        return getMenuItem(displayType, langKey, null, min, max);
    }

    @Deprecated
    @Override
    public @NotNull MenuItemTime getMenuItem(final @Nullable ItemType displayType, final @Nullable Component name,
                                             final @Nullable List<@NotNull Component> description) {
        return getMenuItem(displayType, name, description, 0L, null);
    }

    public @NotNull MenuItemTime getMenuItem(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey,
                                             final @Nullable List<@NotNull Component> description, final @Nullable Long min, final @Nullable Long max) {
        return getMenuItem(displayType, MessageManager.getMessage(langKey), description, min, max);
    }

    public @NotNull MenuItemTime getMenuItem(final @Nullable ItemType displayType, final @Nullable Component name,
                                             final @Nullable List<@NotNull Component> description, final @Nullable Long min, final @Nullable Long max) {
        return new MenuItemTime(displayType, name, description, new Callback<>() {

            @Override
            public Long getValue() {
                return getFlag();
            }

            @Override
            public void setValue(final Long value) {
                setFlag(value);
            }

        }, min, max);
    }
}
