package space.maxus.macrocosm.item

import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

@Suppress("UNUSED")
object ItemRegistry {
    private val items: HashMap<String, MacrocosmItem> = hashMapOf()
    private var itemCmd: Long = 1000

    fun nextModel() = ++itemCmd

    fun find(id: String) = items[id]!!

    fun register(id: String, item: MacrocosmItem) {
        if(items.containsKey(id))
            return
        items[id] = item
    }

    fun toMacrocosm(item: ItemStack): MacrocosmItem {
        val tag = CraftItemStack.asNMSCopy(item).tag ?: throw Exception("Provided item did not have NBT tag! That is very weird!")
        if(!tag.contains(MACROCOSM_TAG)) {
            return VanillaItem(item.type)
        }

        val nbt = tag.getCompound(MACROCOSM_TAG)
        if(!nbt.contains("ID"))
            return VanillaItem(item.type).parse(item, nbt)
        return find(nbt.getString("ID")).parse(item, nbt)
    }
}
