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
        EntityType.PHANTOM,
        FishingPool.pool(10f).build(),
        1000.0,
        defaultStats {
            speed = 200f
            health = 1_500_000f
            damage = 250f
            strength = 100f
        },
        disguiseSkin = "{\"id\":\"8d5951a3907b49c196b404adcb59f58c\",\"name\":\"sus\",\"properties\":[{\"name\":\"textures\",\"value\":\"ewogICJ0aW1lc3RhbXAiIDogMTY1MzMyNDQwMTc4NywKICAicHJvZmlsZUlkIiA6ICI4ZDU5NTFhMzkwN2I0OWMxOTZiNDA0YWRjYjU5ZjU4YyIsCiAgInByb2ZpbGVOYW1lIiA6ICJzdXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzY3NGY5OTQzNWEzOGQ3Y2Q3NDY5OTE1OTJkM2IzOGEwZDQ0OTc4OGQ0MGVhOTBkMzI5MDczNjNmYWIxZWU1MCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9\",\"signature\":\"VGqMKpWRmsOFNB9xx7e3w0Tvlxzqmyb1+jS71u+zjJj7nbI3GKY7E2fVcTq6wGjqU87+lMyDtp+dngcsqfAK0yXA/C2kfC7uXFxeItjDwbH23+dzpChJpmQKgrV0Ez7yiMF8UfQyIHLeXWkUrG4IWp3pCGwRKrRW3h8qSGVaLtIf9FiL0n+tqae7zzQKYH1YtZhQA9iq0Ijux1G/KGw6dhl9zd6PY4M9FzYW2sK85IejosOLXTiodpTLhgIUrh639qWIa/mn9zTHjxl0BT+o9yKRtSpZwN0rIy6y9yWGMAxtplEcIjsgX6V2VEva4Tc3f0sxmL0gAfiCILsJN1zbsXR468d+iBsQp6uQt/OY8BP2GLGAdJbJlBGzqjC9aAZIupo9xyNA0B9gsUb3LazZYc9caC6SXqvBJLDKtDnMzxh7bHD7X1czIjM0G219kdE7eziq9lkHqpfbXRrI12Ie40UsDXVsqlXpWhMBtelLk2m1zl6Yk/E3QsTz9fEarkwa2g1m8U63sfYEOD1cS/Xyk68ZQfYjs+q703dFo/wIn9fKF9nfnoMfFfMCbIe5r6EOJ2ndWi6yRneGdlz1osE/Th7sAvIuQBRe6kla128i9DfBoltZebVahcYzKb5jpG+Zh3ci6NKQAUt2MOGOSIm6y6lSEzJ9K6eE1nQjpiqlK4w=\"}]}"
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
