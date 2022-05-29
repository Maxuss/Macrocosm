package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.events.CostCompileEvent
import space.maxus.macrocosm.events.AbilityCostApplyEvent
import kotlin.math.roundToInt

object WiseDragonBonus: FullSetBonus("Wise Blood", "Decreases <aqua>Mana Cost<gray> of abilities by <gold>33%<gray>.") {
    override fun registerListeners() {
        listen<AbilityCostApplyEvent> { e ->
            if(!ensureSetRequirement(e.player))
                return@listen
            e.mana = (e.mana * .67).roundToInt()
        }

        listen<CostCompileEvent> { e ->
            if(e.player == null || !ensureSetRequirement(e.player))
                return@listen
            e.cost ?: return@listen
            e.cost = AbilityCost((e.cost!!.mana * .67).roundToInt(), e.cost!!.health, e.cost!!.cooldown)
        }
    }
}
