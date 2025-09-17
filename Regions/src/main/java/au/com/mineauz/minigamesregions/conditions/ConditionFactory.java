package au.com.mineauz.minigamesregions.conditions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ConditionFactory {
    @NotNull ACondition makeNewCondition();

    @NotNull String getName();

    default @Deprecated(forRemoval = true) @Nullable String getOldName() { // data fixer upper
        return null;
    }
}
