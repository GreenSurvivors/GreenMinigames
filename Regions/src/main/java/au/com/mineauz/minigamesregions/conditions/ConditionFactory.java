package au.com.mineauz.minigamesregions.conditions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ConditionFactory {
    @NotNull ACondition makeNewCondition();

    @NotNull String getName();

    @Deprecated(forRemoval = true)
    default  @Nullable String getOldName() { // datafixerupper
        return null;
    }
}
