package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.Callback;
import au.com.mineauz.minigames.menu.MenuItemItemNbt;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemFlag extends AFlag<ItemStack> {

    public ItemFlag(@NotNull ItemStack itemStack, @NotNull String name) {
        super(name, itemStack, itemStack);
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
        if (config.contains(path + config.options().pathSeparator() + getName())) {
            Object object = config.get(path + config.options().pathSeparator() + getName());

            if (object instanceof ItemStack itemStack) { // bukkit already did the work for us
                setFlag(itemStack);
            } else if (object instanceof ConfigurationSection configSection) { // configs are weird.
                Map<String, Object> stringMap = configSection.getValues(false);
                setFlag(ItemStack.deserialize(stringMap));
            } else if (object instanceof Map<?, ?> objMap) {
                Map<String, Object> stringMap = new HashMap<>();

                for (Map.Entry<?, ?> entry : objMap.entrySet()) {
                    stringMap.put(entry.getKey().toString(), entry.getValue());
                }

                setFlag(ItemStack.deserialize(stringMap));
            }
        } else {
            setFlag(getDefaultFlag());
        }
    }

    public @NotNull MenuItemItemNbt getMenuItem(@NotNull Component name) {
        return new MenuItemItemNbt(getFlagOrDefault(), name, new Callback<>() {
            @Override
            public ItemStack getValue() {
                return getFlag();
            }

            @Override
            public void setValue(ItemStack value) {
                setFlag(value);
            }
        });
    }

    @Override
    public @NotNull MenuItemItemNbt getMenuItem(@Nullable Material displayMaterial, @Nullable Component name) {
        return getMenuItem(displayMaterial, name, null);
    }

    @Override
    public @NotNull MenuItemItemNbt getMenuItem(@Nullable Material displayMaterial, @Nullable Component name,
                                                @Nullable List<@NotNull Component> description) {
        return new MenuItemItemNbt(displayMaterial, name, description, new Callback<>() {
            @Override
            public ItemStack getValue() {
                return getFlag();
            }

            @Override
            public void setValue(ItemStack value) {
                setFlag(value);
            }
        });
    }
}
