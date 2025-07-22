plugins {
    `java-setup` // our own java / tasks shared logic
    id("io.papermc.paperweight.userdev")
}

description = "Minigames-Regions"
version = rootProject.version
group = rootProject.group

repositories {
    //paper
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    paperweight.paperDevBundle("${rootProject.properties["mcVersion"]}-R0.1-SNAPSHOT")

    implementation(project(":Minigames")) {
        exclude("org.kitteh", "paste-gg-api") // the resolving of this artifact is pretty odd. But not needed anyway
    }
}

