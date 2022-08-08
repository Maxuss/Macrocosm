package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.async
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.data.MutableContainer
import space.maxus.macrocosm.util.generic.Ticker
import space.maxus.macrocosm.util.generic.id
import space.maxus.macrocosm.util.metrics.report
import java.util.*

object FlamethrowerAbility : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Burn",
    "Toggle flamethrower, shooting fire forward of yourself. The flames deal <red>[1000:0.3] ${Statistic.DAMAGE.display}<gray>/s."
) {
    override fun buildLore(lore: MutableList<Component>, player: MacrocosmPlayer?) {
        super.buildLore(lore, player)
        lore.add(text("<dark_gray>Consumption: <yellow>1 Light Gasoline Can<dark_gray>/s").noitalic())
    }

    private val enabled: MutableContainer<KSpigotRunnable> = MutableContainer.empty()

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val p = e.player.paper ?: report("Player in RightClickEvent was null!") { return@listen }
            enabled.revoke(e.player.ref) { run ->
                // enabled, need to disable
                run.cancel()
                sound(Sound.BLOCK_LEVER_CLICK) {
                    pitch = 0f
                    volume = 5f

                    playAt(p.location)
                }

                return@listen
            }.otherwise {
                val gasoline = Registry.ITEM.findOrNull(id("light_gasoline"))?.build(e.player)
                    ?: report("Could not find light gasoline in registry!") { return@otherwise }
                if (p.inventory.containsAtLeast(gasoline, 1)) {
                    sound(Sound.BLOCK_LEVER_CLICK) {
                        pitch = 0f
                        volume = 5f

                        playAt(p.location)
                    }
                    val ticker = Ticker(0..3)
                    val dmg = DamageCalculator.calculateMagicDamage(1000, .3f, e.player.stats()!!)
                    enabled[e.player.ref] = task(period = 5, delay = 0) {
                        val tick = ticker.tick()

                        if (it.isCancelled)
                            return@task

                        val paper = e.player.paper
                        if (paper == null || !paper.inventory.containsAtLeast(gasoline, 1)) {
                            it.cancel()
                            if (paper == null)
                                return@task
                            sound(Sound.BLOCK_LEVER_CLICK) {
                                pitch = 0f
                                volume = 5f

                                playAt(p.location)
                            }
                            p.sendActionBar(text("<red><bold>NOT ENOUGH GASOLINE"))
                            enabled.remove(e.player.ref)
                            return@task
                        }

                        if (tick == 3)
                            p.inventory.removeItemAnySlot(gasoline)

                        renderFlamethrower(e.player, p.eyeLocation, p.eyeLocation.direction, dmg, tick)
                        sound(Sound.ENTITY_BLAZE_SHOOT) {
                            pitch = 0f
                            volume = 3f

                            playAt(p.location)
                        }
                    }!!

                } else {
                    e.player.sendMessage("<red>Not enough gasoline!")
                }
            }.call()
        }
    }

    fun renderFlamethrower(
        player: MacrocosmPlayer,
        location: Location,
        direction: org.bukkit.util.Vector,
        damage: Float,
        tick: Int
    ) {
        async {
            val p = player.paper!!
            val loc = location.add(vec(y = -.3))

            val adv = direction.clone().multiply(.3f)

            val angDec = .3

            var i = 1
            val hit = mutableListOf<UUID>()
            while (i < 20) {
                loc.add(adv)

                adv.rotateAroundZ(Math.toRadians(angDec * 3))

                particle(Particle.FLAME) {
                    extra = .03f
                    amount = 5
                    offset = adv

                    spawnAt(loc)
                }

                if (tick != 3 && tick != 0)
                    return@async

                task(delay = 0) {
                    // drifting to synchronous env
                    for (entity in loc.getNearbyLivingEntities(2.0) {
                        it !is Player && it !is ArmorStand && !hit.contains(
                            it.uniqueId
                        )
                    }) {
                        hit.add(entity.uniqueId)

                        entity.macrocosm?.damage(damage, p)
                        DamageHandlers.summonDamageIndicator(loc, damage, DamageType.FIRE)
                    }
                }

                i++
            }
        }
    }
}
