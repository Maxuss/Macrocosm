package space.maxus.macrocosm.listeners

import net.axay.kspigot.extensions.pluginKey
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import space.maxus.macrocosm.entity.Entities
import space.maxus.macrocosm.entity.readNbt
import space.maxus.macrocosm.item.MACROCOSM_TAG

object EntityHandlers : Listener {
    @EventHandler
    fun entitySpawn(e: CreatureSpawnEvent) {
        val entity = e.entity
        if(entity.persistentDataContainer.has(pluginKey("__IGNORE")))
            return
        if (entity.readNbt().contains(MACROCOSM_TAG) || entity is Player || entity is ArmorStand)
            return

        Entities.toMacrocosm(entity)
    }
}
