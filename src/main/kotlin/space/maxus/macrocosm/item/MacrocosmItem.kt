package space.maxus.macrocosm.item

import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.items.flags
import net.axay.kspigot.items.meta
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.enchants.EnchantmentRegistry
import space.maxus.macrocosm.enchants.UltimateEnchantment
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.reforge.ReforgeRegistry
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp

const val MACROCOSM_TAG = "MacrocosmValues"

val ItemStack.macrocosm: MacrocosmItem? get() = ItemRegistry.toMacrocosm(this)

interface MacrocosmItem {
    var stats: Statistics
    var specialStats: SpecialStatistics

    val id: String
    val type: ItemType
    val name: Component
    val base: Material
    var rarity: Rarity
    var rarityUpgraded: Boolean
    var reforge: Reforge?
    val abilities: MutableList<ItemAbility>
    val enchantments: HashMap<Enchantment, Int>

    fun buildLore(lore: MutableList<Component>) {

    }

    fun addExtraNbt(cmp: CompoundTag) {

    }

    fun reforge(ref: Reforge) {
        if (reforge != null) {
            stats.decrease(reforge!!.stats(rarity))
        }
        reforge = ref
        stats.increase(ref.stats(rarity))
    }

    fun stats(): Statistics {
        val base = stats.clone()
        val special = specialStats()
        base.multiply(1 + special.statBoost)
        return base
    }

    fun specialStats(): SpecialStatistics {
        val base = specialStats.clone()
        for ((ench, level) in enchantments) {
            base.increase(ench.special(level))
        }
        return base
    }

    fun upgradeRarity(): Boolean {
        if (rarityUpgraded) {
            return false
        }

        val prev = rarity
        rarity = rarity.next()

        if (reforge != null) {
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

    fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        rarityUpgraded = nbt.getBoolean("RarityUpgraded")
        if (rarityUpgraded)
            rarity = rarity.next()

        val reforge = nbt.getString("Reforge")
        if (reforge != "NULL") {
            reforge(ReforgeRegistry.find(reforge)!!)
        }

        val enchants = nbt.getCompound("Enchantments")
        for (k in enchants.allKeys) {
            if (k == "NULL")
                continue
            enchantments[EnchantmentRegistry.find(k)!!] = enchants.getInt(k)
        }
        return this
    }

    fun enchant(enchantment: Enchantment, level: Int): Boolean {
        if (!enchantment.levels.contains(level) || !enchantment.applicable.contains(type))
            return false
        val name = EnchantmentRegistry.nameOf(enchantment)
        enchantments.filter { (ench, _) ->
            ench.conflicts.contains("ALL")
        }.forEach { (ench, _) ->
            enchantments.remove(ench)
        }
        if(enchantment.conflicts.contains("ALL")) {
            enchantments.filter{ (ench, _) ->
                ench.name != "Telekinesis"
            }.forEach { (ench, _) ->
                enchantments.remove(ench)
            }
        } else {
            enchantments.filter { (ench, _) ->
                ench.conflicts.contains(name)
            }.forEach { (ench, _) ->
                enchantments.remove(ench)
            }
            if (enchantment is UltimateEnchantment) {
                enchantments.filter { (ench, _) ->
                    ench is UltimateEnchantment
                }.forEach { (ench, _) ->
                    enchantments.remove(ench)
                }
            }
        }
        enchantments[enchantment] = level
        return true
    }

    /**
     * Builds this item
     */
    @Suppress("UNCHECKED_CAST")
    fun build(): ItemStack? {
        if (base == Material.AIR)
            return null

        val item = alternativeCtor() ?: ItemStack(base, 1)
        item.meta<ItemMeta> {
            // lore
            val lore = mutableListOf<Component>()

            // stats
            val formattedStats = stats.formatSimple(reforge?.stats(rarity))
            lore.addAll(formattedStats)
            if(formattedStats.isNotEmpty())
                lore.add("".toComponent())

            // enchants
            if (enchantments.isNotEmpty()) {
                val cloned = enchantments.clone() as HashMap<Enchantment, Int>
                if (cloned.size > 6) {
                    val cmp = StringBuilder()
                    cloned.filter { (ench, _) -> ench is UltimateEnchantment }.forEach { (ench, lvl) ->
                        cloned.remove(ench)
                        cmp.append(", ${MiniMessage.miniMessage().serialize(ench.displaySimple(lvl))}<!bold>")
                    }
                    cloned.map { (ench, lvl) -> ench.displaySimple(lvl) }.forEach {
                        cmp.append(", ${MiniMessage.miniMessage().serialize(it)}")
                    }
                    val reduced = cmp.toString().trim(',').trim().split(", ").joinToString(", ").reduceToList(30).map { comp(it).noitalic() }
                    lore.addAll(reduced)
                    lore.add("".toComponent())
                } else {
                    cloned.filter { (ench, _) -> ench is UltimateEnchantment }.forEach { (ench, lvl) ->
                        cloned.remove(ench)
                        ench.displayFancy(lore, lvl)
                    }
                    for ((ench, lvl) in enchantments) {
                        ench.displayFancy(lore, lvl)
                    }
                    lore.add("".toComponent())
                }
            }

            // abilities
            for (ability in abilities) {
                ability.buildLore(lore)
            }

            // reforge
            reforge?.buildLore(lore)

            // extra lore
            buildLore(lore)

            // rarity
            lore.add(rarity.format(rarityUpgraded, type))

            lore(lore)

            // name
            var display = name
            if (reforge != null)
                display = comp("${reforge!!.name} ").append(display)

            displayName(display.color(rarity.color).noitalic())

            // item flags
            flags(*ItemFlag.values())
        }

        // NBT
        val nbt = CompoundTag()

        // stats
        nbt.put("Stats", stats.compound())

        // special stats
        nbt.put("SpecialStats", specialStats.compound())

        // rarity
        nbt.putBoolean("RarityUpgraded", rarityUpgraded)
        nbt.putInt("Rarity", rarity.ordinal)

        // reforges
        if (reforge != null)
            nbt.putString("Reforge", ReforgeRegistry.nameOf(reforge!!) ?: "NULL")
        else
            nbt.putString("Reforge", "NULL")

        // enchants
        val enchants = CompoundTag()
        for ((ench, level) in enchantments) {
            enchants.putInt(EnchantmentRegistry.nameOf(ench) ?: "NULL", level)
        }
        nbt.put("Enchantments", enchants)

        // item ID
        nbt.putString("ID", ItemRegistry.nameOf(this) ?: "NULL")

        // adding extra nbt
        addExtraNbt(nbt)

        val nms = CraftItemStack.asNMSCopy(item)
        nms.tag?.put(MACROCOSM_TAG, nbt)
        return nms.asBukkitCopy()
    }

    fun clone(): MacrocosmItem
}
