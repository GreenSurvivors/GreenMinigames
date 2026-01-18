package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemInteger;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class IntegerFlag extends AFlag<Integer> {

    public IntegerFlag(final @NotNull String name, final Integer defaultVal) {
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
        setFlag(config.node(getName()).getInt(getDefaultFlag()));
    }

    @Deprecated
    @Override
    public @NotNull MenuItemInteger getMenuItem(@Nullable ItemType displayType, @Nullable Component name) {
        return getMenuItem(displayType, name, 0, null);
    }

    public @NotNull MenuItemInteger getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                                @Nullable Integer min, @Nullable Integer max) {
        return getMenuItem(displayType, name, null, min, max);
    }

    public @NotNull MenuItemInteger getMenuItem(@Nullable ItemType displayType, @NotNull MinigameLangKey langKey,
                                                @Nullable Integer min, @Nullable Integer max) {
        return new MenuItemInteger(displayType, langKey, null, new Callback<>() {

            @Override
            public Integer getValue() {
                return getFlag();
            }

            @Override
            public void setValue(Integer value) {
                setFlag(value);
            }

        }, min, max);
    }

    @Deprecated
    @Override
    public @NotNull MenuItemInteger getMenuItem(@Nullable ItemType displayMat, @Nullable Component name,
                                                @Nullable List<@NotNull Component> description) {
        return getMenuItem(displayMat, name, description, 0, null);
    }

    public @NotNull MenuItemInteger getMenuItem(@Nullable ItemType displayType, @NotNull MinigameLangKey nameLangKey,
                                                @NotNull MinigameLangKey descriptionLangkey, @Nullable Integer min, @Nullable Integer max) {
        return new MenuItemInteger(displayType, nameLangKey, MinigameMessageManager.getMgMessageList(descriptionLangkey), new Callback<>() {

            @Override
            public Integer getValue() {
                return getFlag();
            }

            @Override
            public void setValue(Integer value) {
                setFlag(value);
            }

        }, min, max);
    }

    public @NotNull MenuItemInteger getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                                @Nullable List<@NotNull Component> description, @Nullable Integer min, @Nullable Integer max) {
        return new MenuItemInteger(displayType, name, description, new Callback<>() {

            @Override
            public Integer getValue() {
                return getFlag();
            }

            @Override
            public void setValue(Integer value) {
                setFlag(value);
            }

        }, min, max);
    }
}
