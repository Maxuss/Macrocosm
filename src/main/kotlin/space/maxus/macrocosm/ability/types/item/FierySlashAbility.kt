package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageKind
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerLeftClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.players.MacrocosmPlayer
import java.util.concurrent.atomic.AtomicBoolean

object FierySlashAbility : AbilityBase(
    AbilityType.LEFT_CLICK,
    "Fiery Slash",
    "Creates <gold>3 Fiery Arcs<gray> that extend <green>7 blocks<gray> ahead of you, dealing your melee damage to every mob that they hit!"
) {
    override val cost: AbilityCost = AbilityCost(25, cooldown = 0.3f)

    override fun registerListeners() {
        listen<PlayerLeftClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val p = e.player.paper!!
            val front = p.eyeLocation.clone()

            val left = front.clone()
            val right = front.clone()
            left.direction = p.eyeLocation.direction.rotateAroundY(Mth.DEG_TO_RAD * -40.0)
            right.direction = p.eyeLocation.direction.rotateAroundY(Mth.DEG_TO_RAD * 40.0)

            val moveFront = p.eyeLocation.direction.multiply(2f).normalize()
            val moveLeft = moveFront.clone().rotateAroundY(Mth.DEG_TO_RAD * -40.0).multiply(2f).normalize()
            val moveRight = moveFront.clone().rotateAroundY(Mth.DEG_TO_RAD * 40.0).multiply(2f).normalize()

            var ticker = 0

            val frontMoving = AtomicBoolean(false)
            val leftMoving = AtomicBoolean(false)
            val rightMoving = AtomicBoolean(false)

            val stats = e.player.stats()!!

            val (damage, crits) = DamageCalculator.calculateStandardDealt(stats.damage, stats)

            sound(Sound.ENTITY_GHAST_SHOOT) {
                pitch = 0f
                playAt(p.location)
            }

            task(period = 1L) {
                ticker++
                if (ticker >= 7) {
                    it.cancel()
                    return@task
                }

                ensureNotSolid(front, frontMoving)
                ensureNotSolid(left, leftMoving)
                ensureNotSolid(right, rightMoving)

                extendIfPossible(front, moveFront, frontMoving)
                extendIfPossible(left, moveLeft, leftMoving)
                extendIfPossible(right, moveRight, rightMoving)

                renderIfPossible(front, frontMoving)
                renderIfPossible(left, leftMoving)
                renderIfPossible(right, rightMoving)

                damageIfPossible(front, e.player, damage, crits, frontMoving)
                damageIfPossible(left, e.player, damage, crits, leftMoving)
                damageIfPossible(right, e.player, damage, crits, rightMoving)
            }
        }
        listen<PlayerDealDamageEvent> { e ->
            if (e.player.mainHand?.abilities?.contains(this) != true || !e.isContact)
                return@listen

            e.isCancelled = true
        }
    }

    private fun ensureNotSolid(loc: Location, ref: AtomicBoolean) {
        if (ref.get()) {
            // the arc is already not rendering, ignore
            return
        }
        if (loc.block.type.isSolid) {
            // we hit solid block, disable this arc from rendering and dealing damage
            ref.set(true)
        }
    }

    private fun damageIfPossible(
        loc: Location,
        player: MacrocosmPlayer,
        damage: Float,
        crit: Boolean,
        ref: AtomicBoolean
    ) {
        if (ref.get()) {
            // we should not move
            return
        }
        val nearby = loc.getNearbyLivingEntities(1.5).filter { it !is Player && it !is ArmorStand && !it.isDead }
        if (nearby.isEmpty()) {
            // no entities nearby
            return
        }
        val entity = nearby.first()

        // dissipating the arc
        ref.set(true)

        val event = PlayerDealDamageEvent(player, entity, damage, crit, DamageKind.ARTIFICIAL)
        event.callEvent()

        if (event.isCancelled) {
            return
        }

        val mc = entity.macrocosm!!
        val finalDamage = DamageCalculator.calculateStandardReceived(event.damage, mc.calculateStats())
        mc.damage(finalDamage, player.paper)
        DamageHandlers.summonDamageIndicator(
            entity.location,
            finalDamage,
            if (event.crit) DamageType.CRITICAL else DamageType.DEFAULT
        )
    }

    private fun extendIfPossible(loc: Location, direction: Vector, ref: AtomicBoolean) {
        if (ref.get()) {
            // we should not move
            return
        }
        loc.add(direction)
    }

    private fun renderIfPossible(location: Location, ref: AtomicBoolean) {
        if (ref.get()) {
            // we should not move
            return
        }

        val radius = .5
        var angle = 0f
        val inc = Mth.PI / 12
        while (angle < Mth.PI) {
            angle += inc

            val v = Vector()
            v.x = Mth.cos(angle) * radius
            v.z = Mth.sin(angle) * radius

            v.rotateAroundX(Math.toRadians(location.pitch.toDouble()))
            v.rotateAroundY(Math.toRadians(-location.yaw.toDouble()))

            particle(Particle.FLAME) {
                amount = 1
                extra = 0f
                spawnAt(location.clone().add(v))
            }
        }
    }
}
