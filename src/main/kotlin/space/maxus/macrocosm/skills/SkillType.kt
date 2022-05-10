package space.maxus.macrocosm.skills

import space.maxus.macrocosm.reward.boostStat
import space.maxus.macrocosm.stats.Statistic

enum class SkillType(val profession: String, val inst: Skill, val descriptor: (lvl: Int) -> String) {
    COMBAT("Warrior", skill(
        "Combat", (
            boostStat(Statistic.DAMAGE_BOOST, 0.04, hide = true)
                and
                boostStat(Statistic.CRIT_CHANCE, 0.5, false)
            ).repeating()
    ), { lvl -> "<white>Deal <dark_gray>${(lvl - 1) * 4}%➜<green>${lvl * 4}%<white> more damage to enemies." }
    ),
    FARMING("Farmhand",
        skill(
            "Farming", (
                boostStat(Statistic.FARMING_FORTUNE, 4.0, hide = true)
                    and
                    boostStat(Statistic.HEALTH, 2.0, false)
                ).repeating()
        ),
        { lvl -> "<white>Grants <dark_gray>+${(lvl - 1) * 4}➜<green>+${lvl * 4}<white> ${Statistic.FARMING_FORTUNE.display}." }),
    FORAGING("Logger",
        skill(
            "Foraging", (
                boostStat(Statistic.FORAGING_FORTUNE, 4.0, hide = true)
                    and
                    boostStat(Statistic.STRENGTH, 2.0, false)
                ).repeating()
        ),
        { lvl -> "<white>Grants <dark_gray>+${(lvl - 1) * 4}➜<green>+${lvl * 4}<white> ${Statistic.FARMING_FORTUNE.display}." }),
    FISHING("Fisherman",
        skill(
            "Fishing", (
                boostStat(Statistic.TREASURE_CHANCE, 1.0, hide = true)
                    and
                    boostStat(Statistic.HEALTH, 2.0, false)
                ).repeating()
        ),
        { lvl -> "<white>Grants <dark_gray>+${(lvl - 1)}%➜<green>+${lvl}%<white> ${Statistic.TREASURE_CHANCE.display}." }),
    MINING("Prospector",
        skill(
            "Mining", (
                boostStat(Statistic.MINING_FORTUNE, 4.0, hide = true)
                    and
                    boostStat(Statistic.DEFENSE, 2.0, false)
                ).repeating()
        ),
        { lvl -> "<white>Grants <dark_gray>+${(lvl - 1) * 4}➜<green>+${lvl * 4}<white> ${Statistic.MINING_FORTUNE.display}." }),
    EXCAVATING("Digger",
        skill(
            "Excavating", (
                boostStat(Statistic.EXCAVATING_FORTUNE, 4.0, hide = true)
                    and
                    boostStat(Statistic.TRUE_DEFENSE, 1.0, false)
                ).repeating()
        ),
        { lvl -> "<white>Grants <dark_gray>+${(lvl - 1) * 4}➜<green>+${lvl * 4}<white> ${Statistic.EXCAVATING_FORTUNE.display}." }),
    ENCHANTING("Enchanter",
        skill(
            "Enchanting", (
                boostStat(Statistic.ABILITY_DAMAGE, 2.0, hide = true)
                    and
                    boostStat(Statistic.INTELLIGENCE, 1.0, false)
                ).repeating()
        ),
        { lvl -> "<white>Grants <dark_gray>+${(lvl - 1) * 2}%➜<green>+${lvl * 2}%<white> ${Statistic.ABILITY_DAMAGE.display}." }),
    ALCHEMY(
        "Brewer",
        skill("Alchemy", (AlchemyReward and boostStat(Statistic.INTELLIGENCE, 2.0, false)).repeating()),
        { lvl -> "<white>Collect <dark_gray>${(lvl - 1) * 4}%➜<green>${lvl * 4}%<white> more experience orbs from any source." }),
    RUNECRAFTING("Mystic",
        skill(
            "Runecrafting", (
                boostStat(Statistic.INTELLIGENCE, .01, hide = true, multiply = true)
                    and
                    boostStat(Statistic.MAGIC_FIND, .5, false)
                ).repeating()
        ),
        { lvl -> "<white>Grants a <dark_gray>${(lvl - 1)}%➜<green>${lvl}%<white> boost to your ${Statistic.INTELLIGENCE.display}." }),
    CARPENTRY("Crafter",
        skill(
            "Carpentry", (
                boostStat(Statistic.MAGIC_FIND, .02, hide = true, multiply = true)
                    and
                    boostStat(Statistic.CRIT_DAMAGE, 1.5, false)
                ).repeating()
        ),
        { lvl -> "<white>Grants a <dark_gray>${(lvl - 1) * 2}%➜<green>${lvl * 2}%<white> boost to your ${Statistic.MAGIC_FIND.display}." }),
    ;

    fun descript(lvl: Int): String {
        return descriptor(lvl)
    }
}
