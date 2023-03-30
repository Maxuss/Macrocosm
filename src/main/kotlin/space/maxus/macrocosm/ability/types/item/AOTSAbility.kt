package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.util.EulerAngle
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageKind
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.registry.anyPoints
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.joml
import java.util.*
import kotlin.math.min

object AOTSAbility : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Throw",
    "Throw your axe forward dealing <red>10%<gray> of your normal ${Statistic.DAMAGE.display}<gray> and healing <red>50 ${Statistic.HEALTH.display}<gray> per enemy hit. The ${Statistic.DAMAGE.display}<gray> and <dark_aqua>Mana Cost<gray> increases each throw."
) {
    override val cost: AbilityCost = AbilityCost(10)

    private var playerCosts: HashMap<UUID, Int> = hashMapOf()
    private var costTask: HashMap<UUID, KSpigotRunnable> = hashMapOf()

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if (e.player.mainHand?.abilities?.anyPoints(this) != true)
                return@listen

            val p = e.player.paper!!
            val cost = playerCosts[p.uniqueId] ?: 10
            val newCost = min(cost * 2, 320)
            playerCosts[p.uniqueId] = newCost
            val task = costTask.remove(p.uniqueId)
            task?.cancel()
            costTask[p.uniqueId] = task(delay = 5 * 20L) {
                playerCosts.remove(p.uniqueId)
            }!!
            if (!AbilityCost(newCost).ensureRequirements(e.player, id("aots_throw")))
                return@listen

            val stats = e.player.stats()!!
            var (dmg, _) = DamageCalculator.calculateStandardDealt(stats.damage, stats)
            dmg *= (min((cost / 2), 160) / 100f)
            val display = p.world.spawnEntity(p.location, EntityType.ITEM_DISPLAY) as ItemDisplay
            display.itemStack = e.item?.build(e.player)
            display.isInvulnerable = true
            display.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.THIRDPERSON_LEFTHAND
            sound(Sound.ENTITY_EGG_THROW) {
                pitch = 0f
                playAt(p.location)
            }

            val dir = p.location.direction.multiply(.8f).normalize()
            var tick = 0
            val baseLocation: Location = display.location
            val mod = dir.clone()
            var currentPose = EulerAngle(.0, .0, .0)
            val norm = dir.clone().normalize()
            task(period = 1L) {
                tick++
                if (tick >= 120 || baseLocation.block.isSolid) {
                    it.cancel()
                    display.remove()
                    return@task
                }
                currentPose = currentPose.add(0.4, .0, .0)
                baseLocation.add(dir)
                val quat = Quaternionf()
                quat.rotationXYZ(currentPose.x.toFloat(), currentPose.y.toFloat(), currentPose.z.toFloat())
                val playerRot = Quaternionf()
                playerRot.rotationTo(Vector3f(0f, 0f, 1f), norm.joml().apply { y = 0f })
                display.interpolationDelay = 0
                display.interpolationDuration = 5
                display.transformation = Transformation(mod.add(dir).joml(), playerRot, display.transformation.scale, quat)

                for (entity in baseLocation.getNearbyLivingEntities(1.0)) {
                    if (entity is Player || entity is ArmorStand)
                        continue

                    val mc = entity.macrocosm!!
                    val event = PlayerDealDamageEvent(
                        e.player,
                        entity,
                        DamageCalculator.calculateStandardReceived(dmg, mc.calculateStats()),
                        false,
                        DamageKind.MELEE,
                        false
                    )
                    event.callEvent()
                    if (!event.isCancelled) {
                        mc.damage(event.damage, p, event.kind)
                        DamageHandlers.summonDamageIndicator(entity.location, event.damage)
                    }
                }
            }
        }
    }
}
