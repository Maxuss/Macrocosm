package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.events.ItemCalculateStatsEvent
import space.maxus.macrocosm.stats.Statistic

object StrongDragonBonus : FullSetBonus(
    "Strong Blood",
    "Buffs <blue>Aspect of the End<gray> and adds <red>+100 ${Statistic.DAMAGE.display}<gray> and <red>+75 ${Statistic.STRENGTH.display}<gray> to it."
) {
    override fun registerListeners() {
        listen<ItemCalculateStatsEvent> { e ->
            if (e.player == null || !ensureSetRequirement(e.player))
                return@listen
            if (e.item.id.path == "aspect_of_the_end" || e.item.id.path == "aspect_of_the_void") {
                e.stats.strength += 75f
                e.stats.damage += 100f
            }
        }
    }
}
