package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftLivingEntity
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.BlockIterator
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerLeftClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.stats.Statistic


object HoneycombBulwarkAbility : AbilityBase(
    AbilityType.LEFT_CLICK,
    "Honeycomb Bulwark",
    "Shoot a <gold>Honeycomb Wave<gray> in front of you, pushing all enemies away, and dealing them <red>10.000 ${Statistic.DAMAGE.display}<gray>.",
    AbilityCost(health = 500, cooldown = 3)
) {
    override fun registerListeners() {
        listen<PlayerLeftClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            spawnHoneycombWave(e.player.paper!!, e.player.paper!!.location)
        }
    }

    private fun spawnHoneycombWave(player: Player, loc: Location) {
        val specials = player.macrocosm!!.specialStats()

        val projection = BlockIterator(loc, 1.0, 10)
        val dmg = DamageCalculator.calculateMagicDamage(10000, .3f, player.macrocosm!!.stats()!!)
        val data = Material.HONEY_BLOCK.createBlockData()
        val data2 = Material.HONEYCOMB_BLOCK.createBlockData()
        task(period = 2L) {
            if (projection.hasNext()) {
                val next = projection.next()
                particle(Particle.BLOCK_CRACK) {
                    this.data = data
                    amount = 10
                    offset = Vector.getRandom()
                    spawnAt(next.location)
                }
                particle(Particle.BLOCK_CRACK) {
                    this.data = data2
                    amount = 10
                    offset = Vector.getRandom()
                    spawnAt(next.location)
                }

                sound(Sound.BLOCK_HONEY_BLOCK_BREAK) {
                    pitch = 0f
                    playAt(next.location)
                }
                for (entity in next.location.getNearbyLivingEntities(3.0)) {
                    if (entity !is LivingEntity || entity is ArmorStand || entity is Player)
                        continue
                    val knockbackAmount = .5226 * (1 + specials!!.knockbackBoost + .36)
                    val nmsDamaged = (entity as CraftLivingEntity).handle
                    val nmsDamager = (player as CraftLivingEntity).handle
                    nmsDamaged.knockback(
                        knockbackAmount,
                        Mth.sin(nmsDamager.yRot * 0.017453292F).toDouble(),
                        -Mth.cos(nmsDamager.yRot * 0.017453292F).toDouble(),
                        nmsDamager
                    )
                    entity.macrocosm!!.damage(dmg)
                    DamageHandlers.summonDamageIndicator(entity.location, dmg)
                }
            } else {
                it.cancel()
                return@task
            }
        }
    }
}
