package space.maxus.macrocosm.accessory

import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.accessory.power.AccessoryPower
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.mongo.MongoConvert
import space.maxus.macrocosm.mongo.data.MongoAccessoryBag
import space.maxus.macrocosm.mongo.data.MongoAccessoryContainer
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.CompoundComponent
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.*
import java.io.Externalizable
import java.io.ObjectInput
import java.io.ObjectOutput
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set
import kotlin.math.ln
import kotlin.math.pow

/**
 * A container for accessories, stored inside the accessory bag
 *
 * @property item The ID of accessory item
 * @property family The family of accessory
 * @property rarity The rarity of accessory
 */
class AccessoryContainer(var item: Identifier, var family: String, var rarity: Rarity) : Externalizable {
    constructor() : this(Identifier.NULL, "null", Rarity.COMMON)

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

/**
 * A container for player accessory bag
 */
class AccessoryBag : Serializable, MongoConvert<MongoAccessoryBag> {
    /**
     * Selected [AccessoryPower] for this bag
     */
    var power: Identifier = Identifier.NULL

    /**
     * Maximum capacity of this accessory bag
     */
    var capacity: Int = 9

    /**
     * Slots obtained from the redstone collection
     */
    var redstoneCollSlots: Int = 0

    /**
     * Slots obtained from Jacqueefis (Jacobus)
     */
    var jacobusSlots: Int = 0

    /**
     * Slots obtained from the mithril collection
     */
    var mithrilCollSlots: Int = 0

    /**
     * Actual accessories stored inside this bag
     */
    val accessories: MutableList<AccessoryContainer> = mutableListOf()

    /**
     * Serialization version control
     */
    val serialVersionUID = 1_100_000_000L

    /**
     * Gets total magic power of this bag
     */
    val magicPower get() = accessories.sumOf { rarityToMp[it.rarity]!! }

    /**
     * Gets power stat modifier for this bag
     */
    fun statModifier(): Double {
        return Companion.statModifier(magicPower)
    }

    /**
     * Adds an accessory to this bag
     *
     * @param item Accessory to be added
     * @return `true` if added the accessory, `false` if failed to add (not enough space/accessory already present/accessory of same family already present)
     */
    fun addAccessory(item: AccessoryItem): Boolean {
        if (accessories.size + 1 > capacity || accessories.any { it.item == item.id } || accessories.any { it.family == item.family })
            return false
        accessories.add(item.container)
        return true
    }

    /**
     * Loads the accessory UI for player
     */
    fun ui(player: MacrocosmPlayer): MacrocosmUI {
        val (size, beginCompound, endCompound) = when (capacity) {
            in 1..9 -> Triple(UIDimensions.TWO_X_NINE, Slot.RowOneSlotOne, Slot.RowOneSlotNine)
            in 10..18 -> Triple(UIDimensions.THREE_X_NINE, Slot.RowOneSlotOne, Slot.RowTwoSlotNine)
            in 19..27 -> Triple(UIDimensions.FOUR_X_NINE, Slot.RowOneSlotOne, Slot.RowThreeSlotNine)
            in 28..36 -> Triple(UIDimensions.FIVE_X_NINE, Slot.RowOneSlotOne, Slot.RowFourSlotNine)
            else -> Triple(UIDimensions.SIX_X_NINE, Slot.RowOneSlotOne, Slot.RowFiveSlotNine)
        }
        return macrocosmUi("accessory_bag", size) {
            title = "Accessories"

            val partitioned = accessories.padNullsForward(capacity).chunked(45)

            for ((index, _) in partitioned.withIndex()) {
                page(index) {
                    background()

                    close()

                    val lightGrayGlass = ItemValue.placeholder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                    val grayGlass = ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE)
                    var unoccupied: Int

                    var compound: CompoundComponent<Pair<Int, ItemStack>>? = null
                    compound = compound(beginCompound rect endCompound, {
                        val repartitioned = accessories.padNullsForward(capacity).chunked(45)
                        val reAccs = repartitioned.getOrNull(index) ?: listOf()
                        unoccupied = reAccs.count { it == null }
                        var mapped: MutableCollection<Pair<Int, ItemStack>> = mutableListOf()
                        runCatchingReporting(player.paper ?: return@compound listOf()) {
                            mapped = reAccs.mapIndexedNotNull { index, item ->
                                index to run {
                                    val base = Registry.ITEM.findOrNull(item?.item ?: return@mapIndexedNotNull null)
                                        ?: return@run ItemValue.NULL.item.build(player)!!
                                    if (item.rarity != base.rarity) {
                                        base.rarity = item.rarity
                                        base.rarityUpgraded = true
                                    }
                                    base.build(player) ?: ItemValue.NULL.item.build(player)!!
                                }
                            }.let {
                                it.padForward(it.size + unoccupied, Pair(-1, lightGrayGlass))
                                    .padForward(45, Pair(-1, grayGlass))
                            }
                        }
                        mapped.toList()
                    }, { it.second }) { e, (index, item) ->
                        if (index != -1 && e.paper.inventory.emptySlots != 0) {
                            accessories.removeAt(index)
                            e.paper.giveOrDrop(item)
                            compound?.recalculateSlicedContent()
                            e.instance.reload()
                        }
                    }

                    if (capacity > 45) {
                        if (index != partitioned.size - 1)
                            changePage(Slot.RowLastSlotNine, index + 1)
                        if (index != 0)
                            changePage(Slot.RowLastSlotEight, index - 1)
                    }
                }
            }
        }
    }


    companion object {
        private val cachedResults = ConcurrentHashMap<Int, Double>()

        /**
         * Calculates a power stat modifier with provided magic power value
         */
        fun statModifier(mp: Int): Double {
            if (cachedResults.containsKey(mp))
                return cachedResults[mp]!!
            val res = (29.97 * (ln(.0019 * mp + 1))).pow(1.2)
            cachedResults[mp] = res
            return res
        }

    }

    object Handlers : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onClick(e: InventoryClickEvent) {
            val clicker = e.whoClicked
            if (e.view.title()
                    .str() != "Accessories" || e.clickedInventory == e.view.topInventory || clicker !is Player
            )
                return
            e.isCancelled = true
            val clickedItem = e.currentItem
            val clicked = clickedItem?.macrocosm
            if (clicked == null || clicked !is AccessoryItem)
                return
            val bag = clicker.macrocosm?.accessoryBag ?: return
            if (!bag.addAccessory(clicked)) {
                sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                    pitch = 0f
                    playFor(clicker)
                }
                return
            }
            if (clickedItem.amount > 1) {
                clicked.amount = 1
                clickedItem.amount -= 1
            } else {
                e.clickedInventory!!.clear(e.slot)
            }
            bag.ui(clicker.macrocosm!!).open(clicker)
        }
    }

    override val mongo: MongoAccessoryBag
        get() = MongoAccessoryBag(
            power.toString(),
            capacity,
            redstoneCollSlots,
            mithrilCollSlots,
            jacobusSlots,
            accessories.map { MongoAccessoryContainer(it.item.toString(), it.family, it.rarity) })
}

