package au.com.mineauz.minigames.managers;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.MinigameSave;
import au.com.mineauz.minigames.managers.language.MessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.objects.ResourcePack;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ResourcePackManager { //todo work with multiple ressource packs
    private final @NotNull Minigames plugin;
    private final @NotNull Path resourceDir;
    private final @NotNull Map<@NotNull String, @NotNull ResourcePack> resources = new HashMap<>();
    private boolean enabled = true;
    private MinigameSave config;

    public ResourcePackManager(final @NotNull Minigames plugin) {
        this.plugin = plugin;
        this.resourceDir = Paths.get(plugin.getDataFolder().toString(), "resources");
        if (!Files.notExists(resourceDir))
            try {
                Path path = Files.createDirectories(resourceDir);
                if (Files.notExists(path)) {
                    plugin.getComponentLogger().error("Cannot create a resource directory to house resources - they will be unavailable");
                    enabled = false;
                }

            } catch (final @NotNull IOException e) {
                plugin.getComponentLogger().error("Cannot create a resource directory to house resources - they will be unavailable.", e);
                enabled = false;
            }
    }

    public @NotNull Path getResourceDir() {
        return resourceDir;
    }

    private boolean loadEmptyPack() {
        try {
            URL url = new URI("https://github.com/AddstarMC/Minigames/blob/master/Minigames/src/main/resources/resourcepack/emptyResourcePack.zip").parseServerAuthority().toURL();
            ResourcePack empty = new ResourcePack(MessageManager.getMessage(MgMiscLangKey.MINIGAME_RESSOURCEPACK_EMPTY_NAME), url);
            addResourcePack(empty);
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    public @Nullable ResourcePack getResourcePack(@NotNull String name) {
        if (enabled) {
            ResourcePack pack = resources.get(name);

            if (pack != null && pack.isValid()) {
                return pack;
            }
        }

        return null;
    }

    public @Nullable ResourcePack addResourcePack(@NotNull ResourcePack pack) {
        if (!enabled) return null;
        return resources.put(pack.getName(), pack);
    }

    public void removeResourcePack(final @NotNull ResourcePack pack) {
        if (!enabled) return;
        resources.remove(pack.getName());
        try {
            saveResources();
        } catch (final @NotNull IOException e) {
            plugin.getComponentLogger().error("Couldn't remove resource pack " + pack.getName(), e);
        }
    }

    public boolean initialize() {
        config = MinigameSave.forGlobalData(Path.of("resources"));
        boolean emptyPresent = false;
        final @NotNull List<@NotNull ResourcePack> resources;
        try {
            resources = config.getConfigRoot().node("resources").getList(TypeToken.get(ResourcePack.class), List.of()); // todo serializer
        } catch (final @NotNull ConfigurateException e) {
            plugin.getComponentLogger().error("Couldn't load resource packs!", e);

            return false;
        }

        for (final @NotNull ResourcePack pack : resources) {
            if (pack.getName().equals(MessageManager.getStrippedMessage(MgMiscLangKey.MINIGAME_RESSOURCEPACK_EMPTY_NAME))) {
                emptyPresent = true;
                enabled = true;
            }
            addResourcePack(pack);
        }
        if (!emptyPresent) {
            if (!loadEmptyPack()) {
                plugin.getComponentLogger().warn("Minigames Resource Manager could not create the empty reset pack");
                enabled = false;
                return false;
            }
        }
        enabled = true;
        return true;
    }

    public void saveResources() throws IOException {
        List<ResourcePack> resourceList = new ArrayList<>(resources.values());
        config.getConfigRoot().node("resources").setList(TypeToken.get(ResourcePack.class), resourceList);
        config.saveConfig();
    }

    public @NotNull Set<@NotNull ResourcePack> getResourcePacks() {
        return new HashSet<>(resources.values());
    }

    public @NotNull Set<@NotNull String> getResourceNames() {
        return resources.keySet();
    }
}
