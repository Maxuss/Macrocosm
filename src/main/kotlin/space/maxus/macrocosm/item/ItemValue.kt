package space.maxus.macrocosm.item

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.items.flags
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.ability.Ability
import space.maxus.macrocosm.ability.types.InstantTransmission
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.item.types.WitherBlade
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.Identifier
import java.util.*
import java.util.concurrent.TimeUnit

enum class ItemValue(val item: MacrocosmItem) {
    ASPECT_OF_THE_END(AbilityItem(ItemType.SWORD, "Aspect of the End", Rarity.RARE, Material.DIAMOND_SWORD, stats {
        damage = 100f
        strength = 50f
        intelligence = 150f
    }, mutableListOf(InstantTransmission))),

    UNREFINED_WITHER_BLADE(WitherBlade("Unrefined Wither Blade", Material.IRON_SWORD, stats {
        damage = 260f
        strength = 110f
        intelligence = 50f
        ferocity = 30f
    })),

    ASTRAEA(WitherBlade("Astraea", Material.STONE_SWORD, stats {
        damage = 270f
        strength = 150f
        defense = 250f
        intelligence = 50f
        trueDefense = 20f
        ferocity = 30f
    })),

    SCYLLA(WitherBlade("Scylla", Material.GOLDEN_SWORD, stats {
        damage = 270f
        strength = 150f
        critChance = 12f
        critDamage = 35f
        intelligence = 50f
        ferocity = 30f
    })),

    HYPERION(WitherBlade("Hyperion", Material.DIAMOND_SWORD, stats {
        damage = 260f
        strength = 150f
        intelligence = 350f
        ferocity = 30f
    })),

    VALKYRIE(WitherBlade("Valkyrie", Material.NETHERITE_SWORD, stats {
        damage = 270f
        strength = 145f
        intelligence = 60f
        ferocity = 60f
    })),

    EMERALD_BOOTS(AbilityItem(ItemType.BOOTS, "Emerald Boots", Rarity.RARE, Material.LEATHER_BOOTS, stats {
        health = 75f
        miningFortune = 25f
        defense = 25f
        strength = 15f
    }, mutableListOf(Ability.EMERALD_AFFECTION.ability), metaModifier = {
        it.isUnbreakable = true
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0x0FBB65))
        leather
    })),

    EMERALD_LEGGINGS(AbilityItem(ItemType.BOOTS, "Emerald Leggings", Rarity.RARE, Material.LEATHER_LEGGINGS, stats {
        health = 80f
        miningFortune = 30f
        defense = 30f
        strength = 15f
    }, mutableListOf(Ability.EMERALD_AFFECTION.ability), metaModifier = {
        it.isUnbreakable = true
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0x0FBB65))
        leather
    })),

    EMERALD_CHESTPLATE(AbilityItem(ItemType.BOOTS, "Emerald Chestplate", Rarity.RARE, Material.LEATHER_CHESTPLATE, stats {
        health = 100f
        miningFortune = 30f
        defense = 30f
        strength = 20f
    }, mutableListOf(Ability.EMERALD_AFFECTION.ability), metaModifier = {
        it.isUnbreakable = true
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0x0FBB65))
        leather
    })),

    EMERALD_HELMET(AbilityItem(ItemType.HELMET, "Emerald Helmet", Rarity.RARE, Material.LEATHER_HELMET, stats {
        health = 75f
        miningFortune = 26f
        defense = 25f
        strength = 12f
    }, mutableListOf(Ability.EMERALD_AFFECTION.ability), metaModifier = {
        it.isUnbreakable = true
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0x0FBB65))
        leather
    })),

    EMERALD_PICKAXE(AbilityItem(ItemType.PICKAXE, "Emerald Pickaxe", Rarity.RARE, Material.DIAMOND_PICKAXE, stats {
        damage = 30f
        defense = 50f
        miningSpeed = 450f
        miningFortune = 150f
    }, mutableListOf(Ability.EMERALD_AFFECTION_PICKAXE.ability)))

    ;

    companion object {
        fun enchanted(type: Material) = ItemRegistry.find(Identifier.macro("enchanted_${type.name.lowercase()}"))
        fun placeholder(type: Material) = itemStack(type) {
            meta {
                displayName(Component.empty())
                persistentDataContainer[pluginKey("placeholder"), PersistentDataType.BYTE] = 1
                flags(*ItemFlag.values())
            }
        }

        fun placeholderHead(skin: String, name: String, description: String) = itemStack(Material.PLAYER_HEAD) {
            meta {
                displayName(comp(name))
                persistentDataContainer[pluginKey("placeholder"), PersistentDataType.BYTE] = 1
                flags(*ItemFlag.values())
                meta<SkullMeta> {
                    val id = UUID(
                        skin.substring(skin.length - 20).hashCode().toLong(),
                        skin.substring(skin.length - 10).hashCode().toLong()
                    )
                    val profile = GameProfile(id, "Player")
                    profile.properties.put("textures", Property("textures", skin))
                    val profileField = javaClass.getDeclaredField("profile")
                    profileField.isAccessible = true
                    profileField.set(this, profile)

                    lore(description.reduceToList().map { comp(it) })
                }
            }
        }

        fun placeholder(type: Material, name: String, vararg extra: String) = itemStack(type) {
            meta {
                displayName(comp(name))
                persistentDataContainer[pluginKey("placeholder"), PersistentDataType.BYTE] = 1
                for (e in extra) {
                    persistentDataContainer[pluginKey(e), PersistentDataType.BYTE] = 1
                }
                flags(*ItemFlag.values())
            }
        }

        val allowedEnchantedMats = listOf(
            // mining
            "DIAMOND",
            "EMERALD",
            "LAPIS_LAZULI",
            "COAL",
            "REDSTONE",
            "COBBLESTONE",
            "IRON_INGOT",
            "GOLD_INGOT",
            "OBSIDIAN",
            "END_STONE",
            "NETHERITE_SCRAP",
            "QUARTZ",
            // foraging
            "OAK_LOG",
            "BIRCH_LOG",
            "SPRUCE_LOG",
            "ACACIA_LOG",
            "DARK_OAK_LOG",
            "JUNGLE_LOG",
            "WARPED_STEM",
            "CRIMSON_STEM",
            // farming
            "POTATO",
            "CARROT",
            "WHEAT",
            "RED_MUSHROOM",
            "BROWN_MUSHROOM",
            "CHORUS_FLOWER",
            "NETHER_WART",
            "BEETROOT",
            "SUGAR_CANE",
            "CACTUS",
            "MELON_SLICE",
            "PUMPKIN",
            "BAMBOO",
            "PORKCHOP",
            "BEEF",
            "CHICKEN",
            "RABBIT",
            "RABBIT_FOOT",
            "HONEYCOMB",
            // excavating
            "DIRT",
            "GRAVEL",
            "SAND",
            "CLAY_BALL",
            "SOUL_SAND",
            "SOUL_SOIL",
            // combat
            "ROTTEN_FLESH",
            "ENDER_PEARL",
            "STRING",
            "SPIDER_EYE",
            "PHANTOM_MEMBRANE",
            "GUNPOWDER",
            "GHAST_TEAR",
            "BONE",
            "SLIME_BALL",
            "MAGMA_CREAM",
            "BLAZE_POWDER",
            // fishing
            "COD",
            "SALMON",
            "SPONGE",
            "TROPICAL_FISH",
            "PRISMARINE_SHARD",
            "PRISMARINE_CRYSTALS",
            "INK_SAC",
            "PUFFERFISH",
            "KELP",
            "LILY_PAD",
            // more
            "NETHERITE_INGOT",
            "NETHERITE_BLOCK"
        )

        private fun initEnchanted() {
            val pool = Threading.pool()

            for (allowed in allowedEnchantedMats.parallelStream()) {
                pool.execute {
                    val mat = Material.valueOf(allowed)
                    val item = EnchantedItem(mat, rarityFromMaterial(mat).next())

                    ItemRegistry.register(Identifier.macro("enchanted_${allowed.lowercase()}"), item)
                }
            }

            pool.shutdown()
            val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            if (!success)
                throw IllegalStateException("Could not execute all tasks in the thread pool!")
        }

        fun init() {
            Ability.init()

            Threading.start {
                initEnchanted()
            }

            // using thread pools to not create a bottleneck
            val pool = Threading.pool()

            for (item in values().toList().parallelStream()) {
                pool.execute {
                    ItemRegistry.register(Identifier.macro(item.name.lowercase()), item.item)
                }
            }

            pool.shutdown()
            val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            if (!success)
                throw IllegalStateException("Could not execute all tasks in the thread pool!")
        }
    }
}
