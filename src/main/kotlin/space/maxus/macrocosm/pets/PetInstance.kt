package space.maxus.macrocosm.pets

import net.axay.kspigot.extensions.geometry.multiply
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.util.EulerAngle
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.util.Identifier
import java.util.*


class PetInstance(private val entityId: UUID, val base: Identifier, var hashKey: String) {
    val prototype: Pet get() = PetRegistry.find(base)
    private val entity: LivingEntity? get() = Bukkit.getEntity(entityId) as? LivingEntity

    fun referring(player: MacrocosmPlayer): StoredPet {
        return player.ownedPets[hashKey]!!
    }

    fun rarity(player: MacrocosmPlayer): Rarity {
        return referring(player).rarity
    }

    fun level(player: MacrocosmPlayer): Int {
        return referring(player).level
    }

    private inline fun modifySave(player: MacrocosmPlayer, modifier: StoredPet.() -> Unit) {
        val modified = player.ownedPets[hashKey]!!.apply(modifier)
        val newHash = "${prototype.id}@${modified.hashCode().toString(16)}"
        player.ownedPets.remove(hashKey)
        this.hashKey = newHash
        player.ownedPets[newHash] = modified
    }

    fun despawn(player: MacrocosmPlayer) {
        val e = entity
        if (e != null && !e.isDead)
            e.remove()
        player.activePet = null
        player.sendMessage("<green>You despawned your <${rarity(player).color.asHexString()}>${prototype.name}<green>.")
    }

    fun addExperience(player: MacrocosmPlayer, amount: Double, skill: SkillType) {
        if (player.activePet != this)
            return

        val amt = if (prototype.preferredSkill != skill) amount * .4 else amount
        modifySave(player) {
            this.overflow += amt
            if (prototype.table.shouldLevelUp(this.level, this.overflow, .0)) {
                this.level++
                this.overflow = .0
                player.sendMessage("<green>Your <${rarity.color.asHexString()}>${prototype.name}<green> leveled up to level $level!")
                sound(Sound.ENTITY_PLAYER_LEVELUP) {
                    pitch = 2f
                    playFor(player.paper!!)
                }
            }
        }
    }

    fun respawn(player: MacrocosmPlayer) {
        despawn(player)
        prototype.spawn(player, hashKey)
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
        loc.yaw = p.eyeLocation.yaw
        loc.add(p.location)
        loc.y = loc.y + 1.4
        loc.add(1.0, 0.0, 1.0)

        stand.teleport(loc)
    }

    fun floatTick(player: MacrocosmPlayer, pos: Location) {
        var ticker = 0
        var negative = false
        val base = prototype
        val e = entity
        val cachedRarity = rarity(player)
        task(period = 1L) {
            if (player.activePet != this) {
                it.cancel()
                return@task
            }

            val paper = player.paper
            if (paper == null) {
                despawn(player)
                it.cancel()
                return@task
            }

            // ticking
            if (negative)
                ticker++
            else
                ticker--


            if (ticker <= -10)
                negative = true
            else if (ticker >= 10)
                negative = false

            // calculating movement
            val entity = e as? ArmorStand ?: run {
                it.cancel()
                return@task
            }

            val dir = (((paper.eyeLocation.direction.normalize() multiply -4.8).normalize() multiply 4.3).rotateAroundY(
                Math.toRadians(75.0)
            ) multiply 3.4).normalize()
            var location = dir.toLocation(pos.world)

            entity.headPose = EulerAngle(.0, Math.toRadians(paper.eyeLocation.yaw.toDouble()), .0)
            location.add(paper.location)
            location = location.toVector().add(vec(y = .5 + (1 + ticker / 7.5))).toLocation(pos.world)

            entity.teleport(location)

            base.effects.spawnParticles(Location(location.world, location.x, location.y + .6, location.z), cachedRarity)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is PetInstance && entityId == other.entityId
    }

    override fun hashCode(): Int {
        var result = entityId.hashCode()
        result = 31 * result + base.hashCode()
        result = 31 * result + hashKey.hashCode()
        return result
    }
}
