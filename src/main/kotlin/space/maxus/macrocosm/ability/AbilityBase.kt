package space.maxus.macrocosm.ability

import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.text

/**
 * An abstract wrapper class for the [MacrocosmAbility] interface, allowing for easier abstraction
 *
 * @property type Type of this ability, only used for visual lore display
 * @property name Name of this ability. It is later parsed using MiniMessage, therefore may contain MM tags
 * @property description Description of this ability. Will later be partitioned for every 25 characters, not including MM tags
 * @property cost Cost to perform this ability
 */
abstract class AbilityBase(
    override val type: AbilityType, override val name: String, override val description: String,
    override val cost: AbilityCost? = null
) : MacrocosmAbility {
    /**
     * ID of this ability, got from the [Registry.ABILITY])
     */
    val id: Identifier get() = Registry.ABILITY.byValue(this) ?: Identifier.NULL

    /**
     * Ensures that the provided [player] has item with this ability in the [slot], as well as doing
     * the [AbilityCost.ensureRequirements] checks.
     *
     * @param player Player against which the requirements will be checked
     * @param slot Slot, in which the player must hold this item
     * @param silent Whether to loudly send player the message that the ability cost failed checks
     * @return True if the item and cost requirements are met, false otherwise
     */
    @Suppress("SameParameterValue")
    protected open fun ensureRequirements(
        player: MacrocosmPlayer,
        slot: EquipmentSlot,
        silent: Boolean = false
    ): Boolean {
        val item = player.paper!!.inventory.getItem(slot)
        val mc = item.macrocosm
        if (mc == null || !mc.abilities.any { it.pointer == this.id })
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

    /**
     * Ensures that player has enough summoning slots
     *
     * @param player player to be tested against
     * @param silent whether to do silent check
     * @return if all checks passed
     */
    protected fun ensureSlotRequirements(player: MacrocosmPlayer, silent: Boolean): Boolean {
        val stats = player.stats()!!
        val power = stats.summoningPower
        val usedSlots = player.summonSlotsUsed
        if (usedSlots >= power) {
            if (!silent) {
                player.paper!!.sendActionBar(text("<red><bold>NOT ENOUGH SUMMONING POWER"))
            }
            return false
        }
        return true
    }

}
