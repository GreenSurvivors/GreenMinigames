package au.com.mineauz.minigames.managers.language;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.events.MinigamesBroadcastEvent;
import au.com.mineauz.minigames.managers.language.langkeys.LangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.managers.language.langkeys.MinigameLangKey;
import au.com.mineauz.minigames.minigame.Minigame;
import au.com.mineauz.minigames.objects.MinigamePlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class will hold and store all messages that are required for minigames
 */
public class MessageManager { // todo cache unformatted // todo clean all the different sendMessages - there are to many similar
    private static final @NotNull String BUNDLE_NAME = "messages";
    private static final @NotNull Pattern LIST_PATTERN = Pattern.compile("<newline>");

    /**
     * Stores each prop file with an identifier
     */
    private static final @NotNull ConcurrentHashMap<@NotNull Key, @NotNull ResourceBundle> propertiesHashMap = new ConcurrentHashMap<>();
    public static final @NotNull Component DEBUG_PREFIX = Component.text("[Debug]", NamedTextColor.RED);
    private static final @MonotonicNonNull Minigames PLUGIN = Minigames.getPlugin();

    public static void registerCoreLanguage() {
        final @NotNull CodeSource src = Minigames.class.getProtectionDomain().getCodeSource();
        if (src != null) {
            initLangFiles(src, BUNDLE_NAME);
        } else {
            PLUGIN.getComponentLogger().warn("Couldn't save lang files: no CodeSource!");
        }

        String tag = PLUGIN.getConfig().getString("lang", Locale.getDefault().toLanguageTag()); // todo seems like this beaks on the first startup, when the config is still not saved on disk
        Locale locale = Locale.forLanguageTag(tag.replace("_", "-"));

        // fall back if locale is undefined
        if (locale.getLanguage().isEmpty()) {
            locale = Locale.getDefault();
        }

        PLUGIN.getComponentLogger().info("MessageManager set locale for language:" + locale.toLanguageTag());

        Path file = PLUGIN.getDataPath().resolve("lang").resolve("minigames.properties");
        registerCoreLanguage(file, locale);
    }

    private static @NotNull String saveConvert(final @NotNull String theString, final boolean escapeSpace) {
        final int len = theString.length();
        int bufLen = len * 2;
        if (bufLen < 0) {
            bufLen = Integer.MAX_VALUE;
        }
        final @NotNull StringBuilder convertedStrBuilder = new StringBuilder(bufLen);

        for (int i = 0; i < theString.length(); i++) {
            final char aChar = theString.charAt(i);
            // Handle common case first
            if ((aChar > 61) && (aChar < 127)) {
                if (aChar == '\\') {
                    if (i + 1 < theString.length()) {
                        final char bChar = theString.charAt(i + 1);
                        if (bChar == ' ' || bChar == 't' || bChar == 'n' || bChar == 'r' ||
                            bChar == 'f' || bChar == '\\' || bChar == 'u' || bChar == '=' ||
                            bChar == ':' || bChar == '#' || bChar == '!') {
                            // don't double escape already escaped chars
                            convertedStrBuilder.append(aChar);
                            convertedStrBuilder.append(bChar);
                            i++;
                            continue;
                        } else {
                            // any other char following
                            convertedStrBuilder.append('\\');
                        }
                    } else {
                        // last char was a backslash. escape!
                        convertedStrBuilder.append('\\');
                    }
                }
                convertedStrBuilder.append(aChar);
                continue;
            }

            // escape non escaped chars that have to get escaped
            switch (aChar) {
                case ' ' -> {
                    if (escapeSpace) {
                        convertedStrBuilder.append('\\');
                    }
                    convertedStrBuilder.append(' ');
                }
                case '\t' -> convertedStrBuilder.append("\\t");
                case '\n' -> convertedStrBuilder.append("\\n");
                case '\r' -> convertedStrBuilder.append("\\r");
                case '\f' -> convertedStrBuilder.append("\\f");
                case '=', ':', '#', '!' -> {
                    convertedStrBuilder.append('\\');
                    convertedStrBuilder.append(aChar);
                }
                default -> convertedStrBuilder.append(aChar);
            }
        }

        return convertedStrBuilder.toString();
    }

    // Thanks, @Feuerreiter, for code from Padlock. Nice Plugin, check it out!
    // #self-marketing
    public static void initLangFiles(final @NotNull CodeSource src, final @NotNull String bundleName) {
        final Pattern bundleFileNamePattern = Pattern.compile(bundleName + "(?:_.*)?.properties");

        URL jarUrl = src.getLocation();
        try (ZipInputStream zipStream = new ZipInputStream(jarUrl.openStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zipStream.getNextEntry()) != null) {
                String entryName = zipEntry.getName();

                if (bundleFileNamePattern.matcher(entryName).matches()) {
                    Path langFile = PLUGIN.getDataPath().resolve(bundleName).resolve(entryName);
                    if (!Files.isRegularFile(langFile)) { // don't overwrite existing files
                        Files.copy(zipStream, langFile);
                    } else { // add defaults to file to expand in case there are key-value pairs missing
                        Properties defaults = new Properties();
                        // no try with since we need to keep the ZipStream open
                        try {
                            defaults.load(new InputStreamReader(zipStream, StandardCharsets.UTF_8));
                        } catch (Exception e) {
                            PLUGIN.getComponentLogger().warn("couldn't get default properties file for " + entryName + "!", e);
                            continue;
                        }

                        Properties current = new Properties();
                        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(langFile), StandardCharsets.UTF_8)) {
                            current.load(reader);
                        } catch (Exception e) {
                            PLUGIN.getComponentLogger().warn("couldn't get default properties file for " + entryName + "!", e);
                            continue;
                        }

                        // we are NOT using Properties#store since it gets rid of comments and doesn't guarantee ordering
                        try (final @NotNull BufferedWriter bw = Files.newBufferedWriter(langFile, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
                            boolean updated = false; // only write comment once
                            for (final @NotNull Map.Entry<@NotNull Object, @NotNull Object> translationPair : defaults.entrySet()) { //todo guarantee ordering; default Properties are backed up by hashmap!
                                if (current.get(translationPair.getKey()) == null) {
                                    if (!updated) {
                                        // most likely this will generate an empty line, since the last line should be empty.
                                        // however this is NOT guaranteed and therefore might write the command onto an existing line and ruin the translation there!
                                        bw.newLine();
                                        bw.write("# New Values where added. Is everything else up to date? Time of update: " + new Date());
                                        bw.newLine();

                                        PLUGIN.getComponentLogger().info("Updated langfile \"" + entryName + "\". Might want to check the new translation strings out!");

                                        updated = true;
                                    }

                                    String key = saveConvert((String) translationPair.getKey(), true);
                                    /* No need to escape embedded and trailing spaces for value, hence
                                     * pass false to flag.
                                     */
                                    String val = saveConvert((String) translationPair.getValue(), false);
                                    bw.write((key + "=" + val));
                                    bw.newLine();
                                } // current already knows the key
                            } // end of for
                        } // end of try
                    } // end of else (file exists)
                } // doesn't match
            } // end of elements
        } catch (IOException e) {
            PLUGIN.getComponentLogger().warn("Couldn't save lang files", e);
        }
    }

    public static void registerCoreLanguage(final @NotNull Path file, final @NotNull Locale locale) {
        ResourceBundle langBundleMinigames = null;
        if (Files.isRegularFile(file)) {
            try (InputStreamReader inputStreamReader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
                langBundleMinigames = new PropertyResourceBundle(inputStreamReader);
            } catch (IOException e) {
                PLUGIN.getComponentLogger().warn("couldn't get Ressource bundle from file " + file, e);
            }
        } else {
            try {
                langBundleMinigames = ResourceBundle.getBundle("messages", locale, PLUGIN.getClass().getClassLoader(), new UTF8ResourceBundleControl());
            } catch (MissingResourceException e) {
                PLUGIN.getComponentLogger().warn("couldn't get Ressource bundle for lang " + locale.toLanguageTag(), e);
            }
        }
        if (langBundleMinigames != null) {
            registerMessageFile(MinigameLangKey.BUNDLE_KEY, langBundleMinigames);
        } else {
            PLUGIN.getComponentLogger().error("No Core Language Resource Could be loaded...messaging will be broken");
        }
    }

    /**
     * Register a new Bundle
     * To load the bundle use the {@link UTF8ResourceBundleControl instance as the resource control.
     * This loads the resource with UTF8
     *
     * @param identifier Unique identifier for your resource bundle
     * @param bundle     the ResourceBundle
     * @return true on success.
     */
    public static boolean registerMessageFile(final @NotNull Key identifier, final @NotNull ResourceBundle bundle) {
        if (propertiesHashMap.containsKey(identifier)) {
            return false;
        } else {
            if (propertiesHashMap.put(identifier, bundle) == null) {
                PLUGIN.getComponentLogger().info("Loaded and registered Resource Bundle " + bundle.getBaseBundleName()
                    + " with Locale:" + bundle.getLocale().toLanguageTag() + " Added " + bundle.keySet().size() + " keys");
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean unregisterMessageFile(final @NotNull Key identifier) {
        return (propertiesHashMap.remove(identifier) != null);
    }

    public static @NotNull Component formatBlockLocation(final @NotNull Location location) {
        return Component.text(location.blockX() + ", " + location.blockY() + " ," + location.blockZ());
    }

    /**
     * If the identifier is null this uses the core language file
     *
     * @param key        key
     * @param resolvers  resolver of placeholders
     * @return Formatted String.
     */
    public static @NotNull Component getMessage(final @NotNull LangKey key, final @NotNull TagResolver @NotNull... resolvers) {
        final @NotNull String raw = getRawMessage(key);

        return MiniMessage.miniMessage().deserialize(raw, resolvers);
    }

    /**
     * If the identifier is null this uses the core language file
     *
     * @param key        key
     * @param resolvers  resolver of placeholders
     * @return String stripped of format.
     */
    public static @NotNull String getStrippedMessage(final @NotNull LangKey key, final @NotNull TagResolver @NotNull ... resolvers) {
        return PlainTextComponentSerializer.plainText().serialize(MiniMessage.miniMessage().deserialize(getRawMessage(key), resolvers));
    }

    /**
     * If the identifier is null this uses the core language file
     * <p>
     * Reads a String from the ressource bundle, splits it at "{@code <newline>}" and then deserializes it via MiniMessage.
     * This was first and formost written for Lore of ItemStacks, where newlines don't exist in the common way,
     * but every line is an element of a list.
     * This way a Component read by getMessage would look the same as getMessageList in different scenarios.
     * And be hopefully intuitive how to make multiline lore in the translation files.
     */
    public static @NotNull List<Component> getMessageList(final @NotNull LangKey key, final @NotNull TagResolver @NotNull... resolvers) {
        MiniMessage miniMessage = MiniMessage.miniMessage(); // cached to use multiple times in stream

        final @NotNull String raw = getRawMessage(key);
        //split at new line then deserialize to component
        return Arrays.stream(LIST_PATTERN.split(raw)).map(str -> miniMessage.deserialize(str, resolvers)).collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @param langKey        key
     * @return Unformatted (raw) String.
     * @throws MissingResourceException If bundle not found.
     */
    public static @NotNull String getRawMessage(final @NotNull LangKey langKey) throws MissingResourceException { //todo don't crash if bundle is missing or can't get string. simply return key
        final @Nullable ResourceBundle bundle = propertiesHashMap.get(langKey.bundleKey());
        if (bundle == null) {
            throw new MissingResourceException(langKey.bundleKey().asString(), "MessageManager", langKey.path());
        }
        return bundle.getString(langKey.path());
    }

    public static void sendClickCommandMessage(final @NotNull Audience target, final @NotNull String command,
                                               final @NotNull LangKey key,
                                               final @NotNull TagResolver @NotNull... resolvers) {
        Component init = getPluginPrefix(MinigameMessageType.INFO);
        Component message = getMessage(key, resolvers).
            clickEvent(ClickEvent.runCommand(command));

        // don't use color of prefix
        message = message.colorIfAbsent(NamedTextColor.WHITE);

        target.sendMessage(init.append(message));
    }

    public static void sendMessage(final @NotNull Audience target, final @NotNull MinigameMessageType type, final @NotNull LangKey key,
                                   final @NotNull TagResolver @NotNull... resolvers) {
        Component init = getPluginPrefix(type);
        Component message = getMessage(key, resolvers);

        // don't use color of prefix
        message = message.colorIfAbsent(NamedTextColor.WHITE);

        target.sendMessage(init.append(message));
    }

    private static @NotNull Component getPluginPrefix(final @NotNull MinigameMessageType type) {
        Component init = getMessage(MgMiscLangKey.PLUGIN_PREFIX).appendSpace();
        return switch (type) {
            case ERROR, TIE -> init.color(NamedTextColor.RED);
            case WARNING -> init.color(NamedTextColor.GOLD);
            case SUCCESS, WIN -> init.color(NamedTextColor.GREEN);
            case LOSS -> init.color(NamedTextColor.DARK_RED);
            case NONE -> Component.empty();
            default -> init.color(NamedTextColor.AQUA);
        };
    }

    /**
     * Broadcasts a message with a defined permission for everyone on a server.
     *
     * @param message    - The message to be broadcastServer (Can be manipulated with MinigamesBroadcastEvent)
     * @param minigame   - The Minigame this broadcast is related to.
     * @param permission - The permission required to see this broadcastServer message.
     */
    public static void broadcastServer(@NotNull Component message, final @NotNull Minigame minigame, final @NotNull String permission) {
        // don't use color of prefix
        message = message.colorIfAbsent(NamedTextColor.WHITE);

        MinigamesBroadcastEvent ev = new MinigamesBroadcastEvent(getPluginPrefix(MinigameMessageType.DEFAULT), message, minigame);
        Bukkit.getPluginManager().callEvent(ev);

        // Only send broadcastServer if event was not cancelled and is not empty
        if (!ev.isCancelled()) {
            Bukkit.getServer().broadcast(ev.getMessageWithPrefix(), permission);
        }
    }

    /**
     * Broadcasts a server message without a permission for everyone on a server.
     *
     * @param message  - The message to be broadcasted (Can be manipulated with MinigamesBroadcastEvent)
     * @param minigame - The Minigame this broadcast is related to.
     * @param type     - The color to be used in the prefix.
     */
    public static void broadcastServer(@NotNull Component message, final @NotNull Minigame minigame, final @NotNull MinigameMessageType type) {
        // don't use color of prefix
        message = message.colorIfAbsent(NamedTextColor.WHITE);

        final @NotNull Component init = getPluginPrefix(type);
        final @NotNull MinigamesBroadcastEvent ev = new MinigamesBroadcastEvent(init, message, minigame);

        // Only send broadcastServer if event was not cancelled and is not empty
        if (ev.callEvent()) {
            Bukkit.getServer().broadcast(ev.getMessageWithPrefix());
        }
    }


    /**
     * Sending a general info Broadcast to all players in the minigame.
     *
     * @param minigame The minigame in which this message shall be sent
     * @param message  The message
     */
    public static void sendMinigameMessage(final @NotNull Minigame minigame, final @NotNull Component message) {
        sendMinigameMessage(minigame, message, MinigameMessageType.INFO);
    }

    /**
     * Sending a general Broadcast to all players in the minigame.
     *
     * @param minigame The minigame in which this message shall be sent
     * @param message  The message
     * @param type     Message Type
     */
    public static void sendMinigameMessage(final @NotNull Minigame minigame, final @NotNull Component message,
                                           final @Nullable MinigameMessageType type) {
        sendMinigameMessage(minigame, message, type, (List<MinigamePlayer>) null);
    }

    /**
     * Sending a general Broadcast to all players in the minigame.
     *
     * @param minigame The minigame in which this message shall be sent
     * @param message  The message
     * @param type     Message Type
     * @param exclude  Player, who shall not get this message
     */
    public static void sendMinigameMessage(final @NotNull Minigame minigame, final @NotNull Component message,
                                           final @Nullable MinigameMessageType type, final @NotNull MinigamePlayer exclude) {
        sendMinigameMessage(minigame, message, type, Collections.singletonList(exclude));
    }

    /**
     * Sending a general Broadcast to all players in the minigame.
     *
     * @param minigame The minigame in which this message shall be sent
     * @param message  The message
     * @param type     Message Type
     * @param exclude  Players, which shall not get this message
     */
    public static void sendMinigameMessage(final @NotNull Minigame minigame, final @NotNull Component message,
                                           @Nullable MinigameMessageType type,
                                           final @Nullable List<@NotNull MinigamePlayer> exclude) {
        if (!minigame.getShowPlayerBroadcasts()) {
            return;
        }
        sendBroadcastMessageUnchecked(minigame, message, type, exclude);
    }

    // This sends a message to every player which is not excluded from the exclude list
    public static void sendBroadcastMessageUnchecked(final @NotNull Minigame minigame, final @NotNull Component message,
                                                     @Nullable MinigameMessageType type,
                                                     final @Nullable List<@NotNull MinigamePlayer> exclude) {
        if (type == null) {
            type = MinigameMessageType.INFO;
        }

        final @NotNull List<@NotNull MinigamePlayer> playersSendTo = new ArrayList<>();
        playersSendTo.addAll(minigame.getPlayers());
        playersSendTo.addAll(minigame.getSpectators());
        if (exclude != null) {
            playersSendTo.removeAll(exclude);
        }

        for (final @NotNull MinigamePlayer player : playersSendTo) {
            MessageManager.sendMessage(player, type, message);
        }
    }

    public static void sendMessage(final @NotNull Audience audience, final @NotNull MinigameMessageType messageType,
                                   final @NotNull LangKey key) {
        // don't use color of prefix
        Component message = getMessage(key);
        message = message.colorIfAbsent(NamedTextColor.WHITE);

        audience.sendMessage(getPluginPrefix(messageType).append(message));
    }

    public static void sendMessage(final @NotNull Audience audience, final @NotNull MinigameMessageType messageType,
                                   @NotNull Component message) {
        // don't use color of prefix
        message = message.colorIfAbsent(NamedTextColor.WHITE);
        audience.sendMessage(getPluginPrefix(messageType).append(message));
    }

    public static void debugMessage(final @NotNull String message) {
        debugMessage(Component.text(message));
    }

    public static void debugMessage(final @NotNull Component message) {
        if (PLUGIN.isDebugging()) {
            PLUGIN.getComponentLogger().info(Component.text().append(DEBUG_PREFIX).appendSpace().append(message).asComponent());
        }
    }
}
