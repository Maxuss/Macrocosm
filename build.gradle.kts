import java.net.URL

plugins {
    kotlin("jvm") version "1.8.0"
    id("xyz.jpenilla.run-paper") version "2.0.0"
    id("io.papermc.paperweight.userdev") version "1.3.11"
    id("org.hidetake.swagger.generator") version "2.19.2"
    id("org.jetbrains.dokka") version "1.7.20"

}

group = "space.maxus"
version = "0.5.0-alpha"
val apiVersion = "0.6.5-alpha"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://repo.md-5.net/content/groups/public/") }
    maven { url = uri("https://jitpack.io") }
}

val exposedVersion: String by project

dependencies {
    paperDevBundle("1.19.3-R0.1-SNAPSHOT")
    implementation("net.axay:kspigot:1.19.1")
    implementation("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    implementation("LibsDisguises:LibsDisguises:10.0.31-SNAPSHOT") {
        exclude("org.spigotmc")
    }
    implementation("io.ktor:ktor-server-core-jvm:2.2.1")
    @Suppress("VulnerableLibrariesLocal") // suppressing while the stable update is not out
    implementation("io.ktor:ktor-server-netty-jvm:2.2.1")
    implementation("io.ktor:ktor-server-default-headers-jvm:2.2.1")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.2.1")
    implementation("net.dv8tion:JDA:5.0.0-beta.2") {
        exclude(module = "opus-java")
    }
    implementation("club.minnced:discord-webhooks:0.8.2")
    implementation("org.litote.kmongo:kmongo:4.8.0")
    implementation("io.prometheus:simpleclient:0.16.0")
    implementation("io.prometheus:simpleclient_httpserver:0.16.0")
    implementation(kotlin("stdlib-jdk8"))
}

tasks {
    val generateVersionInfo = task("generateVersionInfo") {
        val versionInfo = file("$rootDir/src/main/resources/MACROCOSM_VERSION_INFO")
        versionInfo.writeText("""{"version":"$version","apiVersion":"$apiVersion"}""")
    }
    build {
        dependsOn(reobfJar, generateVersionInfo)
        doLast {
            generateReDoc.get().exec()
        }
    }
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(17)
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }
    runServer {
        System.getProperties().forEach { key, value ->
            val str = key.toString()
            if (str.startsWith("macrocosm")) {
                systemProperty(str, value)
            }
        }
    }
}

tasks.dokkaHtml.configure {
    dokkaSourceSets {
        named("main") {
            moduleName.set("Macrocosm")
            includes.from("docs/Module.md")
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/Maxuss/Macrocosm/tree/master/src/main/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}

tasks.generateReDoc.configure {
    inputFile = file("$rootDir/src/main/resources/swagger.yml")
    outputDir = file("$rootDir/src/main/resources/doc")
    options = mapOf(
        "spec-url" to "./doc/swagger.yml"
    )
    doLast {
        copy {
            from("$rootDir/src/main/resources/pack/pack.png")
            into("$rootDir/src/main/resources/doc")
        }
    }
}
