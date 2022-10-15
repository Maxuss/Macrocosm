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

    val ARMOR_OF_ORDER = register(
        ArmorItem(
            "",
            "polarity_order",
            "LEATHER",
            Rarity.LEGENDARY,
            stats {
                health = 200f
                defense = 200f
                vitality = 20f
            },
            abilities = listOf(
                Ability.ORDER_POLARITY.ability,
                Ability.BLESSED_POLARITIES.ability
            ),
            runes = listOf(RuneSlot.COMBAT, RuneSlot.UTILITY, RuneSlot.COMBAT),
            bootMeta = colorMeta(0xEA9208),
            legsMeta = colorMeta(0x2F6D00),
            chestMeta = colorMeta(0xE8AE13),
            headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODQ4OGIwMzQzOTgwZmNmMjE3ZDdjYmI3NWQ3MzBhZGNhMDRhOGI2YTdiMTM4Mzg2YWNhYTdmYzk5NGE4NThhMiJ9fX0=",
            bootsName = "Sandals of Order",
            legsName = "Greaves of Order",
            chestName = "Breastplate of Order",
            headName = "Helm of Order"
        )
    )

    val ARMOR_OF_EARTH = register(
        ArmorItem(
            "",
            "polarity_earth",
            "LEATHER",
            Rarity.LEGENDARY,
            stats {
                health = 350f
                defense = 500f
                speed = -20f
                vitality = 10f
            },
            abilities = listOf(
                Ability.EARTH_POLARITY.ability,
                Ability.BLESSED_POLARITIES.ability
            ),
            runes = listOf(
                RuneSlot.COMBAT,
                RuneSlot.specific(RuneSpec.DEFENSIVE),
                RuneSlot.specific(RuneSpec.DEFENSIVE)
            ),
            bootMeta = colorMeta(0x15882E),
            headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmNlZGVjMDRkMjM4MGNkNzcwMjdmOWQ0NDQ1NWM5OGI3ZWRjNWY2NjRjYTBkZDMwYTYxMDY5MDM5MTUzOTFkYiJ9fX0=",
            bootsName = "Boots of Earth",
            legsName = "Leggings of Earth",
            chestName = "Chestplate of Earth",
            headName = "Helmet of Earth",
        )
    )

    val ARMOR_OF_FIRE = register(
        ArmorItem(
            "",
            "polarity_fire",
            "LEATHER",
            Rarity.LEGENDARY,
            stats {
                health = 100f
                defense = 175f
                speed = 20f
                strength = 120f
                attackSpeed = 20f
            },
            abilities = listOf(
                Ability.FIRE_POLARITY.ability,
                Ability.BLESSED_POLARITIES.ability
            ),
            runes = listOf(
                RuneSlot.COMBAT,
                RuneSlot.specific(RuneSpec.OFFENSIVE),
                RuneSlot.specific(RuneSpec.OFFENSIVE)
            ),
            bootMeta = colorMeta(0xE47A00),
            legsMeta = colorMeta(0xC54704),
            chestMeta = colorMeta(0x231C27),
            headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDkwMzIyMTEyNGUxYTI3NDAyMjc2YzNlMDE3NmJjOWY2MzIzOGQ3ZWE3NzEzZTliNTc5YTg3OGRhY2EyNDgxOSJ9fX0=",
            bootsName = "Boots of Fire",
            legsName = "Leggings of Fire",
            chestName = "Chestplate of Fire",
            headName = "Helmet of Fire"
        )
    )

    val ARMOR_OF_WATER = register(
        ArmorItem(
            "",
            "polarity_water",
            "LEATHER",
            Rarity.LEGENDARY,
            stats {
                health = 200f
                defense = 75f
                intelligence = 250f
                abilityDamage = 10f
                vigor = 25f
            },
            abilities = listOf(
                Ability.WATER_POLARITY.ability,
                Ability.FORBIDDEN_POLARITIES.ability
            ),
            runes = listOf(
                RuneSlot.UTILITY,
                RuneSlot.specific(RuneSpec.OFFENSIVE),
                RuneSlot.specific(RuneSpec.DEFENSIVE)
            ),
            bootMeta = colorMeta(0x003179),
            legsMeta = colorMeta(0x0B78AD),
            chestMeta = colorMeta(0x0E867F),
            headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2QyMmFkNGEyMTY1YTlhMDI5ZjI5NTYwMDhlMGVmMjQ2NmYyNDMwNzdhNzgxZTY3MGUzNmFjOWY0OGRjNmU3NiJ9fX0=",
            bootsName = "Boots of Downpour",
            legsName = "Leggings of Downpour",
            chestName = "Chestplate of Downpour",
            headName = "Helmet of Downpour"
        )
    )

    val ARMOR_OF_AIR = register(
        ArmorItem(
            "",
            "polarity_air",
            "LEATHER",
            Rarity.LEGENDARY,
            stats {
                health = 100f
                defense = 25f
                speed = 50f
                critChance = 20f
                critDamage = 60f
                strength = 40f
            },
            abilities = listOf(
                Ability.AIR_POLARITY_1.ability,
                Ability.AIR_POLARITY_2.ability,
                Ability.FORBIDDEN_POLARITIES.ability
            ),
            runes = listOf(
                RuneSlot.UTILITY,
                RuneSlot.specific(RuneSpec.OFFENSIVE),
                RuneSlot.specific(RuneSpec.OFFENSIVE)
            ),
            bootMeta = colorMeta(0xE5FFFF),
            legsMeta = colorMeta(0xD3E6E6),
            chestMeta = colorMeta(0xA7B1B1),
            headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODBkNzkyMjAxZmRjZTkxZTg4NGZmMzJhMDZkZDRjYzdjZDk4MzgyMmIxNjVkNDUzZWNjYjRjNjBhZWRjM2JhYSJ9fX0=",
            bootsName = "Waders of Typhoon",
            legsName = "Cords of Typhoon",
            chestName = "Chainmail of Typhoon",
            headName = "Hood of Typhoon"
        )
    )

    val ARMOR_OF_BLOOD = register(
        ArmorItem(
            "",
            "polarity_blood",
            "LEATHER",
            Rarity.LEGENDARY,
            stats {
                health = 250f
                defense = 25f
                ferocity = 30f
                strength = 30f
                speed = 15f
                attackSpeed = 15f
            },
            abilities = listOf(
                Ability.BLOOD_POLARITY.ability,
                Ability.FORBIDDEN_POLARITIES.ability
            ),
            runes = listOf(
                RuneSlot.COMBAT,
                RuneSlot.specific(RuneSpec.DEFENSIVE),
                RuneSlot.specific(RuneSpec.OFFENSIVE)
            ),
            bootMeta = colorMeta(0x8C0821),
            legsMeta = colorMeta(0xA20624),
            chestMeta = colorMeta(0x580011),
            headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTJmNjgyNjg0YjQ0Y2Q2ZTViMjJiYmFiMGUyYTI4NTZhNDQzMDk4OThiZTMxYmNlMDA2MWZkY2EwNDRkZjM5MiJ9fX0=",
            bootsName = "Boots of Blood Moon",
            legsName = "Leggings of Blood Moon",
            chestName = "Chestplate of Blood Moon",
            headName = "Helmet of Blood Moon"
        )
    )

    val ARMOR_OF_SPIRITS = register(
        ArmorItem(
            "",
            "polarity_spirits",
            "LEATHER",
            Rarity.LEGENDARY,
            stats {
                health = 150f
                defense = 100f
                intelligence = 80f
                strength = 40f
                speed = 20f
            },
            abilities = listOf(
                Ability.SPIRITS_POLARITY.ability,
                Ability.CURSED_POLARITIES.ability
            ),
            runes = listOf(
                RuneSlot.UTILITY,
                RuneSlot.typeBound(StatRune.DIAMOND),
                RuneSlot.typeBound(StatRune.ADAMANTITE)
            ),
            bootMeta = colorMeta(0x4F8BD6),
            legsMeta = colorMeta(0x4F5DD6),
            chestMeta = colorMeta(0x590FAD),
            headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzc2ZjNjMTI1ODc1ZmVkNTg4YTg3NWI4Mzg3Nzk1NDQxYjgzMzU1YjI1MTUyZjRhNjIwOTg0NTk4NWZhZWUifX19",
            bootsName = "Boots of Spirits",
            legsName = "Leggings of Spirits",
            chestName = "Chestplate of Spirits",
            headName = "Helmet of Spirits"
        )
    )

    val ARMOR_OF_VOID = register(
        ArmorItem(
            "",
            "polarity_void",
            "LEATHER",
            Rarity.LEGENDARY,
            stats {
                health = 175f
                defense = 120f
                intelligence = 50f
                vigor = 15f
                strength = 40f
                attackSpeed = 15f
            },
            abilities = listOf(
                Ability.VOID_POLARITY.ability,
                Ability.CURSED_POLARITIES.ability
            ),
            runes = listOf(
                RuneSlot.COMBAT,
                RuneSlot.typeBound(StatRune.AMETHYST),
                RuneSlot.typeBound(StatRune.MOONSTONE)
            ),
            bootMeta = colorMeta(0x2D0856),
            legsMeta = colorMeta(0x54228C),
            chestMeta = colorMeta(0x7510E6),
            headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWJhOTY0N2VjN2M4ZjM1OWQ4ZDA5NTJiZGJmNzJjYmI0YjU3NDNjZjg0NTVkY2I3NjY0ZTJiZjliZGY4YjcxOCJ9fX0=",
            bootsName = "Boots of Void",
            legsName = "Leggings of Void",
            chestName = "Chestplate of Void",
            headName = "Helmet of Void"
        )
    )

    val ARMOR_OF_CHAOS = register(
        ArmorItem(
            "",
            "polarity_chaos",
            "LEATHER",
            Rarity.LEGENDARY,
            stats {
                health = 120f
                defense = 150f
                intelligence = 60f
                strength = 60f
                critChance = 15f
                attackSpeed = 20f
                ferocity = 20f
                abilityDamage = 10f
            },
            abilities = listOf(
                Ability.CHAOS_POLARITY_1.ability,
                Ability.CHAOS_POLARITY_2.ability,
                Ability.CURSED_POLARITIES.ability
            ),
            runes = listOf(RuneSlot.COMBAT, RuneSlot.specific(RuneSpec.OFFENSIVE), RuneSlot.COMBAT),
            bootMeta = colorMeta(0xCC3F0C),
            legsMeta = colorMeta(0x902802),
            chestMeta = colorMeta(0xE06C17),
            headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWI2ZTVjZDYxMmM3M2NkOGU2YzdhNzIwYzI2MjgzZTc1NDhjYTcyOGQ4YjMwNjAxODQwZjdkYTVkNDMzZDgzYiJ9fX0=",
            bootsName = "Sandals of Chaos",
            legsName = "Greaves of Chaos",
            chestName = "Tunic of Chaos",
            headName = "Mask of Chaos"
        )
    )

    val ARMOR_OF_CONFLUX = register(
        ArmorItem(
            "",
            "polarity_conflux",
            "LEATHER",
            Rarity.RELIC,
            stats {
                health = 300f
                defense = 250f
                intelligence = 150f
                strength = 100f
                ferocity = 50f
            },
            abilities = listOf(
                Ability.CONFLUX_POLARITY.ability,
                Ability.ENVOY_OF_POLARITIES.ability,
                Ability.POLARITY_ORIGINS.ability
            ),
            runes = listOf(RuneSlot.COMBAT, RuneSlot.UNIVERSAL, RuneSlot.UNIVERSAL),
            bootMeta = colorMeta(0xFDDCEF),
            legsMeta = colorMeta(0xDCE7FD),
            chestMeta = colorMeta(0xDCFDEC),
            headSkin = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjQ3YjMxOTI3MWQ5NTczYTVjYjM2MzVjNzg1YTJmZmE5MmEyZDc1OWFjZThiZGExMzhiYmNmZDVhY2YxMGExOCJ9fX0=",
            bootsName = "Boots of Conflux",
            legsName = "Leggings of Conflux",
            chestName = "Chestplate of Conflux",
            headName = "Skull of Conflux"
        )
    )

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
        Threading.contextBoundedRunAsync("Armor Registry", true) {
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
