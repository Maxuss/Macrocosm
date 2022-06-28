package space.maxus.macrocosm.generators

import space.maxus.macrocosm.util.GSON
import java.util.concurrent.ConcurrentHashMap

interface MetaFile {
    fun string(): String {
        return GSON.toJson(this)
    }
}

data class AnimationData(val animation: Animation): MetaFile
open class Animation(frames: Int, val frametime: Int = 2, val interpolate: Boolean = false) {
    open val frames: List<Int> = (0 until frames).toList()
}

class RawAnimation(override val frames: List<Int>, frametime: Int = 2, interpolate: Boolean = false): Animation(-1, frametime, interpolate)

object MetaGenerator: ResGenerator {
    private val enqueued: ConcurrentHashMap<String, String> = ConcurrentHashMap()

    fun enqueue(path: String, file: MetaFile) {
        enqueued["$path.mcmeta"] = file.string()
    }

    override fun yieldGenerate(): Map<String, String> {
        return enqueued
    }
}
