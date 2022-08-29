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
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.Ability
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.generators.Animation
import space.maxus.macrocosm.generators.AnimationData
import space.maxus.macrocosm.generators.MetaGenerator
import space.maxus.macrocosm.generators.Model
import space.maxus.macrocosm.item.buffs.BuffRegistry
import space.maxus.macrocosm.item.runes.RuneItem
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.item.runes.RuneSpec
import space.maxus.macrocosm.item.runes.rarityToRuneTier
import space.maxus.macrocosm.item.types.InfernalGreatsword
import space.maxus.macrocosm.item.types.WitherBlade
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.id

enum class ItemValue(
    val item: MacrocosmItem,
    private val model: Model? = null,
    private val animation: Animation? = null
) {
    NULL(AbilityItem(ItemType.OTHER, "null", Rarity.COMMON, Material.PLAYER_HEAD, Statistics.zero())),

    ENCHANTED_BOOK(EnchantedBook()),

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

    // entity-limited items
    RADIOACTIVE_TRIDENT(AbilityItem(
        ItemType.SWORD,
        "Radioactive Trident",
        Rarity.LEGENDARY,
        Material.TRIDENT,
        stats {
            damage = 250f
            critChance = 100f
            critDamage = 250f
        }
    ).apply { enchantUnsafe(space.maxus.macrocosm.enchants.Enchant.THUNDERBOLT.enchant, 7) }
    ),

    // shortbows
    THE_QUEENS_STINGER(
        Shortbow(
            "The Queen's Stinger",
            Rarity.LEGENDARY,
            stats = stats {
                damage = 250f
                strength = 200f
                critDamage = 150f
                critChance = 25f
            },
            extraAbilities = listOf(Ability.HONEYCOMB_BULWARK.ability),
            runes = listOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.OFFENSIVE))
        )
    ),

    // entity equipment
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
    ),

    // admin items
    MAXUS_CAT_PLUSHIE(
        LimitedEditionItem(
            ItemType.OTHER,
            "Maxus' Cat Plushie",
            Rarity.SPECIAL,
            Material.PLAYER_HEAD,
            stats { intelligence = -1f },
            metaModifier = {
                val skull = it as SkullMeta
                val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
                profile.setProperty(
                    ProfileProperty(
                        "textures",
                        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjM1MjUzYjBjOTZhZTRmYmNkZTY2OWJjYmE4ZWRjNDk5MTRmMmU5NTVmMDUyMjU5NTkxMWE3OTE5Yjk1OTdkMyJ9fX0"
                    )
                )
                skull.playerProfile = profile
            })
    ),

    SPELL_SCROLL(SpellScroll()),

    // indev
    ENCHANTED_ADAMANTITE(
        RecipeItem(
            Material.REDSTONE,
            Rarity.EPIC,
            "Enchanted Adamantite",
            description = "Dwarves thought this metal was a myth...",
            glow = true
        )
    ),

    INFERNAL_GREATSWORD(InfernalGreatsword()),
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
                displayName(text(name))
                persistentDataContainer[pluginKey("placeholder"), PersistentDataType.BYTE] = 1
                flags(*ItemFlag.values())
                val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
                profile.setProperty(ProfileProperty("textures", skin))
                this.playerProfile = profile

                lore(description.reduceToList().map { text(it) })
            }
        }

        fun placeholderHeadDesc(skin: String, name: String, vararg description: String) =
            itemStack(Material.PLAYER_HEAD) {
                meta<SkullMeta> {
                    displayName(text(name).noitalic())
                    persistentDataContainer[pluginKey("placeholder"), PersistentDataType.BYTE] = 1
                    val reduced = description.map { s -> text("<gray>$s").noitalic() }.toMutableList()
                    lore(reduced)
                    flags(*ItemFlag.values())

                    val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
                    profile.setProperty(ProfileProperty("textures", skin))
                    this.playerProfile = profile
                }
            }

        fun placeholderDescripted(type: Material, name: String, vararg description: String) = itemStack(type) {
            meta {
                displayName(text(name).noitalic())
                persistentDataContainer[pluginKey("placeholder"), PersistentDataType.BYTE] = 1
                val reduced = description.map { s -> text("<gray>$s").noitalic() }.toMutableList()
                lore(reduced)
                flags(*ItemFlag.values())
            }
        }

        fun placeholder(type: Material, name: String, vararg extra: String) = itemStack(type) {
            meta {
                displayName(text(name).noitalic())
                persistentDataContainer[pluginKey("placeholder"), PersistentDataType.BYTE] = 1
                for (e in extra) {
                    persistentDataContainer[pluginKey(e), PersistentDataType.BYTE] = 1
                }
                flags(*ItemFlag.values())
            }
        }

        val enchantedItem = listOf(
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
            "QUARTZ_BLOCK",
            "NETHERITE_INGOT",
            "NETHERITE_BLOCK",
            "DIAMOND_BLOCK",
            "EMERALD_BLOCK",
            "LAPIS_BLOCK",
            "COAL_BLOCK",
            "REDSTONE_BLOCK",
            "IRON_BLOCK",
            "GOLD_BLOCK",
            "GLOWSTONE_DUST",
            "GLOWSTONE",
            "ICE",
            "PACKED_ICE",
            "BLACKSTONE",
            "DEEPSLATE",
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
            "BAKED_POTATO",
            "CARROT",
            "GOLDEN_CARROT",
            "WHEAT",
            "HAY_BLOCK",
            "RED_MUSHROOM",
            "BROWN_MUSHROOM",
            "RED_MUSHROOM_BLOCK",
            "BROWN_MUSHROOM_BLOCK",
            "CHORUS_FRUIT",
            "CHORUS_FLOWER",
            "NETHER_WART",
            "NETHER_WART_BLOCK",
            "HONEY_BLOCK",
            "BEETROOT",
            "SUGAR_CANE",
            "SUGAR",
            "CACTUS",
            "GREEN_DYE",
            "COCOA_BEANS",
            "COOKIE",
            "MELON_SLICE",
            "PUMPKIN",
            "BAMBOO",
            "PORKCHOP",
            "COOKED_PORKCHOP",
            "BEEF",
            "COOKED_BEEF",
            "CHICKEN",
            "COOKED_CHICKEN",
            "EGG",
            "RABBIT",
            "COOKED_RABBIT",
            "RABBIT_FOOT",
            "RABBIT_HIDE",
            "HONEYCOMB",
            "HONEYCOMB_BLOCK",
            // excavating
            "DIRT",
            "GRAVEL",
            "FLINT",
            "SAND",
            "RED_SAND",
            "SCULK",
            "CLAY_BALL",
            "SOUL_SAND",
            "SNOW_BLOCK",
            "SNOWBALL",
            // combat
            "ROTTEN_FLESH",
            "ENDER_PEARL",
            "ENDER_EYE",
            "STRING",
            "SPIDER_EYE",
            "FERMENTED_SPIDER_EYE",
            "PHANTOM_MEMBRANE",
            "GUNPOWDER",
            "GHAST_TEAR",
            "BONE",
            "SLIME_BALL",
            "SLIME_BLOCK",
            "BONE_BLOCK",
            "MAGMA_CREAM",
            "MAGMA_BLOCK",
            "BLAZE_POWDER",
            "BLAZE_ROD",
            // fishing
            "COD",
            "COOKED_COD",
            "SALMON",
            "COOKED_SALMON",
            "SPONGE",
            "WET_SPONGE",
            "TROPICAL_FISH",
            "PRISMARINE_SHARD",
            "PRISMARINE_CRYSTALS",
            "PRISMARINE",
            "DARK_PRISMARINE",
            "INK_SAC",
            "PUFFERFISH",
            "KELP",
            "DRIED_KELP",
            "DRIED_KELP_BLOCK",
            "LILY_PAD",
        )

        private val runeQualities = listOf(
            Rarity.COMMON,
            Rarity.UNCOMMON,
            Rarity.RARE,
            Rarity.EPIC,
            Rarity.LEGENDARY
        )

        private fun initRunes() {
            val pool = Threading.newFixedPool(5)

            for ((id, allowed) in BuffRegistry.runes) {
                pool.execute {
                    val baseName = id.path.replace("_", " ").capitalized()
                    for (rarity in runeQualities) {
                        val item = RuneItem(
                            allowed,
                            "${allowed.display} <${allowed.color.asHexString()}>${rarityToRuneTier(rarity)} $baseName Rune",
                            rarity,
                            allowed.headSkin
                        )

                        Registry.ITEM.register(item.id, item)
                    }
                }
            }

            pool.shutdown()
        }

        private fun initEnchanted() {
            // preventing a huge memory leak
            val pool = Threading.newFixedPool(12)

            for (allowed in enchantedItem.parallelStream()) {
                pool.execute {
                    val mat = Material.valueOf(allowed)
                    val item = EnchantedItem(mat, rarityFromMaterial(mat).next())

                    Registry.ITEM.register(Identifier.macro("enchanted_${allowed.lowercase()}"), item)
                }
            }

            pool.shutdown()
        }

        fun init() {
            Ability.init()

            Threading.runAsync("Enchanted Item Generator", true) {
                info("Initializing enchanted items...")
                initEnchanted()
            }

            Threading.runAsync("Rune Generator", true) {
                info("Initializing runes...")
                initRunes()
            }

            Threading.runAsync("macrocosm:item Delegate", true) {
                this.info("Starting macrocosm:item registry Delegate")
                val pool = Threading.newFixedPool(8)

                for (value in ItemValue.values()) {
                    pool.execute {
                        val (item, model, animation) = Triple(value.item, value.model, value.animation)
                        val id = id(value.name.lowercase())
                        Registry.ITEM.register(id(value.name.lowercase()), item)
                        if (model != null) {
                            Registry.MODEL_PREDICATES.register(id, model)
                            if (animation != null) {
                                MetaGenerator.enqueue(
                                    "assets/macrocosm/textures/${
                                        model.to.replace(
                                            "macrocosm:",
                                            ""
                                        )
                                    }.png", AnimationData(animation)
                                )
                            }
                        }
                    }
                }

                pool.shutdown()
                this.info("Successfully registered ${ItemValue.values().size} items in delegate.")
            }
        }
    }
}
