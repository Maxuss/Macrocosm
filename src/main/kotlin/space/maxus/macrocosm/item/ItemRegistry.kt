package space.maxus.macrocosm.item

import org.bukkit.Material
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNUSED")
object ItemRegistry {
    val items: ConcurrentHashMap<String, MacrocosmItem> = ConcurrentHashMap(hashMapOf())
    private var itemCmd: Long = 1000

    fun nextModel() = ++itemCmd

    fun find(id: String): MacrocosmItem {
        return items[id]!!.clone()
    }

    fun register(id: String, item: MacrocosmItem): MacrocosmItem {
        if (items.containsKey(id))
            return item
        items[id] = item
        return item
    }

    fun nameOf(item: MacrocosmItem) = items.filter { (_, v) -> v.id == item.id }.map { (k, _) -> k }.firstOrNull()

    fun toMacrocosm(item: ItemStack): MacrocosmItem? {
        if (item.type == Material.AIR)
            return null
        val tag = CraftItemStack.asNMSCopy(item).tag ?: return VanillaItem(item.type)
        if (!tag.contains(MACROCOSM_TAG)) {
            return VanillaItem(item.type)
        }

        val nbt = tag.getCompound(MACROCOSM_TAG)
        if (!nbt.contains("ID") || nbt.getString("ID") == "NULL")
            return VanillaItem(item.type).convert(item, nbt)
        return find(nbt.getString("ID")).convert(item, nbt)
    }
}
