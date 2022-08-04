package space.maxus.macrocosm.ability.types.turret

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.particles.particle
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.data.MutableContainer
import space.maxus.macrocosm.util.generic.id
import space.maxus.macrocosm.util.math.MathHelper.extractYawPitch
import space.maxus.macrocosm.util.metrics.report
import space.maxus.macrocosm.util.runNTimes
import java.util.*

object HeavyTurretActive: AbilityBase(AbilityType.RIGHT_CLICK, "Heavy Turret", "Constructs a <red>Heavy Turret<gray> at your current position that will stand for <green>20 seconds<gray>. While active, the turret deals <red>[5000:0.2] ${Statistic.DAMAGE.display}<gray> every <green>2 seconds<gray> to nearest enemy within <green>7 blocks<gray>.<br><dark_gray>Only one Heavy Turret can be active<br><dark_gray>at once.") {
    override val cost: AbilityCost = AbilityCost(500, cooldown = 90, summonDifficulty = 3)

    val turrets = MutableContainer.empty<UUID>()

    override fun ensureRequirements(player: MacrocosmPlayer, slot: EquipmentSlot, silent: Boolean): Boolean {
        val slots = super.ensureRequirements(player, slot, silent)
        if (!slots)
            return false
        turrets.take(player.ref) {
            player.sendMessage("<red>You already have a Heavy Turret active!")
            return false
        }
        return ensureSlotRequirements(player, silent)
    }

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            e.player.summonSlotsUsed += 3
            val p = e.player.paper ?: report("Player was null in RightClickEvent") { return@listen }
            val stand = summonTurret(p.location, e.player)
            turrets[p.uniqueId] = stand.uniqueId

            val damage = DamageCalculator.calculateMagicDamage(5000, .2f, e.player.stats()!!)

            runNTimes(10, 40L, {
                e.player.summonSlotsUsed -= 3
                turrets.remove(p.uniqueId)
                particle(Particle.CLOUD) {
                    amount = 5
                    offset = Vector.getRandom()

                    spawnAt(stand.location)
                }
                sound(Sound.ENTITY_GENERIC_EXTINGUISH_FIRE) {
                    pitch = 0f
                    volume = 5f

                    playAt(stand.location)
                }
                stand.remove()
            }) {
                val nearest = stand.location.getNearbyLivingEntities(7.0) { en -> en !is Player && en !is ArmorStand }.firstOrNull() ?: return@runNTimes
                val location = stand.eyeLocation
                val look = nearest.eyeLocation.toVector().subtract(location.toVector()).normalize()
                val (yaw, _) = look.extractYawPitch()
                stand.setRotation(yaw, 0f)
                val arrow = stand.location.world.spawnArrow(location, look, 1f, 12f)
                arrow.persistentDataContainer.set(pluginKey("damage_deal"), PersistentDataType.FLOAT, damage)
                arrow.persistentDataContainer.set(pluginKey("owner"), PersistentDataType.STRING, e.player.ref.toString())

                sound(Sound.ENTITY_ARROW_SHOOT) {
                    pitch = 0f
                    volume = 5f

                    playAt(stand.location)
                }
            }
        }
    }

    private fun summonTurret(at: Location, player: MacrocosmPlayer): ArmorStand {
        val stand = at.world.spawnEntity(at, EntityType.ARMOR_STAND) as ArmorStand
        stand.isInvulnerable = true
        stand.isInvisible = true
        stand.disabledSlots.addAll(EquipmentSlot.values())
        stand.persistentDataContainer.set(pluginKey("ignore_damage"), PersistentDataType.BYTE, 0)
        stand.equipment.setItem(EquipmentSlot.HEAD, Registry.ITEM.find(id("turret_heavy")).build(player))
        stand.customName(text("<gold>${player.paper?.name}'s <red>Heavy Turret"))
        stand.isCustomNameVisible = true
        return stand
    }
}

object HeavyTurretPassive: AbilityBase(AbilityType.PASSIVE, "Support", "Players within <green>7 blocks<gray> of <br><red>Heavy Turret<gray> deal <red>+15% ${Statistic.DAMAGE.display}<gray>.") {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            val p = e.player.paper ?: return@listen
            HeavyTurretActive.turrets.iter {
                    standId ->
                val stand = p.world.getEntity(standId) ?: return@iter
                val distance = Mth.sqrt(p.location.distanceSquared(stand.location).toFloat())
                if(distance <= 7f) {
                    e.damage *= 1.15f
                }
            }
        }
    }
}
