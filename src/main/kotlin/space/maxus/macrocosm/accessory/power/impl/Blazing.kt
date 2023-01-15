package space.maxus.macrocosm.accessory.power.impl

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.accessory.power.StoneAccessoryPower
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

object Blazing: StoneAccessoryPower(
    "blazing",
    "Blazing",
    "Magma Urchin",
    Rarity.EPIC,
    "Master",
    25,
    "<yellow>+15${Statistic.BONUS_ATTACK_SPEED.display}",
    "Can be fished in very <gold>Hot<br>places.",
    stats {
        strength = 8f
        critDamage = 2f
    },
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQ0MDQxYzAzMzRmN2IxOGZlOTdmN2Q0Y2MzODI2NzgzMjY2Y2E0ZTQ1MWI0ZjEwYTA4MDA2YzUzMjk4NTRmYiJ9fX0="
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player))
                return@listen
            e.stats.attackSpeed += 15
        }
    }
}
