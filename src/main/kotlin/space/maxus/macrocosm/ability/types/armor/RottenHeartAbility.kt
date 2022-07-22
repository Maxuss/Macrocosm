package space.maxus.macrocosm.ability.types.armor

import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.stats.Statistic

class RottenHeartAbility(mul: Float): FullSetBonus("Rotten Heart", "Healing Wands heal <red>+${Formatting.stats((mul * 100f).toBigDecimal())} ${Statistic.HEALTH.display}<gray>.", true)
