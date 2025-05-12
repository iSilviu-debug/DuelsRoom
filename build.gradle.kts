plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "it.isilviu"
version = "1.2-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "PaperMC"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "silvioRepo"
        url = uri("https://repo.silvio.top/releases/")
    }
    maven {
        name = "EngineHub"
        url = uri("https://maven.enginehub.org/repo/")
    }
    maven {
        name = "ExtendedClip"
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

dependencies {
    implementation("org.bstats:bstats-base:3.1.1")
    implementation("org.bstats:bstats-bukkit:3.1.1")

    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    // Required for all platforms
    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.12")

    // Required for Spigot and Paper
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-rc.12")

    // Brigadier module
    implementation("io.github.revxrsal:lamp.brigadier:4.0.0-rc.12")

    // WorldGuard and Fawe. Require Java 21.
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.0")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.20")

    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.6")

    // DeluxeCombat
    compileOnly("com.github.timderspieler:DeluxeCombat-API:1.5.1")
}

tasks.withType<JavaCompile> { // Preserve parameter names in the bytecode
    options.compilerArgs.add("-parameters")
}

tasks {
    shadowJar {
        relocate("org.bstats", "it.isilviu.duelsroom.lib.bstats")
        relocate("revxrsal", "it.isilviu.duelsroom.lib.revxrsal")
    }

    build {
        dependsOn(shadowJar)
    }
}


java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.test {
    useJUnitPlatform()
}