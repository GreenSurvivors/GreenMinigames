plugins {
  `my-conventions`
  id("io.papermc.paperweight.userdev")
}

description = "The Minigames plugin for Paper servers."
version = rootProject.version

dependencies {
  paperweight.paperDevBundle("${rootProject.project.extra.properties["mcVersion"]}-R0.1-SNAPSHOT")

  api("org.bstats", "bstats-bukkit", "3.1.0")
  api("org.kitteh", "paste-gg-api", "2.0.0-SNAPSHOT")
  api("org.apache.commons", "commons-lang3", "3.18.0")
  api("org.apache.commons", "commons-text", "1.13.1")
  api("commons-io", "commons-io", "2.20.0")

  compileOnly("com.github.MilkBowl", "VaultAPI", "1.7.1") {
    exclude("org.bukkit", "bukkit")
    exclude("org.bukkit", "craftbukkit")
  }
  compileOnly("me.clip", "placeholderapi", "2.11.6") {
    exclude("net.kyori", "adventure-api")
  }
  compileOnly("com.sk89q.worldedit", "worldedit-bukkit", "7.4.0-SNAPSHOT")
  compileOnly("org.jetbrains", "annotations", "26.0.2")

  compileOnly("org.xerial", "sqlite-jdbc", "3.50.2.0")

  compileOnly("com.mysql", "mysql-connector-j", "9.3.0")
}
