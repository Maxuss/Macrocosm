package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.damage.relativeLocation
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.data.MutableContainer

object ShieldOfDeadAbility: AbilityBase(AbilityType.RIGHT_CLICK, "Soul Strike", "Accumulate all damage you deal within the next <green>5 seconds<gray>.<br>All the damage accumulated will then be converted to a powerful ray and <green>half<gray> of it is dealt to nearby enemies as ${Statistic.TRUE_DAMAGE.display}<gray>.") {
    override val cost: AbilityCost = AbilityCost(500, 100, 25)

    private val enabled = MutableContainer.empty<Float>()
    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            enabled.takeMut(e.player.ref) {
                it + e.damage
            }
        }
        listen<PlayerRightClickEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.OFF_HAND))
                return@listen

            enabled[e.player.ref] = 0f

            taskRunLater(5 * 20L) {
                val amount = enabled.remove(e.player.ref)!!
                if(amount <= 0f)
                    return@taskRunLater
                e.player.sendMessage("<gray>Your <aqua>Soul Strike<gray> accumulated <red>${Formatting.withCommas(amount.toBigDecimal())} ${Statistic.DAMAGE.display}<gray>!")
                val p = e.player.paper ?: return@taskRunLater
                sound(Sound.ENTITY_ZOMBIE_HORSE_DEATH) {
                    pitch = 2f
                    volume = 4f

                    playAt(p.location)
                }

                IceConeAbility.spawnHelix(p.eyeLocation.direction, p, Color.TEAL)

                p.eyeLocation.direction.multiply(2f).relativeLocation(p.eyeLocation).getNearbyLivingEntities(4.0) { it !is Player && it !is ArmorStand }.forEach { entity ->
                    entity.macrocosm?.damage(amount / 2f)
                    DamageHandlers.summonDamageIndicator(entity.location, amount / 2f)
                }
            }
        }
    }
}
