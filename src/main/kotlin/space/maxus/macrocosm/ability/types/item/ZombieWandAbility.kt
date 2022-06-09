package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.stats.Statistic
import java.util.UUID

class ZombieWandAbility(name: String, val amount: Float, val length: Int, manaCost: Int): AbilityBase(AbilityType.RIGHT_CLICK, name, "Heal <red>$amount ${Statistic.HEALTH.specialChar}<gray>/s for <green>${length}s<gray>.<br><dark_gray>Wand heals don't stack.", AbilityCost(manaCost, cooldown = length + length / 2)) {
    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.OFF_HAND))
                return@listen
            val id = e.player.ref
            healTasks.remove(id)?.cancel()
            val stats = e.player.stats()!!
            var tick = 0
            healTasks[id] = task(period = 20L) {
                tick++
                if(tick >= length) {
                    it.cancel()
                    healTasks.remove(id)
                    return@task
                }
                e.player.heal(amount, stats)
            }!!
        }
    }

    companion object {
        private val healTasks: HashMap<UUID, KSpigotRunnable> = hashMapOf()
    }
}
