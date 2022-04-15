package space.maxus.macrocosm.item

import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.items.meta
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.stats.Statistics

const val MACROCOSM_TAG = "MacrocosmValues"

interface MacrocosmItem {
    var stats: Statistics

    val name: Component
    val base: Material
    var rarity: Rarity
    var rarityUpgraded: Boolean

    fun buildLore(lore: MutableList<Component>) {

    }

    fun addExtraNbt(cmp: CompoundTag) {

    }

    fun upgradeRarity(): Boolean {
        if(rarityUpgraded) {
            return false
        }
        rarity = rarity.next()
        rarityUpgraded = true
        return true
    }

    /**
    Constructs base item stack differently, by default returns null
     **/
    fun alternativeCtor(): ItemStack? = null

    fun parse(from: ItemStack, nbt: CompoundTag): MacrocosmItem

    /**
     * Builds this item
     */
    fun build(): ItemStack {
        val item = alternativeCtor() ?: ItemStack(base, 1)
        item.meta<ItemMeta> {
            // lore
            val lore = mutableListOf<Component>()

            // stats
            lore.addAll(stats.formatSimple())
            lore.add(" ".toComponent())

            // extra lore
            buildLore(lore)

            // rarity
            lore.add(rarity.format(rarityUpgraded))

            lore(lore)

            // name
            displayName(name.color(rarity.color).noitalic())
        }

        // NBT
        val nbt = CompoundTag()

        // stats
        nbt.put("Stats", stats.compound())

        // upgraded
        nbt.putBoolean("RarityUpgraded", rarityUpgraded)

        nbt.putInt("Rarity", rarity.ordinal)
        // adding extra nbt
        addExtraNbt(nbt)

        val nms = CraftItemStack.asNMSCopy(item)
        nms.tag?.put(MACROCOSM_TAG, nbt)
        return nms.asBukkitCopy()
    }
}
