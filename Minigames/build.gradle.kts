plugins {
    `java-setup` // our own java / tasks shared logic
    id("io.papermc.paperweight.userdev")
}

description = "The Minigames plugin for Paper servers."
version = rootProject.version
group = rootProject.group

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    mavenLocal()

    //paper
    maven("https://repo.papermc.io/repository/maven-public/")

    // Bstats
    maven("https://repo.codemc.org/repository/maven-public")

    // PlaceHolderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi")

    // Vault
    maven("https://jitpack.io")

    // WorldEdit
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    paperweight.paperDevBundle("${rootProject.properties["mcVersion"]}-R0.1-SNAPSHOT")

    api("org.bstats", "bstats-bukkit", rootProject.properties["bstatsVersion"].toString())
    api("org.kitteh", "paste-gg-api", rootProject.properties["paste-ggVersion"].toString())
    api("org.apache.commons", "commons-lang3", rootProject.properties["commons-lang3Version"].toString())
    api("org.apache.commons", "commons-text", rootProject.properties["commons-textVersion"].toString())
    api("commons-io", "commons-io", rootProject.properties["commons-ioVersion"].toString())

    compileOnly("com.github.MilkBowl", "VaultAPI", rootProject.properties["vaultVersion"].toString()) {
        exclude("org.bukkit", "bukkit")
        exclude("org.bukkit", "craftbukkit")
    }
    compileOnly("me.clip", "placeholderapi", rootProject.properties["placeholderApiVersion"].toString()) {
        exclude("net.kyori", "adventure-api")
    }
    compileOnly("com.sk89q.worldedit", "worldedit-bukkit", rootProject.properties["worldeditVersionCompile"].toString())
    compileOnly("org.jetbrains", "annotations", rootProject.properties["jetbrainsAnnotations"].toString())

    compileOnly("org.xerial", "sqlite-jdbc", rootProject.properties["sqlite-jdbcVersion"].toString())

    compileOnly("com.mysql", "mysql-connector-j", rootProject.properties["mysql-connector-jVersion"].toString())
}
