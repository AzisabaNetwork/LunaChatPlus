plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.4.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

group = "net.azisaba"
version = "3.4.0+1.21.11"
description = "lunachatplus"
java.sourceCompatibility = JavaVersion.VERSION_21

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

paperweight.reobfArtifactConfiguration.set(io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION)

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://repo.azisaba.net/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.mikeprimm.com/")
    }

    maven {
        url = uri("https://repo.onarandombox.com/multiverse-releases")
    }

    maven {
        url = uri("https://repo.codemc.org/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.wea-ondara.net/repository/public/")
    }

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    api("org.bstats:bstats-bukkit:1.7")
    api("org.bstats:bstats-bungeecord:1.7")
    api("com.google.code.gson:gson:2.8.9")
    api("org.apache.commons:commons-lang3:3.18.0")
    api("org.jetbrains:annotations:20.1.0")
    testImplementation("junit:junit:4.13.1")
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly("org.dynmap:dynmap-api:2.0") {
        exclude("org.bukkit", "bukkit")
    }
    compileOnly("com.onarandombox.multiversecore:multiverse-core:4.3.9") {
        exclude("me.main__.util", "SerializationConfig")
        exclude("com.pneumaticraft.commandhandler", "CommandHandler")
        exclude("com.dumptruckman.minecraft", "buscript")
        exclude("com.dumptruckman.minecraft", "Logging")
        exclude("de.themoep.idconverter", "mappings")
    }
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.1.146") {
        exclude("org.apache.maven.scm", "maven-scm-provider-gitexe")
        exclude("org.bstats", "bstats-bukkit")
        exclude("org.spigotmc", "spigot-api")
        exclude("com.sk89q.worldguard", "worldguard-core")
        exclude("com.sk89q.worldguard", "worldguard-legacy")
    }
    compileOnly("net.luckperms:api:5.1")
}

tasks {
    processResources {
        from(
            sourceSets.main
                .get()
                .resources.srcDirs,
        ) {
            include("**")
            val tokenReplacementMap =
                mapOf(
                    "version" to project.version,
                    "description" to project.description,
                )
            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
        }
        filteringCharset = "UTF-8"
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from(projectDir) { include("LICENSE") }
    }

    compileJava {
        options.encoding = "UTF-8"
    }
}

publishing {
    repositories {
        maven {
            name = "repo"
            credentials(PasswordCredentials::class)
            url =
                uri(
                    if (project.version.toString().endsWith("SNAPSHOT")) {
                        project.findProperty("deploySnapshotURL")
                            ?: System.getProperty("deploySnapshotURL", "https://repo.azisaba.net/repository/maven-snapshots/")
                    } else {
                        project.findProperty("deployReleasesURL")
                            ?: System.getProperty("deployReleasesURL", "https://repo.azisaba.net/repository/maven-releases/")
                    },
                )
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.reobfJar)
        }
    }
}
