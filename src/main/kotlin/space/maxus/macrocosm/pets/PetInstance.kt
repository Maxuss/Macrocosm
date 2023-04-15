package space.maxus.macrocosm.pets

import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.util.general.Ticker
import space.maxus.macrocosm.util.math.LevelingTable
import java.util.*


class PetInstance(private val entityId: UUID, val base: Identifier, var stored: StoredPet) {
    val prototype: Pet get() = Registry.PET.find(base)
    var runningTask: KSpigotRunnable? = null; private set
    private val entity: LivingEntity? get() = Bukkit.getEntity(entityId) as? LivingEntity

    fun table(): LevelingTable {
        return ProgressivePetTable((stored.rarity.ordinal + 1) / 7.3f)
    }

    private inline fun modifySave(player: MacrocosmPlayer, modifier: StoredPet.() -> Unit) {
        val modified = stored.apply(modifier)
        entity?.customName(prototype.buildName(modified, player))
    }

    fun despawn(player: MacrocosmPlayer) {
        val e = entity
        if (e != null && !e.isDead)
            e.remove()
        player.activePet = null
        player.sendMessage("<green>You have despawned your <${stored.rarity.color.asHexString()}>${prototype.name}<green>.")
    }

    fun addExperience(player: MacrocosmPlayer, amount: Double, skill: SkillType) {
        if (player.activePet != this)
            return

        val amt = if (prototype.preferredSkill != skill) amount * .4 else amount
        modifySave(player) {
            this.overflow += amt
            if (this.level < 100 && table().shouldLevelUp(this.level, this.overflow)) {
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
        prototype.spawn(player, stored)
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

    fun idleFloat(player: MacrocosmPlayer) {
        this.runningTask?.cancel()
        val ticker = Ticker(-6..6)
        val base = prototype
        val e = entity
        val cachedRarity = stored.rarity
        this.runningTask = task(period = 2L) {
            if (player.activePet != this) {
                it.cancel()
                runningTask = null
                return@task
            }

            val paper = player.paper
            if (paper == null) {
                despawn(player)
                it.cancel()
                runningTask = null
                return@task
            }

            val tick = ticker.tick()

            // calculating movement
            val entity = e as? ArmorStand ?: run {
                it.cancel()
                return@task
            }

            if(!entity.world.getNearbyEntities(entity.location, 1.5, 1.5, 1.5).contains(paper)) {
                it.cancel()
                this.followFloat(player)
                return@task
            }

            val loc = entity.location.add(vec(y = .03 * tick))
            entity.teleport(loc)

            base.effects.spawnParticles(entity.eyeLocation, cachedRarity)
        }
    }

    fun followFloat(player: MacrocosmPlayer) {
        this.runningTask?.cancel()
        val e = entity
        val base = prototype
        this.runningTask = task(period = 1L) {
            if (player.activePet != this) {
                it.cancel()
                runningTask = null
                return@task
            }

            val paper = player.paper

            if (paper == null) {
                despawn(player)
                it.cancel()
                runningTask = null
                return@task
            }

            // calculating movement
            val stand = e as? ArmorStand ?: run {
                it.cancel()
                return@task
            }

            if(stand.world.getNearbyEntities(stand.location, 1.5, 1.5, 1.5).contains(paper)) {
                it.cancel()
                this.idleFloat(player)
                return@task
            }

            val pLoc = paper.eyeLocation.add(
                paper.location.direction.rotateAroundY(Math.PI + Mth.DEG_TO_RAD * 60.0).multiply(1.5f)
            )
            pLoc.yaw = paper.location.yaw
            pLoc.pitch = 0f
            val sLoc = stand.location
            val dir = pLoc.toVector().subtract(sLoc.toVector()).multiply(.2)
            val delta = paper.location.subtract(stand.location).toVector().normalize()

            stand.teleport(sLoc.clone().add(dir).setDirection(delta))
            base.effects.spawnParticles(stand.eyeLocation, stored.rarity)
        }
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is PetInstance && entityId == other.entityId
    }

    override fun hashCode(): Int {
        var result = entityId.hashCode()
        result = 31 * result + base.hashCode()
        result = 31 * result + stored.hashCode()
        return result
    }
}
