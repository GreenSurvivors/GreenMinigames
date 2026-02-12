package au.com.mineauz.minigamesregions.language;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigamesregions.RegionsMain;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class RegionMessageManager {
    private static final String BUNDLE_KEY = "minigames-regions";

    public static void register() {
        CodeSource src = RegionsMain.class.getProtectionDomain().getCodeSource();
        if (src != null) {
            MessageManager.initLangFiles(src, BUNDLE_KEY);
        } else {
            RegionsMain.getPlugin().getComponentLogger().warn("Couldn't save lang files: no CodeSource!");
        }

        String tag = Minigames.getPlugin().getConfig().getString("lang", Locale.getDefault().toLanguageTag());
        Locale locale = Locale.forLanguageTag(tag.replace("_", "-"));

        // fall back if locale is undefined
        if (locale.getLanguage().isEmpty()) {
            locale = Locale.getDefault();
        }

        File file = new File(new File(Minigames.getPlugin().getDataFolder(), "lang"), "minigames_regions.properties");

        ResourceBundle langBundleMinigameRegions = null;
        if (file.exists()) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                langBundleMinigameRegions = new PropertyResourceBundle(inputStreamReader);
            } catch (IOException e) {
                RegionsMain.getPlugin().getComponentLogger().warn("couldn't get Ressource bundle from file " + file.getName(), e);
            }
        } else {
            try {
                langBundleMinigameRegions = ResourceBundle.getBundle(BUNDLE_KEY, locale, RegionsMain.getPlugin().getClass().getClassLoader(), new UTF8ResourceBundleControl());
            } catch (MissingResourceException e) {
                RegionsMain.getPlugin().getComponentLogger().warn("couldn't get Ressource bundle for lang " + locale.toLanguageTag(), e);
            }
        }
        if (langBundleMinigameRegions != null) {
            MessageManager.registerMessageFile(getBundleKey(), langBundleMinigameRegions);
        } else {
            RegionsMain.getPlugin().getComponentLogger().error("No region language Resource Could be loaded...messaging will be broken");
        }
    }

    public static @NotNull Key getBundleKey() {
        return new NamespacedKey(RegionsMain.getPlugin(), BUNDLE_KEY);
    }

    public static void debugMessage(final @NotNull String message) { //todo
        if (Minigames.getPlugin().isDebugging()) {
            RegionsMain.getPlugin().getComponentLogger().info(ChatColor.RED + "[Debug] " + ChatColor.WHITE + message);
        }
    }
}
