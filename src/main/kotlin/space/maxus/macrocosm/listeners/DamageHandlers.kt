package space.maxus.macrocosm.listeners

import net.axay.kspigot.extensions.bukkit.kill
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.extensions.geometry.multiply
import net.axay.kspigot.runnables.taskRunLater
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.NamespacedKey
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
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.text.comp
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random

fun summonDamageIndicator(loc: Location, damage: Float, crit: Boolean) {
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
    stand.isVisible = false
    stand.customName(display)
    stand.isCustomNameVisible = true
    stand.setGravity(false)
    stand.isSmall = true
    stand.persistentDataContainer.set(NamespacedKey(Macrocosm, "ignore_damage"), PersistentDataType.BYTE, 0)
    taskRunLater(30, runnable = stand::remove)
}

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

        // TODO: test this
        if (damager is Player) {
            damager.macrocosm!!.onAtsCooldown = true
            taskRunLater((((1 - (damagerStats.attackSpeed / 100f))) * 20f).roundToLong()) {
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

        val (damage, crit) = DamageCalculator.calculateStandardDealt(damagerStats.damage, damagerStats)
        val received = DamageCalculator.calculateStandardReceived(damage, damagedStats)

        if (damaged is Player) {
            damaged.macrocosm!!.damage(received, damagerName)
        } else {
            damaged.macrocosm.damage(received)
        }

        damaged.velocity = damager.eyeLocation.direction multiply 2f

        summonDamageIndicator(damaged.location, received, crit)
    }
}
