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
import space.maxus.macrocosm.ability.types.armor.EntombedMaskAbility
import space.maxus.macrocosm.ability.types.armor.ReaperMaskAbility
import space.maxus.macrocosm.ability.types.armor.WardenHelmetAbility
import space.maxus.macrocosm.ability.types.item.*
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.generators.*
import space.maxus.macrocosm.item.buffs.BuffRegistry
import space.maxus.macrocosm.item.runes.*
import space.maxus.macrocosm.item.types.InfernalGreatsword
import space.maxus.macrocosm.item.types.WitherBlade
import space.maxus.macrocosm.reforge.ReforgeType
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.generic.id
import java.util.*

enum class ItemValue(val item: MacrocosmItem, private val model: Model? = null, private val animation: Animation? = null) {
    NULL(AbilityItem(ItemType.OTHER, "null", Rarity.COMMON, Material.PLAYER_HEAD, Statistics.zero())),

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

    ASPECT_OF_THE_DRAGONS(AbilityItem(ItemType.SWORD, "Aspect of the Dragons", Rarity.LEGENDARY, Material.DIAMOND_SWORD, stats {
        damage = 225f
        strength = 100f
        critChance = 15f
    }, mutableListOf(AOTDAbility), runeTypes = listOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.OFFENSIVE)))),

    ICE_SPRAY_WAND(AbilityItem(ItemType.WAND, "Ice Spray Wand", Rarity.RARE, Material.STICK, stats {
        damage = 120f
        intelligence = 300f
        abilityDamage = 5f
    }, mutableListOf(IceConeAbility), runeTypes = listOf(RuneSlot.COMBAT, RuneSlot.UTILITY))),

    ETERNAL_TERROR_WAND(
        AbilityItem(
            ItemType.WAND,
            "Eternal Terror Wand",
            Rarity.EPIC,
            Material.BLAZE_ROD,
            stats {
                damage = 80f
                intelligence = 250f
                health = 250f
                abilityDamage = 15f
            },
            mutableListOf(InfiniteTerrorAbility),
            runeTypes = listOf(RuneSlot.specific(RuneSpec.OFFENSIVE), RuneSlot.specific(RuneSpec.DEFENSIVE)),
            description = "It's morbin' time"
        ),
        Model(0, "item/blaze_rod", "macrocosm:item/eternal_terror_wand", "item/handheld"),
        Animation(10, 4, true)
    ),

    HYPERIONS_RING(AbilityItem(
        ItemType.OTHER,
        "Hyperion's Ring",
        Rarity.LEGENDARY,
        Material.GOLD_INGOT,
        stats {
            intelligence = 50f
            abilityDamage = 5f
        },
        mutableListOf(DeathDefyAbility),
        description = "Only works in off hand"
    ),
        RawModel(1, "item/gold_ingot", "macrocosm:item/hyperions_ring")
    ),

    YETI_SWORD(AbilityItem(ItemType.SWORD, "Yeti Sword", Rarity.LEGENDARY, Material.IRON_SWORD, stats {
        damage = 180f
        strength = 250f
        intelligence = 300f
    }, mutableListOf(TerrainTossAbility), runeTypes = listOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.DEFENSIVE), RuneSlot.specific(RuneSpec.DEFENSIVE)))),

    VOID_PRISM(AbilityItem(ItemType.SWORD, "Void Prism", Rarity.LEGENDARY, Material.PRISMARINE_SHARD, stats {
        damage = 150f
        strength = 80f
        intelligence = 500f
    }, mutableListOf(VoidPrismAbility), runeTypes = listOf(RuneSlot.COMBAT, RuneSlot.typeBound(StatRune.DIAMOND), RuneSlot.typeBound(StatRune.DIAMOND)))),

    RANCOROUS_STAFF(AbilityItem(ItemType.SWORD, "Rancorous Staff", Rarity.EPIC, Material.STICK, stats {
        damage = 80f
        strength = 70f
        intelligence = 150f
    }, mutableListOf(RancorousStaffAbility), runeTypes = listOf(RuneSlot.specific(RuneSpec.OFFENSIVE), RuneSlot.UTILITY))),

    // entity-limited items
    RADIOACTIVE_TRIDENT(AbilityItem(ItemType.SWORD, "Radioactive Trident", Rarity.LEGENDARY, Material.TRIDENT, stats {
        damage = 250f
        critChance = 100f
        critDamage = 250f
    }).apply { enchantUnsafe(space.maxus.macrocosm.enchants.Enchant.THUNDERBOLT.enchant, 7) }
    ),

    // shortbows
    THE_QUEENS_STINGER(Shortbow("The Queen's Stinger", Rarity.LEGENDARY, stats = stats {
        damage = 250f
        strength = 200f
        critDamage = 150f
        critChance = 25f
    }, extraAbilities = listOf(Ability.HONEYCOMB_BULWARK.ability), runes = listOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.OFFENSIVE)))),

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
    ),

    // admin items
    MAXUS_CAT_PLUSHIE(
        LimitedEditionItem(ItemType.OTHER, "Maxus' Cat Plushie", Rarity.SPECIAL, Material.PLAYER_HEAD, stats { intelligence = -1f }, metaModifier = {
            val skull = it as SkullMeta
            val profile = Bukkit.createProfile(UUID.randomUUID())
            profile.setProperty(ProfileProperty("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjM1MjUzYjBjOTZhZTRmYmNkZTY2OWJjYmE4ZWRjNDk5MTRmMmU5NTVmMDUyMjU5NTkxMWE3OTE5Yjk1OTdkMyJ9fX0"))
            skull.playerProfile = profile
        })
    ),

    // other stuff
    COLOSSAL_PILE_OF_WOOD(RecipeItem(Material.PLAYER_HEAD, Rarity.RARE, "Colossal Pile of Wood", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2QyY2UzYjI2M2E1ZjJjNzcxMzJlYmVmOWUxZDdjZTgyODg2ZmMzNWM4OTNmYjJhZjk3ZGY3OGU5NjFmYzQifX19", "How can you even carry that much?", true)),

    // slayer stiff

    //#region zombie

    //#region drops
    REVENANT_FLESH(RecipeItem(Material.ROTTEN_FLESH, Rarity.UNCOMMON, "Revenant Flesh", glow = true)),
    FOUL_FLESH(RecipeItem(Material.CHARCOAL, Rarity.RARE, "Foul Flesh", glow = true)),
    RANCID_FLESH(RecipeItem(Material.PLAYER_HEAD, Rarity.EPIC, "Rancid Flesh", description = "It has been decomposing for ages", headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFkN2M4MTZmYzhjNjM2ZDdmNTBhOTNhMGJhN2FhZWZmMDZjOTZhNTYxNjQ1ZTllYjFiZWYzOTE2NTVjNTMxIn19fQ==")),
    REVENANT_VISCERA(RecipeItem(Material.COOKED_PORKCHOP, Rarity.RARE, "Revenant Viscera", glow = true)),
    REVENANT_INNARDS(RecipeItem(Material.COOKED_SALMON, Rarity.EPIC, "Revenant Innards", glow = true, description = "Totally not tasty.")),

    REVENANT_CATALYST(RecipeItem(Material.PLAYER_HEAD, Rarity.EPIC, "Revenant Catalyst", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODc2NDA2MGI4MThlNGE2NzA2NzEzYzg1MWRmNTJhZmZiNzAwODM3MDE5Y2JkNzcxMjZlYTE1NGU3MGNkNjcxYyJ9fX0=", "Smells really weird...")),
    REAPER_CATALYST(RecipeItem(Material.PLAYER_HEAD, Rarity.EPIC, "Reaper Catalyst", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmU2Y2EzY2U1ZDg5YWNiMDZhNjQzZjQ5ZjUyNzQ4NTVlZGVlNmVlYjFhNTQ1MTViMzA0MGExYjNmOGNiYjdhMSJ9fX0=", "Some necromantic symbols are scrawled on it...")),
    BEHEADED_HORROR(RecipeItem(Material.PLAYER_HEAD, Rarity.EPIC, "Beheaded Horror", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGJhZDk5ZWQzYzgyMGI3OTc4MTkwYWQwOGE5MzRhNjhkZmE5MGQ5OTg2ODI1ZGExYzk3ZjZmMjFmNDlhZDYyNiJ9fX0=", "Makes for a nice trophy, or... you can wear it.")),
    DECAYING_BRAIN(RecipeItem(Material.PLAYER_HEAD, Rarity.LEGENDARY, "Decaying Brain", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjNmYTIxMTk5OWJkMzcxYjcwZTlmZjhkZTUyMDFlMTEzNDMwNGUzNDBiODEzZjVhZjE2MTE1ZWRkNjVhMmFjZCJ9fX0=", "Gathered straight from the Revenant's cranium, and preserved as it is.")),
    SCYTHE_BLADE(RecipeItem(Material.DIAMOND, Rarity.LEGENDARY, "Scythe Blade", description = "Did not became dull at all despite being in Revenant's tomb for centuries.")),
    WARDENS_HEART(RecipeItem(Material.PLAYER_HEAD, Rarity.LEGENDARY, "Warden's Heart", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjZkNzljMDI2ODc0Nzk0MWRmOWEyYTQ1MTAzY2JkNzMxZmRlZGNiYTU4OGY2NDNiNjcwZmQ3N2FhMmJkOTE4YyJ9fX0=", "The <gold>Legendary<dark_gray> Warden's Heart gave Revenant godly powers. Now it's yours.")),
    RAGING_ESSENCE(RecipeItem(Material.PLAYER_HEAD, Rarity.MYTHIC, "Raging Essence", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmIwNTVjODEwYmRkZmQxNjI2NGVjOGQ0MzljNDMyODNlMzViY2E3MWE1MDk4M2UxNWUzNjRjZDhhYjdjNjY4ZiJ9fX0=", "The <gold>Legendary<dark_gray> Raging Essence gave Entombed Revenant godly powers. Now it's yours.")),

    FORBIDDEN_SCROLLS(AbilityItem(ItemType.OTHER, "Forbidden Scrolls", Rarity.EPIC, Material.PAPER, stats {  }, mutableListOf(Ability.NECROMANTIC_RITUAL.ability))),
    //#endregion

    //#region weapons

    UNDEAD_SWORD(AbilityItem(ItemType.SWORD, "Undead Sword", Rarity.UNCOMMON, Material.STONE_SWORD, stats {
        damage = 50f
        strength = 50f
    }, mutableListOf(Ability.UNDEAD_SWORD.ability), runeTypes = listOf(RuneSlot.specific(RuneSpec.OFFENSIVE)))),

    REVENANT_FALCHION(AbilityItem(ItemType.SWORD, "Revenant Falchion", Rarity.RARE, Material.IRON_SWORD, stats {
        damage = 100f
        strength = 80f
    }, mutableListOf(Ability.REVENANT_FALCHION.ability, Ability.REVENANT_LIFE_STEAL.ability), runeTypes = listOf(RuneSlot.COMBAT))),

    REAPER_FALCHION(AbilityItem(ItemType.SWORD, "Reaper Falchion", Rarity.EPIC, Material.DIAMOND_SWORD, stats {
        damage = 200f
        strength = 120f
        ferocity = 10f
    }, mutableListOf(Ability.REAPER_FALCHION.ability, Ability.REAPER_LIFE_STEAL.ability), runeTypes = listOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.OFFENSIVE)))),

    REAPER_SCYTHE(AbilityItem(ItemType.SWORD, "Reaper Scythe", Rarity.LEGENDARY, Material.DIAMOND_HOE, stats {
        damage = 333f
        strength = 33f
        intelligence = 333f
    }, mutableListOf(Ability.REAPER_WEAPON.ability, ReaperScytheAbility), runeTypes = listOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.DEFENSIVE), RuneSlot.typeBound(StatRune.DIAMOND)))),

    AXE_OF_THE_SHREDDED(AbilityItem(ItemType.SWORD, "Axe of the Shredded", Rarity.LEGENDARY, Material.DIAMOND_AXE, stats {
        damage = 250f
        strength = 180f
        ferocity = 20f
    }, mutableListOf(Ability.REAPER_WEAPON.ability, AOTSAbility), runeTypes = listOf(RuneSlot.COMBAT, RuneSlot.COMBAT, RuneSlot.UTILITY))),
    //#endregion

    //#region wands

    WAND_OF_HEALING(AbilityItem(ItemType.WAND, "Wand of Healing", Rarity.UNCOMMON, Material.STICK, Statistics.zero(), mutableListOf(Ability.SMALL_HEAL.ability), runeTypes = listOf(RuneSlot.specific(RuneSpec.DEFENSIVE)))),
    WAND_OF_MENDING(AbilityItem(ItemType.WAND, "Wand of Mending", Rarity.RARE, Material.STICK, Statistics.zero(), mutableListOf(Ability.MEDIUM_HEAL.ability), runeTypes = listOf(RuneSlot.specific(RuneSpec.DEFENSIVE)))),
    WAND_OF_RESTORATION(AbilityItem(ItemType.WAND, "Wand of Restoration", Rarity.EPIC, Material.STICK, Statistics.zero(), mutableListOf(Ability.BIG_HEAL.ability), runeTypes = listOf(RuneSlot.specific(RuneSpec.DEFENSIVE), RuneSlot.typeBound(StatRune.DIAMOND)))),
    WAND_OF_ATONEMENT(AbilityItem(ItemType.WAND, "Wand of Atonement", Rarity.LEGENDARY, Material.STICK, Statistics.zero(), mutableListOf(Ability.HUGE_HEAL.ability), runeTypes = listOf(RuneSlot.specific(RuneSpec.DEFENSIVE), RuneSlot.typeBound(StatRune.DIAMOND), RuneSlot.UTILITY))),

    //#endregion wands

    //#region stuff
    REAPER_GEM(ReforgeStone(ReforgeType.BLOOD_SOAKED.ref, "Reaper Gem", Rarity.LEGENDARY, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjMxNzUyODZjZDNiYTFhM2E5YzkwODI5NzdkMDlkZDM3YjE3N2FiZjM3YTQ2NjU4MGMyN2QxZGVlNzJiM2MxOCJ9fX0=")),

    VOODOO_DOLL(AbilityItem(ItemType.OTHER, "Voodoo Doll", Rarity.RARE, Material.PUFFERFISH, Statistics.zero(), mutableListOf(VoodooDollAbility), description = "Who do you voodoo?")),

    //#endregion

    //#region armor
    UNDEAD_HEART(SkullAbilityItem(
        ItemType.HELMET,
        "Undead Heart",
        Rarity.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTVlYjI1ZDZjNWE5OGM5YWRhMjE5NDE0Zjg1YzAzYzU4MDY1YmMzMzJkNGY0YzE1MWFjYWJmZDJmMjhhNTlmMCJ9fX0=",
        stats {
            health = 200f
            defense = 50f
        },
        mutableListOf(Ability.NEGATE.ability),
        runeTypes = listOf(RuneSlot.specific(RuneSpec.DEFENSIVE))
    )),

    CRYSTALLIZED_HEART(SkullAbilityItem(
        ItemType.HELMET,
        "Crystallized Heart",
        Rarity.RARE,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTY0ZjI1Y2ZmZjc1NGYyODdhOTgzOGQ4ZWZlMDM5OTgwNzNjMjJkZjdhOWQzMDI1YzQyNWUzZWQ3ZmY1MmMyMCJ9fX0=",
        stats {
            health = 250f
            defense = 120f
        },
        mutableListOf(Ability.VITIATE.ability),
        runeTypes = listOf(RuneSlot.specific(RuneSpec.DEFENSIVE), RuneSlot.specific(RuneSpec.DEFENSIVE))
    )),

    REVIVED_HEART(SkullAbilityItem(
        ItemType.HELMET,
        "Revived Heart",
        Rarity.EPIC,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTg1MTRkODIzMGI3NTUxMWE1YTVhNjljYTkzZGNiMmQzZTdjZDFhMjhjNDhkYzM4MDg3ZjE1OGQyODNiN2ZhNyJ9fX0=",
        stats {
            health = 350f
            defense = 150f
            intelligence = 50f
        },
        mutableListOf(Ability.BELIE.ability),
        runeTypes = listOf(RuneSlot.specific(RuneSpec.DEFENSIVE), RuneSlot.specific(RuneSpec.UTILITY), RuneSlot.specific(RuneSpec.OFFENSIVE)),
        description = "Gross! Why would anyone ever wear this!"
    )),

    WARDENS_HELMET(SkullAbilityItem(
        ItemType.HELMET,
        "Warden's Helmet",
        Rarity.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlYjBiZDg1YWFkZGYwZDI5ZWQwODJlYWMwM2ZjYWRlNDNkMGVlODAzYjBlODE2MmFkZDI4YTYzNzlmYjU0ZSJ9fX0=",
        stats {
            health = 250f
            defense = 300f
            strength = 50f
        },
        mutableListOf(WardenHelmetAbility),
        runeTypes = listOf(
            RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.OFFENSIVE), RuneSlot.UTILITY
        )
    )),

    REAPER_MASK(SkullAbilityItem(
        ItemType.HELMET,
        "Reaper Mask",
        Rarity.EPIC,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDhiZWUyM2I1YzcyNmFlOGUzZDAyMWU4YjRmNzUyNTYxOWFiMTAyYTRlMDRiZTk4M2I2MTQxNDM0OWFhYWM2NyJ9fX0=",
        stats {
            health = 150f
            defense = 100f
            intelligence = 50f
            strength = 10f
        },
        mutableListOf(ReaperMaskAbility),
        runeTypes = listOf(
            RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.DEFENSIVE)
        )
    )),

    ENTOMBED_MASK(SkullAbilityItem(
        ItemType.HELMET,
        "Entombed Mask",
        Rarity.LEGENDARY,
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmUyNGZkOWM2NDAwNTc3N2M3ODZmZTMxYWY2NWM1NzJlMzBkYTc5Njk4YTllZDIxMWFmMTQ3YzY0ZjcyYmE1ZCJ9fX0=",
        stats {
            health = 300f
            defense = 400f
            intelligence = 150f
            strength = 50f
        },
        mutableListOf(EntombedMaskAbility),
        runeTypes = listOf(
            RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.DEFENSIVE), RuneSlot.specific(RuneSpec.DEFENSIVE)
        )
    )),
    //#endregion armor

    //#endregion

    //#region transmutation
    SAPPHIRE_SHARD(
        RecipeItem(Material.AMETHYST_SHARD, Rarity.EPIC, "Sapphire Shard", description = "It shines with sorrow", glow = true),
        Model(2, "item/amethyst_shard", "macrocosm:item/sapphire_shard")
    ),

    PRISMATIC_SHARD(
        RecipeItem(Material.AMETHYST_SHARD, Rarity.LEGENDARY, "Prismatic Shard", description = "It is said that this gemstone can produce solar energy out of nothing."),
        Model(3, "item/amethyst_shard", "macrocosm:item/prismatic_shard"),
        Animation(11)
    ),

    // indev
    ENCHANTED_ADAMANTITE(RecipeItem(Material.REDSTONE, Rarity.EPIC, "Enchanted Adamantite", description = "Dwarves thought this metal was a myth...", glow = true)),

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
                val profile = Bukkit.createProfile(UUID.randomUUID())
                profile.setProperty(ProfileProperty("textures", skin))
                this.playerProfile = profile

                lore(description.reduceToList().map { text(it) })
            }
        }

        fun placeholderHeadDesc(skin: String, name: String, vararg description: String) = itemStack(Material.PLAYER_HEAD) {
            meta<SkullMeta> {
                displayName(text(name).noitalic())
                persistentDataContainer[pluginKey("placeholder"), PersistentDataType.BYTE] = 1
                val reduced = description.map { s -> text("<gray>$s").noitalic() }.toMutableList()
                lore(reduced)
                flags(*ItemFlag.values())

                val profile = Bukkit.createProfile(UUID.randomUUID())
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
            "HAY_BLOCK",
            "RED_MUSHROOM",
            "BROWN_MUSHROOM",
            "RED_MUSHROOM_BLOCK",
            "BROWN_MUSHROOM_BLOCK",
            "CHORUS_FLOWER",
            "NETHER_WART",
            "HONEY_BLOCK",
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
            "SLIME_BLOCK",
            "BONE_BLOCK",
            "MAGMA_CREAM",
            "MAGMA_BLOCK",
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
                    for(rarity in runeQualities) {
                        val item = RuneItem(allowed, "${allowed.display} <${allowed.color.asHexString()}>${rarityToRuneTier(rarity)} $baseName Rune", rarity, allowed.headSkin)

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
                        if(model != null) {
                            Registry.MODEL_PREDICATES.register(id, model)
                            if(animation != null) {
                                MetaGenerator.enqueue("assets/macrocosm/textures/${model.to.replace("macrocosm:", "")}.png", AnimationData(animation))
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
