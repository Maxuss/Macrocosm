package space.maxus.macrocosm.item

import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.items.meta
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.reforge.ReforgeRegistry
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp

const val MACROCOSM_TAG = "MacrocosmValues"

val ItemStack.macrocosm: MacrocosmItem get() = ItemRegistry.toMacrocosm(this)

interface MacrocosmItem {
    var stats: Statistics

    val name: Component
    val base: Material
    var rarity: Rarity
    var rarityUpgraded: Boolean
    var reforge: Reforge?
    val abilities: MutableList<ItemAbility>

    fun buildLore(lore: MutableList<Component>) {

    }

    fun addExtraNbt(cmp: CompoundTag) {

    }

    fun reforge(ref: Reforge) {
        if(reforge != null) {
            stats.decrease(reforge!!.stats(rarity))
        }
        reforge = ref
        stats.increase(ref.stats(rarity))
    }

    fun upgradeRarity(): Boolean {
        if(rarityUpgraded) {
            return false
        }

        val prev = rarity
        rarity = rarity.next()

        if(reforge != null) {
            stats.decrease(reforge!!.stats(prev))
            stats.increase(reforge!!.stats(rarity))
        }
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
            lore.addAll(stats.formatSimple(reforge?.stats(rarity)))
            lore.add("".toComponent())

            // abilities
            for(ability in abilities) {
                lore.addAll(ability.buildLore())
            }

            // extra lore
            buildLore(lore)

            // rarity
            lore.add(rarity.format(rarityUpgraded))

            lore(lore)

            // name
            var display = name
            if(reforge != null)
                display = comp("${reforge!!.name} ").append(display)

            displayName(display.color(rarity.color).noitalic())
        }

        // NBT
        val nbt = CompoundTag()

        // stats
        nbt.put("Stats", stats.compound())

        // rarity
        nbt.putBoolean("RarityUpgraded", rarityUpgraded)
        nbt.putInt("Rarity", rarity.ordinal)

        // reforges
        if(reforge != null)
            nbt.putString("Reforge", ReforgeRegistry.nameOf(reforge!!))
        else
            nbt.putString("Reforge", "NULL")

        // adding extra nbt
        addExtraNbt(nbt)

        val nms = CraftItemStack.asNMSCopy(item)
        nms.tag?.put(MACROCOSM_TAG, nbt)
        return nms.asBukkitCopy()
    }
}
