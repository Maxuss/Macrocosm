package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import kotlin.math.ceil

class ScavengerTalisman(applicable: String, private val modifier: Float, description: String = "Monsters drop coins when killed"): AccessoryAbility(applicable, description) {
    override fun registerListeners() {
        listen<PlayerKillEntityEvent> { e ->
            if(!hasAccs(e.player))
                return@listen
            val mc = e.killed.macrocosm ?: return@listen
            val amount = ceil(mc.level * .05f) * modifier
            e.player.purse += amount.toBigDecimal()
            sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP) {
                pitch = 2f
                volume = .5f
                playAt(e.killed.location)
            }
        }
    }
}
