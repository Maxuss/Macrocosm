package space.maxus.macrocosm.item

import org.bukkit.inventory.meta.LeatherArmorMeta
import space.maxus.macrocosm.ability.Ability
import space.maxus.macrocosm.ability.types.armor.*
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.generators.CMDGenerator
import space.maxus.macrocosm.generators.Model
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.item.runes.RuneSpec
import space.maxus.macrocosm.item.runes.StatRune
import space.maxus.macrocosm.item.types.DragonArmor
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.util.general.id
import java.util.concurrent.TimeUnit

object Armor {
    private val cache: MutableList<Pair<ArmorItem, Model?>> = mutableListOf()

    val EMERALD_ARMOR = register(ArmorItem("Emerald", "emerald", "LEATHER", Rarity.RARE, stats {
        health = 100f
        miningFortune = 30f
        defense = 30f
        strength = 10f
    }, abilities = listOf(EmeraldArmorBonus), commonMeta = {
        val leather = it as LeatherArmorMeta
        leather.setColor(org.bukkit.Color.fromRGB(0x0FBB65))
    }, runes = listOf(RuneSlot.typeBound(StatRune.EMERALD), RuneSlot.UTILITY)))

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
    }, runes = listOf(RuneSlot.typeBound(StatRune.AMETHYST), RuneSlot.GATHERING)))

    val BEEKEEPER_ARMOR = register(
        ArmorItem(
            "Beekeeper", "beekeeper", "LEATHER", Rarity.EPIC, stats {
                health = 100f
                defense = 35f
                speed = 15f
                strength = 8f
                critDamage = 5f
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
            runes = listOf(RuneSlot.COMBAT, RuneSlot.UTILITY)
        )
    )

    val SUPERIOR_DRAGON_ARMOR = register(
        DragonArmor(
            "Superior",
            "superior_dragon",
            stats {
                health = 150f
                defense = 190f
                speed = 3f
                critChance = 2f
                strength = 15f
                intelligence = 25f
                critDamage = 15f
            },
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzU1OGVmYmU2Njk3NjA5OWNmZDYyNzYwZDllMDUxNzBkMmJiOGY1MWU2ODgyOWFiOGEwNTFjNDhjYmM0MTVjYiJ9fX0=",
            0xF2DF11,
            0xF2DF11,
            0xF25D18,
            abilities = mutableListOf(SuperiorDragonBonus),
            applicableRuns = mutableListOf(RuneSlot.COMBAT, RuneSlot.GATHERING, RuneSlot.UTILITY),
        )
    )

    val STRONG_DRAGON_ARMOR = register(
        DragonArmor(
            "Strong",
            "strong_dragon",
            stats {
                strength = 25f
                health = 120f
                defense = 160f
            },
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWZkZTA5NjAzYjAyMjViOWQyNGE3M2EwZDNmM2UzYWYyOTA1OGQ0NDhjY2Q3Y2U1YzY3Y2QwMmZhYjBmZjY4MiJ9fX0=",
            0xD91E41,
            0xE09419,
            0xF0D124,
            abilities = mutableListOf(StrongDragonBonus),
            applicableRuns = mutableListOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.OFFENSIVE), RuneSlot.UTILITY),
        )
    )

    val OLD_DRAGON_ARMOR = register(
        DragonArmor(
            "Old",
            "old_dragon",
            stats {
                health = 200f
                defense = 160f
            },
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTllOWU1NjAwNDEwYzFkMDI1NDQ3NGE4MWZlY2ZiMzg4NWMxY2Y2ZjUwNDE5MGQ4NTZmMGVjN2M5ZjA1NWM0MiJ9fX0=",
            0xF0E6AA,
            0xF0E6AA,
            0xF0E6AA,
            abilities = mutableListOf(OldDragonBonus),
            applicableRuns = mutableListOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.DEFENSIVE), RuneSlot.UTILITY),
        )
    )

    val YOUNG_DRAGON_ARMOR = register(
        DragonArmor(
            "Young",
            "young_dragon",
            stats {
                health = 120f
                defense = 160f
                speed = 20f
                attackSpeed = 10f
            },
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWM0ODZhZjNiODgyNzY2ZTgyYTBiYzE2NjVmZjAyZWI2ZTg3M2I2ZTBkNzcxZjNhZGFiYzc1OWI3MjAyMjZhIn19fQ==",
            0xDDE4F0,
            0xDDE4F0,
            0xCCFD6B,
            abilities = mutableListOf(YoungDragonBonus),
            applicableRuns = mutableListOf(RuneSlot.COMBAT, RuneSlot.UTILITY, RuneSlot.UTILITY)
        )
    )

    val UNSTABLE_DRAGON_ARMOR = register(
        DragonArmor(
            "Unstable",
            "unstable_dragon",
            stats {
                health = 120f
                defense = 160f
                critChance = 5f
                critDamage = 15f
            },
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjkyMmI1ZjhkNTU0Y2E5MjNmOTY4MzJhNWE0ZTkxNjliYzJjZGIzNjBhMmIwNmViZWMwOWI2YTZhZjQ2MThlMyJ9fX0=",
            0xB212E3,
            0xB212E3,
            0x2C013A,
            abilities = mutableListOf(UnstableDragonBonus),
            applicableRuns = mutableListOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.OFFENSIVE), RuneSlot.UTILITY)
        )
    )

    val PROTECTOR_DRAGON_ARMOR = register(
        DragonArmor(
            "Protector",
            "protector_dragon",
            stats {
                health = 100f
                defense = 250f
                trueDefense = 25f
            },
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjM3YTU5NmNkYzRiMTFhOTk0OGZmYTM4YzJhYTNjNjk0MmVmNDQ5ZWIwYTM5ODIyODFkM2E1YjVhMTRlZjZhZSJ9fX0=",
            0x99978B,
            0x99978B,
            0x99978B,
            abilities = mutableListOf(ProtectorDragonBonus),
            applicableRuns = mutableListOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.DEFENSIVE), RuneSlot.UTILITY)
        )
    )

    val WISE_DRAGON_ARMOR = register(
        DragonArmor(
            "Wise",
            "wise_dragon",
            stats {
                health = 120f
                defense = 160f
                intelligence = 150f
                abilityDamage = 8f
            },
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWEyOTg0Y2YwN2M0OGRhOTcyNDgxNmE4ZmYwODY0YmM2OGJjZTY5NGNlOGJkNmRiMjExMmI2YmEwMzEwNzBkZSJ9fX0=",
            0x29F0E9,
            0xACF7F4,
            0x29F0E9,
            abilities = mutableListOf(WiseDragonBonus),
            applicableRuns = mutableListOf(RuneSlot.COMBAT, RuneSlot.typeBound(StatRune.DIAMOND), RuneSlot.UTILITY)
        )
    )

    val MASTER_NECROMANCER_ARMOR = register(
        ArmorItem(
            "Master Necromancer",
            "master_necromancer",
            "LEATHER",
            Rarity.EPIC,
            stats {
                health = 200f
                defense = 250f
                intelligence = 50f
                strength = 10f
                summoningPower = 1
            },
            abilities = listOf(MasterNecromancerBonus),
            runes = listOf(RuneSlot.COMBAT, RuneSlot.UTILITY),
            chestMeta = colorMeta(0x550F73),
            legsMeta = colorMeta(0x3E0158),
            bootMeta = colorMeta(0x1E002B),
            headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTcxNThlNTc2NzFiZWNlNTAxYmRlZjU2MzExYzBlYTYzZTE5NDYxYTg0YzViZDJiZjk0N2RhYjg0YTI0ZWVjZSJ9fX0="
        )
    )

    val REVENANT_ARMOR = register(object : KillCountingArmor(
        "Revenant",
        "revenant",
        "DIAMOND",
        Rarity.EPIC,
        stats {
            health = 120f
            defense = 80f
            strength = 10f
            intelligence = 20f
        },
        abilities = listOf(Ability.REVENANT_ARMOR_BONUS.ability, Ability.ROTTEN_HEART_T1.ability),
        runes = listOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.DEFENSIVE))
    ), ThreePieceArmor {})

    val REAPER_ARMOR = register(object : KillCountingArmor(
        "Reaper",
        "reaper",
        "LEATHER",
        Rarity.LEGENDARY,
        stats {
            health = 250f
            defense = 150f
            strength = 40f
            intelligence = 50f
        },
        abilities = listOf(
            Ability.REAPER_ARMOR_BONUS.ability,
            Ability.GHOUL_BUSTER_ARMOR_BONUS.ability,
            Ability.ROTTEN_HEART_T2.ability
        ),
        runes = listOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.DEFENSIVE), RuneSlot.specific(RuneSpec.DEFENSIVE)),
        chestMeta = colorMeta(0x1B1B1B),
        legsMeta = colorMeta(0x1B1B1B),
        bootMeta = colorMeta(0x272727)
    ), ThreePieceArmor {})

    val HEAT_RESISTANT_ARMOR = register(object : KillCountingArmor(
        "Heat Resistant",
        "heat_resistant",
        "LEATHER",
        Rarity.RARE,
        stats {
            health = 50f
            defense = 100f
            strength = 15f
            ferocity = 5f
        },
        abilities = listOf(Ability.HEAT_RESISTANT_ARMOR_BONUS.ability, Ability.FIRE_BRANDS_ARMOR_BONUS.ability),
        runes = listOf(RuneSlot.COMBAT, RuneSlot.UTILITY),
        chestMeta = colorMeta(0x333333),
        legsMeta = colorMeta(0x505050),
        bootMeta = colorMeta(0x232323),
        headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2Y0MTNmNzYxMTgwNzI5NzBjN2NmZThmOTFmZTk4NjU4ZDJhZTk2M2IyZTQ2ZWFhOTQ0NDMzZDMzMjU5ZmUyYyJ9fX0="
    ) {})

    val VOLCANO_STRIDER_ARMOR = register(object : KillCountingArmor(
        "Volcano Strider",
        "volcano_strider",
        "LEATHER",
        Rarity.EPIC,
        stats {
            health = 100f
            defense = 200f
            strength = 30f
            ferocity = 10f
        },
        abilities = listOf(
            Ability.VOLCANO_STRIDER_ARMOR_BONUS.ability,
            Ability.ENRAGE_ARMOR_ABILITY.ability,
            Ability.FIRE_VORTICES_ARMOR_BONUS.ability
        ),
        runes = listOf(RuneSlot.COMBAT, RuneSlot.COMBAT, RuneSlot.UTILITY),
        chestMeta = colorMeta(0xFD9C02),
        legsMeta = colorMeta(0xFFBB50),
        bootMeta = colorMeta(0xDA8603),
        headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzM5OWVkNGIzZjdkZmNkNTg5NjUxYmNlNjEzNWM1MDRjZmY2MDc0YzUwYTI4NzMyY2IzOGQwYjliYWRhMTYwZiJ9fX0="
    ) {})

    private fun register(item: ArmorItem, model: Model? = null): ArmorItem {
        cache.add(Pair(item, model))
        return item
    }

    private fun internalRegisterSingle(item: ArmorItem) {
        if (item !is ThreePieceArmor)
            Registry.ITEM.register(id("${item.baseId}_helmet"), item.helmet())
        Registry.ITEM.register(id("${item.baseId}_chestplate"), item.chestplate())
        Registry.ITEM.register(id("${item.baseId}_leggings"), item.leggings())
        Registry.ITEM.register(id("${item.baseId}_boots"), item.boots())
    }

    fun init() {
        Threading.runAsync("Armor Registry", true) {
            info("Initializing Armor Registry daemon...")

            val pool = Threading.newFixedPool(8)

            for (element in cache) {
                pool.execute {
                    internalRegisterSingle(element.first)
                    if (element.second != null)
                        CMDGenerator.enqueue(element.second!!)
                }
            }

            pool.shutdown()
            val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            if (!success)
                throw IllegalStateException("Could not execute all tasks in the thread pool!")
        }
    }
}
