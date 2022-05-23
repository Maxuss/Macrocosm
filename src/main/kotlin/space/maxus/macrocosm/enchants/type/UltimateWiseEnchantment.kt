package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.UltimateEnchantment
import space.maxus.macrocosm.events.AbilityCompileEvent
import space.maxus.macrocosm.events.AbilityCostApplyEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.text.comp
import kotlin.math.roundToInt

object UltimateWiseEnchantment :
    UltimateEnchantment("Ultimate Wise", "", 1..5, ItemType.weapons(), conflicts = listOf("ULTIMATE_BULK")) {
    override fun description(level: Int): List<Component> {
        val str = "Decreases <aqua>Mana Cost<gray> of your abilities by <yellow>${level * 10}%<gray>."
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

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
    UltimateEnchantment("Ultimate Bulk", "", 1..5, ItemType.weapons(), conflicts = listOf("ULTIMATE_WISE")) {
    override fun description(level: Int): List<Component> {
        val str = "Decreases <red>Health Cost<gray> of your abilities by <yellow>${level * 10}%<gray>."
        val reduced = str.reduceToList(25).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

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
