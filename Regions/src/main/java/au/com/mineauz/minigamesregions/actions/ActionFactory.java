package au.com.mineauz.minigamesregions.actions;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface ActionFactory extends Keyed {
    @NotNull IAction makeNewAction();
}
