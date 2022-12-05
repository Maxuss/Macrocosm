package space.maxus.macrocosm.reforge.types

import net.axay.kspigot.extensions.geometry.vec
import org.bukkit.Location
import org.bukkit.event.EventHandler
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.reforge.ReforgeBase
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

object LambentReforge : ReforgeBase(
    "Lambent",
    "<gradient:#fccd14:#fcf9ea>Luminodynamics</gradient>",
    "Grants a <blue>4%<gray> chance to <yellow>dodge<gray> an attack and teleport away if you are below <red>25% ${Statistic.HEALTH.display}<gray>.<br>The chance grows if more armor pieces are reforged.",
    ItemType.armor(),
    stats {
        speed = 4f
        critDamage = 3f
        health = -3f
        intelligence = 2f
    }
) {
    @EventHandler
    fun onTakeDamage(e: PlayerReceiveDamageEvent) {
        val amount = getArmorUsedAmount(e.player)
        if (amount <= 0) {
            return
        }
        val chance = amount * .05f
        if (Macrocosm.random.nextFloat() <= chance) {
            e.isCancelled = true

            val p = e.player.paper!!
            val loc = p.location
            decideTeleportPosition(loc)

            p.teleport(loc)

            GleamingReforge.fancyEffect(loc)
        }
    }

    private fun decideTeleportPosition(base: Location) {
        val dX = (Macrocosm.random.nextDouble() - .5) * 32
        val dY = Macrocosm.random.nextInt(32) - 16
        val dZ = (Macrocosm.random.nextDouble() - .5) * 32

        base.add(vec(dX, dY, dZ))

        while (!base.block.isSolid) {
            base.add(.0, -1.0, .0)
        }
    }

    override fun clone(): Reforge {
        return LambentReforge
    }
}
