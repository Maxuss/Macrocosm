package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.extensions.server
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageKind
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.data.MutableContainer
import java.time.Duration
import java.time.Instant
import java.util.*
import kotlin.math.min

object SpiritsPolaritySetBonus : FullSetBonus(
    "Blessing of Spirits",
    "Attacking enemies with <red>Melee<gray> restores <aqua>50 ${Statistic.INTELLIGENCE.specialChar} Mana<gray> and grants <red>+2.5% ${Statistic.ABILITY_DAMAGE.display}<gray> for <green>5 seconds <dark_gray>(capped at +50%)<gray>.<br> Your ${Statistic.VIGOR.display}<gray> is set to <red>0<gray>.${
        exemptsEssence(
            "<red>Chaotic <green>Good"
        )
    }"
) {
    private val currentBonus = MutableContainer.empty<Float>()

    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if (e.kind != DamageKind.MELEE || !ensureSetRequirement(e.player))
                return@listen
            e.player.currentMana += min(e.player.stats()?.intelligence ?: return@listen, e.player.currentMana + 50)
            currentBonus.setOrTakeMut(e.player.ref) { bonus ->
                if (bonus == null || bonus != 50f) {
                    e.player.tempStats.abilityDamage += 2.5f
                    particle(Particle.SOUL) {
                        amount = 4
                        offset = Vector.getRandom()
                        spawnAt(e.damaged.location)
                    }
                    taskRunLater(5 * 20L, sync = false) {
                        e.player.tempStats.abilityDamage -= 2.5f
                    }
                    (bonus ?: 0f) + 2.5f
                } else bonus
            }
        }
        listen<PlayerCalculateStatsEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            e.stats.vigor = 0f
        }
    }
}

object VoidPolaritySetBonus : FullSetBonus(
    "Blessing of Void",
    "Damaging <red>10 different enemies<gray> within <green>10 seconds<gray> summons a <dark_purple>Void Storm<gray>, dealing <red>500%<gray> of your weapon's normal ${Statistic.DAMAGE.display}<gray> to all <red>damaged<gray> enemies. You can not deal <yellow>any<gray> <red>Melee <gray>or <red>Ranged ${Statistic.DAMAGE.display}<gray> for the next <green>5 seconds<gray>.${
        exemptsEssence(
            "<red>Chaotic <yellow>Neutral"
        )
    }"
) {
    override val cost: AbilityCost = AbilityCost(cooldown = 20)

    private val damaged = MutableContainer.empty<MutableList<UUID>>()
    private val lastUse = MutableContainer.empty<Long>()

    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if (e.damaged is Player)
                return@listen
            lastUse.setOrTakeMut(e.player.ref) { lastUsed ->
                if (lastUsed != null && lastUsed != -1L) {
                    val secs = Duration.between(Instant.ofEpochMilli(lastUsed), Instant.now()).seconds
                    if (secs < 3) {
                        e.isCancelled = true
                        sound(Sound.ENTITY_IRON_GOLEM_HURT) {
                            pitch = 0f
                            playAt(e.player.paper!!.location)
                        }
                        return@setOrTakeMut lastUsed
                    } else if (secs < 20) {
                        return@setOrTakeMut lastUsed
                    } else {
                        if (!ensureSetRequirement(e.player))
                            return@setOrTakeMut -1L
                        else {
                            doCounterTick(e)
                            return@setOrTakeMut -1L
                        }
                    }
                } else {
                    if (!ensureSetRequirement(e.player))
                        return@setOrTakeMut -1L
                    else {
                        doCounterTick(e)
                        return@setOrTakeMut -1L
                    }
                }
            }
        }
    }

    private fun doCounterTick(e: PlayerDealDamageEvent) {
        damaged.setOrTakeMut(e.player.ref) {
            val damaged = it ?: mutableListOf()
            if (damaged.contains(e.damaged.uniqueId))
                return
            damaged.add(e.damaged.uniqueId)
            if (damaged.size >= 10) {
                val p = e.player.paper ?: return
                val damage = DamageCalculator.calculateStandardDealt(
                    p.inventory.itemInMainHand.macrocosm!!.stats(e.player).damage,
                    e.player.stats() ?: return
                )
                taskRunLater(1L) {
                    // we need to specifically set it on the next tick
                    lastUse[p.uniqueId] = Instant.now().toEpochMilli()
                }
                damaged.forEach { entity ->
                    val living = p.world.getEntity(entity) as? LivingEntity ?: return@forEach
                    if (living.isDead)
                        return@forEach
                    val mc = living.macrocosm ?: return@forEach
                    mc.damage(
                        DamageCalculator.calculateStandardReceived(damage.first, mc.calculateStats()),
                        p,
                        DamageKind.MAGIC
                    )
                    particle(Particle.REDSTONE) {
                        amount = 15
                        data = DustOptions(Color.fromRGB(0x230037), 2f)
                        offset = Vector.getRandom()
                        spawnAt(living.location)
                    }
                    particle(Particle.REDSTONE) {
                        amount = 7
                        data = DustOptions(Color.fromRGB(0x7433C9), 1.7f)
                        offset = Vector.getRandom()
                        spawnAt(living.eyeLocation)
                    }
                }
                sound(Sound.ENTITY_EVOKER_FANGS_ATTACK) {
                    volume = 2f
                    pitch = 0f
                    playAt(p.location)
                }
                return@setOrTakeMut mutableListOf()
            }
            damaged
        }
    }
}

object ChaosPolaritySetBonus1 : FullSetBonus(
    "Blessing of Chaos",
    "Deal <red>+50% Ranged ${Statistic.DAMAGE.display}<gray> when being in less than <green>5 blocks<gray> to the enemy, otherwise you only deal <red>45% ${Statistic.DAMAGE.display}<gray>.<br>Gain <red>+30% ${Statistic.ABILITY_DAMAGE.display}<gray> when you have less than <aqua>20% ${Statistic.INTELLIGENCE.specialChar} Mana<gray>, otherwise your ${Statistic.ABILITY_DAMAGE.display}<gray> is capped at <red>25%<gray>."
) {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if (e.kind != DamageKind.RANGED || !ensureSetRequirement(e.player))
                return@listen
            val p = e.player.paper ?: return@listen
            val distance = p.location.distance(e.damaged.location)
            if (distance <= 5f) {
                e.damage *= 1.5f
            } else {
                e.damage *= .45f
            }
        }
        listen<PlayerCalculateStatsEvent>(priority = EventPriority.LOWEST) { e ->
            if (!ensureSetRequirement(e.player) || e.player.statCache == null)
                return@listen
            val manaPercentage = e.player.currentMana / (e.player.stats()?.intelligence ?: return@listen)
            if (manaPercentage <= .2f) {
                e.stats.abilityDamage += 30f
            } else {
                e.stats.abilityDamage = min(e.stats.abilityDamage, 25f)
            }
        }
    }
}

object ChaosPolaritySetBonus2 : FullSetBonus(
    "Chaos Shield",
    "Kill <red>3 enemies<gray> to accumulate a layer of a <gold>Chaos Shield<dark_gray> (3 layers max)<gray>. The <gold>Chaos Shield<gray> returns <red>250%<gray> of ${Statistic.DAMAGE.display}<gray> received to the damager.${
        exemptsEssence(
            "<red>Devotedly Chaotic"
        )
    }"
) {
    private val kills = MutableContainer.empty<Int>()
    private val layers = MutableContainer.empty<Int>()

    override fun registerListeners() {
        listen<PlayerKillEntityEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            kills.setOrTakeMut(e.player.ref) {
                var new = (it ?: 0) + 1
                if (new >= 3)
                    layers.setOrTakeMut(e.player.ref) { possibleLayers ->
                        new = 0
                        val layers = possibleLayers ?: 0
                        if (layers < 3) {
                            layers + 1
                        } else {
                            layers
                        }
                    }
                new
            }
        }
        listen<PlayerReceiveDamageEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            layers.takeMutOrRemove(e.player.ref) { layers ->
                val returnDamage = e.damage * 2.5f
                sound(Sound.ENTITY_GENERIC_EXPLODE) {
                    pitch = 2f
                    volume = 5f
                    playAt(e.damager.location)
                }
                e.damager.macrocosm?.damage(returnDamage)
                val newLayers = layers - 1
                if (newLayers <= 0)
                    -1 to MutableContainer.TakeResult.REVOKE
                else newLayers to MutableContainer.TakeResult.RETAIN
            }
        }
        // rendering the shield
        task(period = 20L) {
            layers.iterFull { (pid, layers) ->
                val p = server.getPlayer(pid) ?: return@iterFull
                if (!ensureSetRequirement(p.macrocosm ?: return@iterFull))
                    return@iterFull
                val color = when (layers) {
                    1 -> 0xC97E33
                    2 -> 0x40C933
                    3 -> 0x33C99E
                    else -> 0x000000
                }
                particle(Particle.REDSTONE) {
                    amount = 15
                    data = DustOptions(Color.fromRGB(color), 2.0f)
                    offset = Vector.getRandom()
                    spawnAt(p.location.add(vec(y = .5)))
                }
            }
        }
    }
}
