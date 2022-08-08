package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.events.PlayerSneakEvent
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.data.MutableContainer
import kotlin.math.min

object BrittleBonesAbility : AbilityBase(
    AbilityType.SNEAK,
    "Brittle Bones",
    "Cap your ${Statistic.DEFENSE.display}<gray> at <green>1/2<gray> of your ${Statistic.HEALTH.display}<gray> but reflect <red>200%<gray> of ${Statistic.DAMAGE.display}<gray> you take for next <green>6 seconds<gray>."
) {
    override val cost: AbilityCost = AbilityCost(100, cooldown = 10)

    private val enabled = MutableContainer.empty<Boolean>()

    override fun registerListeners() {
        listen<PlayerSneakEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.CHEST))
                return@listen

            enabled[e.player.ref] = true

            taskRunLater(6 * 20L) {
                enabled.remove(e.player.ref)
            }

            sound(Sound.ENTITY_ITEM_BREAK) {
                pitch = 0f
                volume = 4f

                playAt(e.player.paper!!.location)
            }

            particle(Particle.BLOCK_CRACK) {
                data = Material.BONE_BLOCK.createBlockData()

                amount = 12
                offset = Vector.getRandom()

                spawnAt(e.player.paper!!.location)
            }
        }
        listen<PlayerCalculateStatsEvent> { e ->
            enabled.take(e.player.ref) {
                e.stats.defense = min(e.stats.health * .5f, e.stats.defense)
            }
        }
        listen<PlayerReceiveDamageEvent> { e ->
            enabled.take(e.player.ref) {
                e.damager.macrocosm?.damage(e.damage * 2f)
            }
        }
    }
}
