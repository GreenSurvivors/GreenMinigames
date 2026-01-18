package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public abstract class AFlag<T> {
    private T value;
    private @NotNull String name;
    private T defaultVal;

    protected AFlag(@NotNull String name, T defaultVal) {
        this.name = name;
        this.value = defaultVal;
        this.defaultVal = defaultVal;
    }

    protected AFlag(@NotNull String name, T defaultVal, T value) {
        this.name = name;
        this.defaultVal = defaultVal;
        this.value = value;
    }

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

    public @NotNull Callback<T> getCallback() {
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

    public abstract void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException;

    public abstract void loadValue(final @NotNull CommentedConfigurationNode config) throws ConfigurateException;

    public @NotNull MenuItem getMenuItem(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey) {
        return getMenuItem(displayType, MinigameMessageManager.getMgMessage(langKey));
    }

    public @NotNull MenuItem getMenuItem(@Nullable ItemType displayType, @Nullable Component name) {
        return getMenuItem(displayType, name, null);
    }

    public @NotNull MenuItem getMenuItem(@Nullable ItemType displayType, @NotNull MinigameLangKey nameLangKey,
                                         @NotNull MinigameLangKey descriptionLangKey) {
        return getMenuItem(displayType, MinigameMessageManager.getMgMessage(nameLangKey),
            MinigameMessageManager.getMgMessageList(descriptionLangKey));
    }

    public abstract @NotNull MenuItem getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                                  @Nullable List<@NotNull Component> description);
}
