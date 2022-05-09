package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.comp

object ExecuteEnchantment : EnchantmentBase("Execute", "", 1..6, ItemType.melee(), conflicts = listOf("PROSECUTE")) {
    override fun description(level: Int): List<Component> {
        val str =
            "<gray>Increases ${Statistic.DAMAGE.display}<gray> you deal by <green>${Formatting.stats((level * .2f).toBigDecimal())}%<gray> for every percent of ${Statistic.HEALTH.display}<blue> missing<gray> on your target."
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        if (e.damaged.isDead)
            return
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val modifier = lvl * .002f
        val mc = e.damaged.macrocosm!!
        val damagedStats = mc.calculateStats()
        val missing = damagedStats.health - mc.currentHealth
        val ratio = missing / damagedStats.health
        e.damage *= (1 + (ratio * modifier * 100))
    }
}

object ProsecuteEnchantment : EnchantmentBase("Prosecute", "", 1..6, ItemType.melee(), conflicts = listOf("EXECUTE")) {
    override fun description(level: Int): List<Component> {
        val str =
            "<gray>Increases ${Statistic.DAMAGE.display}<gray> you deal by <green>${Formatting.stats((level * .15f).toBigDecimal())}%<gray> for every percent of ${Statistic.HEALTH.display}<blue> missing<gray> on your target."
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        if (e.damaged.isDead)
            return
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val modifier = lvl * .0015f
        val mc = e.damaged.macrocosm!!
        val damagedStats = mc.calculateStats()
        val ratio = mc.currentHealth / damagedStats.health
        e.damage *= (1 + (ratio * modifier * 100))
    }
}
