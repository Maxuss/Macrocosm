package space.maxus.macrocosm.generators

import com.google.common.collect.Multimap
import space.maxus.macrocosm.util.GSON
import space.maxus.macrocosm.util.multimap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Prefix for custom model data numbers as stated in
 * https://mc-datapacks.github.io/en/conventions/custom_model_id.html
 *
 * Currently supports numbers of 4 numbers length
 */
const val MODEL_PREFIX = 732_0000

open class Model(data: Int, val from: String, val to: String, val parent: String = "item/generated") {
    val data: Int = MODEL_PREFIX + data

    companion object {
        private val id: AtomicInteger = AtomicInteger(0)

        fun nextId(): Int {
            return id.getAndIncrement()
        }
    }
}

class RawModel(data: Int, from: String, to: String): Model(data, from, to)

object CMDGenerator: ResGenerator {
    private val enqueued: ConcurrentLinkedQueue<Model> = ConcurrentLinkedQueue()

    fun enqueue(model: Model) {
        enqueued.add(model)
    }

    private fun build(value: Model): ModelObject {
        return ModelObject(value.parent, ModelTextures(value.from), listOf(ModelOverride(ModelPredicate(value.data), value.to)))
    }

    override fun yieldGenerate(): Map<String, String> {
        val map: Multimap<String, Model> = multimap()
        for(it in enqueued) {
            val texture = "assets/minecraft/models/${it.from}.json"
            map.put(texture, it)
        }
        return map.keySet().associateWith { path ->
            val values = map[path]
            val proto = values.first()
            GSON.toJson(ModelObject(proto.parent, ModelTextures(proto.from), values.sortedBy { model -> model.data }.map { model -> ModelOverride(ModelPredicate(model.data), model.to) }))
        }.toMap()
    }

    private data class ModelObject(val parent: String, val textures: ModelTextures, val overrides: List<ModelOverride>)

    private data class ModelTextures(val layer0: String)

    private data class ModelOverride(val predicate: ModelPredicate, val model: String)

    private data class ModelPredicate(val custom_model_data: Int)
}
