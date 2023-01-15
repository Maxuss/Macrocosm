package space.maxus.macrocosm.accessory.power.impl

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.accessory.power.StoneAccessoryPower
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

object Warping: StoneAccessoryPower(
    "warping",
    "Warping",
    "Twisted Urchin",
    Rarity.EPIC,
    "Master",
    25,
    "<aqua>+20${Statistic.MAGIC_FIND.display}",
    "Can be fished in very <dark_purple>Otherworldly<br>places.",
    stats {
        critChance = -.5f
        critDamage = 7f
        intelligence = 7f
    },
    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODJjZDkxNGEzMGU2NGUzMDI4MDMzMGY4ODVhODVkYzc5ODM0ZWYwN2VjZGM4ZDNhY2M0Nzg5YThiZDA5MGE3YSJ9fX0="
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureRequirements(e.player))
                return@listen
            e.stats.magicFind += 20
        }
    }
}
