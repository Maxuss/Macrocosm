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
import kotlin.math.min

object GiantKillerEnchantment :
    EnchantmentBase("Giant Killer", "", 1..7, ItemType.melee(), conflicts = listOf("TITAN_KILLER")) {
    override fun description(level: Int): List<Component> {
        val str =
            "Increases ${Statistic.DAMAGE.display}<gray> you deal by <green>${Formatting.stats((0.02 * level).toBigDecimal())}%<gray> for each percent of extra ${Statistic.HEALTH.display}<gray> that your target has above you up to <green>${
                Formatting.stats(
                    (5 * level).toBigDecimal(),
                    true
                )
            }%<gray>."
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
        val cap = lvl * .05f

        val stats = e.damaged.macrocosm!!.calculateStats()
        val playerStats = e.player.stats()!!
        val health = stats.health
        val ratio = health / playerStats.health
        if (ratio < 1f)
            return
        val percentage = min(ratio / 100f, cap)
        val extra = (1 + (percentage * (1 + modifier)))
        e.damage *= extra
    }
}

object TitanKillerEnchantment :
    EnchantmentBase("Titan Killer", "", 1..7, ItemType.melee(), conflicts = listOf("GIANT_KILLER")) {
    override fun description(level: Int): List<Component> {
        val str =
            "Increases ${Statistic.DAMAGE.display}<gray> you deal by <green>${Formatting.stats((2 * level).toBigDecimal())}%<gray> for each percent of extra ${Statistic.DEFENSE.display}<gray> that your target has above you up to <green>${
                Formatting.stats(
                    (10 * level).toBigDecimal(),
                    true
                )
            }%<gray>."
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

        val modifier = lvl * .02f
        val cap = lvl * .1f

        val stats = e.damaged.macrocosm!!.calculateStats()
        val playerStats = e.player.stats()!!
        val defense = stats.defense
        val ratio = defense / playerStats.defense
        if (ratio < 1f)
            return
        val percentage = min(ratio / 100f, cap)
        val extra = (1 + (percentage * (1 + modifier)))
        e.damage *= extra
    }
}
