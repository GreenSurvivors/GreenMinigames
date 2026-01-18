package au.com.mineauz.minigames.presets;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.AFlag;
import au.com.mineauz.minigames.config.MinigameSave;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.MinigameMessageType;
import au.com.mineauz.minigames.managers.language.MinigamePlaceHolderKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PresetLoader {
    private static final @NotNull Pattern PRESET_FILE_NAME_PATTERN = Pattern.compile("presets/(?<filename>.*?.yml)");

    public static void initPresets(final @NotNull Minigames plugin) {
        CodeSource src = plugin.getClass().getProtectionDomain().getCodeSource();
        if (src != null) {
            final @NotNull URL jarUrl = src.getLocation();

            try (final @NotNull ZipInputStream zipStream = new ZipInputStream(jarUrl.openStream())) {
                @Nullable ZipEntry zipEntry;
                while ((zipEntry = zipStream.getNextEntry()) != null) {
                    final @NotNull String entryName = zipEntry.getName(); // looks like presets/ctf.yml
                    final @NotNull Matcher matcher = PRESET_FILE_NAME_PATTERN.matcher(entryName);

                    if (matcher.matches()) {
                        // does per default nothing if the file doesn't exist
                        Files.copy(zipStream, plugin.getDataPath().resolve("presets").resolve(matcher.group("filename")), StandardCopyOption.ATOMIC_MOVE);
                    }
                }
            } catch (final @NotNull IOException e) {
                plugin.getComponentLogger().warn("Couldn't save preset files", e);
            }
        } else {
            plugin.getComponentLogger().warn("Couldn't save preset files: no CodeSource!");
        }
    }

    public static void loadPreset(@NotNull String preset, final @NotNull Minigame minigame, final @NotNull Audience audience) {
        preset = preset.toLowerCase();
        final MinigameSave save = MinigameSave.forGlobalData(Path.of("presets", preset));

        if (save.existsOnDisk()) {
            try {
                final @NotNull CommentedConfigurationNode config = save.getConfigRoot().node(preset);

                for (final @NotNull Map.Entry<@NotNull Object, @NotNull CommentedConfigurationNode> entry : config.childrenMap().entrySet()) {
                    final @Nullable AFlag<?> flag = minigame.getConfigFlag(entry.getKey().toString());
                    if (flag != null) {
                        flag.loadValue(config);
                    }
                }

                MinigameMessageManager.sendMessage(audience, MinigameMessageType.SUCCESS,
                    MinigameMessageManager.getMgMessage(MgMiscLangKey.PRESET_LOAD_SUCCESS,
                        Placeholder.unparsed(MinigamePlaceHolderKey.PRESET.getKey(), WordUtils.capitalizeFully(preset)),
                        Placeholder.unparsed(MinigamePlaceHolderKey.MINIGAME.getKey(), minigame.getName())));
            } catch (final @NotNull ConfigurateException e) {
                if (e.getMessage() != null) {
                    audience.sendMessage(Component.text(e.getMessage()));
                }

                Minigames.getPlugin().getComponentLogger().error("Couldn't load preset", e);
            }
        } else {
            MinigameMessageManager.sendMessage(audience, MinigameMessageType.ERROR,
                MinigameMessageManager.getMgMessage(MgMiscLangKey.PRESET_LOAD_ERROR_NOTFOUND,
                    Placeholder.unparsed(MinigamePlaceHolderKey.PRESET.getKey(), WordUtils.capitalize(preset))));
        }
    }

    public static void getPresetInfo(@NotNull String preset, final @NotNull Audience audience) {
        preset = preset.toLowerCase();
        final @NotNull MinigameSave save = MinigameSave.forGlobalData(Path.of("presets", preset));
        if (save.existsOnDisk()) {
            try {
                final @NotNull CommentedConfigurationNode presetNode = save.getConfigRoot().node(preset);

                final @Nullable String info = presetNode.node("info").getString();
                if (info != null) {
                    MinigameMessageManager.sendMessage(audience, MinigameMessageType.INFO,
                        MiniMessage.miniMessage().deserialize(info));
                } else {
                    MinigameMessageManager.sendMgMessage(audience, MinigameMessageType.WARNING,
                        MgMiscLangKey.PRESET_INFO_NOINFO);
                }
            } catch (final @NotNull ConfigurateException e) {
                if (e.getMessage() != null) {
                    MinigameMessageManager.sendMessage(audience, MinigameMessageType.ERROR, Component.text(e.getMessage()));
                }
                Minigames.getPlugin().getComponentLogger().error("Couldn't get preset info for the preset named " + preset, e);
            }
        } else {
            MinigameMessageManager.sendMessage(audience, MinigameMessageType.ERROR,
                MinigameMessageManager.getMgMessage(MgMiscLangKey.PRESET_LOAD_ERROR_NOTFOUND,
                    Placeholder.unparsed(MinigamePlaceHolderKey.PRESET.getKey(), WordUtils.capitalize(preset))));
        }
    }
}
