import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.3"
}

group = "com.github.drakepork"
version = "4.0.1"
description = "A plugin to teleport players inside specific worldguard region(s) to specific location(s)."


repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("dev.jorel:commandapi-bukkit-shade-mojang-mapped:9.7.0")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9-beta1")
    compileOnly("net.essentialsx:EssentialsX:2.20.1") {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly(fileTree("libs") { include("*.jar")})
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.jar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

tasks.shadowJar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

tasks {
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        val props = mapOf(
                "name" to project.name,
                "version" to project.version,
                "description" to project.description,
                "apiVersion" to "1.21"
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

tasks.named("build") {
    dependsOn("shadowJar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<ShadowJar> {
    dependencies {
        include(dependency("dev.jorel:commandapi-bukkit-shade-mojang-mapped:9.7.0"))
        include(dependency("org.bstats:bstats-base:3.0.2"))
        include(dependency("org.bstats:bstats-bukkit:3.0.2"))
    }
    relocate("org.bstats", "com.github.drakepork.regionteleport.bstats")
    relocate("dev.jorel.commandapi", "com.github.drakepork.regionteleport.commandapi")
}
