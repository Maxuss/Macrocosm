package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import org.bukkit.entity.EntityType
import org.bukkit.event.EventPriority
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.stats.Statistic

object GhoulBusterAbility: FullSetBonus("Ghoul Buster", "You deal <red>200% ${Statistic.DAMAGE.display}<gray> to <blue>Zombies<gray> but only <red>1% ${Statistic.DAMAGE.display}<gray> to other mobs.", true) {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent>(priority = EventPriority.LOW) { e ->
            if(!ensureSetRequirement(e.player))
                return@listen

            if(e.damaged.type == EntityType.ZOMBIE) {
                e.damage *= 2f
            } else {
                e.damage = e.damage * 0.01f
            }
        }
    }
}
