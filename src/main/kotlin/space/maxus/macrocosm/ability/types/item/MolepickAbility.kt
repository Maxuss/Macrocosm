package space.maxus.macrocosm.ability.types.item

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.sound.sound
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerBreakBlockEvent
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.NULL
import space.maxus.macrocosm.util.data.MutableContainer
import space.maxus.macrocosm.util.game.Fmt
import space.maxus.macrocosm.util.runNTimes
import space.maxus.macrocosm.util.superCritMod

object MolepickAbility : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Deep Drilling",
    "Dig straight into ground for <green>3 seconds<gray>, becoming fully <yellow>invulnerable<gray>. Upon digging out, heal for <red>50% ${Statistic.HEALTH.display}<gray>.<br>Your next attack after digging out will<br>be ${Fmt.SUPER_CRIT}.<br>You can not attack or jump while underground.<br><red>Ability can only be activated<br><red>after breaking 100 blocks."
) {
    private val blocksBroken = MutableContainer.empty<Int>()
    private val enabled = MutableContainer.trulyEmpty()
    private val strike = MutableContainer.trulyEmpty()
    override fun registerListeners() {
        listen<PlayerBreakBlockEvent>(priority = EventPriority.LOWEST) { e ->
            if (e.isCancelled || !ensureRequirements(e.player, EquipmentSlot.HAND, true))
                return@listen

            blocksBroken.setOrTakeMut(e.player.ref) {
                (it ?: 0) + 1
            }
        }
        listen<PlayerRightClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            blocksBroken.takeMut(e.player.ref) {
                if (it < 100) {
                    e.player.sendMessage("<red>Not enough blocks broken! $it/100")
                    return@takeMut it
                }
                enableAbility(e.player)

                0
            }.otherwise {
                e.player.sendMessage("<red>Break 100 blocks first!")
            }.call()
        }
        listen<PlayerReceiveDamageEvent> { e ->
            enabled.take(e.player.ref) {
                e.isCancelled = true
            }
        }
        listen<PlayerDealDamageEvent> { e ->
            enabled.take(e.player.ref) {
                e.isCancelled = true
            }.otherwise {
                strike.revoke(e.player.ref) {
                    e.damage *= superCritMod(e.player)
                    e.isSuperCrit = true
                }
            }.call()
        }
        listen<PlayerJumpEvent> { e ->
            enabled.take(e.player.uniqueId) {
                e.isCancelled = true
            }
        }
    }

    @Suppress("UnstableApiUsage")
    private fun enableAbility(player: MacrocosmPlayer) {
        val p = player.paper ?: return
        for (op in Bukkit.getOnlinePlayers()) {
            op.hideEntity(Macrocosm, p)
        }
        enabled[player.ref] = NULL
        sound(Sound.BLOCK_STONE_BREAK) {
            pitch = 0f
            volume = 5f
            playAt(p.location)
        }
        runNTimes(3 * 20L, 1L, {
            for (op in Bukkit.getOnlinePlayers())
                op.showEntity(Macrocosm, p)
            sound(Sound.BLOCK_STONE_BREAK) {
                pitch = 0f
                volume = 5f
                playAt(p.location)
            }
            enabled.remove(player.ref)
            strike[player.ref] = NULL
            player.heal(player.stats()!!.health * .5f)
        }) {
            particle(Particle.BLOCK_CRACK) {
                data = p.location.add(vec(y = -1)).block.blockData
                amount = 5
                offset = Vector.getRandom()

                spawnAt(p.location)
            }
            sound(Sound.BLOCK_ROOTED_DIRT_HIT) {
                volume = 5f

                playAt(p.location)
            }
        }
    }
}
