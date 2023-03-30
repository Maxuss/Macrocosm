package space.maxus.macrocosm.slayer.zombie

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.extensions.worlds
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.async
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.listeners.FallingBlockListener
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.slayer.SlayerAbility
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.data.MutableContainer
import space.maxus.macrocosm.util.equalsAny
import space.maxus.macrocosm.util.general.Ticker
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object ZombieAbilities {
    val REGENERATION = SlayerAbility(
        "regeneration",
        SlayerType.REVENANT_HORROR,
        "<red>Regeneration",
        "Boss rapidly regenerates <red>[50/150/1000/2500] ${Statistic.HEALTH.display}<gray> every second."
    ) {
        val healths = arrayOf(50, 150, 1000, 2500)
        task(period = 20L) {
            applyToBosses { mc, living, lvl ->
                if (lvl > 4)
                    return@applyToBosses
                val healthToRegen = healths[lvl - 1]
                mc.currentHealth = min(mc.currentHealth + healthToRegen, mc.calculateStats().health)
                sound(Sound.ENTITY_ARROW_HIT_PLAYER) {
                    volume = 1f
                    pitch = 2f
                    playAt(living.location)
                }
                particle(Particle.VILLAGER_HAPPY) {
                    offset = Vector.getRandom()
                    amount = 7
                    spawnAt(living.location)
                }
                mc.loadChanges(living)
            }
        }
    }

    val CRUMBLING_TOUCH = SlayerAbility(
        "crumbling_touch",
        SlayerType.REVENANT_HORROR,
        "<yellow>Crumbling Touch",
        "Every <green>5 seconds<gray> boss will shatter your armor, <yellow>decreasing<gray> your ${Statistic.DEFENSE.display}<gray> by <red>[10/20/30/50]%<gray>."
    ) {
        val multiples = listOf(.1, .2, .3, .5)
        task(period = 5 * 20L) {
            applyToBosses { _, living, lvl ->
                if (lvl > 4 || lvl < 2)
                    return@applyToBosses
                val amount = multiples[lvl - 2]

                val nearby = living.getNearbyEntities(6.0, 6.0, 6.0).filterIsInstance<Player>()
                for (player in nearby) {
                    val m = player.macrocosm!!
                    val defense = amount * m.stats()!!.defense
                    m.tempStats.defense -= defense.toFloat()
                    task(delay = 10 * 20L) {
                        m.tempStats.defense += defense.toFloat()
                    }
                    particle(Particle.BLOCK_CRACK) {
                        data = Material.NETHERITE_BLOCK.createBlockData()
                        this@particle.amount = 8
                        spawnAt(player.location)
                    }
                    sound(Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR) {
                        pitch = 0f
                        playAt(player.location)
                    }
                }
            }
        }
    }

    val CONSTANT_FEAR = SlayerAbility(
        "constant_fear",
        SlayerType.REVENANT_HORROR,
        "<red>Constant Fear",
        "Boss reduces ${Statistic.SPEED.display}<gray> of nearby players by <red>50%<gray> and steals <red>250 ${Statistic.HEALTH.display}/s<gray> from them."
    ) {
        val affected = mutableListOf<UUID>()
        task(period = 20L) {
            applyToBosses { boss, living, _ ->
                living.location.getNearbyPlayers(15.0) { !affected.contains(it.uniqueId) }.forEach {
                    val mc = it.macrocosm ?: return@forEach
                    if (!affected.contains(it.uniqueId)) {
                        val mod = -mc.stats()!!.speed * .5f
                        mc.tempStats.speed += mod
                        affected.add(it.uniqueId)

                        taskRunLater(5 * 20L) {
                            affected.remove(it.uniqueId)
                            mc.tempStats.speed -= mod
                        }
                    }
                    mc.damage(250f, text("<red>Constant Fear"))
                    boss.currentHealth = min(boss.currentHealth + 250, 10_000_000f)
                }
            }
        }
    }

    val IMPENDING_DOOM = SlayerAbility(
        "impending_doom",
        SlayerType.REVENANT_HORROR,
        "<dark_red>Impending Doom",
        "Every second that boss is alive, <red>Doom Level<gray> rises up. Let the boss hit yourself to <yellow>slightly reduce<gray> it."
    ) {
        task(period = 1L, delay = 21L) {
            doomCounter.iterFull { (eid, cid) ->
                val e = worlds[0].getEntity(eid) ?: return@iterFull
                val c = worlds[0].getEntity(cid) ?: return@iterFull
                c.teleport((e as LivingEntity).eyeLocation.add(vec(y = .7)))
            }
        }
        task(period = 20L) {
            doomCounter.filterAll { (eId, cId) ->
                val e = worlds[0].getEntity(eId)
                if (!(e == null || e.isDead)) {
                    true
                } else {
                    val c = worlds[0].getEntity(cId)
                    c?.remove()
                    false
                }
            }
            applyToBosses { _, living, lvl ->
                if (lvl != 6)
                    return@applyToBosses
                doomMeter.setOrTakeMut(living.uniqueId) { l ->
                    val t = l ?: 0
                    doomCounter.take(living.uniqueId) {
                        living.world.getEntity(it)?.customName(text("<red>⚡ Doom: <gold>${t + 1}"))
                    }.otherwise {
                        val counter = living.world.spawnEntity(living.location, EntityType.ARMOR_STAND) as ArmorStand
                        counter.isMarker = true
                        counter.isInvisible = true
                        counter.isCustomNameVisible = true
                        counter.customName(text("<red>⚡ Doom: <gold>${t + 1}"))
                        counter.persistentDataContainer.set(pluginKey("ignore_damage"), PersistentDataType.BYTE, 0)
                        living.passengers.add(counter)
                        doomCounter[living.uniqueId] = counter.uniqueId
                    }.call()
                    t + 1
                }
            }
        }
        listen<PlayerReceiveDamageEvent> { e ->
            val (success, tier) = isSlayerBoss(e.damager)
            if (!success || tier != 6)
                return@listen
            var level = 0
            doomMeter.takeMut(e.damager.uniqueId) {
                level = max(0, it - ceil(e.damage / 1000f).roundToInt())
                level
            }.then {
                doomCounter.take(e.damager.uniqueId) {
                    e.damager.world.getEntity(it)?.customName(text("<red>⚡ Doom: <gold>${level}"))
                }
            }.call()
        }    }

    val DOOMSTONE = SlayerAbility(
        "doomstone",
        SlayerType.REVENANT_HORROR,
        "<yellow>Doomstone Tomb",
        "<dark_gray>Every 10 Doom levels<br>Summons tombstones over each player within <green>5 blocks<gray>. The tombstones deal <red>0,5%<gray> of boss's missing ${Statistic.HEALTH.display}<gray> as ${Statistic.DAMAGE.display}<gray>."
    ) {
        task(period = 20L) {
            doomMeter.iterFull { (id, count) ->
                if (count % 10 == 0) {
                    val living = (worlds[0].getEntity(id) as? LivingEntity) ?: return@iterFull
                    val boss = living.macrocosm ?: return@iterFull
                    val missing = 10_000_000f - boss.currentHealth
                    val percentage = missing * .005f
                    for (nearby in living.location.getNearbyPlayers(5.0)) {
                        summonDoomstone(nearby.location, percentage)
                    }
                }
            }
        }
    }

    val MEAT_SKEWER = SlayerAbility(
        "meat_skewer",
        SlayerType.REVENANT_HORROR,
        "<gold>Tactical Acceleration",
        "<dark_gray>At 25/50/100/200 Doom<br>Boss charges at nearest player, piercing everything it collides with, and dealing <red>1,000 ${Statistic.DAMAGE.display}<gray>."
    ) {
        task(period = 20L) {
            doomMeter.iterFull { (id, count) ->
                if (count.equalsAny(25, 50, 100, 200)) {
                    val living = (worlds[0].getEntity(id) as? LivingEntity) ?: return@iterFull
                    living.macrocosm ?: return@iterFull
                    chargeAtNearby(
                        (living.location.getNearbyPlayers(10.0).firstOrNull() ?: return@iterFull).location,
                        living
                    )
                }
            }
        }
    }

    val EXANIMATED_REPEL = SlayerAbility(
        "exanimated_repel",
        SlayerType.REVENANT_HORROR,
        "<aqua>Exanimated Repel",
        "<dark_gray>At 75/150/300 Doom<br>Boss <red>repels<gray> all players around itself, dealing <red>1%<gray> of its current ${Statistic.HEALTH.display}<gray> as ${Statistic.DAMAGE.display}<gray> to <gold>all<gray> players hit by shockwave. It then constructs a <dark_aqua>Shield<gray>, that absorbs next <yellow>3<gray> hits, and deals <red>1,000 ${Statistic.DAMAGE.display}<gray> each."
    ) {
        task(period = 20L) {
            doomMeter.iterFull { (id, count) ->
                if (count.equalsAny(75, 150, 300)) {
                    val living = (worlds[0].getEntity(id) as? LivingEntity) ?: return@iterFull
                    val boss = living.macrocosm ?: return@iterFull
                    repel(living, boss)
                }
            }
        }
        listen<PlayerDealDamageEvent> { e ->
            shieldCount.takeMutOrRemove(e.damaged.uniqueId) {
                sound(Sound.ENTITY_ITEM_BREAK) {
                    pitch = 0f
                    volume = 5f
                    playAt(e.damaged.location)
                }
                e.isCancelled = true
                Pair(it - 1, if (it == 1) MutableContainer.TakeResult.REVOKE else MutableContainer.TakeResult.RETAIN)
            }
        }
        // rendering shields
        task(period = 3L) {
            shieldCount.iterFull { (eId, count) ->
                val e = worlds[0].getEntity(eId) ?: return@iterFull
                val point = e.location.add(vec(y = .5))

                async {
                    for (_i in 0 until count) {
                        val at = point.clone().add(vec(z = -1.5))

                        var i = 0f

                        while (i < Mth.PI * 2f) {
                            val x = Mth.cos(i) * .4f
                            val z = Mth.sin(i) * .4f

                            at.add(vec(x = x, z = z))

                            particle(Particle.CRIT_MAGIC) {
                                amount = 3
                                extra = 0

                                spawnAt(at)
                            }

                            i += Mth.PI / 15f
                        }

                        point.add(vec(y = .6))
                    }
                }
            }
        }
    }

    private fun repel(entity: LivingEntity, mc: MacrocosmEntity) {
        sound(Sound.BLOCK_SCULK_SHRIEKER_SHRIEK) {
            volume = 5f
            playAt(entity.location)
        }

        // repelling part
        task(delay = 10L) {
            val dmg = mc.currentHealth * .001f
            sound(Sound.ENTITY_WARDEN_HURT) {
                pitch = 0f
                volume = 5f

                playAt(entity.location)
            }
            for (nearest in entity.location.getNearbyPlayers(3.0)) {
                nearest.macrocosm?.damage(dmg, text("<dark_green>Exanimated Repel"))
                DamageHandlers.summonDamageIndicator(nearest.location, dmg)
                nearest.velocity = nearest.location.toVector().subtract(entity.location.toVector()).normalize()
                    .rotateAroundX(Math.toRadians(45.0)).normalize().multiply(6f)
            }

            // shield
            task(delay = 10L) {
                sound(Sound.BLOCK_AMETHYST_BLOCK_BREAK) {
                    pitch = 0f
                    volume = 5f

                    playAt(entity.location)

                    // blocks
                    shieldCount[entity.uniqueId] = 3
                }
            }
        }

    }

    private fun chargeAtNearby(to: Location, entity: LivingEntity) {
        val ticker = Ticker(0..20)
        sound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH) {
            volume = 5f
            playAt(entity.location)
        }
        val hit = mutableListOf<UUID>()
        task(period = 2L) {
            if (to.getNearbyLivingEntities(2.0).contains(entity)) {
                it.cancel()
                return@task
            }
            val tick = ticker.tick()
            val dir = to.toVector().subtract(entity.location.toVector()).normalize().multiply(3f)
            entity.velocity = dir

            for (player in entity.location.getNearbyPlayers(3.0) { p -> !hit.contains(p.uniqueId) }) {
                player.macrocosm?.damage(1500f, text("Entombed Horror"))
                hit.add(player.uniqueId)
                DamageHandlers.summonDamageIndicator(player.location, 7500f)
            }
            particle(Particle.REDSTONE) {
                data = DustOptions(Color.BLACK, 2f)
                amount = 6
                offset = Vector.getRandom()
                spawnAt(entity.location)
            }
            sound(Sound.BLOCK_NOTE_BLOCK_SNARE) {
                pitch = tick / 10f
                volume = 5f

                playAt(entity.location)
            }
        }
    }

    private fun summonDoomstone(at: Location, dmg: Float) {
        val pivot = at.add(vec(y = 4))
        val fallingBlock = FallingBlockListener.spawnBlock(pivot, bd)
        fallingBlock.velocity = vec(y = -1).normalize().multiply(.7f)
        task(delay = 10L) {
            fallingBlock.location.getNearbyPlayers(4.0).forEach {
                it.macrocosm?.damage(
                    dmg,
                    text("Doomstone Tomb")
                ); DamageHandlers.summonDamageIndicator(it.location, dmg)
            }
            sound(Sound.BLOCK_ANVIL_LAND) {
                volume = 4f
                pitch = 0f
                playAt(at)
            }
            particle(Particle.BLOCK_CRACK) {
                data = bd
                amount = 10
                offset = Vector.getRandom()
                spawnAt(at)
            }
        }
    }

    private val shieldCount = MutableContainer.empty<Int>()
    private val bd = Material.BLACKSTONE_STAIRS.createBlockData()
    val doomCounter = MutableContainer.empty<UUID>()
    private val doomMeter = MutableContainer.empty<Int>()
}
