package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerJoinEvent
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.events.PlayerDeathEvent
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.util.data.MutableContainer

class PiggyBankAbility(applicable: String, private val times: Int) : AccessoryAbility(
    applicable,
    "Saves you from losing coins on death <green>$times<gray> times every <red>15 minutes<gray>. Triggers only when losing 20k+ coins."
) {
    private val playerData = MutableContainer.empty<Int>()

    override fun registerListeners() {
        task(period = 15 * 60 * 20L) {
            for (player in Macrocosm.loadedPlayers.values) {
                if (hasAccs(player))
                    playerData[player.ref] = times
            }
        }
        listen<PlayerJoinEvent>(priority = EventPriority.LOWEST) { e ->
            val mc = e.player.macrocosm ?: return@listen
            if (hasAccs(mc))
                playerData[mc.ref] = times
        }
        listen<PlayerDeathEvent> { e ->
            if(hasAccs(e.player) && !playerData.contains(e.player.ref))
                playerData[e.player.ref] = times
            playerData.takeMut(e.player.ref) {
                if (it <= 0)
                    return@listen
                e.reduceCoins = 0f.toBigDecimal()
                sound(Sound.BLOCK_GLASS_BREAK) {
                    pitch = 0f
                    volume = 5f
                    playAt(e.player.paper!!.location)
                }
                if(it - 1 <= 0) {
                    e.player.sendMessage("<bold><red>Your piggy ${if(times == 3) "Piggy Vault" else "Piggy Bank"} broke!")
                }
                it - 1
            }
        }
    }
}
