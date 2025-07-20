plugins {
    `my-conventions`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1" // Adds runServer task for testing
}

// todo:
//  - figure out how distribution packaging would work
//  - add all dependency versions into gradle.properties
//  - get run-paper working
//  - Fix Sound deprecations(key AND name isn't valid anymore!)
//  - remove build source again

version = buildString {
    append(project.ext.properties["pluginVersion"])

    if ((project.ext.properties["release"] as String).toBoolean().not()) {
        append("-Snapshot")
    }

    append("+${project.ext.properties["mcVersion"]}")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${project.ext.properties["mcVersion"]}-R0.1-SNAPSHOT")

    runtimeOnly(project(":Minigames"))
    runtimeOnly(project(":Minigames-Regions"))
}

tasks.jar {
    manifest.attributes(
        "paperweight-mappings-namespace" to "mojang",
    )
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
    minecraftVersion(project.ext.properties["mcVersion"] as String)

    // add our two plugins
    dependsOn(":Minigames:jar", ":Minigames-Regions:jar")
    pluginJars.from(
        project(":Minigames").tasks.jar.flatMap{it.archiveFile },
        project(":Minigames-Regions").tasks.jar.flatMap{it.archiveFile }
    )

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
