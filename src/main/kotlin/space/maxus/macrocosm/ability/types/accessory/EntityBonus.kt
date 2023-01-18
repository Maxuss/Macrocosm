package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import kotlin.math.roundToInt

class ZombieTalisman(applicable: String, private val reduction: Float) : AccessoryAbility(
    applicable,
    "Reduces damage taken from Zombies by <green>${(reduction * 100f).roundToInt()}%<gray>."
) {
    private val zombies = arrayOf(EntityType.ZOMBIE, EntityType.ZOMBIFIED_PIGLIN, EntityType.HUSK)
    override fun registerListeners() {
        listen<PlayerReceiveDamageEvent> { e ->
            if (!hasAccs(e.player) || !zombies.contains(e.damager.type))
                return@listen
            e.damage *= (1 - reduction)
        }
    }
}

class WitherTalisman(applicable: String, private val reduction: Float) : AccessoryAbility(
    applicable,
    "Reduces damage taken from Withers by <green>${(reduction * 100f).roundToInt()}%<gray>."
) {
    private val withers = arrayOf(EntityType.WITHER_SKELETON, EntityType.WITHER, EntityType.WITHER_SKULL)
    override fun registerListeners() {
        listen<PlayerReceiveDamageEvent> { e ->
            if (!hasAccs(e.player) || !withers.contains(e.damager.type))
                return@listen
            e.damage *= (1 - reduction)
        }
    }
}

class SpiderTalisman(applicable: String, private val reduction: Float) : AccessoryAbility(
    applicable,
    "Reduces damage taken from Spiders by <green>${(reduction * 100f).roundToInt()}%<gray>."
) {
    private val spiders = arrayOf(EntityType.SPIDER, EntityType.CAVE_SPIDER)
    override fun registerListeners() {
        listen<PlayerReceiveDamageEvent> { e ->
            if (!hasAccs(e.player) || !spiders.contains(e.damager.type))
                return@listen
            e.damage *= (1 - reduction)
        }
    }
}

class EnderTalisman(applicable: String, private val reduction: Float) : AccessoryAbility(
    applicable,
    "Reduces damage taken from Ender Creatures by <green>${(reduction * 100f).roundToInt()}%<gray>."
) {
    private val zombies = arrayOf(EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.ENDER_DRAGON)
    override fun registerListeners() {
        listen<PlayerReceiveDamageEvent> { e ->
            if (!hasAccs(e.player) || !zombies.contains(e.damager.type))
                return@listen
            e.damage *= (1 - reduction)
        }
    }
}

