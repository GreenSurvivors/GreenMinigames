plugins{
    `my-conventions`
    id("io.papermc.paperweight.userdev")
}

description = "Minigames-Regions"
version = rootProject.version

dependencies {
    paperweight.paperDevBundle("${rootProject.project.extra.properties["mcVersion"]}-R0.1-SNAPSHOT")

    implementation(project(":Minigames"))
}

