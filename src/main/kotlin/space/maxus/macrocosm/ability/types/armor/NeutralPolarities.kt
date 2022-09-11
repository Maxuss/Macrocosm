package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.LocationArea
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import net.axay.kspigot.structures.fillBlocks
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.damage.DamageKind
import space.maxus.macrocosm.events.*
import space.maxus.macrocosm.stats.Statistic
import kotlin.math.roundToInt

object WaterPolaritySetBonus : FullSetBonus(
    "Blessing of Water",
    "Your spells use <aqua>25%<gray> less mana and your ${Statistic.VIGOR.display}<gray> is increased by <green>250%<gray>, but you can't deal <red>Melee ${Statistic.DAMAGE.display}<gray>.${
        exemptsEssence(
            "<yellow>Neutral <green>Good"
        )
    }"
) {
    override fun registerListeners() {
        listen<AbilityCostApplyEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            e.mana = e.mana.toDouble() * .75
            particle(Particle.WATER_WAKE) {
                amount = 15
                offset = Vector.getRandom()
                spawnAt(e.player.paper!!.eyeLocation)
            }
        }
        listen<CostCompileEvent> { e ->
            if (!ensureSetRequirement(e.player!!))
                return@listen
            val cost = e.cost ?: return@listen
            e.cost = AbilityCost((cost.mana * .75).roundToInt(), cost.health, cost.cooldown, cost.summonDifficulty)
        }
        listen<PlayerCalculateStatsEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            e.stats.vigor *= 2.5f
        }
    }
}

object AirPolaritySetBonus1 : FullSetBonus(
    "Blessing of Air",
    "Your arrows deal <red>+150% ${Statistic.DAMAGE}<gray>, but your ${Statistic.DEFENSE.display}<gray> is reduced by <red>60%<gray>."
) {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if (e.kind != DamageKind.RANGED || !ensureSetRequirement(e.player))
                return@listen
            e.damage *= 2.5f
            e.damaged.location.let { it.world.strikeLightningEffect(it) }
        }
        listen<PlayerCalculateStatsEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            e.stats.defense *= .30f
        }
    }
}

object AirPolaritySetBonus2 : FullSetBonus(
    "In the wake of Zephyrus",
    "Launch yourself in air and create a temporary platform beneath yourself. The platform disappears in <green>10 seconds<gray>.${
        exemptsEssence(
            "<yellow>Devotedly Neutral"
        )
    }"
) {
    override val type: AbilityType = AbilityType.SNEAK
    override val cost: AbilityCost = AbilityCost(500, cooldown = 20)

    override fun registerListeners() {
        listen<PlayerSneakEvent> { e ->
            if (!ensureSetRequirement(e.player) || !ensureRequirements(e.player, EquipmentSlot.CHEST))
                return@listen

            val mc = e.player
            val p = mc.paper ?: return@listen
            val velocity = p.velocity
            p.velocity = velocity.add(vec(y = 2.6)).multiply(1.4f)
            taskRunLater(2 * 20L) {
                val area = LocationArea(p.location.add(vec(-1, -1, -1)), p.location.add(vec(1, -1, 1)))
                val previousBlockStates = hashMapOf<Location, Material>()
                area.fillBlocks.forEach { block ->
                    previousBlockStates[block.location] = block.type; block.type = Material.WHITE_CONCRETE
                }
                sound(Sound.BLOCK_LAVA_EXTINGUISH) {
                    pitch = 0f
                    volume = 5f
                    playAt(p.location)
                }
                taskRunLater(10 * 20L) {
                    previousBlockStates.forEach { (pos, type) -> pos.world.getBlockAt(pos).type = type }
                }
            }
        }
    }
}

object BloodPolaritySetBonus : FullSetBonus(
    "Blessing of Blood Moon",
    "You regenerate <green>part<gray> of <red>Melee ${Statistic.DAMAGE.display}<gray> dealt, but your ${Statistic.VITALITY.display}<gray> is set to <red>0<gray>.<br><dark_gray>Amount of life steal depends<br><dark_gray>on damage dealt.${
        exemptsEssence(
            "<yellow>Neutral <red>Evil"
        )
    }"
) {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent>(priority = EventPriority.LOW) { e ->
            if (e.kind != DamageKind.MELEE || e.isCancelled || !ensureSetRequirement(e.player))
                return@listen
            e.player.heal(determineHealingAmount(e.damage))
            sound(Sound.ENTITY_PHANTOM_BITE) {
                pitch = 2f
                volume = 1.3f
                playAt(e.player.paper!!.location)
            }
            particle(Particle.BLOCK_CRACK) {
                data = Material.REDSTONE_BLOCK.createBlockData()
                amount = 10
                offset = Vector.getRandom()
                spawnAt(e.damaged.eyeLocation)
            }
        }
        listen<PlayerCalculateStatsEvent>(priority = EventPriority.LOWEST) { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            e.stats.vitality = 0f
        }
    }

    private fun determineHealingAmount(damage: Float): Float {
        return if (damage >= 1_000_000f) {
            1500f
        } else if (damage >= 100_000f) {
            damage * 0.001f
        } else if (damage >= 10_000f) {
            damage * 0.02f
        } else {
            damage * 0.25f
        }
    }
}
