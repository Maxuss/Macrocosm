package space.maxus.macrocosm.damage

enum class DamageType {
    DEFAULT,
    CRITICAL,
    FIRE,
    FROST,
    ELECTRIC,
    @Deprecated("Magic damage type looks really bad and should not be used", replaceWith = ReplaceWith("DamageType.DEFAULT"))
    MAGIC,
}
