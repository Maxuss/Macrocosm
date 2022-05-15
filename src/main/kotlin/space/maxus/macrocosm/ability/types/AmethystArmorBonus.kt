package space.maxus.macrocosm.ability.types

import net.axay.kspigot.event.listen
import org.bukkit.event.EventPriority
import space.maxus.macrocosm.ability.TieredSetBonus
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent

object AmethystArmorBonus: TieredSetBonus("Deflection", "Deflect <yellow>5%<gray> of damage you take back to the damager per <gold>Tier<gray> of this armor.") {
    override fun registerListeners() {
        listen<PlayerReceiveDamageEvent>(priority = EventPriority.LOWEST) { e ->
            val (ok, tier) = getArmorTier(e.player)
            if(!ok)
                return@listen
            e.damager.macrocosm!!.damage(e.damage * (.05f * tier))
        }
    }
}
