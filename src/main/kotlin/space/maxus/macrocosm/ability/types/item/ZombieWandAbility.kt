package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.*
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.stats.Statistic
import java.util.*

class ZombieWandAbility(name: String, val amount: Float, val length: Int, manaCost: Int) : AbilityBase(
    AbilityType.RIGHT_CLICK,
    name,
    "Heal <red>$amount ${Statistic.HEALTH.specialChar}<gray>/s for <green>${length}s<gray>.<br><dark_gray>Wand heals don't stack.",
    AbilityCost(manaCost, cooldown = length + length / 2)
) {
    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.OFF_HAND))
                return@listen
            val id = e.player.ref
            healTasks.remove(id)?.cancel()
            val stats = e.player.stats()!!
            var tick = 0
            val trueAmount =
                if ((Ability.ROTTEN_HEART_T1.ability as FullSetBonus).ensureSetRequirement(e.player)) amount * 1.75f else if ((Ability.ROTTEN_HEART_T2.ability as FullSetBonus).ensureSetRequirement(
                        e.player
                    )
                ) amount * 2.5f else amount
            healTasks[id] = task(period = 20L) {
                tick++
                if (tick >= length) {
                    it.cancel()
                    healTasks.remove(id)
                    return@task
                }
                e.player.heal(trueAmount, stats)
            }!!
        }
    }

    companion object {
        private val healTasks: HashMap<UUID, KSpigotRunnable> = hashMapOf()
    }
}
