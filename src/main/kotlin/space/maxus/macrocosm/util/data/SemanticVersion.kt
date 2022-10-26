package space.maxus.macrocosm.util.data

data class SemanticVersion(val release: Int, val major: Int, val minor: Int, val info: String) {
    companion object {
        fun fromString(str: String): SemanticVersion {
            val (version, info) = str.split("-").let { if(it.size < 2) it.toMutableList().add("release"); it }
            val (release, major, minor) = version.split(".").map(String::toInt)
            return SemanticVersion(release, major, minor, info)
        }
    }

    override fun toString() = "$release.$major.$minor-$info"
}
