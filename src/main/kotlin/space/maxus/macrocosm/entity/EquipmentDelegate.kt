package space.maxus.macrocosm.entity

import net.axay.kspigot.extensions.server
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
import java.util.*
import kotlin.reflect.KProperty

fun equipment(entity: UUID, slot: EquipmentSlot) = EquipmentDelegate(slot, entity)

class EquipmentDelegate internal constructor(private val slot: EquipmentSlot, private val entity: UUID) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) =
        (server.getEntity(entity) as? LivingEntity)?.equipment?.getItem(slot)?.macrocosm

    operator fun setValue(thisRef: Any?, property: KProperty<*>, item: MacrocosmItem?) =
        (server.getEntity(entity) as? LivingEntity)?.equipment?.setItem(slot, item?.build())
}
