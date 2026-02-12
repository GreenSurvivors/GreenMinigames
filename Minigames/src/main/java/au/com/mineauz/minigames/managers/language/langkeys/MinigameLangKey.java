package au.com.mineauz.minigames.managers.language.langkeys;

import au.com.mineauz.minigames.objects.MinigamesKey;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

/**
 * This interface indicates that any implementing translation of any implementing LangKey uses the standard identifier of the Minigames Plugin.
 */
public sealed interface MinigameLangKey extends LangKey permits MgCommandLangKey, MgMenuLangKey, MgSignLangKey, MgMiscLangKey {
    @NotNull Key BUNDLE_KEY = MinigamesKey.minigames("minigames");
}
