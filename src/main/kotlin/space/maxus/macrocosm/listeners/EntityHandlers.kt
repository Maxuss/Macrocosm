package space.maxus.macrocosm.listeners

import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.runnables.task
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import space.maxus.macrocosm.entity.Entities
import space.maxus.macrocosm.entity.readNbt
import space.maxus.macrocosm.item.MACROCOSM_TAG
import space.maxus.macrocosm.nms.NativeMacrocosmEntity

object EntityHandlers : Listener {
    @EventHandler
    fun entitySpawn(e: CreatureSpawnEvent) {
        val entity = e.entity
        if(entity.persistentDataContainer.has(pluginKey("__IGNORE")))
            return
        if (entity.readNbt().contains(MACROCOSM_TAG) || entity is Player || entity is ArmorStand)
            return

        if((entity as CraftEntity).handle is NativeMacrocosmEntity) {
            // delaying task to let it spawn
            task(delay = 20L) {
                Entities.toMacrocosmReloading(entity)
            }
        } else {
            Entities.toMacrocosmReloading(entity)
        }
    }
}
