package space.maxus.macrocosm.entity

import net.axay.kspigot.extensions.pluginManager
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.item.MACROCOSM_TAG
import space.maxus.macrocosm.util.Identifier
import space.maxus.macrocosm.util.getId
import java.util.concurrent.ConcurrentHashMap

val LivingEntity.macrocosm: MacrocosmEntity? get() = EntityRegistry.toMacrocosm(this)

@Suppress("unused")
object EntityRegistry {
    private val entities: ConcurrentHashMap<Identifier, MacrocosmEntity> = ConcurrentHashMap(hashMapOf())

    fun register(name: Identifier, entity: MacrocosmEntity): MacrocosmEntity {
        if (entities.containsKey(name))
            return entity
        entities[name] = entity
        pluginManager.registerEvents(entity, Macrocosm)
        return entity
    }

    fun nameOf(ref: MacrocosmEntity) = entities.filter { (_, v) -> v == ref }.map { (k, _) -> k }.firstOrNull()

    fun find(name: Identifier) = entities[name]

    fun toMacrocosm(entity: LivingEntity): MacrocosmEntity? {
        if (entity is Player)
            return null
        val tag = entity.readNbt()
        if (tag.contains(MACROCOSM_TAG)) {
            val id = tag.getCompound(MACROCOSM_TAG).getId("ID")
            if (id.namespace != "minecraft") {
                val custom = CustomEntity(entity.uniqueId)
                custom.loadChanges(entity)
                return custom
            }
        }
        return VanillaEntity.from(entity)
    }
}
