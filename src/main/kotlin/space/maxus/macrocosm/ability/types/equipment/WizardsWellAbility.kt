package space.maxus.macrocosm.ability.types.equipment

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.EquipmentAbility
import space.maxus.macrocosm.events.AbilityCostApplyEvent
import space.maxus.macrocosm.events.CostCompileEvent
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerTickEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.metrics.report
import kotlin.math.roundToInt

object CharmOfManaAbility: EquipmentAbility("Mana Charm", "Regenerate additional <aqua>2% ${Statistic.INTELLIGENCE.specialChar} Mana<gray> each second.") {
    override fun registerListeners() {
        listen<PlayerTickEvent> { e ->
            if(!ensureRequirements(e.player, ItemType.NECKLACE, ItemType.BELT))
                return@listen
            e.player.currentMana += e.player.stats()!!.intelligence * .02f
        }
    }
}

object TalismanOfManaAbility: EquipmentAbility("Mana Talisman", "Increases your total ${Statistic.INTELLIGENCE.display}<gray> by <aqua>+5%<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player, ItemType.NECKLACE, ItemType.BELT))
                return@listen
            e.stats.intelligence *= 1.05f
        }
    }
}

object ManaOrbAbility: EquipmentAbility("Mana Orb", "Decreases <aqua>Mana Cost<gray> of item abilities by <green>10%<gray>.") {
    override fun registerListeners() {
        listen<AbilityCostApplyEvent> { e ->
            if(!ensureRequirements(e.player, ItemType.NECKLACE, ItemType.BELT))
                return@listen
            e.mana = e.mana.toFloat() * .9f
        }
        listen<CostCompileEvent> { e ->
            if(e.player == null || !ensureRequirements(e.player, ItemType.NECKLACE, ItemType.BELT))
                return@listen
            val cost = e.cost ?: return@listen
            e.cost = AbilityCost((cost.mana.toFloat() * .9f).roundToInt(), cost.health, cost.cooldown, cost.summonDifficulty)
        }
    }
}

object WizardsWellAbility1: EquipmentAbility("Infinite Knowledge", "Increases your ${Statistic.INTELLIGENCE.display}<gray> by <aqua>10%<gray>, and decreases <green>Cooldown<gray> of item abilities by <green>25%<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player, ItemType.NECKLACE, ItemType.BELT))
                return@listen
            e.stats.intelligence *= 1.10f
        }
        listen<AbilityCostApplyEvent> { e ->
            if(!ensureRequirements(e.player, ItemType.NECKLACE, ItemType.BELT))
                return@listen
            e.mana = e.mana.toFloat() * .9f
        }
        listen<CostCompileEvent> { e ->
            if(e.player == null || !ensureRequirements(e.player, ItemType.NECKLACE, ItemType.BELT))
                return@listen
            val cost = e.cost ?: return@listen
            e.cost = AbilityCost(cost.mana, cost.health, cost.cooldown.toFloat() * .75f, cost.summonDifficulty)
        }
    }
}

object WizardsWellAbility2: EquipmentAbility("Wistful Steadiness", "Regenerate additional <aqua>5% ${Statistic.INTELLIGENCE.specialChar} Mana<gray> while standing still.") {
    override fun registerListeners() {
        listen<PlayerTickEvent> { e ->
            if(!ensureRequirements(e.player, ItemType.NECKLACE, ItemType.BELT))
                return@listen
            val p = e.player.paper ?: report("Player was null in TickEvent!") { return@listen }
            val len = p.velocity.length()
            if (len > 0.15f)
                return@listen

            e.player.currentMana += e.player.stats()!!.intelligence * .05f
        }
    }
}
