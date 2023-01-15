package space.maxus.macrocosm.accessory.power.impl

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.accessory.power.StoneAccessoryPower
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

object Freezing: StoneAccessoryPower(
    "freezing",
    "Freezing",
    "Frigid Urchin",
    Rarity.EPIC,
    "Master",
    25,
    "<red>+25${Statistic.ABILITY_DAMAGE.display}",
    "Can be fished in very <blue>Cold<br>places.",
    stats {
        intelligence = 10f
        vigor = 2f
    },
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTU5ZGY5YTM2ZTUyYTBmMzM3ODczOGZhMjM5YzZmYTkzMmIzZjQ0NDZiZjBhMGUwYjExZDhiNTFlN2IyMTViMyJ9fX0="
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player))
                return@listen
            e.stats.abilityDamage += 25
        }
    }
}
