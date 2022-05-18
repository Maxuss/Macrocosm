package space.maxus.macrocosm.reforge.types

import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.events.AbilityCompileEvent
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
    fun onAbilityCompile(e: AbilityCompileEvent) {
        if (e.item.reforge == null || e.item.reforge != this)
            return
        val cost = e.ability.cost ?: return
        val clonedCost = AbilityCost((cost.mana * .75).roundToInt(), cost.health, (cost.cooldown * .75).roundToInt())
        val interceptor = ItemAbility.Interceptor(e.ability, cost = clonedCost)

        val outputLore = mutableListOf<Component>()
        interceptor.buildLore(outputLore, null)
        e.lore = outputLore
    }

    override fun clone(): Reforge {
        return UndulantReforge
    }
}
