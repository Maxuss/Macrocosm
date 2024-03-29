package space.maxus.macrocosm.listeners

import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise
import net.axay.kspigot.extensions.bukkit.kill
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.extensions.geometry.increase
import net.axay.kspigot.extensions.geometry.reduce
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.util.Mth
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftLivingEntity
import org.bukkit.entity.*
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageKind
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.damage.relativeLocation
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.EntityActivateFerocityEvent
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlin.random.nextInt

fun Entity.isPlayerDisguise() = DisguiseAPI.isDisguised(this) && DisguiseAPI.getDisguise(this) is PlayerDisguise

object DamageHandlers : Listener {
    fun internalHandleDamage(
        bukkitEvent: Cancellable,
        damager: LivingEntity,
        damaged: LivingEntity,
        kind: DamageKind,
        checkAts: Boolean = true /* e: EntityDamageEvent */
    ) {
        if (damager is Player) {
            val mc = damager.macrocosm!!
            if (checkAts && mc.onAtsCooldown)
                return
        }

        val (damagerStats, damagerSpecials) = if (damager is Player)
            Pair(damager.macrocosm!!.stats()!!, damager.macrocosm!!.specialStats()!!)
        else
            Pair(damager.macrocosm!!.calculateStats(), damager.macrocosm!!.specialStats())

        if (damager is Player && checkAts) {
            damager.macrocosm!!.onAtsCooldown = true
            // 0.45s is default attack speed, becomes 0.2s at 100 attack speed
            taskRunLater(((1.2 - (damagerStats.attackSpeed / 100f)) * 9f).roundToLong()) {
                damager.macrocosm?.onAtsCooldown = false
            }
        }

        val damagerName = if (damager is Player)
            damager.displayName()
        else
            damager.macrocosm!!.name

        val (damagedStats, damagedSpecials) = if (damaged is Player)
            Pair(damaged.macrocosm!!.stats()!!, damaged.macrocosm!!.specialStats()!!)
        else
            Pair(damaged.macrocosm!!.calculateStats(), damaged.macrocosm!!.specialStats())

        var (damage, crit) = DamageCalculator.calculateStandardDealt(damagerStats.damage, damagerStats)

        var superCrit = false
        if (damager is Player) {
            val event = PlayerDealDamageEvent(damager.macrocosm!!, damaged, damage, crit, kind)
            val cancelled = !event.callEvent()
            if (cancelled) {
                bukkitEvent.isCancelled = true
                return
            }
            damage = event.damage
            crit = event.crit
            superCrit = event.isSuperCrit
        }

        if (damaged is Player) {
            val event = PlayerReceiveDamageEvent(damaged.macrocosm!!, damager, damage, crit)
            val cancelled = !event.callEvent()
            if (cancelled) {
                bukkitEvent.isCancelled = true
                return
            }
            damage = event.damage
            crit = event.crit
        }

        val received = DamageCalculator.calculateStandardReceived(damage, damagedStats)

        // damaged.lastDamageCause = e

        if (damaged is Player) {
            damaged.macrocosm!!.damage(received, damagerName)
        } else {
            damaged.macrocosm!!.damage(received, damager)
        }

        // get knockback level here
        val knockbackAmount = .5226 * (1 + damagerSpecials.knockbackBoost) * (1 - damagedSpecials.knockbackResistance)
        val nmsDamaged = (damaged as CraftLivingEntity).handle
        val nmsDamager = (damager as CraftLivingEntity).handle
        nmsDamaged.knockback(
            knockbackAmount,
            Mth.sin(nmsDamager.yRot * 0.017453292F).toDouble(),
            -Mth.cos(nmsDamager.yRot * 0.017453292F).toDouble(),
            nmsDamager
        )
        nmsDamager.deltaMovement = nmsDamager.deltaMovement.multiply(.6, 1.0, 0.6)

        summonDamageIndicator(
            damaged.location,
            received,
            if (superCrit) DamageType.SUPER_CRITICAL else if (crit) DamageType.CRITICAL else DamageType.DEFAULT
        )

        if (superCrit) {
            sound(Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR) {
                pitch = 2f
                volume = 3f

                playAt(damaged.location)
            }
        }

        processFerocity(received, crit, damagerStats, damaged, damagerName, damager)
    }

    @EventHandler
    fun onStandardDamage(e: EntityDamageByEntityEvent) {
        if (e.isCancelled)
            return
        e.damage = 0.0
        val damager = e.damager
        val damaged = e.entity
        if (damaged is ArmorStand) {
            if (damaged.persistentDataContainer.has(NamespacedKey(Macrocosm, "ignore_damage")))
                return
            damaged.kill()
            return
        }

        if (damager !is LivingEntity || damaged !is LivingEntity)
            return

        internalHandleDamage(e, damager, damaged, DamageKind.MELEE)
    }

    @EventHandler
    fun onProjectileHit(e: ProjectileHitEvent) {
        if (e.entity.persistentDataContainer.has(pluginKey("despawn_me")))
            e.entity.remove()
        if (e.entity.persistentDataContainer.has(pluginKey("damage_deal"))) {
            val id =
                UUID.fromString(e.entity.persistentDataContainer.get(pluginKey("owner"), PersistentDataType.STRING))
            val damager = e.entity.world.getEntity(id) ?: return
            val dmg = e.entity.persistentDataContainer.get(pluginKey("damage_deal"), PersistentDataType.FLOAT)!!
            val damaged = e.hitEntity
            if (damaged == null || damaged !is LivingEntity)
                return
            damaged.macrocosm?.damage(dmg, damager)
            summonDamageIndicator(damaged.location, dmg)
            return
        }
        val shooter = e.entity.shooter
        if (shooter == null || shooter !is LivingEntity || e.hitEntity is ArmorStand) {
            e.isCancelled = true
            return
        }
        val damaged = e.hitEntity
        if (damaged == null || damaged !is LivingEntity)
            return

        if (damaged is ArmorStand) {
            if (damaged.persistentDataContainer.has(NamespacedKey(Macrocosm, "ignore_damage")))
                return
            damaged.kill()
            return
        }

        internalHandleDamage(e, shooter, damaged, DamageKind.RANGED, false)
    }

    @EventHandler
    fun onFallDamage(e: EntityDamageEvent) {
        if (e.cause != DamageCause.FALL)
            return
        val entity = e.entity as LivingEntity
        var damage = e.damage * 5
        e.damage = .0
        if (entity is Player) {
            val stats = entity.macrocosm!!.specialStats()!!
            damage *= (1 - stats.fallResistance)
            entity.macrocosm!!.damage(damage.toFloat(), text("fall"))
        } else {
            val stats = entity.macrocosm!!.specialStats()
            damage *= (1 - stats.fallResistance)
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat())
    }

    private val fireCauses = listOf(
        DamageCause.FIRE,
        DamageCause.MELTING,
        DamageCause.LAVA,
        DamageCause.HOT_FLOOR,
        DamageCause.LAVA,
        DamageCause.FIRE_TICK,
        DamageCause.DRYOUT
    )

    @EventHandler
    fun onFireDamage(e: EntityDamageEvent) {
        if (!fireCauses.contains(e.cause))
            return
        var damage = e.damage * 10
        e.damage = .0
        if (e.entity !is LivingEntity)
            return
        val entity = e.entity as LivingEntity
        if (entity is Player) {
            val stats = entity.macrocosm!!.specialStats()!!
            damage *= (1 - stats.fireResistance)
            entity.macrocosm!!.damage(damage.toFloat(), text("fire"))
        } else {
            val stats = entity.macrocosm!!.specialStats()
            damage *= (1 - stats.fireResistance)
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat(), DamageType.FIRE)
    }

    @EventHandler
    fun onWitherDamage(e: EntityDamageEvent) {
        if (e.cause != DamageCause.WITHER)
            return
        val damage = e.damage * 10
        e.damage = .0
        val entity = e.entity as LivingEntity
        if (entity is Player) {
            entity.macrocosm!!.damage(damage.toFloat(), text("withering"))
        } else {
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat())
    }

    @EventHandler
    fun onPoisonDamage(e: EntityDamageEvent) {
        if (e.cause != DamageCause.POISON)
            return
        val damage = e.damage * 10
        e.damage = .0
        val entity = e.entity as LivingEntity
        if (entity is Player) {
            entity.macrocosm!!.damage(damage.toFloat(), text("poison"))
        } else {
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat())
    }

    @EventHandler
    fun onDrowningDamage(e: EntityDamageEvent) {
        if (e.cause != DamageCause.DROWNING)
            return
        val damage = e.damage * 10
        e.damage = .0
        val entity = e.entity as LivingEntity
        if (entity is Player) {
            entity.macrocosm!!.damage(damage.toFloat(), text("drowning"))
        } else {
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat())
    }

    @EventHandler
    fun onExplosionDamage(e: EntityDamageEvent) {
        if (e.cause != DamageCause.ENTITY_EXPLOSION || e.cause != DamageCause.BLOCK_EXPLOSION)
            return
        val damage = e.damage * 25
        e.damage = .0
        val entity = e.entity as LivingEntity
        if (entity is Player) {
            entity.macrocosm!!.damage(damage.toFloat(), text("explosion"))
        } else {
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat(), DamageType.CRITICAL)
    }

    @EventHandler
    fun onLightningHit(e: EntityDamageEvent) {
        if (e.cause != DamageCause.LIGHTNING)
            return
        val damage = e.damage * 10
        e.damage = .0
        val entity = e.entity as? LivingEntity ?: return
        if (entity is Player) {
            entity.macrocosm!!.damage(damage.toFloat(), text("electricity"))
        } else {
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat(), DamageType.ELECTRIC)
    }

    @EventHandler
    fun onMagicHit(e: EntityDamageEvent) {
        if (e.cause != DamageCause.MAGIC)
            return
        val damage = e.damage * 25
        e.damage = .0
        val entity = e.entity as LivingEntity
        if (entity is Player) {
            entity.macrocosm!!.damage(damage.toFloat(), text("<dark_purple>magic<gray>"))
        } else {
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat(), DamageType.CRITICAL)
    }

    @EventHandler
    fun onVoidHit(e: EntityDamageEvent) {
        if (e.cause != DamageCause.VOID)
            return
        val damage = e.damage * 15
        e.damage = .0
        val entity = e.entity as LivingEntity
        if (entity is Player) {
            entity.macrocosm!!.damage(damage.toFloat(), text("<dark_gray>void<gray>"))
        } else {
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat())
    }

    @EventHandler
    fun onStarvationHit(e: EntityDamageEvent) {
        if (e.cause != DamageCause.STARVATION) {
            return
        }
        val damage = e.damage * 40
        e.damage = .0
        val entity = e.entity as LivingEntity
        if (entity is Player) {
            entity.macrocosm!!.damage(damage.toFloat(), text("hunger"))
        } else {
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat())
    }

    @EventHandler
    fun onPhysicsHit(e: EntityDamageEvent) {
        if (e.cause != DamageCause.CRAMMING || e.cause != DamageCause.FLY_INTO_WALL) {
            return
        }
        val damage = e.damage * 10
        e.damage = .0
        val entity = e.entity as LivingEntity
        if (entity is Player) {
            entity.macrocosm!!.damage(damage.toFloat(), text("physics"))
        } else {
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat())
    }

    @EventHandler
    fun onContactHit(e: EntityDamageEvent) {
        if (e.cause != DamageCause.CONTACT) {
            return
        }
        val damage = e.damage * 10
        e.damage = .0
        val entity = e.entity as LivingEntity
        if (entity is Player) {
            entity.macrocosm!!.damage(damage.toFloat(), text("being inaccurate"))
        } else {
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat())
    }

    @EventHandler
    fun onSuicide(e: EntityDamageEvent) {
        if (e.cause != DamageCause.SUICIDE)
            return
        e.isCancelled = true
        val entity = e.entity as LivingEntity
        if (entity is Player) {
            entity.macrocosm!!.kill(text("self-hatred"))
        } else {
            entity.macrocosm!!.kill()
        }
        summonDamageIndicator(entity.location, Int.MAX_VALUE.toFloat(), DamageType.CRITICAL)
    }

    @EventHandler
    fun onFreezeDamage(e: EntityDamageEvent) {
        if (e.cause != DamageCause.FREEZE)
            return
        val damage = e.damage * 10
        e.damage = .0
        val entity = e.entity as LivingEntity
        if (entity is Player) {
            entity.macrocosm!!.damage(damage.toFloat(), text("freezing"))
        } else {
            entity.macrocosm!!.damage(damage.toFloat())
        }
        summonDamageIndicator(entity.location, damage.toFloat(), DamageType.FROST)
    }

    @EventHandler
    fun onDeath(e: EntityDeathEvent) {
        // clearing drops, because they are handled internally
        e.drops.clear()
    }

    private fun processFerocity(
        damage: Float,
        crit: Boolean,
        stats: Statistics,
        entity: LivingEntity,
        source: Component,
        damager: Entity
    ) {
        var ferocity = stats.ferocity
        if (stats.ferocity > 0) {
            val times = floor(stats.ferocity / 100f)
            ferocity -= times
            for (i in 0 until times.toInt()) {
                taskRunLater(15L + i * 3) {
                    activateFerocity(damage, crit, entity, source, damager)
                }
            }
        }
        if (Random.nextFloat() < (ferocity / 100f)) {
            taskRunLater(20L) {
                activateFerocity(damage, crit, entity, source, damager)
            }
        }
    }

    private fun activateFerocity(
        damage: Float,
        crit: Boolean,
        entity: LivingEntity,
        source: Component,
        damager: Entity,
    ) {
        if (entity.isDead)
            return
        val event = EntityActivateFerocityEvent(damager, entity)
        if (!event.callEvent())
            return

        if (entity is Player) {
            entity.macrocosm!!.damage(damage, source)
        } else {
            entity.macrocosm!!.damage(damage, damager)
        }
        val vector = entity.eyeLocation.direction.clone() reduce vec(1) increase vec(y = 1)
        for (i in 0 until 12) {
            entity.world.spawnParticle(
                Particle.REDSTONE,
                vector.relativeLocation(entity.location),
                2 + Random.nextInt(0..2),
                DustOptions(Color.fromRGB(0x870606), 0.6f)
            )
            vector increase vec(x = .2)
        }
        sound(Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR) {
            this.pitch = 2f
            this.volume = 1f
            if (damager is Player)
                playFor(damager)
            else
                playAt(entity.location)
        }
        summonDamageIndicator(entity.location, damage, if (crit) DamageType.CRITICAL else DamageType.DEFAULT)
    }

    fun summonDamageIndicator(
        loc: Location,
        damage: Float,
        damageType: DamageType = DamageType.DEFAULT
    ) {
        val x: Double = loc.x
        val y: Double = loc.y
        val z: Double = loc.z

        val r1: Double = Random.nextDouble()
        val r2: Double = Random.nextDouble()
        val r3: Double = Random.nextDouble()

        val nx = x + if (Random.nextBoolean()) r1 else -r1
        val ny = y + if (Random.nextBoolean()) r2 else -r2 + 1
        val nz = z + if (Random.nextBoolean()) r3 else -r3

        val newLocation = Location(loc.world, nx, ny, nz)

        val damageDisplay = Formatting.withCommas(damage.roundToInt().toBigDecimal())
        val display = when (damageType) {
            DamageType.CRITICAL -> {
                var display = Component.empty()
                var digitIndex = 0
                for (char in damageDisplay) {
                    if (!char.isDigit()) {
                        display.append(",".toComponent().color(NamedTextColor.GOLD))
                        continue
                    }
                    digitIndex++
                    if (digitIndex > 5) {
                        digitIndex = 1
                    }
                    val color = when (digitIndex) {
                        2 -> NamedTextColor.YELLOW
                        3 -> NamedTextColor.GOLD
                        4, 5 -> NamedTextColor.RED
                        else -> NamedTextColor.WHITE
                    }
                    display = display.append(char.toString().toComponent().color(color))
                }
                text("<white>✧</white>").append(display).append(text("<white>✧</white>"))
            }

            DamageType.SUPER_CRITICAL -> {
                var display = Component.empty()
                var digitIndex = 0
                for (char in damageDisplay) {
                    if (!char.isDigit()) {
                        display.append(",".toComponent().color(NamedTextColor.GOLD))
                        continue
                    }
                    digitIndex++
                    if (digitIndex > 5) {
                        digitIndex = 1
                    }
                    val color = when (digitIndex) {
                        2 -> NamedTextColor.RED
                        3 -> NamedTextColor.GOLD
                        4, 5 -> NamedTextColor.YELLOW
                        else -> NamedTextColor.WHITE
                    }
                    display = display.append(char.toString().toComponent().color(color))
                }
                text("<gold>☠</gold>").append(display).append(text("<gold>☠</gold>"))
            }

            DamageType.FIRE -> {
                text("<gold>\uD83D\uDD25 <yellow>$damageDisplay<gold> \uD83D\uDD25")
            }

            DamageType.FROST -> {
                text("<white>❄ <aqua>$damageDisplay<white> ❄")
            }

            DamageType.ELECTRIC -> {
                text("<white>⚡ <yellow>$damageDisplay<white> ⚡")
            }

            else -> {
                damageDisplay.toComponent().color(NamedTextColor.GRAY)
            }
        }

        val dmgDisplay = newLocation.clone().add(vec(y = 1)).world.spawnEntity(newLocation, EntityType.TEXT_DISPLAY) as TextDisplay
        dmgDisplay.text(display)
        dmgDisplay.textOpacity = 220.toByte()
        dmgDisplay.isSeeThrough = false
        dmgDisplay.isShadowed = true
        dmgDisplay.billboard = Display.Billboard.VERTICAL
        taskRunLater(30, runnable = dmgDisplay::remove)
    }
}
