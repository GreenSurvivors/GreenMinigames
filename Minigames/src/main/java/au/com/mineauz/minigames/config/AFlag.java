package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AFlag<T> {
    private T value;
    private String name;
    private T defaultVal;

    public T getFlag() {
        return value;
    }

    public void setFlag(T value) {
        this.value = value;
    }

    public @NotNull String getName() {
        return name;
    }

    protected void setName(@NotNull String name) {
        this.name = name;
    }

    public T getDefaultFlag() {
        return defaultVal;
    }

    protected void setDefaultFlag(T value) {
        defaultVal = value;
    }

    public T getFlagOrDefault() {
        if (value == null) {
            return getDefaultFlag();
        } else {
            return getFlag();
        }
    }

    public Callback<T> getCallback() {
        return new Callback<>() {

            @Override
            public T getValue() {
                return getFlag();
            }

            @Override
            public void setValue(T value) {
                setFlag(value);
            }
        };
    }

    public abstract void saveValue(@NotNull Configuration config, @NotNull String path);

    public abstract void loadValue(@NotNull Configuration config, @NotNull String path);

    public MenuItem getMenuItem(@Nullable Material displayMaterial, @NotNull MinigameLangKey langKey) {
        return getMenuItem(displayMaterial, MinigameMessageManager.getMgMessage(langKey));
    }

    public MenuItem getMenuItem(@Nullable Material displayMaterial, @Nullable Component name) {
        return getMenuItem(displayMaterial, name, null);
    }

    public MenuItem getMenuItem(@Nullable Material displayMat, @NotNull MinigameLangKey nameLangKey,
                                @NotNull MinigameLangKey descriptionLangKey) {
        return getMenuItem(displayMat, MinigameMessageManager.getMgMessage(nameLangKey),
                MinigameMessageManager.getMgMessageList(descriptionLangKey));
    }

    public abstract MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                         @Nullable List<@NotNull Component> description);
}
