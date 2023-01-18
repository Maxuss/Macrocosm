package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import org.bukkit.Bukkit
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.stats.Statistic

object NightCrystalAbility : AccessoryAbility(
    "night_crystal_talisman",
    "Increases your ${Statistic.STRENGTH.display}<gray> and ${Statistic.DEFENSE.display}<gray> by <green>+5<gray> during the Night."
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if (!hasAccs(e.player) || Bukkit.getWorlds().first().isDayTime)
                return@listen
            e.stats.defense += 5
            e.stats.strength += 5
        }
    }
}

object DayCrystalAbility : AccessoryAbility(
    "day_crystal_talisman",
    "Increases your ${Statistic.STRENGTH.display}<gray> and ${Statistic.DEFENSE.display}<gray> by <green>+5<gray> during the Day."
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if (!hasAccs(e.player) || !Bukkit.getWorlds().first().isDayTime)
                return@listen
            e.stats.defense += 5
            e.stats.strength += 5
        }
    }
}
