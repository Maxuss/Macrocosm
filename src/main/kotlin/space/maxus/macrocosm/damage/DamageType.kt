package space.maxus.macrocosm.damage

import org.jetbrains.annotations.ApiStatus

/**
 * Represents type of damage dealt that will be displayed on damage indicators
 */
enum class DamageType {
    /**
     * This is default damage
     */
    DEFAULT,

    /**
     * This is super critical damage
     */
    SUPER_CRITICAL,

    /**
     * This is critical damage
     */
    CRITICAL,

    /**
     * This is fire damage
     */
    FIRE,

    /**
     * This is frost damage
     */
    FROST,

    /**
     * This is electric damage
     */
    ELECTRIC,

    @ApiStatus.ScheduledForRemoval(inVersion = "0.4.0")
    @Deprecated(
        "Magic damage type looks really bad and should not be used",
        replaceWith = ReplaceWith("DamageType.DEFAULT")
    )
    /**
     * This is magic damage
     */
    MAGIC,
}
