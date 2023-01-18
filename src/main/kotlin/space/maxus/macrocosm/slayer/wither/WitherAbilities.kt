package space.maxus.macrocosm.slayer.wither

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.block.data.BlockData
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.entity.raycast
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.listeners.FallingBlockListener
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.slayer.SlayerAbility
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.unreachable
import java.util.*
import kotlin.math.min
import kotlin.random.Random

object WitherAbilities {
    val CAUTERIZE = SlayerAbility(
        "cauterize",
        SlayerType.CINDERFLAME_SPIRIT,
        "<red>Cauterize",
        "The Cinderflame Spirit engulfs itself in flames every <green>5 seconds<gray>, healing itself for <red>[500/1500/5000/25000] ${Statistic.HEALTH.display}<gray>, and deals <yellow>25%<gray> of restored ${Statistic.HEALTH.display}<gray> as damage to nearby players."
    ) {
        val healths = arrayOf(500, 1500, 5000, 25000)
        task(period = 100L) {
            applyToBosses { mc, living, tier ->
                if (tier > 4 || living.isDead)
                    return@applyToBosses
                val healthToRegen = healths[tier - 1]
                mc.currentHealth = min(mc.currentHealth + healthToRegen, mc.calculateStats().health)
                val nearbyPlayers = living.location.getNearbyPlayers(3.0)
                for (player in nearbyPlayers) {
                    val pl = player.macrocosm ?: continue
                    val dmg = DamageCalculator.calculateStandardReceived(healthToRegen * .25f, pl.stats() ?: continue)
                    pl.damage(dmg, text("Cauterization"))
                }
                particle(Particle.FLAME) {
                    amount = 50
                    extra = 0.3
                    // data = DustOptions(Color.ORANGE, 1.3f)
                    offset = Vector.getRandom().add(vec(y = .5))
                    spawnAt(living.location)
                }
                particle(Particle.REDSTONE) {
                    amount = 50
                    data = DustOptions(Color.BLACK, 1.3f)
                    offset = Vector.getRandom().add(vec(y = .5))
                    spawnAt(living.location)
                }
                sound(Sound.ENTITY_BLAZE_SHOOT) {
                    pitch = 0f
                    playAt(living.location)
                }
            }
        }
    }

    val CINDER_BARRIER = SlayerAbility(
        "cinder_barrier",
        SlayerType.CINDERFLAME_SPIRIT,
        "<gold>Cinder Barrier",
        "<dark_gray>At 25%/50% HP<br><gray>Boss gains <yellow>[5/5/10/25]<gray> invincibility layers, absorbing all damage. If the shield is not broken within <green>10 seconds<gray>, the player <red>instantly dies<gray>."
    ) {
        val barriersActivated50 = mutableListOf<UUID>()
        val barriersActivated25 = mutableListOf<UUID>()

        listen<PlayerDealDamageEvent> { e ->
            if (SlayerAbility.bosses[SlayerType.CINDERFLAME_SPIRIT]?.contains(e.damaged.uniqueId) != true)
                return@listen
            val mc = e.damaged.macrocosm ?: return@listen
            val currentBarrierCount = barrierAmount[e.damaged.uniqueId]
            if (currentBarrierCount != null) {
                barrierAmount[e.damaged.uniqueId] = currentBarrierCount - 1
                barrierHolograms[e.damaged.uniqueId]?.customName(text("<bold><gold>Cinder Barrier: <white>${currentBarrierCount - 1}"))
                e.isCancelled = true
                sound(Sound.BLOCK_BASALT_BREAK) {
                    volume = 5f
                    pitch = 0f
                    playAt(e.damaged.location)
                }
                particle(Particle.BLOCK_CRACK) {
                    data = Material.BASALT.createBlockData()
                    amount = 25
                    offset = Vector.getRandom()
                    spawnAt(e.damaged.location)
                }
                if (currentBarrierCount - 1 <= 0) {
                    barrierAmount.remove(e.damaged.uniqueId)
                    barrierHolograms.remove(e.damaged.uniqueId)?.remove()
                }
                return@listen
            }
            val id = mc.getId(e.damaged).path
            val tier = Integer.valueOf(id.replace("${slayerType.name.lowercase()}_", ""))
            if (tier > 4)
                return@listen
            val ratio = mc.currentHealth / mc.calculateStats().health
            if (ratio <= .25f && !barriersActivated25.contains(e.damaged.uniqueId) && !e.damaged.isDead) {
                barriersActivated25.add(e.damaged.uniqueId)
                summonBarrier(e.damaged, tier)
            } else if (ratio <= .5f && !barriersActivated50.contains(e.damaged.uniqueId) && !e.damaged.isDead) {
                barriersActivated50.add(e.damaged.uniqueId)
                summonBarrier(e.damaged, tier)
            }
        }
        listen<PlayerKillEntityEvent> { e ->
            barriersActivated25.remove(e.killed.uniqueId)
            barriersActivated50.remove(e.killed.uniqueId)
        }
    }

    private val barrierLevels = listOf(5, 5, 10, 25)
    private val barrierAmount = hashMapOf<UUID, Int>()
    private val barrierHolograms = hashMapOf<UUID, ArmorStand>()
    fun summonBarrier(entity: LivingEntity, tier: Int) {
        entity.passengers.clear()
        sound(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE) {
            pitch = 0f
            volume = 5f
            playAt(entity.location)
        }
        val amount = barrierLevels[tier - 1]
        val hologram: ArmorStand =
            entity.location.world.spawnEntity(entity.location, EntityType.ARMOR_STAND) as ArmorStand
        hologram.setGravity(false)
        hologram.canPickupItems = false
        hologram.isVisible = false
        hologram.isMarker = true
        hologram.isCustomNameVisible = true
        hologram.persistentDataContainer.set(pluginKey("ignore_damage"), PersistentDataType.BYTE, 0)
        hologram.customName(text("<bold><gold>Cinder Barrier: <white>$amount"))
        barrierAmount[entity.uniqueId] = amount
        barrierHolograms[entity.uniqueId] = hologram
        task(period = 1L) {
            if (hologram.isDead) {
                it.cancel()
                return@task
            }
            hologram.teleport(entity.eyeLocation.add(vec(y = .7)))
        }
        task(delay = 200L) {
            if (barrierAmount[entity.uniqueId].let { it == null || it <= 0 })
                return@task

            // in 10 seconds explode, killing the summoner
            val summoner =
                Macrocosm.loadedPlayers.values.firstOrNull { player -> player.boundSlayerBoss == entity.uniqueId }
                    ?: return@task
            particle(Particle.REDSTONE) {
                data = DustOptions(Color.BLACK, 2f)
                this.amount = 15
                offset = Vector.getRandom()
                spawnAt(entity.location)
            }
            sound(Sound.ENTITY_ENDER_DRAGON_HURT) {
                pitch = 0f
                volume = 5f
                playAt(entity.location)
            }
            task(delay = 10L) { summoner.kill(text("<gold>Cinder Blast")) }
        }
    }

    private val searingClaimed = mutableListOf<Location>()
    val INFERNO_BURST = SlayerAbility(
        "inferno_burst",
        SlayerType.CINDERFLAME_SPIRIT,
        "<yellow>Inferno Burst",
        "Every <green>10 seconds<gray> a burst of <gold>intense flames<gray> covers area around the boss, dealing <red>[0/0/2500/5000] ${Statistic.DAMAGE.display}<gray> to nearby players. The heat leaves behind <red>Scorched Earth<gray>. Players rapidly lose their <aqua>${Statistic.INTELLIGENCE.specialChar} Mana<gray> while standing on it."
    ) {
        task(period = 20L) {
            Macrocosm.loadedPlayers.values.forEach { player ->
                if (searingClaimed.isEmpty())
                    return@forEach
                val stats = player.stats() ?: return@forEach
                if (player.currentMana < stats.intelligence * .2)
                    return@forEach
                val loc = player.paper?.location ?: return@forEach
                val bb = BoundingBox(loc.x - 1, loc.y - 1, loc.z - 1, loc.x + 1, loc.y + 1, loc.z + 1)
                if (searingClaimed.any { claimed -> bb.contains(claimed.x, claimed.y, claimed.z) })
                    player.decreaseMana((player.stats()?.intelligence ?: return@forEach) * .2f)
            }
        }
        task(period = 200L) {
            applyToBosses { _, living, tier ->
                if (tier !in 3..4)
                    return@applyToBosses
                sound(Sound.ENTITY_ENDER_DRAGON_GROWL) {
                    pitch = 2f
                    volume = 5f
                    playAt(living.location)
                }
                // generate a circle of particles around boss

                val center = living.location
                center.getNearbyPlayers(4.0).forEach { player ->
                    val mc = player.macrocosm ?: return@forEach
                    val dmg = DamageCalculator.calculateStandardReceived(
                        if (tier == 3) 2500f else 5000f,
                        mc.stats() ?: return@forEach
                    )
                    mc.damage(dmg, text("Inferno Burst"))
                }
                var i = 0f
                while (i < Mth.TWO_PI) {
                    i += .1f
                    val x = Mth.cos(i) * 4
                    val z = Mth.sin(i) * 4

                    val c = center.clone()
                    c.add(vec(x = x, z = z))
                    particle(Particle.FLAME) {
                        amount = 3
                        offset = vec()
                        extra = 0
                        spawnAt(c)
                    }
                }

                val searingGround = hashMapOf<Location, Pair<Material, BlockData>>()
                val radius = 4
                val sqRadius = radius * radius
                for (j in -radius..radius) {
                    for (k in -radius..radius) {
                        if ((j * j + k * k) <= sqRadius) {
                            // raycasting down, to target our block needed to be replaced
                            val up = Location(center.world, center.x + j, center.y + 1, center.z + k)
                            val target = raycast(up, vec(y = -1), 6).add(vec(y = -1))
                            if (searingClaimed.contains(target))
                                continue
                            val oldData = target.block.blockData
                            val oldType = target.block.type
                            searingClaimed.add(target)
                            searingGround[target] = Pair(oldType, oldData)
                            target.block.type = if (Random.nextBoolean()) Material.BLACKSTONE else Material.MAGMA_BLOCK
                            task(delay = 200L) {
                                searingClaimed.remove(target)
                                val (restoreType, restoreData) = searingGround.remove(target)!!
                                target.block.type = restoreType
                                target.block.blockData = restoreData
                            }
                        }
                    }
                }
            }
        }
    }

    private val disabledVitality = mutableListOf<UUID>()
    private val underworldUnsealed = mutableListOf<UUID>()
    val UNDERWORLD_UNSEALED = SlayerAbility(
        "underworld_unsealed",
        SlayerType.CINDERFLAME_SPIRIT,
        "<dark_red>Underworld Unsealed",
        "<dark_gray>At 10% HP<br><gray>The boss unleashes a massive meteor from the sky, causing an explosion upon impact and dealing <red>instantly killing<gray> all players within <green>8 block<gray> radius. After the explosion, sets your ${Statistic.VITALITY.display}<gray> to <dark_red>zero<gray>, disabling natural regeneration until the end of fight."
    ) {
        listen<PlayerKillEntityEvent> { e ->
            if (e.player.boundSlayerBoss == e.killed.uniqueId && e.player.slayerQuest?.let { it.tier == 4 && it.type == SlayerType.CINDERFLAME_SPIRIT } == true) {
                disabledVitality.remove(e.player.ref)
                underworldUnsealed.remove(e.killed.uniqueId)
            }
        }
        listen<PlayerCalculateStatsEvent> { e ->
            if (disabledVitality.contains(e.player.ref))
                e.stats.vitality = 0f
        }
        listen<PlayerDealDamageEvent> { e ->
            if (SlayerAbility.bosses[SlayerType.CINDERFLAME_SPIRIT]?.contains(e.damaged.uniqueId) != true || underworldUnsealed.contains(
                    e.damaged.uniqueId
                )
            )
                return@listen
            val mc = e.damaged.macrocosm ?: return@listen
            val id = mc.getId(e.damaged).path
            val tier = Integer.valueOf(id.replace("${slayerType.name.lowercase()}_", ""))
            if (tier != 4)
                return@listen
            val ratio = mc.currentHealth / mc.calculateStats().health
            if (ratio > .1f)
                return@listen
            e.player.paper?.title(text("<bold><red>UNDERWORLD UNSEALING"))
            underworldUnsealed.add(e.damaged.uniqueId)
            disabledVitality.add(e.player.ref)
            meteorSummon(e.damaged.location)
        }
    }

    private fun meteorSummon(at: Location) {
        sound(Sound.ENTITY_ENDER_DRAGON_DEATH) {
            volume = 5f
            pitch = 2f
            playAt(at)
        }

        val meteorCenter = at.clone().add(vec(y = 20)) // let's just hope that there are no solid blocks here

        val radius = 2
        val sqRadius = radius * radius // meteor is 2 blocks wide

        val entities = mutableListOf<Entity>()
        (-radius..radius).forEach { i ->
            (-radius..radius).forEach { j ->
                (-radius..radius).forEach { k ->
                    if ((i * i + j * j + k * k) <= sqRadius) {
                        val (entity1, entity2) = summonMeteorEntity(meteorCenter.clone().add(vec(i, j, k)))
                        entities.add(entity1)
                        entities.add(entity2)
                    }
                }
            }
        }

        var index = 0

        task(period = 1L) {
            if (index >= 39) {
                explodeMeteor(entities, at)
                it.cancel()
                return@task
            }

            // teleporting all stands
            for (entity in entities) {
                val relative = entity.location.add(vec(y = -.5))
                entity.teleport(relative)
                if (entity.collidesAt(at)) {
                    it.cancel()
                    explodeMeteor(entities, at)
                    return@task
                }
            }

            index += 1
        }
    }

    fun explodeMeteor(entities: MutableList<Entity>, at: Location) {
        entities.forEach { it.remove() }
        entities.clear()
        particle(Particle.EXPLOSION_LARGE) {
            amount = 8
            spawnAt(at)
        }
        particle(Particle.EXPLOSION_HUGE) {
            amount = 8
            spawnAt(at)
        }
        sound(Sound.ENTITY_GENERIC_EXPLODE) {
            pitch = 0f
            volume = 5f
            playAt(at)
        }
        at.getNearbyPlayers(8.0).forEach { it.macrocosm?.kill(text("<dark_red>Underworld Unsealed")) }
    }

    fun summonMeteorEntity(at: Location): Pair<Entity, Entity> {
        val stand = at.world.spawnEntity(at, EntityType.ARMOR_STAND) as ArmorStand
        stand.setGravity(false)
        stand.canPickupItems = false
        stand.isVisible = false
        stand.isMarker = true
        stand.persistentDataContainer.set(pluginKey("ignore_damage"), PersistentDataType.BYTE, 0)
        val mat = when (Random.nextInt(0, 3)) {
            0 -> Material.BLACKSTONE
            1 -> Material.SMOOTH_BASALT
            2 -> Material.MAGMA_BLOCK
            else -> unreachable()
        }
        val fallingBlock = stand.world.spawnFallingBlock(at, mat.createBlockData())
        fallingBlock.dropItem = false
        FallingBlockListener.stands.add(fallingBlock.uniqueId)
        stand.addPassenger(fallingBlock)
        return Pair(fallingBlock, stand)
    }
}
