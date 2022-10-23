package space.maxus.macrocosm.damage

import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.Statistics
import kotlin.random.Random

/**
 * An object that performs specific damage calculations
 */
object DamageCalculator {
    private fun crits(stats: Statistics) = Random.nextFloat() < (stats.critChance / 100f)

    /**
     * Calculates magic damage that will be dealt
     *
     * @param base base magic damage value
     * @param scaling intelligence damage scaling
     * @param stats statistics that will be used for calculations.
     *
     * Statistics that are used:
     * 1. [Statistic.ABILITY_DAMAGE] -> 0.01 multiplier
     * 2. [Statistic.INTELLIGENCE] -> 0.01 x [scaling] multiplier
     * 3. [Statistic.DAMAGE_BOOST] -> 0.01 multiplier
     *
     * @return calculated magic damage that will be dealt
     */
    fun calculateMagicDamage(base: Number, scaling: Float, stats: Statistics): Float {
        return base.toFloat() * (1 + (stats.abilityDamage) / 100f) * (1 + (stats.intelligence / 100f) * scaling) * (1 + (stats.damageBoost / 100f))
    }

    /**
     * Calculates default non-magic damage that will be dealt
     *
     * NOTE: the resulted damage does not include target's defense. In most cases you will
     * also want to call [calculateStandardReceived] on the result to calculate damage that will
     * be received by the target.
     *
     * @param amount base amount of damage, usually equal to the value of [Statistic.DAMAGE]
     * @param stats statistics that will be used for extra calculations.
     *
     * Statistics that are used:
     * 1. [Statistic.STRENGTH] -> 0.01 multiplier
     * 2. [Statistic.DAMAGE_BOOST] -> 0.01 multiplier
     * 3. [Statistic.CRIT_DAMAGE] -> 0.01 multiplier if the attack crits (see [crits])
     * 4. [Statistic.TRUE_DAMAGE] -> (true damage) / (true damage + 100) multiplier
     *
     * @return pair of (damage dealt, whether the attack was critical)
     */
    fun calculateStandardDealt(amount: Float, stats: Statistics): Pair<Float, Boolean> {
        val strengthMul = (1 + (stats.strength / 100f))
        val extraMul = (1 + (stats.damageBoost / 100f))
        val crit = crits(stats)
        val critMultiplier = 1 + if (crit) (stats.critDamage / 100f) else 0f
        val trueMultiplier = 1 + (stats.trueDamage / (stats.trueDamage + 100f))
        return Pair(amount * strengthMul * critMultiplier * extraMul * trueMultiplier, crit)
    }

    /**
     * Calculates the damage that the target will receive
     *
     * @param amount amount of damage that is going to be dealt
     * @param stats statistics that will be used for extra calculations.
     *
     * Statistics that are used:
     * 1. [Statistic.DAMAGE_REDUCTION] -> whole multiplier
     * 2. [Statistic.DEFENSE] -> (defense) / (defense + 100) multiplier
     * 3. [Statistic.TRUE_DEFENSE] -> (true defense) / (true defense * 10 + 1000) multiplier
     *
     * The maximum damage reduction may not be more than 80%.
     * @return damage that is actually going to be dealt, including the resistances
     */
    fun calculateStandardReceived(amount: Float, stats: Statistics): Float {
        val reduction = clamp(
            stats.damageReduction + (stats.defense / (100 + stats.defense)) + (stats.trueDefense / (stats.trueDefense * 10 + 1000)),
            0f,
            0.80f
        )
        return amount * (1 - reduction)
    }

    /**
     * Calculates true damage that will be received
     *
     * @param amount amount of incoming true damage
     * @param stats statistics that will be used for calculations.
     *
     * Statistics that are used:
     * 1. [Statistic.DAMAGE_REDUCTION] -> whole multiplier
     * 2. [Statistic.TRUE_DEFENSE] -> (true defense) / (true defense + 100) multiplier
     *
     * The damage reduction may not be less than 1% or more than 95%
     *
     * @return damage that is actually going to be dealt, including the resistances
     */
    @Suppress("unused")
    fun calculateTrueReceived(amount: Float, stats: Statistics): Float {
        val reduction = clamp(stats.damageReduction + (stats.trueDefense / (100 + stats.trueDefense)), 0.01f, 0.95f)
        return amount * (1 - reduction)
    }

    /**
     * Calculates the *Light Effective Health (LEHP)*
     *
     * This effective health calculation model is only used for entities
     * to determine how much damage they may take before dying.
     *
     * Formula: `HP * (1 + (DEF / (DEF + 100))`
     *
     * @param health amount of entity's health
     * @param stats statistics that are going to be used.
     *
     * @return LEHP of an entity
     */
    fun calculateLightEffectiveHealth(health: Float, stats: Statistics): Float {
        return health * (1 + (stats.defense / (100f + stats.defense)))
    }

    /**
     * Calculates the *Actual Effective Health (EHP)*
     *
     * This effective health calculation model is used for players
     * to determine how much damage they maay take before dying.
     *
     * Formula: `HP * (1 + (DEF / 100))`
     *
     * @param stats statistics of the player
     *
     * @return EHP of the player
     */
    fun calculateEffectiveHealth(stats: Statistics): Float {
        return stats.health * (1 + (stats.defense / 100f))
    }

    /**
     * Calculates the damage reduction.
     *
     * This model is used to determine the damage reduction multiplier
     * **only** from the [Statistic.DEFENSE], so for most use cases it's better
     * to use [calculateStandardReceived] to determine amount of absorbed damage
     *
     * @param stats statistics of the player
     *
     * @return damage reduction modifier of player
     */
    fun calculateDamageReduction(stats: Statistics): Float {
        return stats.defense / (stats.defense + 100f)
    }
}
