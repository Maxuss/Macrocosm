package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.jetbrains.annotations.NotNull
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.MathHelper
import space.maxus.macrocosm.util.createFloatingBlock


object TerrainTossAbility: AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Terrain Toss",
    "Throw a chunk of terrain in front of you, that explodes on landing, and deals <red>30,000 ${Statistic.DAMAGE.display}<gray>."
) {
    override val cost: AbilityCost = AbilityCost(800, cooldown = 2)

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val damage = DamageCalculator.calculateMagicDamage(30000, .1f, e.player.stats()!!)
            val p = e.player.paper!!

            val armorStands: ArrayList<ArmorStand> = ArrayList()
            val landingLoc: Location = p.getTargetBlock(null, 30).location
            landingLoc.block
            landingLoc.add(Vector(0.0, 1.0, 0.0))
            val loc1: Location = p.eyeLocation.add(Vector(0.0, 0.1, 0.0))
            val path: List<Location> = MathHelper.parabola(loc1, landingLoc, 18)
            val locationIterator = path.iterator()

            // bottom Layer
            addStand(loc1, p.location, 0, 0, 0, armorStands)
            addStand(loc1, p.location, 1, 0, 0, armorStands)
            addStand(loc1, p.location, -1, 0, 0, armorStands)
            addStand(loc1, p.location, 0, 0, 1, armorStands)
            addStand(loc1, p.location, 0, 0, -1, armorStands)

            // top layer
            addStand(loc1, p.location, 0, 1, 0, armorStands)
            addStand(loc1, p.location, 1, 1, 0, armorStands)
            addStand(loc1, p.location, -1, 1, 0, armorStands)
            addStand(loc1, p.location, 2, 1, 0, armorStands)
            addStand(loc1, p.location, -2, 1, 0, armorStands)
            addStand(loc1, p.location, 0, 1, 1, armorStands)
            addStand(loc1, p.location, 0, 1, -1, armorStands)
            addStand(loc1, p.location, 0, 1, 2, armorStands)
            addStand(loc1, p.location, 0, 1, -2, armorStands)
            addStand(loc1, p.location, 1, 1, 1, armorStands)
            addStand(loc1, p.location, -1, 1, -1, armorStands)
            addStand(loc1, p.location, -1, 1, 1, armorStands)
            addStand(loc1, p.location, 1, 1, -1, armorStands)

            sound(Sound.ENTITY_IRON_GOLEM_ATTACK) {
                playAt(p.location)
            }

            task(delay = 0L, period = 1L) {
                var next: Location? = null
                if (locationIterator.hasNext() && locationIterator.next().also { a -> next = a; }.block.type.isAir) {
                    moveStands(armorStands, next!!)
                } else {
                    particle(Particle.EXPLOSION_HUGE) {
                        val loc = armorStands[0].location
                        spawnAt(loc)
                    }
                    sound(Sound.ENTITY_IRON_GOLEM_DEATH) {
                        pitch = 0f
                        playAt(p.location)
                    }
                    val nearbyEntities = armorStands[0].location.getNearbyLivingEntities(5.0)
                    for (entity in nearbyEntities) {
                        if(entity is Player || entity is ArmorStand)
                            continue
                        entity.macrocosm!!.damage(damage, p)
                        DamageHandlers.summonDamageIndicator(entity.location, damage)
                    }
                    for (stand in armorStands) {
                        stand.passengers.forEach { passenger -> passenger.remove() }
                        stand.remove()
                    }
                    it.cancel()
                }
            }
        }
    }

    private fun addStand(
        @NotNull baseLoc: Location,
        @NotNull playerLoc: Location,
        xChange: Int,
        yChange: Int,
        zChange: Int,
        @NotNull arrayList: ArrayList<ArmorStand>
    ) {
        val blockLoc: Location = playerLoc.add(0.0, -2.0, 0.0).add(Vector(xChange, yChange, zChange))
        val block: ItemStack =
            if (blockLoc.block.type !== Material.AIR) ItemStack(blockLoc.block.state.blockData.material) else ItemStack(Material.BLUE_STAINED_GLASS)
        arrayList.add(
            createFloatingBlock(
                baseLoc.clone().add(Vector(xChange, yChange, zChange)),
                block
            )
        )
    }

    private fun moveStands(@NotNull stands: List<ArmorStand>, @NotNull moveTo: Location) {
        val mainStand: ArmorStand = stands[0]
        val xMove: Double = moveTo.x - mainStand.location.x
        val yMove: Double = moveTo.y - mainStand.location.y
        val zMove: Double = moveTo.z - mainStand.location.z
        for (stand in stands) {
            try {
                val passenger: Entity = stand.passengers[0]
                stand.eject()
                stand.teleport(stand.location.add(xMove, yMove, zMove))
                stand.passengers.clear()
                stand.addPassenger(passenger)
            } catch (ignored: NullPointerException) {
            }
        }
    }
}
