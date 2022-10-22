package space.maxus.macrocosm.damage

/**
 * Enum to identifier type of damage.
 *
 * Currently, all the kinds except for [MAGIC] are used, but the magic type is
 * intended to be used in the reworked Damage V2 system.
 */
enum class DamageKind {
    /**
     * The damage is dealt by melee hits
     */
    MELEE,

    /**
     * The damage is dealt from magic
     */
    MAGIC,

    /**
     * The damage is dealt from bows/shortbows/longbows/crossbows/ballistas etc.
     */
    RANGED,

    /**
     * The damage is dealt artificially
     */
    ARTIFICIAL
}
