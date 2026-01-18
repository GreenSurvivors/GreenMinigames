package au.com.mineauz.minigamesregions.config;

import au.com.mineauz.minigames.config.AFlag;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigamesregions.menu.MenuItemSelectEntity;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.entity.CraftEntitySnapshot;
import org.bukkit.craftbukkit.util.CraftNBTTagConfigSerializer;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class EntitySnapshotFlag extends AFlag<EntitySnapshot> {
    public EntitySnapshotFlag(@NotNull String name, @Nullable EntitySnapshot value) {
        super(name, value, value);
    }

    @Override
    public void saveValue(@NotNull CommentedConfigurationNode config) throws SerializationException {
        config.removeChild(getName());

        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.node(getName()).set(CraftNBTTagConfigSerializer.serialize(((CraftEntitySnapshot) getFlag()).getData()));
        }
    }

    @Override
    public void loadValue(@NotNull CommentedConfigurationNode config) {
        final @Nullable String string = config.node(getName()).getString();

        if (string != null) {
            if (CraftNBTTagConfigSerializer.deserialize(string) instanceof CompoundTag tag) {
                setFlag(CraftEntitySnapshot.create(tag));
                return;
            }
        }

        setFlag(getDefaultFlag());
    }

    @Override
    public @NotNull MenuItemSelectEntity getMenuItem(@Nullable ItemType displayType, @Nullable Component name,
                                                     @Nullable List<@NotNull Component> description) {
        return new MenuItemSelectEntity(displayType, name, description, new Callback<>() {
            @Override
            public EntitySnapshot getValue() {
                return getFlag();
            }

            @Override
            public void setValue(EntitySnapshot value) {
                setFlag(value);
            }
        });
    }
}
