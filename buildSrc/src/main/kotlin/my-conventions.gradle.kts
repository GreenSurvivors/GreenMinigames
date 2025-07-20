import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories

plugins {
  `java-library`
  `maven-publish`
}

java {
  // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 17 installed for example.
  // If you need to compile to for example JVM 8 or 17 bytecode, adjust the 'release' option below and keep the toolchain at 21.
  toolchain.languageVersion = JavaLanguageVersion.of(21)
  sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()
  mavenLocal()

  //paper
  maven ("https://repo.papermc.io/repository/maven-public/")

  // Bstats
  maven ("https://repo.codemc.org/repository/maven-public")

  // PlaceHolderAPI
  maven ("https://repo.extendedclip.com/content/repositories/placeholderapi")

  // Vault
  maven ("https://jitpack.io")

  // WorldEdit
  maven ("https://maven.enginehub.org/repo/")
}

tasks {
  withType<JavaCompile>().configureEach {
    // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
    // See https://openjdk.java.net/jeps/247 for more information.
    options.release = 21
  }
  withType<Javadoc>().configureEach {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
  }

  withType<ProcessResources>().configureEach {
    filesNotMatching(setOf("*/*.zip", "*.properties")) { // exclude message files as they are in utf16 and ressourcepacks
      filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything

      expand(rootProject.properties)
    }
  }
}
