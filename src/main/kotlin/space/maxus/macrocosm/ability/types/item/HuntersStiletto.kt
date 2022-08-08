package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.pack.Signs
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.metrics.report
import space.maxus.macrocosm.util.runNTimes
import java.util.*

object HuntersStilettoPassive : AbilityBase(
    AbilityType.PASSIVE,
    "Hunter's Sense",
    "Deal <red>+50% ${Statistic.DAMAGE.display}<gray> to <white>${Signs.ENTITY_MARK}<red> Marked Entities<gray>."
) {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            e.player.paper ?: report("Player was null in DealDamageEvent!") { return@listen }

            if (!HuntersStilettoActive.markedEntities.contains(e.damaged.uniqueId))
                return@listen

            e.damage *= 1.5f
        }
    }
}

object HuntersStilettoActive : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Hunter's Precision",
    "<red>Mark <white>${Signs.ENTITY_MARK}<gray> the entity you are looking at.",
    AbilityCost(250)
) {
    val markedEntities: MutableList<UUID> = mutableListOf()

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            val p = e.player.paper ?: report("Player was null in RightClickEvent!") { return@listen }

            val item = p.inventory.getItem(EquipmentSlot.HAND)
            if (item.macrocosm == null || !item.macrocosm!!.abilities.contains(this))
                return@listen

            val entity = p.getTargetEntity(15) as? LivingEntity ?: run {
                e.player.sendMessage("<red>No target entity!")
                return@listen
            }

            if (entity is ArmorStand) {
                e.player.sendMessage("<red>No target entity!")
                return@listen
            }

            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            markedEntities.add(entity.uniqueId)


            runNTimes(20, 10, {
                markedEntities.remove(entity.uniqueId)
            }) {
                if (entity.isDead) {
                    it.cancel()
                    return@runNTimes
                }

                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.RED, 2f)
                    amount = 8
                    offset = Vector.getRandom()

                    spawnAt(entity.location)
                }
            }
        }
    }
}
