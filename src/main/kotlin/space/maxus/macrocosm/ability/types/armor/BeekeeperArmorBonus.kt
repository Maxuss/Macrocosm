package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.util.id
import java.util.*

object BeekeeperArmorBonus: FullSetBonus(
    "Honey Barrage",
    "Acquire <gold>Honey Shield<gray> when hurt, that <green>blocks<gray> next <green>2<gray> hits.") {
    private val amount: HashMap<UUID, Int> = hashMapOf()
    override val cost: AbilityCost = AbilityCost(cooldown = 10)

    override fun registerListeners() {
        listen<PlayerReceiveDamageEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen

            val hits = amount[e.player.paper!!.uniqueId]
            if (hits != null && hits > 0) {
                amount[e.player.paper!!.uniqueId] = hits - 1
                e.isCancelled = true
                particle(Particle.BLOCK_CRACK) {
                    data = Material.HONEYCOMB_BLOCK.createBlockData()
                    amount = 15
                    offset = Vector.getRandom()
                    spawnAt(e.player.paper!!.location)
                }
                sound(Sound.BLOCK_HONEY_BLOCK_BREAK) {
                    pitch = 0f
                    playAt(e.player.paper!!.location)
                }
                return@listen
            }

            val ok = cost.ensureRequirements(e.player, id("honey_barrage"), true)
            if (!ok)
                return@listen

            // can regenerate shield
            amount[e.player.paper!!.uniqueId] = 2
            particle(Particle.VILLAGER_HAPPY) {
                amount = 12
                offset = Vector.getRandom()
                spawnAt(e.player.paper!!.location)
            }
        }
    }
}
