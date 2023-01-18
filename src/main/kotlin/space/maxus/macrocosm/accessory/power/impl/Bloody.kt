package space.maxus.macrocosm.accessory.power.impl

import net.axay.kspigot.event.listen
import org.bukkit.event.EventPriority
import space.maxus.macrocosm.accessory.power.StoneAccessoryPower
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

object Bloody : StoneAccessoryPower(
    "bloody",
    "Bloody",
    "Giant Heart",
    Rarity.RARE,
    "Intermediate",
    20,
    "<red>+15% ${Statistic.HEALTH.specialChar} Max Health",
    "Rarely drops from mobs with<br>over <red>50,000 ${Statistic.HEALTH.display}<gray>.",
    stats {
        critDamage = 5.5f
        strength = 5.5f
        intelligence = 2f
    },
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDQ1ZjRkMTM5YzllODkyNjJlYzA2YjI3YWFhZDczZmE0ODhhYjQ5MjkwZDJjY2Q2ODVhMjU1NDcyNTM3M2M5YiJ9fX0="
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent>(priority = EventPriority.LOWEST) { e ->
            if (!ensureRequirements(e.player))
                return@listen
            e.stats.health *= 1.15f
        }
    }
}
