package space.maxus.macrocosm.slayer.wither

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.entity.EntitySoundBank
import space.maxus.macrocosm.entity.EntitySoundType
import space.maxus.macrocosm.entity.textureProfile
import space.maxus.macrocosm.item.ColoredEntityArmor
import space.maxus.macrocosm.item.SkullEntityHead
import space.maxus.macrocosm.item.VanillaItem
import space.maxus.macrocosm.slayer.SlayerBase
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.stats.stats

class CinderflameSpirit(stats: Statistics, tier: Int, exp: Double) : SlayerBase(
    EntityType.WITHER_SKELETON,
    SlayerType.CINDERFLAME_SPIRIT,
    tier,
    exp,
    stats,
    mainHand = VanillaItem(Material.STONE_AXE),
    helmet = SkullEntityHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2UzN2RmMjdkMWU5MjVkMmY3OGRlNjkzNDEzYWRhYjc1ZDc5MTg4NDViYWM4ZjYxNzRmZGIyMmVjNTc3NjExZCJ9fX0="),
    chestplate = VanillaItem(Material.CHAINMAIL_CHESTPLATE),
    boots = VanillaItem(Material.NETHERITE_BOOTS)
)

object IncendiaryIncarnation: SlayerBase(
    EntityType.WITHER_SKELETON,
    SlayerType.CINDERFLAME_SPIRIT,
    5,
    4000.0,
    stats {
        health = 4_500_000f
        defense = 1000f
        damage = 400f
        strength = 350f
        speed = 300f
        trueDefense = 500f
        ferocity = 50f
    },
    mainHand = VanillaItem(Material.NETHERITE_SWORD),
    chestplate = ColoredEntityArmor(Material.LEATHER_CHESTPLATE, 0x6c6d6d),
    leggings = ColoredEntityArmor(Material.LEATHER_LEGGINGS, 0x750422),
    boots = ColoredEntityArmor(Material.LEATHER_BOOTS, 0x5b041b),
    sounds = EntitySoundBank.from(
        EntitySoundType.DAMAGED to (Sound.ENTITY_GHAST_SCREAM to 2f),
        EntitySoundType.DEATH to (Sound.ENTITY_GENERIC_EXPLODE to 0f)
    ),
    disguiseSkin = "null",
    actualName = "<gold>Incendiary Incarnation",
    disguiseProfile = textureProfile("ewogICJ0aW1lc3RhbXAiIDogMTY1MjcyMjQ0MzIxMiwKICAicHJvZmlsZUlkIiA6ICIyYzEwNjRmY2Q5MTc0MjgyODRlM2JmN2ZhYTdlM2UxYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOYWVtZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lMGFlM2M4Njk4NzIzNDc2MjQwNjg4OWQwMTU3MmNlNGE3YTNjYmUwMThjYzkwYmYzNjhhN2Y1MTQ3ZTA3OGU3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=", "pLWKq+hadDFt7Fm6oPRSl8inEE3Tpx7h8O+TtS4PuacfRJa4Yk4iNzFpYopqWMefgg8VVAqPiqrQbNqhkhATKM/zdEGCHj17Dab8Zh2LfCtb4qnRWEvxURPGmDfoUekEv6mom9cVtuiCicPeZEHcTMPGjD1zLApAIduDM8DOhAIh/fnmWa7FqzamysIdBxOH8AXpiwed84uttxsjri+drRVrhsZQV4RdlviYSSOS/FUfR7ZO+VU694oDiBEakdAPyGdLhFP0HJoE7dgtKXFnBp/aXyfYOeKedCF9Vxy7WSepl/+BeIqoliikIQhrCJrGwtD4r+i1n0mT1DArJ7hvJ3EhTC0KVrbP7O5O7gO6xUsg1Fy9zBCzTImh3Zu5TiyvAGoSz8/ruU2UuDXvhVkqGW/J7J3A0LNzMoL6Jbi1H3EMjfBYsgL3MXwq8SYXMlz6X34zDFLQgkWhosCxZGq9msA3w6ZNJ205YmJBRYTu2LwYcIsH8VVvPNbB674bYoqJCDgJrzBNFHgAtEiVm9DYsRwyoB3MFJ+LoLhb/oqUo2AE3fOHIG5tM5uPxwjOG8aDaUm+ZutDeD1lBDUSB65OhMnwQkmUUknTlUu+/o/HtLYnRobZv5NbjLWnjfYXfnHEj35naRrTnDjqzpQBVL1ulUyW1h8YAvq2tu0DfsZwQF4=")
)

object PyroclasticGoliath: SlayerBase(
    EntityType.WITHER_SKELETON,
    SlayerType.CINDERFLAME_SPIRIT,
    6,
    7500.0,
    stats {
        health = 12_000_000f
        defense = 1800f
        damage = 350f
        strength = 300f
        trueDefense = 800f
        speed = 150f
        ferocity = 80f
    },
    mainHand = VanillaItem(Material.NETHERITE_AXE),
    chestplate = VanillaItem(Material.NETHERITE_CHESTPLATE),
    leggings = ColoredEntityArmor(Material.LEATHER_LEGGINGS, 0x750422),
    boots = ColoredEntityArmor(Material.LEATHER_BOOTS, 0x5b041b),
    sounds = EntitySoundBank.from(
        EntitySoundType.DAMAGED to (Sound.ENTITY_ENDER_DRAGON_HURT to 0f),
        EntitySoundType.DEATH to (Sound.ENTITY_ENDER_DRAGON_GROWL to 0f)
    ),
    disguiseSkin = "m_xus",
    disguiseProfile = textureProfile("ewogICJ0aW1lc3RhbXAiIDogMTYzNTY5OTQ4MTY3NywKICAicHJvZmlsZUlkIiA6ICIyYzEwNjRmY2Q5MTc0MjgyODRlM2JmN2ZhYTdlM2UxYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOYWVtZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jNDY5MDlmNjNhYmQ5YTQ1Y2IxMDU1NGJiMWQ2ZTAwMGIzYjExMmI3ZGIzOWQ5OTAxNDQxZTUyYTllZjNjMGRjIgogICAgfQogIH0KfQ==", "tIQmgDSbUvTnVbZMvhCm5wS3HbNUu4QxoWYEfrEfoVShO7sWg0V3MPH1lIVr29yege0vSfc6wITqOR0U6HwEGp8nZKvtHTIfdnSqYleOMU/4q2ZDyLo3xB3J0kNzQDYzjjyJ9V3A/vQnaNz9EmACJqj7aHk3kK/KpCTIDuCXXof2qiOZUuJIyebW3THl/VBl0hZTSVOxCDYqT6iUYbdhxBhEQKmc9ObBFtwKN2i3kGrpkFCYgntz2xSAdYq2rk6GF/dhREmdjkqiG8uqs17beaa6Jxjy2KWHKjiRUSHvigr20uyPanMwjk/tXq2NpfZMrwajYvKLuSBB3wsxNj6YRuUtpuqw3YLOny/JhbMzrZi+irbsSAb1vWHBeEn2VWcD0gwsRQ7nSx4i4m9yY4wF4+AZeSUFJohvROFgrifVS//Mw2pd8cDYTpjODTIxwOiISBcXKLEDIXGhJj9lkkAXVKpz2EX/hlrRzfgYRLCwYJns7MM1oYqnbB0bhBNBfSSEHyYbSJg1qJuV+fBvJk9td+ZNRQwSL5jyTvee1SoQyZLHOfdPwh8IuyMNPJyZAS8TyHdqudzFrSyU2OIBtqMqAS00SDq3Is6m5su3+UyO0CmUW723pK39jZrA9k0ZHz3S5720kTnxXqtL0Z7Qr1vGF40eFaZAxCDn+CRBX5Wqrck="),
    actualName = "<dark_red>Pyroclastic Goliath"
)
