plugins {
    java
    `maven-publish`
    id("com.gradleup.shadow") version "9.3.1"
}

group = "com.github.drakepork"
version = "4.0.2"
description = "A plugin to teleport players inside specific worldguard region(s) to specific location(s)."


repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.bstats:bstats-bukkit:3.2.1")
    implementation("dev.jorel:commandapi-paper-shade:11.2.0")
    compileOnly("dev.jorel:commandapi-paper-core:11.2.0")
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.16")
    compileOnly("com.github.Zrips:CMILib:1.5.8.1")
    compileOnly("com.github.Zrips:CMI-API:9.8.6.4")
    compileOnly("net.essentialsx:EssentialsX:2.21.2") {
        exclude(group = "org.spigotmc", module = "spigot-api")
    }
    compileOnly("me.clip:placeholderapi:2.12.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
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
                "apiVersion" to "26.1"
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

tasks.shadowJar {
    configurations = project.configurations.runtimeClasspath.map { setOf(it) }
    dependencies {
        exclude { it.moduleGroup != "org.bstats" && it.moduleGroup != "dev.jorel" }
    }
    relocate("org.bstats", "com.github.drakepork.regionteleport.bstats")
    relocate("dev.jorel.commandapi", "com.github.drakepork.regionteleport.commandapi")
}
