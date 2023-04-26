package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.events.PlayerDealDamageEvent

object LiarsGiftAbility : AccessoryAbility(
    "liars_gift",
    "Grants a <green>10%<gray> chance to deal <green>7%<gray> more damage with each hit, " +
        "as well as <red>9%<gray> chance to deal <red>8%<gray> less damage."
) {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if(!hasAccs(e.player))
                return@listen
            val rollsMore = Macrocosm.random.nextFloat() <= .10
            val rollsLess = Macrocosm.random.nextFloat() <= .09
            if(rollsMore && !rollsLess) {
                sound(Sound.ENTITY_WITHER_AMBIENT) {
                    pitch = 2f
                    volume = .4f
                    playFor(e.player.paper!!)
                }
                e.damage *= 1.07f
            } else if(rollsLess && !rollsMore) {
                sound(Sound.ENTITY_ALLAY_DEATH) {
                    pitch = 1.35f
                    volume = .4f
                    playFor(e.player.paper!!)
                }
                e.damage *= .92f
            }
        }
    }
}
