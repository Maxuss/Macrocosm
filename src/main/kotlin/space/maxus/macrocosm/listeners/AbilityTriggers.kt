package space.maxus.macrocosm.listeners

import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import space.maxus.macrocosm.events.PlayerLeftClickEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.events.PlayerSneakEvent
import space.maxus.macrocosm.item.ItemPet
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text

object AbilityTriggers : Listener {
    @EventHandler
    fun click(e: PlayerInteractEvent) {
        if (e.item == null || e.item?.type == Material.AIR)
            return
        if (e.action.isLeftClick) {
            val event = PlayerLeftClickEvent(e.player.macrocosm!!, e.item!!.macrocosm)
            event.callEvent()
        } else if (e.action.isRightClick) {
            if (e.item!!.macrocosm is ItemPet) {
                val item = e.item!!.macrocosm as ItemPet
                e.player.inventory.setItemInMainHand(null)
                e.player.macrocosm?.addPet(item.stored!!)
                e.player.sendMessage(text("<green>Added ${item.buildName().str()}<green> to your pet menu!"))
                sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP) {
                    playFor(e.player)
                }
            } else {
                val event = PlayerRightClickEvent(e.player.macrocosm!!, e.item!!.macrocosm)
                event.callEvent()
            }
        }
    }

    @EventHandler
    fun sneak(e: PlayerToggleSneakEvent) {
        val event = PlayerSneakEvent(e.player.macrocosm!!)
        event.callEvent()
    }
}
