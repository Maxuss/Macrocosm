package space.maxus.macrocosm.entity

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import space.maxus.macrocosm.item.MACROCOSM_TAG

val LivingEntity.macrocosm: MacrocosmEntity? get() = EntityRegistry.toMacrocosm(this)

@Suppress("unused")
object EntityRegistry {
    private val entities: HashMap<String, MacrocosmEntity> = hashMapOf()

    fun register(name: String, reforge: MacrocosmEntity): MacrocosmEntity {
        if (entities.containsKey(name))
            return reforge
        entities[name] = reforge
        return reforge
    }

    fun nameOf(ref: MacrocosmEntity) = entities.filter { (_, v) -> v == ref }.map { (k, _) -> k }.firstOrNull()

    fun find(name: String) = entities[name]

    fun toMacrocosm(entity: LivingEntity): MacrocosmEntity? {
        if (entity is Player)
            return null
        val tag = entity.readNbt()
        if (tag.contains(MACROCOSM_TAG)) {
            val id = tag.getCompound(MACROCOSM_TAG).getString("ID")
            if (id != "NULL") {
                val e = find(id)!!
                e.loadChanges(entity)
                return e
            }
        }
        return VanillaEntity.from(entity)
    }
}
