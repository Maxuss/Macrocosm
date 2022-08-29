plugins {
    kotlin("jvm") version "1.7.0"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("io.papermc.paperweight.userdev") version "1.3.7"
    id("org.hidetake.swagger.generator") version "2.19.2"
}

group = "space.maxus"
version = "0.2.1-prealpha"
val apiVersion = "0.6.2-alpha"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://repo.md-5.net/content/groups/public/") }
    maven { url = uri("https://jitpack.io") }
}

val exposedVersion: String by project

dependencies {
    paperDevBundle("1.19-R0.1-SNAPSHOT")
    implementation("net.axay:kspigot:1.19.0")
    implementation("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    implementation("LibsDisguises:LibsDisguises:10.0.28-SNAPSHOT") {
        exclude("org.spigotmc")
    }
    implementation("io.ktor:ktor-server-core-jvm:2.1.0")
    implementation("io.ktor:ktor-server-netty-jvm:2.1.0")
    implementation("io.ktor:ktor-server-default-headers-jvm:2.1.0")
    implementation("io.ktor:ktor-server-status-pages-jvm:2.1.0")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
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
        for((a, b) in System.getProperties()) {
            val n = a.toString()
            if(n.startsWith("macrocosm"))
                systemProperties[n] = b
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
