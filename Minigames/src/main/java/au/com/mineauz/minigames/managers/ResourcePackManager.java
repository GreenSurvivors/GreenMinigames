package au.com.mineauz.minigames.managers;

import au.com.mineauz.minigames.Minigames;
import au.com.mineauz.minigames.config.MinigameSave;
import au.com.mineauz.minigames.managers.language.MinigameMessageManager;
import au.com.mineauz.minigames.managers.language.langkeys.MgMiscLangKey;
import au.com.mineauz.minigames.objects.ResourcePack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    final static @NotNull Path resourceDir = Paths.get(Minigames.getPlugin().getDataFolder().toString(), "resources");
    private final @NotNull Map<@NotNull String, @NotNull ResourcePack> resources = new HashMap<>();
    private boolean enabled = true;
    private MinigameSave config;

    public ResourcePackManager() {
        if (!Files.notExists(resourceDir))
            try {
                Path path = Files.createDirectories(resourceDir);
                if (Files.notExists(path)) {
                    Minigames.getCmpnntLogger().error("Cannot create a resource directory to house resources " +
                            "- they will be unavailable");
                    enabled = false;
                } else {
                    if (Files.exists(path))
                        enabled = true;
                    else {
                        enabled = false;
                        Minigames.getCmpnntLogger().error("Cannot create a resource directory to house resources " +
                                "- they will be unavailable.");
                    }
                }

            } catch (IOException e) {
                Minigames.getCmpnntLogger().error("Cannot create a resource directory to house resources " +
                        "- they will be unavailable: Message" + e.getMessage());
                enabled = false;
            }
    }

    public static @NotNull Path getResourceDir() {
        return resourceDir;
    }

    private boolean loadEmptyPack() {
        try {
            URL url = new URI("https://github.com/AddstarMC/Minigames/raw/master/Minigames/src/main/resources/resourcepack/emptyResourcePack.zip").toURL();
            ResourcePack empty = new ResourcePack(MinigameMessageManager.getMgMessage(MgMiscLangKey.MINIGAME_RESSOURCEPACK_EMPTY_NAME), url);
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

    public void removeResourcePack(@NotNull ResourcePack pack) {
        if (!enabled) return;
        resources.remove(pack.getName());
        saveResources();
    }

    public boolean initialize(final @NotNull MinigameSave c) {
        this.config = c;
        boolean emptyPresent = false;
        final List<ResourcePack> resources = new ArrayList<>();
        final Object objects = this.config.getConfig().get("resources");
        if (objects instanceof List<?> obj) {
            for (final Object object : obj) {
                if (object instanceof ResourcePack) {
                    resources.add((ResourcePack) object);
                }
            }
        }
        for (final ResourcePack pack : resources) {
            if (pack.getName().equals(MinigameMessageManager.getStrippedMgMessage(MgMiscLangKey.MINIGAME_RESSOURCEPACK_EMPTY_NAME))) {
                emptyPresent = true;
                enabled = true;
            }
            addResourcePack(pack);
        }
        if (!emptyPresent) {
            if (!loadEmptyPack()) {
                Minigames.getCmpnntLogger().warn("Minigames Resource Manager could not create the empty reset pack");
                enabled = false;
                return false;
            }
        }
        enabled = true;
        return true;
    }

    public void saveResources() {
        List<ResourcePack> resourceList = new ArrayList<>(resources.values());
        config.getConfig().set("resources", resourceList);
        config.saveConfig();
    }

    public @NotNull Set<@NotNull ResourcePack> getResourcePacks() {
        return new HashSet<>(resources.values());
    }

    public @NotNull Set<@NotNull String> getResourceNames() {
        return resources.keySet();
    }
}
