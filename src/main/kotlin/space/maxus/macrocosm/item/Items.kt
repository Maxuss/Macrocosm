package space.maxus.macrocosm.item

import org.bukkit.Material
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.getId

@Suppress("UNUSED")
object Items {
    fun toMacrocosm(item: ItemStack): MacrocosmItem? {
        if (item.type == Material.AIR)
            return null
        val tag = CraftItemStack.asNMSCopy(item).tag ?: return VanillaItem(item.type, item.amount)
        if (!tag.contains(MACROCOSM_TAG)) {
            return VanillaItem(item.type, item.amount)
        }

        val nbt = tag.getCompound(MACROCOSM_TAG)
        if (!nbt.contains("ID") || nbt.getId("ID").namespace == "minecraft")
            return VanillaItem(item.type, item.amount).convert(item, nbt)
        return Registry.ITEM.find(Identifier.parse(nbt.getString("ID"))).convert(item, nbt)
    }
}
