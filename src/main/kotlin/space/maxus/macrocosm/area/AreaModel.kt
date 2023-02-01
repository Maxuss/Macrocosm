package space.maxus.macrocosm.area

import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.entity.Player
import space.maxus.macrocosm.events.PlayerEnterAreaEvent
import space.maxus.macrocosm.registry.Identified
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.text

abstract class AreaModel(
    id: String,
    val name: String,
    val description: List<String>
): Identified {
    override val id = Identifier.parse(id)

    open fun onEnter(e: PlayerEnterAreaEvent) {
        // No specific logic for area entrance
    }
    open fun registerListeners() {
        // Empty, so no listeners needed
    }

    fun announce(to: Player) {
        if(description.isEmpty())
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
        fun impl(id: String, name: String, description: List<String> = listOf()): AreaModel = Impl(id, name, description)
    }

    private class Impl(id: String, name: String, description: List<String> = listOf()): AreaModel(id, name, description)
}
