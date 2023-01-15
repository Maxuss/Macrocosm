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
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.emptySlots
import space.maxus.macrocosm.util.giveOrDrop
import space.maxus.macrocosm.util.padForward
import space.maxus.macrocosm.util.runCatchingReporting
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ln
import kotlin.math.pow

data class AccessoryContainer(var item: Identifier, var rarity: Rarity): Externalizable {
    override fun writeExternal(out: ObjectOutput) {
        out.writeObject(item.toString())
        out.writeInt(rarity.ordinal)
    }

    override fun readExternal(`in`: ObjectInput) {
        item = Identifier.parse(`in`.readObject() as String)
        rarity = Rarity.values()[`in`.readInt()]
    }
}

private val rarityToMp = hashMapOf(
    Rarity.SPECIAL to 3,
    Rarity.VERY_SPECIAL to 5,
    Rarity.COMMON to 3,
    Rarity.UNCOMMON to 5,
    Rarity.RARE to 8,
    Rarity.EPIC to 12,
    Rarity.LEGENDARY to 16,
    Rarity.RELIC to 25,
    Rarity.MYTHIC to 22,
    Rarity.DIVINE to 25,
    Rarity.UNOBTAINABLE to 30,
)

class AccessoryBag: Serializable {
    var power: Identifier = Identifier.NULL
    var capacity: Int = 3
    val accessories: MutableList<AccessoryContainer> = mutableListOf()

    val magicPower get() = accessories.sumOf { rarityToMp[it.rarity]!! }

    fun statModifier(): Double {
        val mp = magicPower
        if(cachedResults.containsKey(mp))
            return cachedResults[mp]!!
        val res = (29.97 * (ln(.0019 * mp + 1))).pow(1.2)
        cachedResults[mp] = res
        return res
    }

    fun addAccessory(item: AccessoryItem): Boolean {
        if(accessories.size + 1 > capacity || accessories.any { it.item == item.id })
            return false
        accessories.add(item.container)
        return true
    }

    fun ui(player: MacrocosmPlayer): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
        defaultPage = 0
        title = text("Accessories")

        page(0) {
            placeholder(Slots.Border, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))
            button(Slots.RowOneSlotFive, ItemValue.placeholder(Material.BARRIER, "<red>Close")) { e ->
                e.bukkitEvent.isCancelled = true
                e.player.closeInventory()
            }

            val compound = createCompound<Pair<Int, ItemStack>>({ it.second }) { e, (index, item) ->
                e.bukkitEvent.isCancelled = true
                if(index != -1 && e.player.inventory.emptySlots != 0) {
                    accessories.removeAt(index)
                    e.player.giveOrDrop(item)
                    e.player.openGUI(ui(player))
                }
            }

            compoundSpace(Slots.RowTwoSlotTwo rectTo Slots.RowFiveSlotEight, compound)

            compoundScroll(Slots.RowOneSlotNine, ItemValue.placeholder(Material.ARROW, "<green>Forward"), compound)
            compoundScroll(Slots.RowOneSlotEight, ItemValue.placeholder(Material.ARROW, "<red>Back"), compound, reverse = true)


            val lightGrayGlass = ItemValue.placeholder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
            val grayGlass = ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE)
            runCatchingReporting(player.paper ?: return@page) {
                val mapped = accessories.mapIndexed { index, item -> index to run {
                    val base = Registry.ITEM.findOrNull(item.item) ?: return@run ItemValue.NULL.item.build(player)!!
                    if(item.rarity != base.rarity) {
                        base.rarity = item.rarity
                        base.rarityUpgraded = true
                    }
                    base.build(player) ?: ItemValue.NULL.item.build(player)!!
                } }.padForward(capacity, Pair(-1, lightGrayGlass)).padForward(BagCapacity.MAXIMAL.amount, Pair(-1, grayGlass))
                compound.addContent(mapped)
            }
        }
    }

    companion object {
        val cachedResults = ConcurrentHashMap<Int, Double>()
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
    PREPOSTEROUS(57),
    MAXIMAL(60)
}
