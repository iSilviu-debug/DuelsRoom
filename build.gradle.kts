plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "it.isilviu"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven {
        name = "CodeMC"
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
    maven(url = "https://jitpack.io")
    maven {
        name = "silvioRepo"
        url = uri("https://repo.silvio.top/releases/")
    }
}

dependencies {
    implementation("org.bstats:bstats-base:3.0.4")
    implementation("org.bstats:bstats-bukkit:3.0.4")

    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    // Required for all platforms
    implementation("com.github.Revxrsal.Lamp:common:3.2.1")

    // Add your specific platform module here
    implementation("com.github.Revxrsal.Lamp:bukkit:3.2.1")
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