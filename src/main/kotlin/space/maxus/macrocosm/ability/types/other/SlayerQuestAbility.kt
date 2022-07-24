package space.maxus.macrocosm.ability.types.other

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.slayer.SlayerType

class SlayerQuestAbility(name: String, description: String, val slayer: SlayerType, val tier: Int) :
    AbilityBase(AbilityType.RIGHT_CLICK, name, description, AbilityCost(health = 1500)) {
    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen
            e.player.paper!!.inventory.setItemInMainHand(null)
            var ticker = 0
            val p = e.player.paper!!
            task(period = 1L) {
                if (ticker >= 20) {
                    e.player.sendMessage("<red>You have made a sacrifice with blood, and started a <gold>Profaned Slayer Quest<red>!")
                    e.player.startSlayerQuest(slayer, tier)
                    it.cancel()
                    return@task
                }
                ticker++
                sound(Sound.ENTITY_PHANTOM_HURT) {
                    pitch = 2f - (ticker / 10f)
                    playFor(p)
                }
                particle(Particle.SPELL_INSTANT) {
                    amount = 20
                    spawnAt(p.location.clone().add(vec(y = (ticker / 10f))))
                }
                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.RED, 1f)
                    amount = 20
                    offset = Vector.getRandom()
                    spawnAt(p.location.clone().add(vec(y = 2 - (ticker / 10f))))
                }
            }
        }
    }
}
