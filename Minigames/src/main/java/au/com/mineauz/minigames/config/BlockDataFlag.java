package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItem;
import au.com.mineauz.minigames.menu.MenuItemBlockData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockDataFlag extends AFlag<BlockData> {

    public BlockDataFlag(@NotNull BlockData value, @NotNull String name) {
        super(name, value, value);
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        if (getFlag() != getDefaultFlag()) {
            config.set(path + config.options().pathSeparator() + getName(), getFlag().getAsString());
        } else {
            config.set(path + config.options().pathSeparator() + getName(), null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        String obj = config.getString(path + config.options().pathSeparator() + getName(), "");
        BlockData data = null;
        try {
            data = Bukkit.createBlockData(obj);
        } catch (NullPointerException | IllegalArgumentException e) {
            Minigames.getCmpnntLogger().warn("couldn't load Blockdata flag. Legacy data loading was removed.", e);
        }

        if (data != null) {
            setFlag(data);
        } else {
            setFlag(getDefaultFlag());
        }
    }

    /**
     * @deprecated use {@link #getMenuItem(MinigameLangKey)}
     */
    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable Material displayMaterial, @NotNull MinigameLangKey langKey) {
        return getMenuItem(MinigameMessageManager.getMgMessage(langKey));
    }

    public @NotNull MenuItem getMenuItem(@NotNull MinigameLangKey langKey) {
        return getMenuItem(MinigameMessageManager.getMgMessage(langKey));
    }

    /**
     * @deprecated use {@link #getMenuItem(Component)}
     */
    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable Material displayMaterial, @Nullable Component name) {
        return getMenuItem(name);
    }

    /**
     * @deprecated use {@link #getMenuItem(Component)}
     */
    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name, @Nullable List<@NotNull Component> description) {
        return getMenuItem(name);
    }

    /**
     * @deprecated use {@link #getMenuItem(MinigameLangKey)}
     */
    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable Material displayMat, @NotNull MinigameLangKey nameLangKey,
                                @NotNull MinigameLangKey descriptionLangKey) {
        return getMenuItem(MinigameMessageManager.getMgMessage(nameLangKey));
    }

    public @NotNull MenuItem getMenuItem(@Nullable Component name) {
        return new MenuItemBlockData(getFlag().getMaterial(), name, new Callback<>() {
            @Override
            public BlockData getValue() {
                return getFlag();
            }

            @Override
            public void setValue(BlockData value) {
                setFlag(value);
            }
        });
    }
}
