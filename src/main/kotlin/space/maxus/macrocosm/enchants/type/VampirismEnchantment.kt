package space.maxus.macrocosm.enchants.type

import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic

object VampirismEnchantment :
    EnchantmentBase(
        "Vampirism",
        "Heals for <green>[1]%<gray> of your missing ${Statistic.HEALTH.display}<gray> upon killing an enemy.",
        1..6,
        ItemType.melee(),
        conflicts = listOf("MANA_EXHAUSTION")
    ) {
    @EventHandler
    fun onKill(e: PlayerKillEntityEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val stats = e.player.stats()!!
        val missing = stats.health - e.player.currentHealth
        val ratio = missing / stats.health
        val amount = missing * (ratio * (lvl * .01f))
        e.player.heal(amount, stats)
    }
}

object ManaExhaustionEnchantment :
    EnchantmentBase(
        "Mana Exhaustion",
        "Regain <green>[2]%<gray> of your missing <aqua>${Statistic.INTELLIGENCE.specialChar} Mana<gray> upon killing an enemy.",
        1..6,
        ItemType.melee(),
        conflicts = listOf("VAMPIRISM")
    ) {
    @EventHandler
    fun onKill(e: PlayerKillEntityEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val stats = e.player.stats()!!
        val missing = stats.intelligence - e.player.currentMana
        val ratio = missing / stats.intelligence
        val amount = missing * (ratio * (lvl * .02f))
        e.player.heal(amount, stats)
    }
}
