package au.com.mineauz.minigamesregions.config;

import au.com.mineauz.minigames.config.AFlag;
import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigamesregions.menu.MenuItemSelectEntity;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.craftbukkit.entity.CraftEntitySnapshot;
import org.bukkit.craftbukkit.util.CraftNBTTagConfigSerializer;
import org.bukkit.entity.EntitySnapshot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntitySnapshotFlag extends AFlag<EntitySnapshot> {
    public EntitySnapshotFlag(@Nullable EntitySnapshot value, @NotNull String name) {
        super(name, value, value);
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        if (getFlag() != null && !getFlag().equals(getDefaultFlag())) {
            config.set(path + config.options().pathSeparator() + getName(), CraftNBTTagConfigSerializer.serialize(((CraftEntitySnapshot) getFlag()).getData()));
        } else {
            config.set(path + config.options().pathSeparator() + getName(), null);
        }
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        final @Nullable String string = config.getString(path + config.options().pathSeparator() + getName());

        if (string != null) {
            if (CraftNBTTagConfigSerializer.deserialize(string) instanceof CompoundTag tag) {
                setFlag(CraftEntitySnapshot.create(tag));
                return;
            }
        }

        setFlag(getDefaultFlag());
    }

    @Override
    public @NotNull MenuItemSelectEntity getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                                     @Nullable List<@NotNull Component> description) {
        return new MenuItemSelectEntity(displayMat, name, description, new Callback<>() {
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
