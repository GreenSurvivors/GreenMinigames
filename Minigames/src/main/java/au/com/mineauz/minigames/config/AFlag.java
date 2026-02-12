package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.menu.Callback;
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

    protected AFlag(final @NotNull String name, final T defaultVal) {
        this.name = name;
        this.value = defaultVal;
        this.defaultVal = defaultVal;
    }

    protected AFlag(final @NotNull String name, final T defaultVal, final T value) {
        this.name = name;
        this.defaultVal = defaultVal;
        this.value = value;
    }

    public T getFlag() {
        return value;
    }

    public void setFlag(final T value) {
        this.value = value;
    }

    public @NotNull String getName() {
        return name;
    }

    protected void setName(final @NotNull String name) {
        this.name = name;
    }

    public T getDefaultFlag() {
        return defaultVal;
    }

    protected void setDefaultFlag(final T value) {
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
            public void setValue(final T value) {
                setFlag(value);
            }
        };
    }

    public abstract void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException;

    public abstract void loadValue(final @NotNull CommentedConfigurationNode config) throws ConfigurateException;

    public @NotNull AMenuItem getMenuItem(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey) {
        return getMenuItem(displayType, MessageManager.getMessage(langKey));
    }

    public @NotNull AMenuItem getMenuItem(final @Nullable ItemType displayType, final @Nullable Component name) {
        return getMenuItem(displayType, name, null);
    }

    public @NotNull AMenuItem getMenuItem(final @Nullable ItemType displayType, final @NotNull MinigameLangKey nameLangKey,
                                          final @NotNull MinigameLangKey descriptionLangKey) {
        return getMenuItem(displayType, MessageManager.getMessage(nameLangKey),
            MessageManager.getMessageList(descriptionLangKey));
    }

    public abstract @NotNull AMenuItem getMenuItem(final @Nullable ItemType displayType, final @Nullable Component name,
                                                   final @Nullable List<@NotNull Component> description);
}
