package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerDeathEvent
import space.maxus.macrocosm.stats.Statistic

object DeathDefyAbility: AbilityBase(
    AbilityType.PASSIVE,
    "Death Defy",
    "Resurrects you with <red>Full ${Statistic.HEALTH.display}<gray> on death, and grants <green>10x ${Statistic.DEFENSE.display}<gray> and <red>+25% ${Statistic.ABILITY_DAMAGE.display}<gray> for <green>5 seconds<gray>."
) {
    override val cost: AbilityCost = AbilityCost(cooldown = 180)

    override fun registerListeners() {
        listen<PlayerDeathEvent> { e ->
            if(e.isCancelled)
                return@listen
            if(!ensureRequirements(e.player, EquipmentSlot.OFF_HAND, true))
                return@listen

            e.isCancelled = true
            e.player.sendMessage("<gradient:#FF8235:#FFB835><bold>DEATH DEFY!</bold></gradient><gold> Your Hyperion's Ring saved you from fatal blow!")
            e.player.tempStats.abilityDamage += 25f
            e.player.tempStats.defense += e.player.stats()!!.defense * 19f
            task(delay = 5 * 20L) {
                e.player.tempStats.abilityDamage -= 50f
            }

            val paper = e.player.paper!!
            e.player.currentHealth = e.player.stats()!!.health

            sound(Sound.ITEM_TOTEM_USE) {
                playAt(paper.location)
            }
            particle(Particle.TOTEM) {
                offset = Vector.getRandom()
                amount = 50
                spawnAt(paper.location)
            }
        }
    }
}
