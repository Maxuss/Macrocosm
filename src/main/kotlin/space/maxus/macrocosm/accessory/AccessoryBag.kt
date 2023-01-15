package space.maxus.macrocosm.accessory

import net.axay.kspigot.gui.*
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.emptySlots
import space.maxus.macrocosm.util.giveOrDrop
import space.maxus.macrocosm.util.runCatchingReporting
import java.io.Serializable

class AccessoryBag: Serializable {
    var capacity: Int = 3
    val accessories: MutableList<AccessoryItem> = mutableListOf()

    fun addAccessory(item: AccessoryItem): Boolean {
        if(accessories.size + 1 > capacity || accessories.any { it.id == item.id })
            return false
        accessories.add(item)
        return true
    }

    fun ui(player: MacrocosmPlayer): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
        defaultPage = 0
        title = text("Accessories")

        page(0) {
            placeholder(Slots.Border, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

            val compound = createCompound<Pair<Int, ItemStack>>({ it.second }) { e, (index, item) ->
                e.bukkitEvent.isCancelled = true
                if(e.player.inventory.emptySlots != 0) {
                    accessories.removeAt(index)
                    e.player.giveOrDrop(item)
                    e.player.openGUI(ui(player))
                }
            }

            compoundSpace(Slots.RowTwoSlotTwo rectTo Slots.RowFiveSlotEight, compound)

            runCatchingReporting(player.paper ?: return@page) {
                compound.addContent(accessories.mapIndexed { index, item -> index to item.build(player)!! })
            }
        }
    }

    object Handlers: Listener {
        @EventHandler(ignoreCancelled = true)
        fun onClick(e: InventoryClickEvent) {
            val clicker = e.whoClicked
            if(e.view.title().str() != "Accessories" || e.clickedInventory == e.view.topInventory || clicker !is Player)
                return
            e.isCancelled = true
            val clickedItem = e.currentItem
            clickedItem?.amount = 1
            val clicked = clickedItem?.macrocosm
            if(clicked == null || clicked !is AccessoryItem)
                return
            val bag = clicker.macrocosm?.accessoryBag ?: return
            if(!bag.addAccessory(clicked)) {
                sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                    pitch = 0f
                    playFor(clicker)
                }
                return
            }
            e.clickedInventory!!.clear(e.slot)
            clicker.openGUI(bag.ui(clicker.macrocosm!!))
        }
    }
}

enum class BagCapacity(val amount: Int) {
    SMALL(3),
    MEDIUM(9),
    LARGE(15),
    GREATER(21),
    GIANT(27),
    MASSIVE(33),
    HUMONGOUS(39),
    COLOSSAL(45),
    TITANIC(51),
    PREPOSTEROUS(57)
}
