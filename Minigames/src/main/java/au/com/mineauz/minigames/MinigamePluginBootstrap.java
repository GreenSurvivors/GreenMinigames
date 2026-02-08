package au.com.mineauz.minigames;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class MinigamePluginBootstrap implements PluginBootstrap, PluginLoader {
    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
    }

    @Override
    public @NotNull Minigames createPlugin(@NotNull PluginProviderContext context) {
        return new Minigames();
    }

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addRepository(new RemoteRepository.Builder("central", "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR).build());
        resolver.addRepository(new RemoteRepository.Builder("bstats", "default", "https://repo.codemc.org/repository/maven-public").build());
        resolver.addRepository(new RemoteRepository.Builder("greensurvivors-repo", "default", "https://maven.greensurvivors.de/releases").build());


        resolver.addDependency(new Dependency(new DefaultArtifact("org.bstats:bstats-bukkit:3.0.2"), null)); // todo insert versions
        resolver.addDependency(new Dependency(new DefaultArtifact("de.greensurvivors:PastefyAPI:1.0.2-SNAPSHOT"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.apache.commons:commons-text:1.10.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("commons-io:commons-io:2.15.1"), null));
        // commons-lang3 is already shipped with the server

        System.setProperty("bstats.relocatecheck", "false"); // bstats author is stubborn and only allows the use via relocation.

        classpathBuilder.addLibrary(resolver);
    }
}
