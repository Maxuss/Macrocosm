package space.maxus.macrocosm.ability.types.summoning

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.nms.NativeMacrocosmSummon
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.registry.anyPoints
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.metrics.report
import java.util.*
import kotlin.math.max

open class SummoningAbility(
    name: String,
    description: String,
    cost: AbilityCost,
    private val entity: String,
    private val nativeCtor: (Level, UUID) -> LivingEntity
) : AbilityBase(AbilityType.RIGHT_CLICK, name, description, cost) {
    private val entityId = id(this.entity)
    override val cost: AbilityCost = super.cost!!

    private val summoned: Multimap<UUID, UUID> = HashMultimap.create()

    override fun buildLore(lore: MutableList<Component>, player: MacrocosmPlayer?) {
        super.buildLore(lore, player)
        lore.add("".toComponent())
        lore.add(text("<gold>Despawn Summons: <bold><yellow>SNEAK RIGHT-CLICK").noitalic())
    }

    override fun ensureRequirements(player: MacrocosmPlayer, slot: EquipmentSlot, silent: Boolean): Boolean {
        val base = super.ensureRequirements(player, slot, silent)
        if (!base)
            return false
        return ensureSlotRequirements(player, silent)
    }

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            val item = e.player.paper?.inventory?.getItem(EquipmentSlot.HAND)
                ?: report("Player in RightClickEvent was null!") { return@listen }
            if (item.macrocosm == null || !item.macrocosm!!.abilities.anyPoints(this))
                return@listen

            val p = e.player.paper ?: report("Player in RightClickEvent was null!") { return@listen }

            if (p.isSneaking) {
                // despawning
                val entities = summoned[e.player.ref]
                entities.forEach { eid ->
                    e.player.summons.remove(eid)
                    p.world.getEntity(eid)?.remove()
                    e.player.summonSlotsUsed -= cost.summonDifficulty
                }
                sound(Sound.ENTITY_ZOMBIE_VILLAGER_CURE) {
                    pitch = 2f
                    volume = 3f

                    playAt(p.location)
                }
                e.player.summonSlotsUsed = max(0, e.player.summonSlotsUsed)
                return@listen
            }

            if (!ensureRequirements(e.player, EquipmentSlot.HAND)) {
                return@listen
            }

            // spawning
            sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                pitch = 2f
                volume = 3f

                playAt(p.location)
            }
            val native = nativeCtor((p.world as CraftWorld).handle, p.uniqueId)
            val mc = Registry.ENTITY.findOrNull(entityId)
                ?: report("Could not find provided entity to summon: $entityId!") { return@listen }
            NativeMacrocosmSummon.summon(native, p.location, mc)
            summoned.put(e.player.ref, native.uuid)
            e.player.summons.add(native.uuid)
            e.player.summonSlotsUsed += cost.summonDifficulty
        }
    }
}
