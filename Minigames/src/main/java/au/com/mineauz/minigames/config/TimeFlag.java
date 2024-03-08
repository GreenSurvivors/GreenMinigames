package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemTime;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TimeFlag extends AFlag<Long> {

    public TimeFlag(Long value, @NotNull String name) {
        setFlag(value);
        setDefaultFlag(value);
        setName(name);
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.set(path + config.options().pathSeparator() + getName(), getFlag());
        } else {
            config.set(path + config.options().pathSeparator() + getName(), null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        setFlag(config.getLong(path + config.options().pathSeparator() + getName(), getDefaultFlag()));
    }

    @Deprecated
    @Override
    public MenuItemTime getMenuItem(@Nullable Material displayMat, @Nullable Component name) {
        return getMenuItem(displayMat, name, null);
    }

    public MenuItemTime getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                    @Nullable Long min, @Nullable Long max) {
        return getMenuItem(displayMat, name, null, min, max);
    }

    public MenuItemTime getMenuItem(@Nullable Material displayMat, @NotNull MinigameLangKey langKey,
                                    @Nullable Long min, @Nullable Long max) {
        return getMenuItem(displayMat, langKey, null, min, max);
    }

    @Deprecated
    @Override
    public MenuItemTime getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                    @Nullable List<@NotNull Component> description) {
        return getMenuItem(displayMat, name, description, 0L, null);
    }

    public MenuItemTime getMenuItem(@Nullable Material displayMat, @NotNull MinigameLangKey langKey,
                                    @Nullable List<@NotNull Component> description, @Nullable Long min, @Nullable Long max) {
        return getMenuItem(displayMat, MinigameMessageManager.getMgMessage(langKey), description, min, max);
    }

    public MenuItemTime getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                    @Nullable List<@NotNull Component> description, @Nullable Long min, @Nullable Long max) {
        return new MenuItemTime(displayMat, name, description, new Callback<>() {

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
