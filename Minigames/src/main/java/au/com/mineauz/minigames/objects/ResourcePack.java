package au.com.mineauz.minigames.objects;

import au.com.mineauz.minigames.Minigames;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class ResourcePack implements ConfigurationSerializable {
    private static final String ext = "resourcepack";
    private final @NotNull String name;
    private final @NotNull Component displayName;
    private final @Nullable URL url;
    private final @NotNull Path local;
    private final @Nullable String description;
    private final @NotNull Minigames plugin = Minigames.getPlugin();
    /**
     * Unique SH1 hash
     */
    private byte @Nullable [] hash;
    private boolean valid = false;

    /**
     * Instantiates a new Resource pack.
     *
     * @param input the input
     */
    public ResourcePack(final @NotNull Map<@NotNull String, @NotNull Object> input) {
        URL url1;
        this.name = MiniMessage.miniMessage().stripTags((String) input.get("name"));
        this.displayName = MiniMessage.miniMessage().deserialize((String) input.get("name"));
        this.description = (String) input.get("description");
        try {
            url1 = new URI((String) input.get("url")).toURL();
        } catch (final MalformedURLException | URISyntaxException e) {
            plugin.getComponentLogger().warn("The URL defined in the configuration is malformed: ", e);
            url1 = null;
            this.valid = false;
        }
        this.url = url1;
        this.local = Minigames.getPlugin().getResourcePackManager().getResourceDir().resolve(name + '.' + ext);
        validate();
    }

    /**
     * Instantiates a new Resource pack.
     *
     * @param displayName the name
     * @param url         the url
     */
    public ResourcePack(final @NotNull Component displayName, final @NotNull URL url) {
        this(displayName, url, null);
    }

    /**
     * Instantiates a new Resource pack.
     *
     * @param displayName the name
     * @param url         the url
     * @param file        the file
     */
    public ResourcePack(final @NotNull Component displayName, final @NotNull URL url, final @Nullable Path file) {
        this(displayName, url, file, null);
    }

    /**
     * Instantiates a new Resource pack.
     *
     * @param displayName the name
     * @param url         the url
     * @param file        the file
     * @param description the description
     */
    public ResourcePack(final @NotNull Component displayName, final @NotNull URL url, final @Nullable Path file, final @Nullable String description) {
        this.name = PlainTextComponentSerializer.plainText().serialize(displayName);
        this.displayName = displayName;
        this.local = file != null ? file : Minigames.getPlugin().getResourcePackManager().getResourceDir().resolve(name + '.' + ext);
        this.url = url;
        this.description = description;
        validate();
    }

    /**
     * Statically create the class
     *
     * @param map A map of values
     * @return ResourcePack resource pack
     */
    @NotNull
    public static ResourcePack valueOf(final @NotNull Map<@NotNull String, @NotNull Object> map) {
        return deserialize(map);
    }

    /**
     * Statically create the class
     *
     * @param map A map of values
     * @return ResourcePack resource pack
     */
    @NotNull
    public static ResourcePack deserialize(final @NotNull Map<@NotNull String, @NotNull Object> map) {
        return new ResourcePack(map);
    }

    private void validate() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (local) {
                if (Files.isRegularFile(local)) {
                    //set the local hash;
                    try (final InputStream stream = Files.newInputStream(local)) {
                        hash = getSH1Hash(stream);
                    } catch (final IOException e) {
                        plugin.getComponentLogger().error("Couldn't get hash for resource file " + local, e);
                    }
                    //Validate the remote file hash = local.
                    final @NotNull Path temp;
                    try (final @NotNull InputStream in = url.openStream()) {
                        temp = Files.createTempFile(name, ext);
                        Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
                    } catch (final IOException e) {
                        plugin.getComponentLogger().warn("", e);
                        valid = false;
                        return;
                    }
                    try (final @NotNull InputStream in = Files.newInputStream(temp)) {
                        final byte[] has = getSH1Hash(in);
                        if (Arrays.equals(has, hash)) {
                            plugin.getComponentLogger().info("Resource Pack: " + displayName + " passed external validation");
                            valid = true;
                            return;
                        }
                    } catch (final IOException e) {
                        plugin.getComponentLogger().warn("", e);
                        valid = false;
                        return;
                    }
                    // Local did not match hash on remote so copy the remote over the local.
                    try (final @NotNull InputStream in = Files.newInputStream(temp)) {
                        Files.copy(in, local, StandardCopyOption.REPLACE_EXISTING);
                    } catch (final IOException e) {
                        plugin.getComponentLogger().error("", e);
                    }
                    //set the new hash as long as it's not null its valid
                    setLocalHash();
                } else {
                    download(local);
                    setLocalHash();
                    valid = true;
                }
            }
        });
    }

    private byte @Nullable [] getSH1Hash(final @NotNull InputStream fis) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try {
                int n = 0;
                final byte[] buffer = new byte[8192];
                while (n != -1) {
                    n = fis.read(buffer);
                    if (n > 0) {
                        digest.update(buffer, 0, n);
                    }
                }
            } catch (final IOException e) {
                plugin.getComponentLogger().warn("", e);
                return null;
            }
            return digest.digest();
        } catch (final NoSuchAlgorithmException e) {
            plugin.getComponentLogger().error("", e);
            return null;
        }
    }

    /**
     * Generate the local SH1 hash
     */
    private void setLocalHash() {
        if (Files.isRegularFile(local)) {
            try (final InputStream in = Files.newInputStream(local)) {
                hash = getSH1Hash(in);
                valid = true;
                return;
            } catch (final IOException e) {
                plugin.getComponentLogger().warn("", e);
                valid = false;
                return;
            }
        }
        valid = false;
    }

    /**
     * Download.
     *
     * @param file the file
     */
    public void download(final @NotNull Path file) {
        if (!Files.isRegularFile(file)) {
            try {
                Files.createDirectories(file);
            } catch (IOException e) {
                plugin.getComponentLogger().error("couldn't download ressource pack because the directory to save the file in couldn't be created", e);
                valid = false;
                return;
            }
        }
        try (final InputStream in = url.openStream()) {
            Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            plugin.getComponentLogger().error("", e);
            valid = false;
        }
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Gets Displayname.
     *
     * @return the name
     */
    public @NotNull Component getDisplayName() {
        return displayName;
    }

    /**
     * True if the resource pack is validated.
     *
     * @return the boolean
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public @Nullable String getDescription() {
        return description;
    }

    /**
     * Get sh 1 hash byte [ ].
     *
     * @return the byte [ ]
     */
    @SuppressWarnings("syncronized")
    public byte[] getSH1Hash() {
        return hash;
    }

    /**
     * Gets the Publicly available URL
     *
     * @return url url
     */
    public @Nullable URL getUrl() {
        return url;
    }

    @Override
    public @NotNull Map<@NotNull String, @NotNull Object> serialize() {
        final Map<String, Object> result = new HashMap<>();
        result.put("name", MiniMessage.miniMessage().serialize(displayName));
        result.put("url", url.toString());
        result.put("description", description);

        return result;
    }
}
