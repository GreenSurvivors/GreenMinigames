plugins {
    `java-setup` // our own java / tasks shared logic
    id("io.papermc.paperweight.userdev")
}

description = "A Minigames addon."
version = rootProject.version
group = rootProject.group

repositories {
    //paper
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("${rootProject.properties["mcVersion"]}-R0.1-SNAPSHOT")

    implementation(project(":Minigames")) {
        exclude("de.greensurvivors", "PastefyAPI") // the resolving of this artifact is pretty odd. But not needed anyway
    }
}

publishing {
    publications {
        create<MavenPublication>("Minigames-Regions") {
            from(components["java"])
            pom {
                name.set("Minigames-Regions")
                description.set(project.description)
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://opensource.org/license/mit")
                    }
                }
                developers {
                    developer {
                        name.set("_Razz_")
                    }
                    developer {
                        name.set("Schmoller")
                    }
                    developer {
                        name.set("Narimm")
                    }
                    developer {
                        name.set("Addstar")
                    }
                    developer {
                        name.set("GreenSurvivors Team")
                        organizationUrl.set("https://greensurvivors.de")
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "greensurvivorsMaven"
            url = uri("https://maven.greensurvivors.de/" +
                    if ((project.properties["release"] as String).toBoolean()) {
                        "releases"
                    } else {
                        "snapshots"
                    }
            )
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

