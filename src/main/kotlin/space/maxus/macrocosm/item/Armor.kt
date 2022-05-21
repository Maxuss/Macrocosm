package space.maxus.macrocosm.item

import org.bukkit.inventory.meta.LeatherArmorMeta
import space.maxus.macrocosm.ability.types.AmethystArmorBonus
import space.maxus.macrocosm.ability.types.EmeraldArmorBonus
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.item.runes.VanillaRune
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

object Armor {
    private val cache: MutableList<ArmorItem> = mutableListOf()

    val EMERALD_ARMOR = register(ArmorItem("Emerald", "emerald", "LEATHER", Rarity.RARE, stats {
        health = 100f
        miningFortune = 30f
        defense = 30f
        strength = 20f
    }, abilities = listOf(EmeraldArmorBonus), commonMeta = {
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0x0FBB65))
    }, runes = listOf(VanillaRune.EMERALD, VanillaRune.DIAMOND)))

    val AMETHYST_ARMOR = register(ArmorItem("Amethyst", "amethyst", "LEATHER", Rarity.RARE, stats {
        health = 80f
        miningFortune = 20f
        defense = 40f
    }, abilities = listOf(AmethystArmorBonus), headMeta = {
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0x6F00CB))
    }, chestMeta = {
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0x5B00A7))
    }, legsMeta = {
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0x6B20A9))
    }, bootMeta = {
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0x50177F))
    }, runes = listOf(VanillaRune.AMETHYST, VanillaRune.DIAMOND)))

    private fun register(item: ArmorItem): ArmorItem {
        cache.add(item)
        return item
    }

    private fun internalRegisterSingle(item: ArmorItem) {
        Registry.ITEM.register(id("${item.baseId}_helmet"), item.helmet())
        Registry.ITEM.register(id("${item.baseId}_chestplate"), item.chestplate())
        Registry.ITEM.register(id("${item.baseId}_leggings"), item.leggings())
        Registry.ITEM.register(id("${item.baseId}_boots"), item.boots())
    }

    fun init() {
        Threading.start("Armor Registry", true) {
            info("Initializing Armor Registry daemon...")

            val pool = Threading.pool()

            for (element in cache) {
                pool.execute {
                    info("Registering ${element.baseName} armor set")
                    internalRegisterSingle(element)
                }
            }

            pool.shutdown()
            val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            if (!success)
                throw IllegalStateException("Could not execute all tasks in the thread pool!")
        }
    }
}
