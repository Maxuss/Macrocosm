package space.maxus.macrocosm.area

import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.entity.Player
import space.maxus.macrocosm.events.PlayerEnterAreaEvent
import space.maxus.macrocosm.registry.Identified
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.text

/**
 * An area model holding actual details about locations
 */
abstract class AreaModel(
    /**
     * ID of this area
     */
    id: String,
    /**
     * Formatted name of this area
     */
    val name: String,
    /**
     * Description lines of this area
     */
    val description: List<String>
) : Identified {
    override val id = Identifier.parse(id)

    /**
     * Called when player enters this location.
     *
     * **NOTE**: Called before any other events
     */
    open fun onEnter(e: PlayerEnterAreaEvent) {
        // No specific logic for area entrance
    }

    /**
     * Registers any external listeners for this area
     */
    open fun registerListeners() {
        // Empty, so no listeners needed
    }

    /**
     * Sends the first entrance messages to player
     */
    fun announce(to: Player) {
        if (description.isEmpty())
            return
        listOf(
            "",
            " <gold><bold>NEW AREA DISCOVERED!",
            "  <gray>⏣ $name",
            "",
            *description.map { obj -> "   <gray>⏹ </gray>$obj" }.toTypedArray()
        ).forEach { to.sendMessage(text(it)) }

        to.title(text(this.name), text("<gold><bold>NEW AREA DISCOVERED!"))
        sound(Sound.ENTITY_PLAYER_LEVELUP) {
            pitch = 0f
            volume = 3f
            playFor(to)
        }
    }

    companion object {
        /**
         * A simple area model implementation
         */
        fun impl(id: String, name: String, description: List<String> = listOf()): AreaModel =
            Impl(id, name, description)
    }

    private class Impl(id: String, name: String, description: List<String> = listOf()) :
        AreaModel(id, name, description)
}
