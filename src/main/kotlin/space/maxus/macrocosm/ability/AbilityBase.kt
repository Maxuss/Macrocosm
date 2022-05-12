package space.maxus.macrocosm.ability

import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.util.Identifier

abstract class AbilityBase(
    override val type: AbilityType, override val name: String, override val description: String,
    override val cost: AbilityCost? = null
) : ItemAbility {
    val id: Identifier get() = AbilityRegistry.nameOf(this) ?: Identifier.NULL

    @Suppress("SameParameterValue")
    protected open fun ensureRequirements(player: MacrocosmPlayer, slot: EquipmentSlot): Boolean {
        val item = player.paper!!.inventory.getItem(slot)
        if (item.macrocosm == null || !item.macrocosm!!.abilities.contains(this))
            return false
        val success = cost?.ensureRequirements(player, id) ?: true
        if (!success) {
            sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                pitch = 0f
                playFor(player.paper!!)
            }
            return false
        }
        return true
    }
}
