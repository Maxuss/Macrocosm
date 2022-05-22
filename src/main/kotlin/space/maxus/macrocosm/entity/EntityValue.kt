package space.maxus.macrocosm.entity

import org.bukkit.Material
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.fishing.FishingPool
import space.maxus.macrocosm.item.coloredArmor
import space.maxus.macrocosm.item.skull
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

enum class EntityValue(val entity: MacrocosmEntity) {
    AMOGUS(EntityBase(
        comp("<red>Imposter"),
        EntityType.GUARDIAN,
        FishingPool.pool(10f).build(),
        1000.0,
        stats {
            health = 1_500_000f
            damage = 250f
            strength = 100f
        },
        disguiseSkin = "noobmaster5513",
    )
    ),

    // sea creatures
    SEA_WALKER(EntityBase(
        comp("Sea Walker"),
        EntityType.ZOMBIE,
        FishingPool.pool(1f).build(),
        102.0,
        stats {
            health = 2000f
            damage = 50f
            defense = 10f
        },
        rewardingSkill = SkillType.FISHING,
        helmet = skull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODk3NzI2ZjU3ZTk0ODc4MDBkZGIyZjRlOGY1MmVhNTM0ODgyNWQxMmRhYWRmZmU2N2E0NjJkYmY5NjBjMDU3YiJ9fX0="),
        chestplate = coloredArmor(Material.LEATHER_CHESTPLATE, 0x276ACC),
        leggings = coloredArmor(Material.LEATHER_LEGGINGS, 0x276ACC),
        boots = coloredArmor(Material.LEATHER_BOOTS, 0x276ACC)
        )
    ),

    SEA_ARCHER(EntityBase(
        comp("Sea Archer"),
        EntityType.SKELETON,
        FishingPool.pool(1.8f).build(),
        201.0,
        stats {
            health = 15000f
            damage = 200f
            strength = 50f
            defense = 150f
        },
        rewardingSkill = SkillType.FISHING,
        chestplate = coloredArmor(Material.LEATHER_CHESTPLATE, 0x176ACC),
        leggings = coloredArmor(Material.LEATHER_LEGGINGS, 0x176ACC),
        boots = coloredArmor(Material.LEATHER_BOOTS, 0x276ACC)
    ))
    ;

    companion object {
        fun init() {
            Threading.start("Entity Registry Daemon") {
                info("Starting Entity Registry daemon...")

                val pool = Threading.pool()
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
