package space.maxus.macrocosm.listeners

import net.axay.kspigot.extensions.bukkit.kill
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.extensions.geometry.increase
import net.axay.kspigot.extensions.geometry.reduce
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.util.Mth
import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftLivingEntity
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.relativeLocation
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlin.random.nextInt

object DamageHandlers : Listener {
    @EventHandler
    fun handleEntityDamage(e: EntityDamageByEntityEvent) {
        e.isCancelled = true
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

        if (damager is Player) {
            val mc = damager.macrocosm!!
            if (mc.onAtsCooldown)
                return
        }

        val damagerStats = if (damager is Player)
            damager.macrocosm!!.calculateStats()!!
        else
            damager.macrocosm.calculateStats()

        if (damager is Player) {
            damager.macrocosm!!.onAtsCooldown = true
            // 0.45s is default attack speed, becomes 0.2s at 100 attack speed
            taskRunLater((((1.2 - (damagerStats.attackSpeed / 100f))) * 9f).roundToLong()) {
                damager.macrocosm!!.onAtsCooldown = false
            }
        }

        val damagerName = if (damager is Player)
            damager.displayName()
        else
            damager.macrocosm.name

        val damagedStats = if (damaged is Player)
            damaged.macrocosm!!.calculateStats()!!
        else
            damaged.macrocosm.calculateStats()

        var (damage, crit) = DamageCalculator.calculateStandardDealt(damagerStats.damage, damagerStats)

        if (damager is Player) {
            val event = PlayerDealDamageEvent(damager.macrocosm!!, damaged, damage, crit)
            val cancelled = !event.callEvent()
            if (cancelled)
                return
            damage = event.damage
            crit = event.crit
        }

        if (damaged is Player) {
            val event = PlayerReceiveDamageEvent(damaged.macrocosm!!, damager, damage, crit)
            val cancelled = !event.callEvent()
            if (cancelled)
                return
            damage = event.damage
            crit = event.crit
        }

        val received = DamageCalculator.calculateStandardReceived(damage, damagedStats)

        if (damaged is Player) {
            damaged.macrocosm!!.damage(received, damagerName)
        } else {
            damaged.macrocosm.damage(received, damager)
        }

        // get knockback level here
        val knockbackAmount = .5226
        val nmsDamaged = (damaged as CraftLivingEntity).handle
        val nmsDamager = (damager as CraftLivingEntity).handle
        nmsDamaged.knockback(
            knockbackAmount,
            Mth.sin(nmsDamager.getYRot() * 0.017453292F).toDouble(),
            -Mth.cos(nmsDamager.getYRot() * 0.017453292F).toDouble(),
            nmsDamager
        )
        nmsDamager.deltaMovement = nmsDamager.deltaMovement.multiply(.6, 1.0, 0.6)

        summonDamageIndicator(damaged.location, received, crit)

        processFerocity(damage, crit, damagerStats, damaged, damagerName)
    }

    private fun processFerocity(
        damage: Float,
        crit: Boolean,
        stats: Statistics,
        entity: LivingEntity,
        source: Component
    ) {
        var ferocity = stats.ferocity
        if (stats.ferocity > 0) {
            val times = floor(stats.ferocity / 100f)
            ferocity -= times
            for (i in 0 until times.toInt()) {
                taskRunLater(15L + i * 3) {
                    activateFerocity(damage, crit, entity, source)
                }
            }
        }
        if (Random.nextFloat() < (ferocity / 100f)) {
            taskRunLater(20L) {
                activateFerocity(damage, crit, entity, source)
            }
        }
    }

    private fun activateFerocity(damage: Float, crit: Boolean, entity: LivingEntity, source: Component) {
        if (entity.isDead)
            return
        if (entity is Player) {
            entity.macrocosm!!.damage(damage, source)
        } else {
            entity.macrocosm.damage(damage)
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
            playAt(entity.location)
        }
        summonDamageIndicator(entity.location, damage, crit)
    }

    private fun summonDamageIndicator(loc: Location, damage: Float, crit: Boolean) {
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
        val display = if (crit) {
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
            comp("<white>✧</white>").append(display).append(comp("<white>✧</white>"))
        } else {
            damageDisplay.toComponent().color(NamedTextColor.GRAY)
        }

        val stand = newLocation.world.spawnEntity(newLocation, EntityType.ARMOR_STAND) as ArmorStand
        stand.isInvulnerable = true
        stand.isMarker = true
        stand.isVisible = false
        stand.customName(display)
        stand.isCustomNameVisible = true
        stand.setGravity(false)
        stand.persistentDataContainer.set(NamespacedKey(Macrocosm, "ignore_damage"), PersistentDataType.BYTE, 0)
        taskRunLater(30, runnable = stand::remove)
    }
}
