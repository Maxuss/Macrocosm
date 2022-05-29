package space.maxus.macrocosm.enchants.type

import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.enchants.UltimateEnchantment
import space.maxus.macrocosm.events.AbilityCompileEvent
import space.maxus.macrocosm.events.AbilityCostApplyEvent
import space.maxus.macrocosm.item.ItemType
import kotlin.math.roundToInt

object UltimateWiseEnchantment :
    UltimateEnchantment("Ultimate Wise", "Decreases <aqua>Mana Cost<gray> of your abilities by <yellow>[10]%<gray>.", 1..5, ItemType.weapons(), conflicts = listOf("ULTIMATE_BULK")) {
    @EventHandler
    fun onAbilityUse(e: AbilityCostApplyEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        e.mana = (e.mana * (1 - (lvl * .1f))).roundToInt()
    }

    @EventHandler
    fun onAbilityCompile(e: AbilityCompileEvent) {
        val (ok, lvl) = ensureRequirements(e.item)
        if (!ok)
            return

        val cost = e.ability.cost ?: return
        val clonedCost = AbilityCost((cost.mana * (1 - (lvl * .1f))).roundToInt(), cost.health, cost.cooldown)
        val interceptor = MacrocosmAbility.Interceptor(e.ability, cost = clonedCost)

        val outputLore = mutableListOf<Component>()
        interceptor.buildLore(outputLore, null)
        e.lore = outputLore
    }
}

object UltimateBulkEnchantment :
    UltimateEnchantment("Ultimate Bulk", "Decreases <red>Health Cost<gray> of your abilities by <yellow>[10]%<gray>.", 1..5, ItemType.weapons(), conflicts = listOf("ULTIMATE_WISE")) {
    @EventHandler
    fun onAbilityUse(e: AbilityCostApplyEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        e.mana = (e.mana * (1 - (lvl * .1f))).roundToInt()
    }

    @EventHandler
    fun onAbilityCompile(e: AbilityCompileEvent) {
        val (ok, lvl) = ensureRequirements(e.item)
        if (!ok)
            return

        val cost = e.ability.cost ?: return
        val clonedCost = AbilityCost(cost.mana, (cost.health * (1 - (lvl * .1f))).roundToInt(), cost.cooldown)
        val interceptor = MacrocosmAbility.Interceptor(e.ability, cost = clonedCost)

        val outputLore = mutableListOf<Component>()
        interceptor.buildLore(outputLore, null)
        e.lore = outputLore
    }
}
