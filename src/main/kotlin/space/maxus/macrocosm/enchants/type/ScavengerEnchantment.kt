package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.text.comp

object ScavengerEnchantment: EnchantmentBase("Scavenger", "", 1..5, ItemType.weapons()) {
    override fun description(level: Int): List<Component> {
        val str = "Scavenge <gold>${Formatting.stats((level * .3).toBigDecimal())} Coins<gray> per monster level on kill."
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler
    fun onKill(e: PlayerKillEntityEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)
        if(!ok)
            return

        if(e.killed is Player)
            return
        val mc = e.killed.macrocosm!!
        val amount = lvl * .3f * mc.level
        e.player.purse += amount
        sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP) {
            pitch = 2f
            volume = 0.2f
            playFor(e.player.paper!!)
        }
    }
}
