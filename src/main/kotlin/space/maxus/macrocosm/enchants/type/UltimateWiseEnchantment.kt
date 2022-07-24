package space.maxus.macrocosm.enchants.type

import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.enchants.UltimateEnchantment
import space.maxus.macrocosm.events.AbilityCostApplyEvent
import space.maxus.macrocosm.events.CostCompileEvent
import space.maxus.macrocosm.item.ItemType
import kotlin.math.roundToInt

object UltimateWiseEnchantment :
    UltimateEnchantment(
        "Ultimate Wise",
        "Decreases <aqua>Mana Cost<gray> of your abilities by <yellow>[10]%<gray>.",
        1..5,
        ItemType.weaponsWand(),
        conflicts = listOf("ULTIMATE_BULK", "ULTIMATE_CONTROL")
    ) {
    @EventHandler
    fun onAbilityUse(e: AbilityCostApplyEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        e.mana = (e.mana * (1 - (lvl * .1f))).roundToInt()
    }

    @EventHandler
    fun onAbilityCompile(e: CostCompileEvent) {
        val (ok, lvl) = ensureRequirements(e.item)
        if (!ok)
            return

        val cost = e.cost ?: return
        e.cost = AbilityCost((cost.mana * (1 - (lvl * .1f))).roundToInt(), cost.health, cost.cooldown)
    }
}

object UltimateBulkEnchantment :
    UltimateEnchantment(
        "Ultimate Bulk",
        "Decreases <red>Health Cost<gray> of your abilities by <yellow>[10]%<gray>.",
        1..5,
        ItemType.weaponsWand(),
        conflicts = listOf("ULTIMATE_WISE", "ULTIMATE_CONTROL")
    ) {
    @EventHandler
    fun onAbilityUse(e: AbilityCostApplyEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        e.health = (e.health * (1 - (lvl * .1f))).roundToInt()
    }

    @EventHandler
    fun onAbilityCompile(e: CostCompileEvent) {
        val (ok, lvl) = ensureRequirements(e.item)
        if (!ok)
            return

        val cost = e.cost ?: return
        e.cost = AbilityCost(cost.mana, (cost.health * (1 - (lvl * .1f))).roundToInt(), cost.cooldown)
    }
}

object UltimateControlEnchantment : UltimateEnchantment(
    "Ultimate Control",
    "Decreases <light_purple>Summoning Difficulty<gray> of your abilities by <yellow>[25]%<gray>.",
    1..2,
    ItemType.weaponsWand(),
    conflicts = listOf("ULTIMATE_BULK", "ULTIMATE_WISE")
) {
    @EventHandler
    fun onAbilityUse(e: AbilityCostApplyEvent) {
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return

        e.summonDifficulty = (e.summonDifficulty * (1 - (lvl * .25f))).roundToInt()
    }

    @EventHandler
    fun onAbilityCompile(e: CostCompileEvent) {
        val (ok, lvl) = ensureRequirements(e.item)
        if (!ok)
            return

        val cost = e.cost ?: return
        e.cost = AbilityCost(
            cost.mana,
            cost.health,
            cost.cooldown,
            (cost.summonDifficulty * (1 - (lvl * .25f))).roundToInt()
        )
    }
}
