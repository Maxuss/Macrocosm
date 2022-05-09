package space.maxus.macrocosm.item

import org.bukkit.Material
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.util.Identifier
import space.maxus.macrocosm.util.getId
import java.util.concurrent.ConcurrentHashMap

@Suppress("UNUSED")
object ItemRegistry {
    val items: ConcurrentHashMap<Identifier, MacrocosmItem> = ConcurrentHashMap(hashMapOf())
    private var itemCmd: Long = 1000

    fun nextModel() = ++itemCmd

    fun find(id: Identifier): MacrocosmItem {
        return if(id.namespace == "minecraft") {
            VanillaItem(Material.valueOf(id.path.uppercase()))
        } else if(id.path == "null")
            VanillaItem(Material.AIR)
        else items[id]!!.clone()
    }

    fun register(id: Identifier, item: MacrocosmItem): MacrocosmItem {
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
        if (!nbt.contains("ID") || nbt.getId("ID").namespace == "minecraft")
            return VanillaItem(item.type).convert(item, nbt)
        return find(Identifier.parse(nbt.getString("ID"))).convert(item, nbt)
    }
}
