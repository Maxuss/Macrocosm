package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.metrics.report
import java.util.*
import kotlin.math.min

object TitansKnowledge : AbilityBase(
    AbilityType.PASSIVE,
    "Titan's Knowledge",
    "This mysterious helmet drains your ability power<gray>.<br>Your ${Statistic.ABILITY_DAMAGE.display}<gray> is <green>halved<gray>."
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HEAD))
                return@listen

            e.stats.abilityDamage /= 2f
        }
    }
}

object TitansEnergy : AbilityBase(
    AbilityType.PASSIVE,
    "Titan's Energy",
    "This magical cuirass drains your mana pool.<br>Your ${Statistic.INTELLIGENCE.display}<gray> is capped at <green>10x<gray> of your ${Statistic.ABILITY_DAMAGE.display}<gray>."
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.CHEST))
                return@listen

            val cap = e.stats.abilityDamage * 10f
            e.stats.intelligence = min(cap, e.stats.intelligence)
        }
    }
}

object TitansMight : AbilityBase(
    AbilityType.PASSIVE,
    "Titan's Might",
    "This powerful gladius is light, making it hard to parry with.<br>Your ${Statistic.DEFENSE.display}<gray> is capped at <green>2x<gray> the <green>sum of<gray> your ${Statistic.SPEED.display}<gray> and ${Statistic.BONUS_ATTACK_SPEED.display}<gray>."
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val cap = 2f * (e.stats.speed + e.stats.attackSpeed)
            e.stats.defense = min(cap, e.stats.defense)
        }
    }
}

object TitansValor : AbilityBase(
    AbilityType.PASSIVE,
    "Titan's Valor",
    "This gleaming shield is unimaginably heavy.<br>Your ${Statistic.STRENGTH.display}<gray> is capped at <green>1/2<gray> of your ${Statistic.DEFENSE.display}<gray>."
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.OFF_HAND))
                return@listen

            val cap = e.stats.defense / 2f
            e.stats.strength = min(cap, e.stats.strength)
        }
    }
}

object TitansEminence : AbilityBase(
    AbilityType.PASSIVE,
    "Titan's Eminence",
    "Boosts your offensive stats based on defensive ones.<br>${Statistic.DAMAGE.display}<gray>: <green>+2x ${Statistic.TRUE_DEFENSE.display}<br>${Statistic.ABILITY_DAMAGE.display}<gray>: <green>+0.03x ${Statistic.INTELLIGENCE.display}<br>${Statistic.CRIT_DAMAGE.display}<gray>: <green>+0.5x ${Statistic.SPEED.display}<gray>."
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val dmgBonus = e.stats.trueDefense * 2f
            val abBonus = e.stats.intelligence * .03f
            val cdBonus = e.stats.speed * .5f

            e.stats.damage += dmgBonus
            e.stats.abilityDamage += abBonus
            e.stats.critDamage += cdBonus
        }
    }
}

object TitansLightning : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Titan's Lightning",
    "Cast a <aqua>Chain Lightning<gray>, going through up to <green>4<gray> enemy iterations. Deals <red>[7000:0.15] ${Statistic.DAMAGE.display}<gray> decreasing by each enemy pierced."
) {
    override val cost: AbilityCost = AbilityCost(mana = 750, cooldown = 3)

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            val p = e.player.paper ?: report("Player in RightClickEvent was null!") { return@listen }

            val item = p.inventory.getItem(EquipmentSlot.HAND)
            if (item.macrocosm == null || !item.macrocosm!!.abilities.contains(this))
                return@listen

            val target = p.getTargetEntity(9) as? LivingEntity ?: p.location.getNearbyLivingEntities(
                8.0,
                4.0,
                8.0
            ) { it !is Player && it !is ArmorStand }.firstOrNull() ?: kotlin.run {
                e.player.sendMessage("<red>No target found!")
                return@listen
            }

            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            renderLine(p.eyeLocation, target.eyeLocation) { mov ->
                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.fromRGB(0xB4FEFF), 0.5f)
                    amount = 5
                    extra = .4f
                    offset = Vector.getRandom()

                    spawnAt(mov)
                }

                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.fromRGB(0xE4FEFF), 0.7f)
                    amount = 3
                    extra = 0f

                    spawnAt(mov)
                }
            }
            sound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER) {
                pitch = 0f
                volume = 4f

                playAt(p.location)
            }

            val damage = DamageCalculator.calculateMagicDamage(7000, .15f, e.player.stats()!!)
            target.macrocosm?.damage(damage, p)
            DamageHandlers.summonDamageIndicator(target.location, damage, DamageType.ELECTRIC)

            val hit = mutableListOf(target.uniqueId)
            val nearest = target.location.getNearbyLivingEntities(5.0)
                .filter { it !is Player && it !is ArmorStand && !hit.contains(it.uniqueId) }

            recurseThroughEntities(target.eyeLocation, p, damage / 2f, nearest, hit, 1)
        }
    }

    private fun recurseThroughEntities(
        previous: Location,
        player: Player,
        damage: Float,
        nearest: List<LivingEntity>,
        hit: MutableList<UUID>,
        iter: Int
    ) {
        if (nearest.isNotEmpty() && hit.size < 15 && iter < 4) {
            nearest.forEach { entity ->
                renderLine(previous, entity.eyeLocation) { mov ->
                    particle(Particle.REDSTONE) {
                        data = DustOptions(Color.fromRGB(0xB4FEFF), 0.5f)
                        amount = 5
                        extra = .4f
                        offset = Vector.getRandom()

                        spawnAt(mov)
                    }

                    particle(Particle.REDSTONE) {
                        data = DustOptions(Color.fromRGB(0xE4FEFF), 0.7f)
                        amount = 3
                        extra = 0f

                        spawnAt(mov)
                    }
                }
                sound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER) {
                    pitch = 0f
                    volume = .4f

                    playAt(entity.location)
                }
                hit.add(entity.uniqueId)
                entity.macrocosm?.damage(damage, player)
                DamageHandlers.summonDamageIndicator(entity.location, damage, DamageType.ELECTRIC)

                recurseThroughEntities(
                    entity.eyeLocation,
                    player,
                    damage / 2f,
                    entity.location.getNearbyLivingEntities(5.0)
                        .filter { it !is Player && it !is ArmorStand && !hit.contains(it.uniqueId) },
                    hit,
                    iter + 1
                )
            }
        }
    }

    fun renderLine(from: Location, to: Location, particleHandler: (Location) -> Unit) {
        val inc = from.distance(to) / 20

        var i = 0
        while (i < 20) {
            val dir = to.toVector().subtract(from.toVector()).normalize().multiply(i * inc)
            val mov = from.clone().add(dir)

            particleHandler(mov)

            i++
        }
    }
}
