package au.com.mineauz.minigamesregions.actions;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public interface ActionFactory {
    @NotNull IAction makeNewAction();

    @NotNull NamespacedKey getKey();
}
