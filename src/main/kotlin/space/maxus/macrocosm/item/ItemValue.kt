package space.maxus.macrocosm.item

import com.destroystokyo.paper.profile.ProfileProperty
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.items.flags
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.ability.Ability
import space.maxus.macrocosm.ability.types.item.InstantTransmission
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.item.runes.VanillaRune
import space.maxus.macrocosm.item.types.WitherBlade
import space.maxus.macrocosm.reforge.ReforgeType
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.id
import java.util.*
import java.util.concurrent.TimeUnit

enum class ItemValue(val item: MacrocosmItem) {
    ENCHANTED_BOOK(EnchantedBook()),

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

    EMERALD_PICKAXE(AbilityItem(ItemType.PICKAXE, "Emerald Pickaxe", Rarity.RARE, Material.DIAMOND_PICKAXE, stats {
        damage = 30f
        defense = 50f
        miningSpeed = 450f
        miningFortune = 150f
    }, mutableListOf(Ability.EMERALD_AFFECTION_PICKAXE.ability), breakingPower = 6)),

    TEST_DRILL(AbilityItem(ItemType.DRILL, "Test Drill", Rarity.SPECIAL, Material.PRISMARINE_SHARD, stats {
        damage = 50f
        defense = 150f
        miningSpeed = 1000f
        miningFortune = 300f
    }, breakingPower = 8)),

    // shortbows
    THE_QUEENS_STINGER(Shortbow("The Queen's Stinger", Rarity.LEGENDARY, stats = stats {
        damage = 250f
        strength = 200f
        critDamage = 150f
        critChance = 25f
    }, extraAbilities = listOf(Ability.HONEYCOMB_BULWARK.ability), runes = listOf(VanillaRune.REDSTONE, VanillaRune.EMERALD))),

    // reforge stones
    WITHER_BLOOD(
        ReforgeStone(
            ReforgeType.WITHERED.ref,
            "Wither Blood",
            Rarity.EPIC,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGExZjMwYWM2N2VjMTBhM2Q1ZTdkZWFmNjRkMGU5YTIwMDRhZTgwMWMwYmQ2Y2U4NWM1ZDYwNzM5YTlkODgyYSJ9fX0="
        )
    ),
    ELDER_SILK(
        ReforgeStone(
            ReforgeType.SILKY.ref,
            "Elder Silk",
            Rarity.RARE,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjc0MTc5MjQ1NjY3ZTYxNjY5Y2MxNDFhM2RjNDZkMjU5ZjE1OWEyOGVhMWI0NjkzNjQyZDlhMzEyNmRhOTU3ZCJ9fX0="
        )
    ),
    DRAGON_CLAW(
        ReforgeStone(
            ReforgeType.FABLED.ref,
            "Dragon Claw",
            Rarity.RARE,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmI3ZjlmNDg3MjZlNTI1YjBkOWEwODY4MTc4YzMyMzM0NzRlZTRlZDNkYTNmNzYxOTg1NzQ0OWQ0MWEwYzYzYSJ9fX0="
        )
    ),

    PERFECT_PRISM(
        ReforgeStone(
            ReforgeType.REFRACTING.ref,
            "Perfect Prism",
            Rarity.LEGENDARY,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWE4NjY1MmY5ZmFiNTNkZjM5NjdlZjExMGY1NzAxMTY2NmNmODBkMjNlMGM5OGQ0YTllYTZjODcwMWJhNTBkZSJ9fX0="
        )
    ),
    ROTTING_FLESH(
        ReforgeStone(
            ReforgeType.NECROTIC.ref,
            "Rotting Flesh",
            Rarity.RARE,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc1NjkyMmMyMWJjMTUxOGJmNjAzNTM1MDkzMGMzYjFmNGM5Yjc0YjEwYjBkNjIxYmFiYWE0ZTQxMTA5YzE3ZiJ9fX0="
        )
    ),
    HARNESSED_JEWEL(
        ReforgeStone(
            ReforgeType.ORNATE.ref,
            "Harnessed Jewel",
            Rarity.EPIC,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWIzNGI0ODk2ZTQzNGVjNGVmMTY2OWQ2MzQzYjZkYTA2Y2Q4MzBkYzkyOTI3ZDYxZTNkODgzMDE3NjgzYzQyMiJ9fX0="
        )
    ),
    AZURE_GEODE(
        ReforgeStone(
            ReforgeType.UNDULANT.ref,
            "Azure Geode",
            Rarity.LEGENDARY,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzhiZjcyOWEzN2M3NDcyNTk5YjA0YWViMmNkMTkwNGNkMzZiN2E1Y2I0NDNiZTlkMmZmNjM2MGE3NWZiZTE3YSJ9fX0="
        )
    ),
    CRIMSON_GEODE(
        ReforgeStone(
            ReforgeType.RELENTLESS.ref,
            "Crimson Geode",
            Rarity.LEGENDARY,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGVjNjVmYzg3YzUwNDQxNWVmZjczMGEyZjRmZTdkMDZkMmYxMTZlYTJhMzEzNDgxZjM2MmQwYTI1ZDY1ZTUwMCJ9fX0="
        )
    ),
    DRAGON_HORN(
        ReforgeStone(
            ReforgeType.RENOWNED.ref,
            "Dragon Horn",
            Rarity.EPIC,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODc0NGM4MTA5OWMxYWViZjIwZjY3N2ZhZWQyNGNhY2U1MjBhMjk0Y2Y0NmJkZWI2YTI1N2Y0MzZhMzIzYTFkOCJ9fX0="
        )
    ),
    SEAFOAM_CAKE(
        ReforgeStone(
            ReforgeType.FOAMY.ref,
            "Seafoam Cake",
            Rarity.EPIC,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2MzMGIzMDc4ZmFiYmRkZDAxNWJjYmI1OTU0Y2YyZmJhM2FmZDEzNjM0NmMwYzAwZTgwODIyZjlmNTc2NjNlOSJ9fX0="
        )
    ),
    SEA_PICKLES(
        ReforgeStone(
            ReforgeType.ABUNDANT.ref,
            "Sea Pickles",
            Rarity.EPIC,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWFlZjVlNGViOTU4OWI1ZjQ4NmRhMDU0ZWMzNjY0NjEzYTQ5MTBlM2UyZjBmNjNlY2U1OTg1MTIwYjQxMzUzMCJ9fX0="
        )
    ),
    BURIED_TREASURE(
        ReforgeStone(
            ReforgeType.MOURNING.ref,
            "Buried Treasure",
            Rarity.LEGENDARY,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjc0ZDEzYjUxMDE2OGM3YWNiNDRiNjQ0MTY4NmFkN2FiMWNiNWI3NDg4ZThjZGY5ZDViMjJiNDdjNDgzZjIzIn19fQ=="
        )
    ),
    SOUL_OF_THE_SEA(
        ReforgeStone(
            ReforgeType.SEABORN.ref,
            "Soul of the Sea",
            Rarity.LEGENDARY,
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTExM2VlNjEwODQxZGVkMjE1YWNkMmI0Y2FhZWVmODdkZmQ2ZTNkNDc2OGU3YWI0ZTE5ZWI3NmIzZDgxMjFjZiJ9fX0="
        )
    ),

    COLORED_LEATHER_HELMET(
        ColoredEntityArmor(
            Material.LEATHER_HELMET,
            0
        )
    ),
    COLORED_LEATHER_CHESTPLATE(
        ColoredEntityArmor(
            Material.LEATHER_CHESTPLATE,
            0
        )
    ),
    COLORED_LEATHER_LEGGINGS(
        ColoredEntityArmor(
            Material.LEATHER_LEGGINGS,
            0
        )
    ),
    COLORED_LEATHER_BOOTS(
        ColoredEntityArmor(
            Material.LEATHER_BOOTS,
            0
        )
    ),
    SKULL_ENTITY_HEAD(
        SkullEntityHead("null")
    )

    ;

    companion object {
        fun enchanted(type: Material) = Registry.ITEM.find(Identifier.macro("enchanted_${type.name.lowercase()}"))
        fun placeholder(type: Material) = itemStack(type) {
            meta {
                displayName(Component.empty())
                persistentDataContainer[pluginKey("placeholder"), PersistentDataType.BYTE] = 1
                flags(*ItemFlag.values())
            }
        }

        fun placeholderHead(skin: String, name: String, description: String) = itemStack(Material.PLAYER_HEAD) {
            meta<SkullMeta> {
                displayName(comp(name))
                persistentDataContainer[pluginKey("placeholder"), PersistentDataType.BYTE] = 1
                flags(*ItemFlag.values())
                val profile = Bukkit.createProfile(UUID.randomUUID())
                profile.setProperty(ProfileProperty("textures", skin))
                this.playerProfile = profile

                lore(description.reduceToList().map { comp(it) })
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
            // preventing a huge memory leak
            val pool = Threading.newFixedPool(12)

            for (allowed in allowedEnchantedMats.parallelStream()) {
                pool.execute {
                    val mat = Material.valueOf(allowed)
                    val item = EnchantedItem(mat, rarityFromMaterial(mat).next())

                    Registry.ITEM.register(Identifier.macro("enchanted_${allowed.lowercase()}"), item)
                }
            }

            pool.shutdown()
            val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            if (!success)
                throw IllegalStateException("Could not execute all tasks in the thread pool!")
        }

        fun init() {
            Ability.init()

            Threading.runAsync("Enchanted Item Generator", true) {
                info("Initializing enchanted items...")
                initEnchanted()
            }

            Registry.ITEM.delegateRegistration(values().map { id(it.name.lowercase()) to it.item }) { _, _ -> }
        }
    }
}
