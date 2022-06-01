package space.maxus.macrocosm.generators

import space.maxus.macrocosm.util.GSON
import java.util.concurrent.ConcurrentLinkedQueue

object TexturedModelGenerator: ResGenerator {
    private val enqueued: ConcurrentLinkedQueue<Model> = ConcurrentLinkedQueue()

    fun enqueue(model: Model) {
        enqueued.add(model)
    }

    private fun build(model: Model): TexturedModel {
        return TexturedModel(model.parent, ModelTextures(model.to))
    }

    override fun yieldGenerate(): Map<String, String> {
        return enqueued.associate { "assets/macrocosm/models/${it.to.replace("macrocosm:", "")}.json" to GSON.toJson(build(it)) }
    }

    private data class TexturedModel(val parent: String, val textures: ModelTextures)

    private data class ModelTextures(val layer0: String)
}
