package space.maxus.macrocosm.item

import org.bukkit.inventory.meta.LeatherArmorMeta
import space.maxus.macrocosm.ability.types.armor.AmethystArmorBonus
import space.maxus.macrocosm.ability.types.armor.BeekeeperArmorBonus
import space.maxus.macrocosm.ability.types.armor.EmeraldArmorBonus
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

    val BEEKEEPER_ARMOR = register(ArmorItem("Beekeeper", "beekeeper", "LEATHER", Rarity.EPIC, stats {
        health = 150f
        defense = 35f
        speed = 15f
        strength = 15f
        critDamage = 10f
    }, abilities = listOf(BeekeeperArmorBonus),
        headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjY0MTY5MDc2ZGJkYjg3ZjI3OTQ0OGQ1YTE2ZmY3OGJiMGEyYjU3NTAzYzIxOGUyMTczMmRiYTlmN2Y5ZjU1YSJ9fX0=",
    chestMeta = {
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0xF1BE66))
    },
    legsMeta = {
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0xECB34F))
    },
    bootMeta = {
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0xECAA38))
    },
    runes = listOf(VanillaRune.REDSTONE, VanillaRune.EMERALD)))

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

        Threading.runAsync("Armor Registry", true) {
            info("Initializing Armor Registry daemon...")

            val pool = Threading.newCachedPool()

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
