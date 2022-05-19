package space.maxus.macrocosm.pets

import net.axay.kspigot.extensions.geometry.multiply
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.runnables.task
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.util.EulerAngle
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.util.Identifier
import java.util.*


class PetInstance(private val entityId: UUID, private val base: Identifier, val rarity: Rarity) {
    private val basePet: Pet get() = PetRegistry.find(base)
    private val entity: LivingEntity? get() = Bukkit.getEntity(entityId) as? LivingEntity

    fun despawn(player: MacrocosmPlayer) {
        val e = entity
        if(e != null && !e.isDead)
            e.remove()
        player.activePet = null
    }

    fun teleport(player: MacrocosmPlayer) {
        val p = player.paper ?: return
        val stand = entity as? ArmorStand ?: return

        val dir = p.eyeLocation.toVector().normalize()
        // invert the direction
        val negative = dir.multiply(-1.2).normalize()
        // move the direction a bit farther
        val far = negative.multiply(2.5).normalize()
        // rotate the vector
        val rotated = far.rotateAroundY(80.0).multiply(2.3).normalize()
        val loc = rotated.toLocation(p.world)
        // rotate the location towards the player
        loc.yaw = rotated.angle(p.eyeLocation.toVector().multiply(-1).normalize())
        loc.add(p.location)
        // make the pet not appear on ground like `rok`
        loc.y = loc.y + 1.4
        loc.add(1.0, 0.0, 1.0)

        stand.teleport(loc)
    }

    fun floatTick(player: MacrocosmPlayer, pos: Location) {
        var ticker = 0
        var negative = false
        val base = basePet
        val e = entity
        task(period = 1L) {
            if(player.activePet != this) {
                despawn(player)
                it.cancel()
                return@task
            }

            val paper = player.paper
            if(paper == null) {
                despawn(player)
                it.cancel()
                return@task
            }

            // ticking
            if(negative)
                ticker++
            else
                ticker--


            if(ticker <= -10)
                negative = true
            else if(ticker >= 10)
                negative = false

            // calculating movement
            val entity = e as? ArmorStand ?: run {
                it.cancel()
                return@task
            }

            val dir = (((paper.eyeLocation.direction.normalize() multiply -4.8).normalize() multiply 4.3).rotateAroundY(Math.toRadians(75.0)) multiply 3.4).normalize()
            var location = dir.toLocation(pos.world)

            entity.headPose = EulerAngle(.0, dir.angle(paper.eyeLocation.toVector().multiply(-2.4).normalize()).toDouble(), .0)
            location.add(paper.location)
            location = location.toVector().add(vec(y = .5 + (1 + ticker / 7.5))).toLocation(pos.world)

            entity.teleport(location)

            base.effects.spawnParticles(Location(location.world, location.x, location.y + .6, location.z), rarity)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is PetInstance && entityId == other.entityId
    }

    override fun hashCode(): Int {
        var result = entityId.hashCode()
        result = 31 * result + base.hashCode()
        result = 31 * result + rarity.hashCode()
        return result
    }
}
