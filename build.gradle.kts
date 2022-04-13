plugins {
  kotlin("jvm") version "1.6.20"
  id("xyz.jpenilla.run-paper") version "1.0.6"
  id("io.papermc.paperweight.userdev") version "1.3.5"
}

group = "space.maxus"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  paperDevBundle("1.18.2-R0.1-SNAPSHOT")
  implementation("net.axay:kspigot:1.18.2")
  implementation("org.xerial:sqlite-jdbc:3.36.0.3")
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
  }
}
