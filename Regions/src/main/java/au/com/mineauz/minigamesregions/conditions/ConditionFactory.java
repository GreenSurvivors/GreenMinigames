package au.com.mineauz.minigamesregions.conditions;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ConditionFactory extends Keyed {
    @NotNull ACondition makeNewCondition();

    @Deprecated
    @NotNull String getName();

    @Deprecated(forRemoval = true)
    default  @Nullable String getOldName() { // datafixerupper
        return null;
    }
}
