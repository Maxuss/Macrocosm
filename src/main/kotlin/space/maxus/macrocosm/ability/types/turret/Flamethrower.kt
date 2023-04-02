package space.maxus.macrocosm.ability.types.turret

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.joml.Quaternionf
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.ability.types.item.FlamethrowerAbility
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.data.MutableContainer
import space.maxus.macrocosm.util.general.Ticker
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.math.MathHelper.extractYawPitch
import space.maxus.macrocosm.util.metrics.report
import java.util.*

object FlamethrowerTurretActive : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Flamethrower Turret",
    "Constructs a <gold>Flamethrower Turret<gray> that showers nearby enemies with fire in two directions. Deals <red>[2000:0.15] ${Statistic.DAMAGE.display}<gray> each second.<br>Consumes <yellow>1 Light Gasoline Can<gray> each second, and can stay as long as you have them in your inventory.<br><dark_gray>Only one Flamethrower Turret can<br><dark_gray>be active at once.<br><dark_gray>Right Click again to remove turret."
) {
    override val cost: AbilityCost = AbilityCost(200, cooldown = 45, summonDifficulty = 3)
    val turrets = MutableContainer.empty<UUID>()
    val enabled = MutableContainer.empty<KSpigotRunnable>()

    override fun ensureRequirements(player: MacrocosmPlayer, slot: EquipmentSlot, silent: Boolean): Boolean {
        val slots = super.ensureRequirements(player, slot, silent)
        if (!slots)
            return false
        turrets.take(player.ref) {
            player.sendMessage("<red>You already have a Flamethrower Turret active!")
            return false
        }
        return ensureSlotRequirements(player, silent)
    }

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            val p = e.player.paper ?: report("Player was null in RightClickEvent") { return@listen }
            enabled.revoke(e.player.ref) {
                turrets.revoke(e.player.ref) { turretId ->
                    p.world.getEntity(turretId)?.remove()
                }
                e.player.summonSlotsUsed -= 3
                it.cancel()
            }.otherwise {
                if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                    return@otherwise

                val gasoline = Registry.ITEM.findOrNull(id("light_gasoline"))?.build(e.player)
                    ?: report("Could not find light gasoline in registry!") { return@otherwise }
                val damage = DamageCalculator.calculateMagicDamage(2000, .15f, e.player.stats()!!)
                if (p.inventory.containsAtLeast(gasoline, 1)) {
                    e.player.summonSlotsUsed += 3
                    val display = summonTurret(p.location, e.player)
                    turrets[p.uniqueId] = display.uniqueId

                    val ticker = Ticker(0..3)
                    enabled[p.uniqueId] = task(period = 5L) {
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
                            p.world.getEntity(turrets.remove(e.player.ref)!!)?.remove()
                            e.player.summonSlotsUsed -= 3
                            return@task
                        }

                        if (tick == 3)
                            p.inventory.removeItemAnySlot(gasoline)

                        val location = display.location
                        val nearest =
                            display.location.getNearbyLivingEntities(7.0) { en -> en !is ArmorStand && en !is Player }
                                .firstOrNull() ?: return@task
                        val look = nearest.eyeLocation.toVector().subtract(location.toVector()).normalize()
                        val (yaw, _) = look.extractYawPitch()
                        val yawQuat = Quaternionf()
                        yawQuat.rotateYXZ(-yaw, 0f, 0f)
                        display.interpolationDelay = 0
                        display.interpolationDuration = 5
                        // display.transformation = display.transformation.mutate(rightRot = yawQuat)
                        display.setRotation(yaw, 0f)

                        FlamethrowerAbility.renderFlamethrower(
                            e.player,
                            display.location.add(vec(y = 1f)),
                            display.location.direction,
                            damage,
                            tick
                        )
                        FlamethrowerAbility.renderFlamethrower(
                            e.player,
                            display.location.add(vec(y = 1f)),
                            display.location.direction.rotateAroundY(Math.toRadians(180.0)),
                            damage,
                            tick
                        )
                        sound(Sound.ENTITY_BLAZE_SHOOT) {
                            pitch = 0f
                            volume = 3f

                            playAt(p.location)
                        }
                    }!!
                }

            }.call()
        }
    }

    private fun summonTurret(at: Location, player: MacrocosmPlayer): ItemDisplay {
        val display = at.world.spawnEntity(at.add(vec(y = 1)), EntityType.ITEM_DISPLAY) as ItemDisplay
        display.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.HEAD
        display.itemStack = Registry.ITEM.find(id("turret_flamethrower")).build(player)
        display.customName(text("<gold>${player.paper?.name}'s <red>Flamethrower Turret"))
        display.isCustomNameVisible = true
        return display
    }
}

object FlamethrowerTurretPassive : AbilityBase(
    AbilityType.PASSIVE,
    "Support",
    "Players within <green>7 blocks<gray> of <br><gold>Flamethrower Turret<gray> gain:<br><red>+100 ${Statistic.STRENGTH.display}<br><green>+5% ${Statistic.DEFENSE.display}"
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            val p = e.player.paper ?: return@listen
            FlamethrowerTurretActive.turrets.iter { standId ->
                val stand = p.world.getEntity(standId) ?: return@iter
                val distance = Mth.sqrt(p.location.distanceSquared(stand.location).toFloat())
                if (distance <= 7f) {
                    e.stats.strength += 100
                    e.stats.defense *= 1.05f
                }
            }

        }
    }
}
