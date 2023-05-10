import java.net.URL

plugins {
    kotlin("jvm") version "1.8.21"
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("io.papermc.paperweight.userdev") version "1.5.3"
    id("org.hidetake.swagger.generator") version "2.19.2"
    id("org.jetbrains.dokka") version "1.8.10"

}

group = "space.maxus"
version = "0.5.2-alpha"
val apiVersion = "0.6.5-alpha"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://repo.md-5.net/content/groups/public/") }
//    maven { url = uri("https://repo.repsy.io/mvn/maxuss/artifacts/") } needed this when KSpigot was not in maven central
    maven { url = uri("https://jitpack.io") }
}

val exposedVersion: String by project

dependencies {
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")
    implementation("net.axay:kspigot:1.19.2")
    implementation("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    implementation("LibsDisguises:LibsDisguises:10.0.33") {
        exclude("org.spigotmc")
        exclude("com.github.dmulloy2") // that's weird
    }
    implementation("io.ktor:ktor-client-core:2.3.0")
    implementation("io.ktor:ktor-client-java:2.3.0")
    implementation("io.ktor:ktor-server-core-jvm:2.2.4")
    implementation("io.ktor:ktor-server-netty-jvm:2.2.4")
    implementation("io.ktor:ktor-server-default-headers-jvm:2.2.4")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.2.4")
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
        minecraftVersion("1.19.4")
        jvmArgs("-Xms2G", "-Xmx2G")
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
