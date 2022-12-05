package space.maxus.macrocosm.reforge.types

import net.axay.kspigot.particles.particle
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.util.Vector
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.reforge.ReforgeBase
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

object GleamingReforge :
    ReforgeBase(
        "Gleaming",
        "Luminokinetics",
        "Grants a <blue>5%<gray> chance to <yellow>dodge<gray> an attack if you are below <red>25% ${Statistic.HEALTH.display}<gray>.<br>The chance grows if more armor pieces are reforged.",
        ItemType.armor(),
        stats {
            speed = 3f
            strength = 2f
            health = -2f
            defense = 2f
        }) {
    @EventHandler
    fun onTakeDamage(e: PlayerReceiveDamageEvent) {
        val amount = getArmorUsedAmount(e.player)
        if (amount <= 0) {
            return
        }
        val chance = amount * .05f
        if (Macrocosm.random.nextFloat() <= chance) {
            e.isCancelled = true
            fancyEffect(e.player.paper!!.location)
        }
    }

    fun fancyEffect(at: Location) {
        particle(Particle.REDSTONE) {
            data = DustOptions(Color.fromRGB(0xF9DD5E), 2f)
            amount = 10
            offset = Vector.getRandom()
        }
        particle(Particle.REDSTONE) {
            data = DustOptions(Color.fromRGB(0x000000), 2f)
            amount = 10
            offset = Vector.getRandom()
        }
        sound(Sound.BLOCK_BELL_RESONATE) {
            pitch = 1.8f
            playAt(at)
        }
    }

    override fun clone(): Reforge {
        return GleamingReforge
    }
}
