package space.maxus.macrocosm.ui

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.players.MacrocosmPlayer

data class UIClickData(
    val bukkit: InventoryClickEvent,
    val paper: Player,
    val player: MacrocosmPlayer,
    val inventory: Inventory,
    val instance: MacrocosmUIInstance
)

data class UICloseData(
    val paper: Player,
    val player: MacrocosmPlayer,
    val inventory: Inventory,
    val instance: MacrocosmUIInstance
)
