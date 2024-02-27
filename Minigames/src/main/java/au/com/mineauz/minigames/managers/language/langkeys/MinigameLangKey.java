package au.com.mineauz.minigames.managers.language.langkeys;

/**
 * This interface indicates that any implementing translation of any implementing LangKey uses the standard identifier of the Minigames Plugin.
 */
public sealed interface MinigameLangKey extends LangKey permits MgCommandLangKey, MgMenuLangKey, MgSignLangKey, MgMiscLangKey {
}
