package space.maxus.macrocosm.entity

import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.item.Armor
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.loot.DropRarity
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.loot.LootRegistry
import space.maxus.macrocosm.loot.vanilla
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

enum class EntityValue(val entity: MacrocosmEntity) {
    TEST_ENTITY(
        EntityBase(
            comp("<gold>Mr. Sketchpad"),
            EntityType.ZOMBIE,
            LootRegistry.register(
                id("test_pool"),
                LootPool.of(vanilla(Material.DIAMOND, 1.0, DropRarity.UNBELIEVABLE, 1..3))
            ),
            stats {
                health = 500000f
                damage = 100f
                strength = 500f
            },
            mainHand = ItemValue.SCYLLA.item,
            helmet = Armor.AMETHYST_ARMOR.helmet(),
            disguiseSkin = "Mr_Sketchpad",
            sounds = EntitySoundBank.from(
                SoundType.DAMAGED to (Sound.ENTITY_IRON_GOLEM_HURT to 0.5f),
                SoundType.DEATH to (Sound.ENTITY_WITHER_DEATH to 0.7f)
            )
        )
    )
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
                        else EntityRegistry.register(id(entity.name.lowercase()), entity.entity)
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
