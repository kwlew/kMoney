plugins {
    id("java-library")
    alias(libs.plugins.run.paper)
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
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
    compileOnly ("net.milkbowl.vault:VaultUnlockedAPI:2.19")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    runServer {
        minecraftVersion(libs.versions.minecraft.get())
        jvmArgs("-Xms2G", "-Xmx2G", "-Dcom.mojang.eula.agree=true")
    }

    register<Copy>("copyPluginJar") {
        doNotTrackState("Deployment target contains external plugin files that Gradle cannot safely fingerprint.")
        from(layout.buildDirectory.file("libs/${project.name}-${project.version}.jar"))
        into(file("C:/Users/Kaleo/Desktop/plugin development server/plugins"))
    }

    build {
        finalizedBy("copyPluginJar")
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
