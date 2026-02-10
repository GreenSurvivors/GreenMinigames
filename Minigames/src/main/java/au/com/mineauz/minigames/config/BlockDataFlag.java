package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.menu.AMenuItem;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemBlockData;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class BlockDataFlag extends AFlag<BlockData> {

    public BlockDataFlag(final @NotNull String name, final BlockData defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public void saveValue(final @NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.node(getName()).set(getFlag().getAsString());
        }
    }

    @Override
    public void loadValue(final @NotNull CommentedConfigurationNode config) {
        final String str = config.node(getName()).getString("");
        BlockData data = null;
        try {
            data = Bukkit.createBlockData(str);
        } catch (NullPointerException | IllegalArgumentException e) {
            Minigames.getPlugin().getComponentLogger().warn("couldn't load Blockdata flag. Legacy data loading was removed.", e);
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
    public @NotNull AMenuItem getMenuItem(final @Nullable ItemType displayType, final @NotNull MinigameLangKey langKey) {
        return getMenuItem(MinigameMessageManager.getMgMessage(langKey));
    }

    public @NotNull AMenuItem getMenuItem(final @NotNull MinigameLangKey langKey) {
        return getMenuItem(MinigameMessageManager.getMgMessage(langKey));
    }

    /**
     * @deprecated use {@link #getMenuItem(Component)}
     */
    @Deprecated
    @Override
    public @NotNull AMenuItem getMenuItem(final @Nullable ItemType displayType, final @Nullable Component name) {
        return getMenuItem(name);
    }

    /**
     * @deprecated use {@link #getMenuItem(Component)}
     */
    @Deprecated
    @Override
    public @NotNull AMenuItem getMenuItem(final @Nullable ItemType displayType, final @Nullable Component name,
                                          final @Nullable List<@NotNull Component> description) {
        return getMenuItem(name);
    }

    /**
     * @deprecated use {@link #getMenuItem(MinigameLangKey)}
     */
    @Deprecated
    @Override
    public @NotNull AMenuItem getMenuItem(final @Nullable ItemType displayType, final @NotNull MinigameLangKey nameLangKey,
                                          final @NotNull MinigameLangKey descriptionLangKey) {
        return getMenuItem(MinigameMessageManager.getMgMessage(nameLangKey));
    }

    public @NotNull AMenuItem getMenuItem(final @Nullable Component name) {
        return new MenuItemBlockData(getFlag().getPlacementMaterial().asItemType(), name, new Callback<>() {
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
