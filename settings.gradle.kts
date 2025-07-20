plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "MinigamesProject"

include(":Minigames")
include(":Minigames-Regions")

project(":Minigames-Regions").projectDir = file("Regions")
