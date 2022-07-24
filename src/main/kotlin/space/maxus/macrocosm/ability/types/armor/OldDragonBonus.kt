package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.events.EnchantCalculateStatsEvent
import space.maxus.macrocosm.registry.Registry

object OldDragonBonus : FullSetBonus(
    "Old Blood",
    "<gold>Triples<gray> the effects of <blue>Growth<gray>, <blue>Protection<gray>, <blue>Feather Falling<gray>, <blue>True Protection<gray>, <blue>Sugar Rush<gray> and <blue>Disturbance<gray>."
) {
    private val allowed =
        listOf("growth", "protection", "true_protection", "disturbance", "sugar_rush", "feather_falling")

    override fun registerListeners() {
        listen<EnchantCalculateStatsEvent> { e ->
            if (e.player == null || !ensureSetRequirement(e.player))
                return@listen
            val id = Registry.ENCHANT.byValue(e.enchant)
            if (id == null || !allowed.contains(id.path))
                return@listen
            e.stats.multiply(3f)
        }
    }
}
