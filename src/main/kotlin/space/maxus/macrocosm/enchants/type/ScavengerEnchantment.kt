package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.item.ItemType

object ScavengerEnchantment : EnchantmentBase(
    "Scavenger",
    "Scavenge <gold>[0.3] Coins<gray> per monster level on kill.",
    1..5,
    ItemType.weapons()
) {
    @EventHandler
    fun onKill(e: PlayerKillEntityEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)
        if (!ok)
            return

        if (e.killed is Player)
            return
        val mc = e.killed.macrocosm!!
        val amount = lvl * .3f * mc.level
        e.player.purse += amount.toBigDecimal()
        sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP) {
            pitch = 2f
            volume = 0.2f
            playFor(e.player.paper!!)
        }
    }
}
