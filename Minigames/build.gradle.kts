plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

// this is the minecraft major version. If you need a subversion like 1.20.1,
// change it in the dependencies section as this is also used as the api version of the plugin.yml
val mcVersion by extra("1.21") //: String by project

group = "au.com.mineauz"
version = "1.0.0-Mc$mcVersion.SNAPSHOT"
description = "The Minigames plugin for Paper servers."

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

    testImplementation ("org.junit.jupiter", "junit-jupiter", "5.10.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    processResources {
        filesNotMatching(setOf("*/*.zip", "*.properties")) { // exclude message files as they are in utf16 and ressourcepacks
            filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything

            expand(project.properties)
        }
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(21)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn.add(javadoc)
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    test {
        useJUnitPlatform ()
    }
    runServer {
        minecraftVersion(mcVersion)
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"]) // dev
        //artifact(tasks.jar.get().outputs.files.singleFile) // production
        artifact(tasks.getByName("sourcesJar"))
        //artifact(tasks.getByName("javadocJar"))
        pom {
            name.set("Minigames")
            description.set(project.description)
            url.set("https://github.com/AddstarMC/Minigames/")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("http://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("Razz")
                    name.set("Razz")
                    url.set("http://github.com/Razz")
                    roles.add("emeritus")
                }
                developer {
                    id.set("AddstarMC")
                    name.set("AddstarMC")
                    url.set("http://github.com/AddstarMC")
                    roles.add("developer")
                    timezone.set("10")
                }
                developer {
                    id.set("GreenSurvivors")
                    name.set("GreenSurvivors Team")
                    url.set("http://github.com/AddstarMC")
                    roles.add("developer")
                    roles.addAll("developer", "maintainer")
                }
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = "https://maven.addstar.com.au/artifactory/ext-release-local"
                val snapshotsRepoUrl = "https://maven.addstar.com.au/artifactory/ext-snapshot-local"

                url = uri(if (project.properties["release"] == "true") releasesRepoUrl else snapshotsRepoUrl)
            }
        }
    }
}
