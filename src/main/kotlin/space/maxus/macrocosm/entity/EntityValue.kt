package space.maxus.macrocosm.entity

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.fishing.FishingPool
import space.maxus.macrocosm.item.ColoredEntityArmor
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.SkullEntityHead
import space.maxus.macrocosm.item.VanillaItem
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.defaultStats
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.id
import java.util.concurrent.TimeUnit

private fun registerPool(id: String, pool: LootPool): LootPool = Registry.LOOT_POOL.register(id(id), pool)

enum class EntityValue(val entity: MacrocosmEntity) {
    TEST_SUMMON(
        EntityBase(
            text("Test Summon"),
            EntityType.SPIDER,
            LootPool.of(),
            .0,
            stats {
                strength = 100f
                health = 50000f
                speed = 200f
            },
            playerFriendly = true
        )
    ),

    // sea creatures
    SQUID(
        EntityBase(
            text("Squid"),
            EntityType.SQUID,
            FishingPool.withStrength(.8f).build(),
            54.0,
            defaultStats {
                health = 1500f
                defense = 25f
            },
            rewardingSkill = SkillType.FISHING,
        )
    ),

    LAVA_SQUID(
        EntityBase(
            text("<red>Lava Squid"),
            EntityType.SQUID,
            FishingPool.withStrength(2.8f).build(),
            245.0,
            defaultStats {
                health = 120_000f
                defense = 45f
            },
            rewardingSkill = SkillType.FISHING,
        )
    ),

    RADIOACTIVE_SQUID(
        EntityBase(
            text("<green>Radioactive Squid"),
            EntityType.DROWNED,
            FishingPool.withStrength(13.0f).build(),
            1200.0,
            defaultStats {
                health = 500_000f
                defense = 250f
                strength = 250f
                damage = 240f
            },
            rewardingSkill = SkillType.FISHING,
            mainHand = ItemValue.RADIOACTIVE_TRIDENT.item,
            helmet = SkullEntityHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVkMzE1NTkyNjFiM2U3OTAyNDc1MWZlMDdiNzExYzhmZWVmNTFkNTZjMDM2MzUyMjY5NTU4MDViYzQyODk0ZSJ9fX0="),
            disguiseSkin = "Gladiator_Kraken",
            sounds = EntitySoundBank.from(
                EntitySoundType.DAMAGED to (Sound.ENTITY_GLOW_SQUID_HURT to 0f),
                EntitySoundType.DAMAGED to (Sound.ENTITY_GLOW_SQUID_DEATH to 0f),
            )
        )
    ),

    SEA_WALKER(
        EntityBase(
            text("Sea Walker"),
            EntityType.ZOMBIE,
            FishingPool.withStrength(1f).build(),
            102.0,
            defaultStats {
                health = 2000f
                damage = 50f
                defense = 10f
            },
            rewardingSkill = SkillType.FISHING,
            helmet = SkullEntityHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk3NzI2ZjU3ZTk0ODc4MDBkZGIyZjRlOGY1MmVhNTM0ODgyNWQxMmRhYWRmZmU2N2E0NjJkYmY5NjBjMDU3YiJ9fX0="),
            chestplate = ColoredEntityArmor(Material.LEATHER_CHESTPLATE, 0x276ACC),
            leggings = ColoredEntityArmor(Material.LEATHER_LEGGINGS, 0x276ACC),
            boots = ColoredEntityArmor(Material.LEATHER_BOOTS, 0x276ACC)
        )
    ),

    SEA_ARCHER(
        EntityBase(
            text("Sea Archer"),
            EntityType.SKELETON,
            FishingPool.withStrength(1.8f).build(),
            201.0,
            defaultStats {
                health = 15000f
                damage = 200f
                strength = 50f
                defense = 150f
            },
            rewardingSkill = SkillType.FISHING,
            mainHand = VanillaItem(Material.BOW),
            offHand = VanillaItem(Material.ARROW, 64),
            chestplate = ColoredEntityArmor(Material.LEATHER_CHESTPLATE, 0x176ACC),
            leggings = ColoredEntityArmor(Material.LEATHER_LEGGINGS, 0x176ACC),
            boots = ColoredEntityArmor(Material.LEATHER_BOOTS, 0x276ACC)
        )
    ),

    // summons
    REAPER_MASK_ZOMBIE(
        EntityBase(
            text("<green>Revenant Zombie"),
            EntityType.ZOMBIE,
            LootPool.of(),
            0.0,
            stats {
                health = 250_000f
                damage = 600f
                strength = 200f
                defense = 250f
                speed = 100f
            },
            playerFriendly = true,
            helmet = SkullEntityHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmI1NTJjOTBmMjEyZTg1NWQxMjI1NWQ1Y2Q2MmVkMzhiOWNkN2UzMGU3M2YwZWE3NzlkMTc2NDMzMGU2OTI2NCJ9fX0="),
            chestplate = VanillaItem(Material.LEATHER_CHESTPLATE),
            boots = VanillaItem(Material.CHAINMAIL_BOOTS),
            mainHand = VanillaItem(Material.IRON_HOE)
        )
    ),

    ENTOMBED_MASK_ZOMBIE(
        EntityBase(
            text("<green>Revenant Ghoul"),
            EntityType.ZOMBIE,
            LootPool.of(),
            0.0,
            stats {
                health = 333_000f
                damage = 900f
                strength = 350f
                ferocity = 70f
                defense = 500f
                speed = 90f
            },
            playerFriendly = true,
            helmet = SkullEntityHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVkOTJlODllNDczN2RjZmFlNWU1NzFjNzZmNzYwNGE3YWI3YzllMDNmNzhjMTgyMTcxZDEwOTU3MTQxNTZkMSJ9fX0="),
            chestplate = ColoredEntityArmor(Material.LEATHER_CHESTPLATE, 0),
            leggings = ColoredEntityArmor(Material.LEATHER_LEGGINGS, 0x4C4C4C),
            boots = VanillaItem(Material.NETHERITE_BOOTS),
            mainHand = VanillaItem(Material.NETHERITE_SWORD),
            sounds = EntitySoundBank.from(
                EntitySoundType.DAMAGED to (Sound.ENTITY_WITHER_SKELETON_HURT to 0.7f),
                EntitySoundType.DEATH to (Sound.ENTITY_WITHER_DEATH to 0.7f)
            )
        )
    ),

    ZOMBIE_YOUNGLING(
        EntityBase(
            text("<green>Youngling Zombie"),
            EntityType.ZOMBIE,
            LootPool.of(),
            0.0,
            stats {
                health = 50_000f
                damage = 250f
                strength = 50f
                defense = 50f
                speed = 200f
            },
            playerFriendly = true,
            helmet = VanillaItem(Material.CHAINMAIL_HELMET),
            chestplate = VanillaItem(Material.IRON_CHESTPLATE),
            boots = VanillaItem(Material.CHAINMAIL_BOOTS),
            mainHand = VanillaItem(Material.STONE_SWORD)
        )
    ),

    ZOMBIE_GOLDEN(
        EntityBase(
            text("<green>Golden Ghoul"),
            EntityType.ZOMBIE,
            LootPool.of(),
            0.0,
            stats {
                health = 250_000f
                damage = 500f
                ferocity = 50f
                strength = 200f
                defense = 300f
                speed = 100f
            },
            playerFriendly = true,
            helmet = VanillaItem(Material.ZOMBIE_HEAD),
            chestplate = VanillaItem(Material.GOLDEN_CHESTPLATE),
            leggings = VanillaItem(Material.GOLDEN_LEGGINGS),
            boots = VanillaItem(Material.GOLDEN_BOOTS),
            mainHand = VanillaItem(Material.GOLDEN_AXE)
        )
    ),

    ZOMBIE_GIANT(
        EntityBase(
            text("<green>Corpse Giant"),
            EntityType.GIANT,
            LootPool.of(),
            0.0,
            stats {
                health = 500_000f
                damage = 2000f
                ferocity = 50f
                strength = 400f
                defense = 100f
                speed = 200f
            },
            playerFriendly = true,
            helmet = SkullEntityHead("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDRiYjI2MjdiMmRmODg0MjRmM2Q2MDY3NDk3ZGQyMzAzOWM1ODI2OTI5NTllYTE2NDhiYzc2YzFhOGNlYTgwIn19fQ=="),
            chestplate = VanillaItem(Material.LEATHER_CHESTPLATE),
            boots = VanillaItem(Material.LEATHER_BOOTS),
            mainHand = VanillaItem(Material.IRON_SWORD)
        )
    ),

    DUMMY(EntityBase(
        text("Training Dummy"),
        EntityType.IRON_GOLEM,
        LootPool.of(),
        .0,
        stats {
            health = 500_000f
            defense = 100f
            speed = 0f
        }
    ))

    ;

    companion object {

        fun init() {
            Threading.contextBoundedRunAsync("Entity Registry Daemon") {
                info("Starting Entity Registry daemon...")

                val pool = Threading.newCachedPool()
                for (entity in values()) {
                    pool.execute {
                        val id = id(entity.name.lowercase())
                        if (entity.entity is EntityBase) entity.entity.register(id)
                        else Registry.ENTITY.register(id, entity.entity)
                    }
                }

                pool.shutdown()
                val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
                if (!success)
                    throw IllegalStateException("Could not execute all tasks in the thread pool!")

                info("Successfully registered ${values().size} entities")
            }
        }
    }
}
