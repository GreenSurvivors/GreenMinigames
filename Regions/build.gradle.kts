plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

// this is the minecraft major version. If you need a subversion like 1.20.1,
// change it in the dependencies section as this is also used as the api version of the plugin.yml
val mcVersion by extra("1.21") //: String by project
description = "Minigames-Regions"

// we only work with paper and downstream!
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 17 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    mavenLocal()

    //paper
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    // Bstats
    maven {
        url = uri("https://repo.codemc.org/repository/maven-public")
    }

    // PlaceHolderAPI
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi")
    }

    // PlaceHolderAPI
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi")
    }

    // Vault
    maven {
        url = uri("https://jitpack.io")
    }

    // WorldEdit
    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {
    paperweight.paperDevBundle("$mcVersion-R0.1-SNAPSHOT")

    api(project(":Minigames"))

    api("org.bstats", "bstats-bukkit", "3.0.2")
    api("org.kitteh", "paste-gg-api", "2.0.0-SNAPSHOT")
    api("org.apache.commons", "commons-lang3", "3.15.0")
    api("org.apache.commons", "commons-text", "1.12.0")
    api("commons-io", "commons-io", "2.16.1")

    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7.1") {
        exclude("org.bukkit", "bukkit")
        exclude("org.bukkit", "craftbukkit")
    }
    compileOnly("me.clip", "placeholderapi", "2.11.6") {
        exclude("net.kyori", "adventure-api")
    }
    compileOnly("com.sk89q.worldedit", "worldedit-bukkit", "7.3.4-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.1.0")

    compileOnly("org.xerial", "sqlite-jdbc", "3.46.0.0")

    compileOnly("com.mysql", "mysql-connector-j", "9.0.0")

    testImplementation("org.junit.jupiter", "junit-jupiter", "5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.register<Wrapper>("wrapper") {
    gradleVersion = "8.10.2"
}
tasks.register("prepareKotlinBuildScriptModel") {}