package space.maxus.macrocosm.ability.types.equipment

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.EquipmentAbility
import space.maxus.macrocosm.events.AbilityCostApplyEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text

object IVBagAbility : EquipmentAbility(
    "Droplets of Mana",
    "When using abilities, take <red>7.5%<gray> of your maximum ${Statistic.HEALTH.display}<gray> as damage if your <aqua>${Statistic.INTELLIGENCE.specialChar} Mana<gray> gets too low to regain your maximum ${Statistic.INTELLIGENCE.display}<gray>."
) {
    override fun registerListeners() {
        listen<AbilityCostApplyEvent> { e ->
            if (!ensureRequirements(
                    e.player,
                    ItemType.BELT
                ) || e.player.currentMana > e.player.stats()!!.intelligence * 0.2f
            )
                return@listen

            sound(Sound.ENTITY_GENERIC_BIG_FALL) {
                pitch = 0f
                volume = 5f
                playAt(e.player.paper!!.location)
            }

            particle(Particle.BLOCK_CRACK) {
                data = Material.REDSTONE_BLOCK.createBlockData()
                amount = 10
                offset = Vector.getRandom()
            }

            e.player.damage(e.player.stats()!!.health * 0.075f, text("<red>IV Bag</red>"))
            e.player.currentMana = e.player.stats()!!.intelligence
        }
    }
}
