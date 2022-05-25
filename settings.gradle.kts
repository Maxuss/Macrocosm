rootProject.name = "Macrocosm"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}
include("src:main:untitled")
findProject(":src:main:untitled")?.name = "untitled"
