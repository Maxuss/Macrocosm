package space.maxus.macrocosm.entity

import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import space.maxus.macrocosm.item.MACROCOSM_TAG
import space.maxus.macrocosm.nms.DelegatedMacrocosmEntity
import space.maxus.macrocosm.util.getId

val LivingEntity.macrocosm: MacrocosmEntity? get() = Entities.toMacrocosm(this)

@Suppress("unused")
object Entities {
    fun toMacrocosmReloading(entity: LivingEntity): MacrocosmEntity? {
        if (entity is Player)
            return null
        val handle = (entity as? CraftEntity)?.handle
        if(handle is DelegatedMacrocosmEntity)
            return CustomEntity(entity.uniqueId)
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

    fun toMacrocosm(entity: LivingEntity): MacrocosmEntity? {
        if (entity is Player)
            return null
        val handle = (entity as? CraftEntity)?.handle
        if(handle is DelegatedMacrocosmEntity)
            return CustomEntity(entity.uniqueId)
        val tag = entity.readNbt()
        if (tag.contains(MACROCOSM_TAG)) {
            val id = tag.getCompound(MACROCOSM_TAG).getId("ID")
            if (id.namespace != "minecraft") {
                // custom.loadChanges(entity)
                return CustomEntity(entity.uniqueId)
            }
        }
        return VanillaEntity.from(entity)
    }
}
