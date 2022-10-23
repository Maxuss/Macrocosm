package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.sound.sound
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Creature
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerRightClickEvent

object BerserkerAbility : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Berserker",
    "Anger all nearest enemies, making them attack each other for the next <yellow>10 seconds<gray>.",
    AbilityCost(350, cooldown = 20)
) {
    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val p = e.player.paper ?: return@listen
            sound(Sound.ITEM_GOAT_HORN_SOUND_5) {
                volume = 10f
                playAt(p.location)
            }
            p.getNearbyEntities(10.0, 4.0, 10.0).filterIsInstance<LivingEntity>().forEach {
                if (it is ArmorStand || it is Player || it !is Creature)
                    return@forEach
                it.target = it.getNearbyEntities(12.0, 3.0, 12.0).randomOrNull() as? LivingEntity
                particle(Particle.SWEEP_ATTACK) {
                    amount = 6
                    offset = Vector.getRandom()
                    spawnAt(it.location)
                }
            }
        }
    }
}
