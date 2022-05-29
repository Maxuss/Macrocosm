package space.maxus.macrocosm.reforge.types

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.events.CostCompileEvent
import space.maxus.macrocosm.events.AbilityCostApplyEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.reforge.ReforgeBase
import space.maxus.macrocosm.stats.stats
import kotlin.math.roundToInt

object UndulantReforge : ReforgeBase(
    "Undulant",
    "Swirling Mana",
    "Reduces <aqua>Mana Cost<gray> and <green>Cooldown<gray> of abilities by <blue>25%<gray>.",
    ItemType.weapons(),
    stats {
        intelligence = 40f
        abilityDamage = 3f
        critDamage = -5f
    }
) {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onCalculate(e: AbilityCostApplyEvent) {
        if (!ensureRequirements(e.player, EquipmentSlot.HAND))
            return
        e.mana = (e.mana * .75f).roundToInt()
        e.cooldown = (e.cooldown * .75f).roundToInt()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onAbilityCompile(e: CostCompileEvent) {
        if (e.item.reforge == null || e.item.reforge != this)
            return
        val cost = e.cost ?: return
        e.cost = AbilityCost((cost.mana * .75).roundToInt(), cost.health, (cost.cooldown * .75).roundToInt())
    }

    override fun clone(): Reforge {
        return UndulantReforge
    }
}
