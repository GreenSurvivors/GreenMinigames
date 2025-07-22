plugins {
    `java-library`
}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 17 installed for example.
    // If you need to compile to for example JVM 8 or 17 bytecode, adjust the 'release' option below and keep the toolchain at 21.
    toolchain.languageVersion = JavaLanguageVersion.of("${rootProject.properties["javaVersion"]}")
    sourceCompatibility = JavaVersion.toVersion(rootProject.properties["javaVersion"]!!)
}

tasks {
    withType<JavaCompile>().configureEach {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release = rootProject.properties["javaVersion"].toString().toInt()
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

    withType<Jar>().configureEach {
        manifest.attributes(
            "paperweight-mappings-namespace" to "mojang",
        )
    }
}
