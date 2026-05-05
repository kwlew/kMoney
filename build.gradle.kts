plugins {
    id("java-library")
    alias(libs.plugins.run.paper)
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("com.gradleup.shadow") version "9.4.1"
}

repositories {
    gradlePluginPortal()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.codemc.io/repository/creatorfromhell/")
}


dependencies {
    paperweight.paperDevBundle("26.1.2.build.+")
    compileOnly ("me.clip:placeholderapi:2.12.2")
    compileOnly ("net.milkbowl.vault:VaultUnlockedAPI:2.16")
    implementation("org.bstats:bstats-bukkit:3.2.1")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        jvmArgs("-Xms4G", "-Xmx4G", "-Dcom.mojang.eula.agree=true")
    }

    shadowJar {
        archiveClassifier.set("")

        dependencies {
            include(dependency("org.bstats:bstats-bukkit:3.2.1"))
            include(dependency("org.bstats:bstats-base:3.2.1"))
        }

        relocate("org.bstats", "${project.group}.bstats")
    }

    jar {
        enabled = false
        dependsOn(shadowJar)
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props: Map<String, Any> = mapOf(
            "version" to project.version,
            "description" to (project.description ?: "")
        )

        inputs.properties(props)

        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}