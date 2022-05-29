package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.pluginKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic

object LethalityEnchantment :
    EnchantmentBase("Lethality", "Reduces the ${Statistic.DEFENSE.display}<gray> of your target by <green>[1.2]%<gray> each time yoy hit them. Stacks up to <green>[1]<gray> times.", 1..6, ItemType.melee(), conflicts = listOf("EXHALATION")) {
    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        if (e.damaged.isDead || e.damaged is Player)
            return
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val pdc = e.damaged.persistentDataContainer

        if (pdc.has(pluginKey("exhalation_stacks")))
            return
        if (!pdc.has(pluginKey("lethality_stacks"))) {
            pdc[pluginKey("lethality_stacks"), PersistentDataType.BYTE] = 0
        }
        val stacks = pdc[pluginKey("lethality_stacks"), PersistentDataType.BYTE]!!
        if (stacks >= lvl)
            return

        pdc[pluginKey("lethality_stacks"), PersistentDataType.BYTE] = (stacks + 1).toByte()

        val modifier = lvl * .012f
        val mc = e.damaged.macrocosm!!
        mc.baseStats.defense *= (1 - modifier)
        mc.loadChanges(e.damaged)
    }
}

object ExhalationEnchantment :
    EnchantmentBase("Exhalation", "Reduces the ${Statistic.DAMAGE.display}<gray> of your target by <green>[0.6]%<gray> each time you git them. Stacks up to <green>[1]<gray> times.", 1..6, ItemType.melee(), conflicts = listOf("LETHALITY")) {
    @EventHandler
    fun onDamage(e: PlayerDealDamageEvent) {
        if (e.damaged.isDead || e.damaged is Player)
            return
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        val pdc = e.damaged.persistentDataContainer
        if (pdc.has(pluginKey("lethality_stacks")))
            return
        if (!pdc.has(pluginKey("exhalation_stacks"))) {
            pdc[pluginKey("exhalation_stacks"), PersistentDataType.BYTE] = 0
        }
        val stacks = pdc[pluginKey("exhalation_stacks"), PersistentDataType.BYTE]!!
        if (stacks >= lvl)
            return

        pdc[pluginKey("exhalation_stacks"), PersistentDataType.BYTE] = (stacks + 1).toByte()

        val modifier = lvl * .006f
        val mc = e.damaged.macrocosm ?: return
        mc.baseStats.damage *= (1 - modifier)
        mc.loadChanges(e.damaged)
    }
}
