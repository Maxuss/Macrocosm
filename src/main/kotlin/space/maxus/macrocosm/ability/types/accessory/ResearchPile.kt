package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.events.PlayerReceiveExpEvent

object ResearchPile: AccessoryAbility("research_pile", "Gain <green>+5%<gray> more <aqua>Skill Experience<gray>.") {
    override fun registerListeners() {
        listen<PlayerReceiveExpEvent> { e ->
            if(!hasAccs(e.player))
                return@listen
            e.amount *= 1.05f
        }
    }
}
