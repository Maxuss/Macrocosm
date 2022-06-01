package space.maxus.macrocosm.generators

import space.maxus.macrocosm.util.GSON
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Prefix for custom model data numbers as stated in
 * https://mc-datapacks.github.io/en/conventions/custom_model_id.html
 *
 * Currently supports numbers of 4 numbers length
 */
const val MODEL_PREFIX = 732_0000

class Model(data: Int, val from: String, val to: String, val parent: String = "item/generated") {
    val data: Int = MODEL_PREFIX + data
}

object CMDGenerator: ResGenerator {
    private val enqueued: ConcurrentLinkedQueue<Model> = ConcurrentLinkedQueue()

    fun enqueue(model: Model) {
        enqueued.add(model)
    }

    private fun build(value: Model): ModelObject {
        return ModelObject(value.parent, ModelTextures(value.from), listOf(ModelOverride(ModelPredicate(value.data), value.to)))
    }

    override fun yieldGenerate(): Map<String, String> {
        val map = hashMapOf<String, String>()
        val amounts = hashMapOf<String, Int>()
        for(it in enqueued) {
            val texture = "assets/minecraft/models/${it.from}.json"
            if(map.contains(texture) || amounts.contains(it.from)) {
                val numbered = amounts[it.from] ?: 0
                map.remove(texture)
                amounts[it.from] = numbered + 1
                map["assets/minecraft/models/${it.from}/$numbered.json"] = GSON.toJson(build(it))
            } else map[texture] = GSON.toJson(build(it))
        }
        return map
    }

    private data class ModelObject(val parent: String, val textures: ModelTextures, val overrides: List<ModelOverride>)

    private data class ModelTextures(val layer0: String)

    private data class ModelOverride(val predicate: ModelPredicate, val model: String)

    private data class ModelPredicate(val custom_model_data: Int)
}
