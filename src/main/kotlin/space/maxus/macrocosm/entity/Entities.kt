package space.maxus.macrocosm.entity

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import space.maxus.macrocosm.item.MACROCOSM_TAG
import space.maxus.macrocosm.util.getId

val LivingEntity.macrocosm: MacrocosmEntity? get() = Entities.toMacrocosm(this)

@Suppress("unused")
object Entities {
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
