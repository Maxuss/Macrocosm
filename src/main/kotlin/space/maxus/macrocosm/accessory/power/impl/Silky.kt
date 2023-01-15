package space.maxus.macrocosm.accessory.power.impl

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.accessory.power.StoneAccessoryPower
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

object  Silky: StoneAccessoryPower(
    "silky",
    "Silky",
    "Luxurious Spool",
    Rarity.UNCOMMON,
    "Intermediate",
    15,
    "<yellow>+5${Statistic.BONUS_ATTACK_SPEED.display}",
    "Rarely drops from spiders<br>around the <green>Macrocosm Island<gray>.",
    stats {
        speed = 3f
        critDamage = 12.5f
    },
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTRhZjVmM2I1MGUyZGVhMjY4N2QyOTJjYzNhNWU0MmMwMjhiODYyNmU2Mzg4NDJmYjRmNzg2NzFkZWJlMjc2YyJ9fX0="
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player))
                return@listen
            e.stats.attackSpeed += 5f
        }
    }
}
