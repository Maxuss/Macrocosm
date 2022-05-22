package space.maxus.macrocosm.entity

import org.bukkit.Material
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.fishing.FishingPool
import space.maxus.macrocosm.item.ColoredEntityArmor
import space.maxus.macrocosm.item.SkullEntityHead
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.defaultStats
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

private fun registerPool(id: String, pool: LootPool): LootPool = Registry.LOOT_POOL.register(id(id), pool)

enum class EntityValue(val entity: MacrocosmEntity) {
    AMOGUS(EntityBase(
        comp("<red>Imposter"),
        EntityType.GUARDIAN,
        FishingPool.pool(10f).build(),
        1000.0,
        defaultStats {
            speed = 200f
            health = 1_500_000f
            damage = 250f
            strength = 100f
        },
        disguiseSkin = "{\"id\":\"a149f81bf7844f8987c554afdd4db533\",\"name\":\"Draco\",\"properties\":[{\"signature\":\"qrqKPr4AlKlkOAXLJMtXIdG4zz1Ui9acYYTYQCm4Ud4h/BhxwTf8kXytocd4zDkp9in2gvgksYkC919shZlh6gMc10ODol4p+ZujOpLDUTBpaXhxxmgL7HoS0fwKYRZ4GfSAmCzXZLJ23ce6yndt6bZjDFOvnJdUN+EA6fzg1CkyjLHVj25+s+hdFz8RnqCr6SePJpGB58g83qUBK9PK6AkbRk9higxLYdkhT16if8fh74rTX+1mlG9VyWWpjRs3h5PFMd0uDHh7ugc06xd6/9u0N9oougud0xWg3J9jqSRq+O+7n9Q3yCRnUQz0UywNk4EUR1mll/+WSjY8JOm/pC+YA03FL6/qJq3VvIdk78tOdQeTZVwouISlXw5SPoqSXhxSMBsL5D0U8tOIVaKRMROl41ZfG0kThQr73zIfXfEpycUhBP3xeXF8jxjGn6TfUl/ZR7DvaBH10oWcPT/Ky8VryeNxysdzwouAZpuSkNu5xLzowZlm4VMXwRspaLZTAN1IB6pyPh2tCU/RzUorzDCyoeBzGhCtYDI5ooBZnVLA9IFEIiPXu6qgcjsQnntkpXgga5e0VnBcQu3JfLgUgFE+mFSSGNOM4cqbpPlmP6jOp3dUDacEn7X9AhUvJoGbfev4UwgCEe+lPMAlXeVQdny0yQRm1lhEuevOxcb5xY4=\",\"name\":\"textures\",\"value\":\"eyJ0aW1lc3RhbXAiOjE1Mjg4NDU3NTg3MzQsInByb2ZpbGVJZCI6ImFkMWM2Yjk1YTA5ODRmNTE4MWJhOTgyMzY0OTllM2JkIiwicHJvZmlsZU5hbWUiOiJGdXJrYW5iejAwIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85MTFlN2ViZGQ5ZmNkMjQ4ZWUwMzQ3ZTJiOTMwYmMyN2NhMThhNGVjMTYzYjZjYWUzYzhkZjQxNjMwYzg1NzhhIn19fQ==\"}]}",
    )
    ),

    // sea creatures
    SEA_WALKER(EntityBase(
        comp("Sea Walker"),
        EntityType.ZOMBIE,
        FishingPool.pool(1f).build(),
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
        FishingPool.pool(1.8f).build(),
        201.0,
        defaultStats {
            health = 15000f
            damage = 200f
            strength = 50f
            defense = 150f
        },
        rewardingSkill = SkillType.FISHING,
        chestplate = ColoredEntityArmor(Material.LEATHER_CHESTPLATE, 0x176ACC),
        leggings = ColoredEntityArmor(Material.LEATHER_LEGGINGS, 0x176ACC),
        boots = ColoredEntityArmor(Material.LEATHER_BOOTS, 0x276ACC)
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
