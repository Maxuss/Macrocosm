package space.maxus.macrocosm.ability.types.turret

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.extensions.pluginKey
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
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.data.MutableContainer
import space.maxus.macrocosm.util.generic.id
import space.maxus.macrocosm.util.metrics.report
import space.maxus.macrocosm.util.runNTimes
import java.util.*

object TeslaCoilActive: AbilityBase(AbilityType.RIGHT_CLICK, "Tesla Coil", "Constructs a <aqua>Tesla Coil<gray> on your position that will stand for <green>15 seconds<gray>. While active, the turret deals <red>[1500:0.12] ${Statistic.DAMAGE.display}<gray> each second to enemies within <green>7 blocks<gray>.<br><dark_gray>Only one Tesla Coil can be active<br><dark_gray>at once.", AbilityCost(500, cooldown = 60, summonDifficulty = 2)) {
    val coils = MutableContainer.empty<UUID>()

    override fun ensureRequirements(player: MacrocosmPlayer, slot: EquipmentSlot, silent: Boolean): Boolean {
        val slots = super.ensureRequirements(player, slot, silent)
        if (!slots)
            return false
        coils.take(player.ref) {
            player.sendMessage("<red>You already have a Tesla Coil active!")
            return false
        }
        return ensureSlotRequirements(player, silent)
    }


    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            e.player.summonSlotsUsed -= 2
            val p = e.player.paper ?: report("Player was null in RightClickEvent") { return@listen }
            val stand = summonCoil(p.location, e.player)
            coils[p.uniqueId] = stand.uniqueId

            val damage = DamageCalculator.calculateMagicDamage(1500, .12f, e.player.stats()!!)

            runNTimes(15, 20L, {
                e.player.summonSlotsUsed += 2
                coils.remove(p.uniqueId)
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
                sound(Sound.ENTITY_GUARDIAN_ATTACK) {
                    pitch = 2f
                    volume = 5f

                    playAt(stand.location)
                }
                stand.location.getNearbyLivingEntities(7.0) { en -> en !is ArmorStand && en !is Player }.forEach {  entity ->
                    entity.macrocosm?.damage(damage, p)
                    DamageHandlers.summonDamageIndicator(entity.location, damage, DamageType.ELECTRIC)
                    particle(Particle.REDSTONE) {
                        data = DustOptions(Color.WHITE, 2f)
                        amount = 5
                        offset = Vector.getRandom()

                        spawnAt(entity.location)
                    }
                }

                renderCircle(stand)

            }
        }
    }

    private fun renderCircle(stand: ArmorStand) {
        async {
            val color = Color.fromRGB(0xD7FFFB)

            var i = 0f

            val start = stand.location.add(vec(z = -8, y = .8f))

            while(i < Mth.TWO_PI) {
                val x = Mth.cos(i) * 1f
                val z = Mth.sin(i) * 1f

                start.add(vec(x = x, z = z))

                particle(Particle.REDSTONE) {
                    data = DustOptions(color, 1.6f)
                    amount = 3

                    spawnAt(start)
                }

                i += Mth.PI / 25f
            }
        }
    }

    private fun summonCoil(at: Location, player: MacrocosmPlayer): ArmorStand {
        val stand = at.world.spawnEntity(at, EntityType.ARMOR_STAND) as ArmorStand
        stand.isInvulnerable = true
        stand.isInvisible = true
        stand.disabledSlots.addAll(EquipmentSlot.values())
        stand.persistentDataContainer.set(pluginKey("ignore_damage"), PersistentDataType.BYTE, 0)
        stand.equipment.setItem(EquipmentSlot.HEAD, Registry.ITEM.find(id("turret_tesla_coil")).build(player))
        return stand
    }
}

object TeslaCoilPassive: AbilityBase(AbilityType.PASSIVE, "Support", "Players within <green>7 blocks<gray> of <aqua>Tesla Coil<gray> gain:<br><aqua>+200 ${Statistic.INTELLIGENCE.display}<br><red>+10% ${Statistic.ABILITY_DAMAGE.display}") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            val p = e.player.paper ?: return@listen
            TeslaCoilActive.coils.iter { standId ->
                val stand = p.world.getEntity(standId) ?: return@iter
                val distance = Mth.sqrt(p.location.distanceSquared(stand.location).toFloat())
                if(distance <= 7f) {
                    e.stats.intelligence += 200
                    e.stats.abilityDamage += 10
                }
            }
        }
    }
}
