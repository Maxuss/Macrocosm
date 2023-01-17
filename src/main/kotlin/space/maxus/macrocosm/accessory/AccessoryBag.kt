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
import space.maxus.macrocosm.util.*
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.math.ln
import kotlin.math.pow

class AccessoryContainer(var item: Identifier, var family: String, var rarity: Rarity): Externalizable {
    constructor(): this(Identifier.NULL, "null", Rarity.COMMON)

    override fun writeExternal(out: ObjectOutput) {
        out.writeObject(item.toString())
        out.writeObject(family)
        out.writeInt(rarity.ordinal)
    }

    override fun readExternal(`in`: ObjectInput) {
        item = Identifier.parse(`in`.readObject() as String)
        family = `in`.readObject() as String
        rarity = Rarity.values()[`in`.readInt()]
    }
}

val rarityToMp = hashMapOf(
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
    var capacity: Int = 9
    var redstoneCollSlots: Int = 0
    var jacobusSlots: Int = 0
    var mithrilCollSlots: Int = 0
    val accessories: MutableList<AccessoryContainer> = mutableListOf()

    val serialVersionUID = 1_100_000_000L

    val magicPower get() = accessories.sumOf { rarityToMp[it.rarity]!! }

    fun statModifier(): Double {
        return Companion.statModifier(magicPower)
    }

    fun addAccessory(item: AccessoryItem): Boolean {
        if(accessories.size + 1 > capacity || accessories.any { it.item == item.id } || accessories.any { it.family == item.family })
            return false
        accessories.add(item.container)
        return true
    }

    fun ui(player: MacrocosmPlayer): GUI<*> {
        val (size, beginCompound, endCompound) = when(capacity) {
            in 1..9 -> Triple(GUIType.TWO_BY_NINE, Slots.RowTwoSlotOne, Slots.RowTwoSlotNine)
            in 10..18 -> Triple(GUIType.THREE_BY_NINE, Slots.RowTwoSlotOne, Slots.RowThreeSlotNine)
            in 19..27 -> Triple(GUIType.FOUR_BY_NINE, Slots.RowTwoSlotOne, Slots.RowFourSlotNine)
            in 28..36 -> Triple(GUIType.FIVE_BY_NINE, Slots.RowTwoSlotOne, Slots.RowFiveSlotNine)
            else -> Triple(GUIType.TWO_BY_NINE, Slots.RowTwoSlotOne, Slots.RowSixSlotNine)
        }
        return kSpigotGUI(size) {
            defaultPage = 0
            title = text("Accessories")

            val partitioned = accessories.padNullsForward(capacity).chunked(45)

            for((index, accs) in partitioned.withIndex()) {
                page(index) {
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

                    @Suppress("UNCHECKED_CAST")
                    compoundSpace((beginCompound rectTo endCompound) as InventorySlotCompound<ForInventoryTwoByNine>, compound)

                    if(capacity > 45) {
                        if (page != partitioned.size - 1)
                            pageChanger(
                                Slots.RowOneSlotNine,
                                ItemValue.placeholder(Material.ARROW, "<green>Next Page"),
                                page + 1,
                                null,
                                null
                            )
                        if (page != 0)
                            pageChanger(
                                Slots.RowOneSlotEight,
                                ItemValue.placeholder(Material.ARROW, "<red>Previous Page"),
                                page - 1,
                                null,
                                null
                            )
                    }

                    val lightGrayGlass = ItemValue.placeholder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                    val grayGlass = ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE)
                    val unoccupied = accs.count { it == null }
                    runCatchingReporting(player.paper ?: return@page) {
                        val mapped = accs.mapIndexedNotNull { index, item -> index to run {
                            val base = Registry.ITEM.findOrNull(item?.item ?: return@mapIndexedNotNull null) ?: return@run ItemValue.NULL.item.build(player)!!
                            if(item.rarity != base.rarity) {
                                base.rarity = item.rarity
                                base.rarityUpgraded = true
                            }
                            base.build(player) ?: ItemValue.NULL.item.build(player)!!
                        } }.let { it.padForward(it.size + unoccupied, Pair(-1, lightGrayGlass)).padForward(45, Pair(-1, grayGlass)) }
                        compound.addContent(mapped)
                    }
                }
            }
        }
    }


    companion object {
        private val cachedResults = ConcurrentHashMap<Int, Double>()

        fun statModifier(mp: Int): Double {
            if(cachedResults.containsKey(mp))
                return cachedResults[mp]!!
            val res = (29.97 * (ln(.0019 * mp + 1))).pow(1.2)
            cachedResults[mp] = res
            return res
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

enum class BagCapacityBonus(val amount: Int) {
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
