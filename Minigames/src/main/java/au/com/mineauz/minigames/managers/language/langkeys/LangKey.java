package au.com.mineauz.minigames.managers.language.langkeys;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface LangKey {
    @NotNull String path();

    @NotNull Key bundleKey();
}
