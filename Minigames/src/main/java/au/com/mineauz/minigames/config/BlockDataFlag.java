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
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class BlockDataFlag extends AFlag<BlockData> {

    public BlockDataFlag(BlockData value, String name) {
        setDefaultFlag(value);
        setFlag(value);
        setName(name);
    }

    @Override
    public void saveValue(@NotNull FileConfiguration config, @NotNull String path) {
        config.set(path + "." + getName(), getFlag().getAsString());
    }

    @Override
    public void loadValue(@NotNull FileConfiguration config, @NotNull String path) {
        String obj = config.getString(path + "." + getName());
        BlockData data = null;
        try {
            data = Bukkit.createBlockData(obj);
        } catch (NullPointerException | IllegalArgumentException e) {
            Minigames.getCmpnntLogger().warn("couldn't load Blockdata flag. Legacy data loading was removed.", e);
        }
        setFlag(Objects.requireNonNullElseGet(data, Material.STONE::createBlockData));
    }

    /**
     *
     * @deprecated use {@link #getMenuItem(MinigameLangKey)}
     */
    @Deprecated
    @Override
    public MenuItem getMenuItem(@Nullable Material displayMaterial, @NotNull MinigameLangKey langKey) {
        return getMenuItem(MinigameMessageManager.getMgMessage(langKey));
    }

    public MenuItem getMenuItem(@NotNull MinigameLangKey langKey) {
        return getMenuItem(MinigameMessageManager.getMgMessage(langKey));
    }

    /**
     * @deprecated use {@link #getMenuItem(Component)}
     */
    @Deprecated
    @Override
    public MenuItem getMenuItem(@Nullable Material displayMaterial, @Nullable Component name) {
        return getMenuItem(name);
    }

    /**
     * @deprecated use {@link #getMenuItem(Component)}
     */
    @Deprecated
    @Override
    public MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name, @Nullable List<@NotNull Component> description) {
        return getMenuItem(name);
    }

    /**
     * @deprecated use {@link #getMenuItem(MinigameLangKey)}
     */
    @Deprecated
    @Override
    public MenuItem getMenuItem(@Nullable Material displayMat, @NotNull MinigameLangKey nameLangKey,
                                @NotNull MinigameLangKey descriptionLangKey) {
        return getMenuItem(MinigameMessageManager.getMgMessage(nameLangKey));
    }

    public MenuItem getMenuItem(@Nullable Component name) {
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
