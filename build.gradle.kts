plugins {
    kotlin("jvm") version "1.7.0"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("io.papermc.paperweight.userdev") version "1.3.7"
}

group = "space.maxus"
version = "1.0"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven { url = uri("https://repo.md-5.net/content/groups/public/") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    paperDevBundle("1.19-R0.1-SNAPSHOT")
    implementation("net.axay:kspigot:1.19.0")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
    implementation("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    implementation("LibsDisguises:LibsDisguises:10.0.28-SNAPSHOT") {
        exclude("org.spigotmc")
    }
    implementation("io.ktor:ktor-server-core-jvm:2.0.3")
    implementation("io.ktor:ktor-server-netty-jvm:2.0.3")
    implementation("io.ktor:ktor-server-default-headers-jvm:2.0.3")
}

tasks {
    build {
        dependsOn(reobfJar)
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
