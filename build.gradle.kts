plugins {
    `java-library` // needed to get the jar from the subprojects jar task without having to mess with file paths
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1" // Adds runServer task for testing
}

// todo:
//  - figure out how distribution packaging would work
//  - Fix Sound deprecations(key AND name isn't valid anymore!)

version = buildString {
    append(project.properties["pluginVersion"])

    if ((project.properties["release"] as String).toBoolean().not()) {
        append("-Snapshot")
    }

    append("+${project.properties["mcVersion"]}")
}

// stop producing a dummy artifact
tasks.named<Jar>("jar") {
    enabled = false
}
tasks.named("classes") {
    enabled = false
}

// ignore a potential artifact from this root project
runPaper.disablePluginJarDetection()
tasks.runServer {
    // configure minecraft
    minecraftVersion(project.properties["mcVersion"] as String)

    pluginJars.from(
        project(":Minigames").tasks.jar.flatMap { it.archiveFile },
        project(":Minigames-Regions").tasks.jar.flatMap { it.archiveFile },
    )

    downloadPlugins {
        // make sure to double-check the version id on the Modrinth version page
        modrinth("worldedit", "${project.properties["worldeditVersionRunTask"]}")
        hangar("PlaceholderAPI", "${project.properties["placeholderApiVersion"]}")
        github("MilkBowl", "Vault", "${project.properties["vaultVersionRunTask"]}", "Vault.jar")
    }

    // disable bstats, as it isn't needed for dev environment
    doFirst { // this happens after downloading the plugins above, but before the server starts
        val cfg = runDirectory.get().asFile.resolve("plugins/bStats/config.yml")
        if (!cfg.exists()) {
            cfg.parentFile.mkdirs()
            cfg.createNewFile()
        }
        cfg.writeText("enabled: false\n")
    }

    // automatically agree to eula
    jvmArgs("-Dcom.mojang.eula.agree=true")
}
