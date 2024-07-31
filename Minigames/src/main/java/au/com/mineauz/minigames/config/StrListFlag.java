package au.com.mineauz.minigames.config;

import au.com.mineauz.minigames.menu.MenuItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StrListFlag extends AFlag<List<String>> { // todo replace with GENERIC<T> list flag AFlag<List<AFlag<T>>>

    public StrListFlag(List<String> value, @NotNull String name) {
        super(name, new ArrayList<>(), value); // default value - saving tests if the flag is equal to their default
    }

    @Override
    public void saveValue(@NotNull Configuration config, @NotNull String path) {
        config.set(path + config.options().pathSeparator() + getName(), getFlag());
    }

    @Override
    public void loadValue(@NotNull Configuration config, @NotNull String path) {
        List<String> result = config.getStringList(path + config.options().pathSeparator() + getName());

        if (result.isEmpty()) {
            result = getDefaultFlag();
        }

        setFlag(result);
    }

    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name) {
        return getMenuItem(displayMat, name, null);
    }

    @Deprecated
    @Override
    public @NotNull MenuItem getMenuItem(@Nullable Material displayMat, @Nullable Component name,
                                         @Nullable List<@NotNull Component> description) {
        return null; //todo
    }
}
