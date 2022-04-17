package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.comp

object VampirismEnchantment: EnchantmentBase("Vampirism", "", 1..6, ItemType.melee(), conflicts = listOf("MANA_EXHAUSTION")) {
    override fun description(level: Int): List<Component> {
        val str = "Heals for <green>$level%<gray> of your missing ${Statistic.HEALTH.display}<gray> upon killing an enemy."
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler
    fun onKill(e: PlayerKillEntityEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if(!ok)
            return

        val stats = e.player.calculateStats()!!
        val missing = stats.health - e.player.currentHealth
        val ratio = missing / stats.health
        val amount = missing * (ratio * (lvl * .01f))
        e.player.heal(amount, stats)
    }
}

object ManaExhaustionEnchantment:  EnchantmentBase("Mana Exhaustion", "", 1..6, ItemType.melee(), conflicts = listOf("VAMPIRISM")) {
    override fun description(level: Int): List<Component> {
        val str = "Regain <green>${level * 2}%<gray> of your missing <aqua>${Statistic.INTELLIGENCE.specialChar} Mana<gray> upon killing an enemy."
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler
    fun onKill(e: PlayerKillEntityEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if(!ok)
            return

        val stats = e.player.calculateStats()!!
        val missing = stats.intelligence - e.player.currentMana
        val ratio = missing / stats.intelligence
        val amount = missing * (ratio * (lvl * .02f))
        e.player.heal(amount, stats)
    }
}
