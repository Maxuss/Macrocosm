package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.nextSignedDouble
import kotlin.random.Random

object VoodooDollAbility : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Acupuncture",
    "Shoots arrows from every direction around the targeted enemy.<br>Hit entity is slowed down and takes <red>[3000:0.2] ${Statistic.DAMAGE.display}<gray>/s for <green>5s<gray>."
) {
    override val cost: AbilityCost = AbilityCost(200, cooldown = 6)

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if (e.player.mainHand?.abilities?.contains(this) != true)
                return@listen

            val p = e.player.paper ?: return@listen
            val entity = p.getTargetEntity(10) as? LivingEntity
            if (entity == null || entity is ArmorStand || entity is Player || entity.isDead) {
                sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                    pitch = 0f
                    playFor(p)
                }
                p.sendMessage(text("<red>No target found!"))
                return@listen
            }
            val mc = entity.macrocosm ?: return@listen

            if (!cost.ensureRequirements(e.player, this.id))
                return@listen

            sound(Sound.ENTITY_GHAST_WARN) {
                pitch = 2f
                playAt(p.location)
            }

            val loc = entity.eyeLocation
            // spawning arrows
            for (i in 0..10) {
                val pos = Location(
                    loc.world,
                    loc.x + Random.nextSignedDouble() * 3,
                    loc.y + Random.nextSignedDouble() * 3,
                    loc.z + Random.nextSignedDouble() * 3
                )
                val arrow = p.world.spawnArrow(
                    pos,
                    loc.toVector().subtract(pos.toVector()).multiply(2f).normalize(),
                    .6f,
                    12f
                )
                arrow.persistentDataContainer.set(pluginKey("despawn_me"), PersistentDataType.BYTE, 0)
            }

            val dmg = DamageCalculator.calculateMagicDamage(3000, .2f, e.player.stats()!!)
            mc.baseStats.speed /= 2

            var ticker = 0
            task(period = 20L) {
                if (ticker >= 5 || entity.isDead) {
                    it.cancel()
                    if (!entity.isDead) {
                        mc.baseStats.speed *= 2
                        mc.loadChanges(entity)
                    }
                    return@task
                }
                ticker++

                mc.damage(dmg, p)
                DamageHandlers.summonDamageIndicator(entity.location, dmg)
            }
        }
    }
}
