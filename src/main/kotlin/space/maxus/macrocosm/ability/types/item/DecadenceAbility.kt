package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.async
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.util.runNTimes
import java.util.*
import kotlin.random.Random

object DecadenceAbility : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Decadence",
    "Constructs a <gradient:#AB6C08:#B77A1A:#291A01>Hatred Shield<gray>, that consumes <red>next 5 hits<gray> you take and explodes, dealing <red>500%<gray> of damage taken to nearby enemies."
) {
    override val cost: AbilityCost = AbilityCost(300, 200, 8)

    private val activePlayers: HashMap<UUID, Int> = hashMapOf()
    private val takenDamage: HashMap<UUID, Float> = hashMapOf()

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val player = e.player
            val p = player.paper!!

            activePlayers[p.uniqueId] = 5
            takenDamage[p.uniqueId] = 0f

            sound(Sound.ENTITY_GHAST_SCREAM) {
                pitch = 0f
                volume = 5f
                playAt(p.location)
            }

            sound(Sound.ENTITY_BLAZE_AMBIENT) {
                pitch = 2f
                volume = 5f
                playAt(p.location)
            }

            runNTimes(70, 2, {
                explodeBall(player, p.eyeLocation, takenDamage[p.uniqueId]!! * 5f)
                activePlayers.remove(p.uniqueId)
                takenDamage.remove(p.uniqueId)
            }) {
                if (activePlayers[p.uniqueId]!! <= 0) {
                    it.cancel()
                    return@runNTimes
                }
                renderBall(p.eyeLocation.add(vec(y = -.5)))
            }
        }

        listen<PlayerReceiveDamageEvent>(priority = EventPriority.LOWEST) { e ->
            val p = e.player.paper ?: return@listen
            if (takenDamage.contains(p.uniqueId)) {
                takenDamage[p.uniqueId] = takenDamage[p.uniqueId]!! + e.damage
                activePlayers[p.uniqueId] = activePlayers[p.uniqueId]!! - 1
                e.isCancelled = true
            }
        }
    }

    fun explodeBall(player: MacrocosmPlayer, pos: Location, damage: Float) {
        async {
            for (i in 0..20) {
                val color = when (Random.nextInt(0, 4)) {
                    0 -> 0xD67E0A
                    1 -> 0x2D2315
                    else -> 0xD67E0A
                }

                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.fromRGB(color), 2f)
                    offset = Vector.getRandom()
                    amount = 5
                    extra = 2f

                    spawnAt(pos)
                }
            }
        }

        val paper = player.paper ?: return
        var total = 0f
        var hit = 0
        for (entity in pos.getNearbyLivingEntities(5.0)) {
            if (entity is ArmorStand || entity is Player)
                continue

            total += damage
            hit++
            entity.macrocosm?.damage(damage, paper)
            DamageHandlers.summonDamageIndicator(entity.location, damage)
        }

        sound(Sound.ENTITY_WARDEN_SONIC_BOOM) {
            pitch = 2f
            volume = 4f

            playAt(pos)
        }

        if (hit != 0 && total != 0f)
            player.sendMessage("<gray>Your <red>Decadence<gray> hit $hit enemies for <red>${Formatting.withCommas(total.toBigDecimal())}<gray> damage!")
    }

    fun renderBall(pos: Location) {
        var i = 0f
        val at = pos.clone()
        while (i < Mth.PI) {
            val radius = Mth.sin(i) * 1.5
            val y = Mth.cos(i) * 1.5

            var a = 0f
            while (a < Mth.PI * 2) {
                val x = Mth.cos(a) * radius
                val z = Mth.sin(a) * radius

                val vecConst = vec(x, y, z)

                at.add(vecConst)

                val color = when (Random.nextInt(0, 4)) {
                    0 -> 0xD67E0A
                    1 -> 0x2D2315
                    else -> 0x1B1002
                }

                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.fromRGB(color), 1.2f)
                    amount = 2

                    spawnAt(at)
                }

                at.subtract(vecConst)

                a += Mth.PI / 10f
            }

            i += Mth.PI / 10f
        }
    }
}
