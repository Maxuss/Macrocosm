package space.maxus.macrocosm.entity

import org.bukkit.Material
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
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

private fun registerPool(id: String, pool: LootPool): LootPool = Registry.LOOT_POOL.register(id(id), pool)

enum class EntityValue(val entity: MacrocosmEntity) {
    // sea creatures
    SQUID(
        EntityBase(
            comp("Squid"),
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
            comp("<red>Lava Squid"),
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
            comp("<green>Radioactive Squid"),
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
            disguiseSkin = "Gladiator_Kraken"
        )
    ),

    SEA_WALKER(EntityBase(
        comp("Sea Walker"),
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

    SEA_ARCHER(EntityBase(
        comp("Sea Archer"),
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
    ))
    ;

    companion object {

        fun init() {
            Threading.runAsync("Entity Registry Daemon") {
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
