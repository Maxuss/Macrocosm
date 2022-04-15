package space.maxus.macrocosm.entity

import net.axay.kspigot.extensions.bukkit.kill
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.extensions.server
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Slime
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.item.MACROCOSM_TAG
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.stats.defaultStats
import java.util.*
import kotlin.math.max

internal fun statsFromEntity(entity: LivingEntity) = defaultStats {
    when(entity.type) {
        EntityType.ELDER_GUARDIAN -> {
            health = 25000f
            defense = 500f
            damage = 100f
            strength = 120f
            ferocity = 5f
        }
        EntityType.WITHER_SKELETON -> {
            health = 3000f
            defense = 150f
            strength = 50f
        }
        EntityType.STRAY -> {
            health = 120f
        }
        EntityType.HUSK -> {
            health = 120f
        }
        EntityType.EVOKER -> {
            health = 1500f
            trueDefense = 50f
        }
        EntityType.VEX -> {
            health = 15f
            trueDefense = 50f
            trueDamage = 100f
        }
        EntityType.SLIME -> {
            val slime = entity as Slime
            health = 100f * (slime.size + 5)
            damage = 5f * (slime.size + 5)
        }
        EntityType.GHAST -> {
            health = 500f
            trueDefense = 100f
        }
        EntityType.ENDERMAN -> {
            val modifier = if (entity.world.environment == World.Environment.THE_END) 12f else 1f
            health = modifier * 1000f
            damage = modifier * 15f
            strength = modifier * 5f
            trueDefense = modifier * 5f
        }
        EntityType.BLAZE -> {
            health = 2500f
            damage = 120f
            strength = 200f
        }
        EntityType.MAGMA_CUBE -> {
            val slime = entity as Slime
            health = 300f * (slime.size + 5)
            damage = 10f * (slime.size + 5)
        }
        EntityType.ENDER_DRAGON -> {
            // we should not appear here, but whatever
            health = 15_000_000f
            damage = 1000f
            strength = 1000f
        }
        EntityType.WITHER -> {
            // we should not appear here either
            health = 10_000_000f
            damage = 1000f
            strength = 800f
        }
        EntityType.GUARDIAN -> {
            health = 800f
            damage = 50f
        }
        EntityType.SHULKER -> {
            health = 600f
            defense = 500f
            trueDefense = 1000f
        }
        EntityType.IRON_GOLEM -> {
            health = 25000f
            damage = 100f
            strength = 120f
            defense = 250f
        }
        EntityType.POLAR_BEAR -> {
            health = 15000f
            damage = 100f
            strength = 500f
            defense = 200f
        }
        EntityType.TURTLE -> {
            health = 1000f
            defense = 150f
        }
        EntityType.PHANTOM -> {
            val days = entity.world.gameTime / 24000f
            val modifier = 500f * max(days / 10f, 1f)
            health = modifier * 500f
            damage = modifier * 10f
        }
        EntityType.PILLAGER -> {
            health = 5000f
            damage = 120f
            strength = 100f
            ferocity = 10f
        }
        EntityType.RAVAGER -> {
            health = 25000f
            defense = 500f
            damage = 250f
            strength = 250f
            ferocity = 25f
        }
        EntityType.HOGLIN -> {
            health = 3000f
            damage = 100f
            strength = 100f
        }
        EntityType.PIGLIN -> {
            health = 2500f
            damage = 250f
        }
        EntityType.ZOGLIN -> {
            health = 5000f
            damage = 100f
            strength = 100f
            ferocity = 5f
        }
        EntityType.PIGLIN_BRUTE -> {
            health = 15000f
            damage = 250f
            strength = 250f
            defense = 300f
            ferocity = 20f
        }
        EntityType.UNKNOWN -> {
            health = 10f
            damage = 10f
            magicFind = 500f
        }
        else -> { }
    }
}

class VanillaEntity(val id: UUID) : MacrocosmEntity {
    companion object {
        fun from(entity: LivingEntity): VanillaEntity {
            if(entity.readNbt().contains(MACROCOSM_TAG)) {
                val vanilla = VanillaEntity(entity.uniqueId)
                val tag = entity.readNbt().getCompound(MACROCOSM_TAG)
                vanilla.currentHealth = tag.getFloat("CurrentHealth")
                vanilla.loadChanges(entity)
                return vanilla
            } else {
                val vanilla = VanillaEntity(entity.uniqueId)
                vanilla.loadChanges(entity)
                return vanilla
            }
        }

        fun from(type: EntityType, loc: Location) =
            from(loc.world.spawnEntity(loc, type) as LivingEntity)
    }

    val paper: LivingEntity? get() = server.getEntity(id) as? LivingEntity

    override var mainHand: MacrocosmItem? by equipment(id, EquipmentSlot.HAND)
    override var offHand: MacrocosmItem? by equipment(id, EquipmentSlot.OFF_HAND)
    override var helmet: MacrocosmItem? by equipment(id, EquipmentSlot.HEAD)
    override var chestplate: MacrocosmItem? by equipment(id, EquipmentSlot.CHEST)
    override var leggings: MacrocosmItem? by equipment(id, EquipmentSlot.LEGS)
    override var boots: MacrocosmItem? by equipment(id, EquipmentSlot.FEET)

    override var baseStats: Statistics = statsFromEntity(paper!!)
    override var currentHealth: Float = baseStats.health

    override val name: Component = type.name.lowercase().split("_").joinToString(separator = " ") { str -> str.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()
        ) else it.toString()
    } }.toComponent()


    override val type: EntityType
        get() = paper!!.type

    override fun damage(amount: Float) {
        if(paper == null)
            return

        val entity = paper!!

        currentHealth -= amount
        if(currentHealth <= 0) {
            kill()
            return
        }
        entity.damage(0.0)

        loadChanges(paper!!)
    }

    override fun kill() {
        // TODO: item drops
        if(paper == null)
            return
        currentHealth = 0f
        loadChanges(paper!!)
        paper!!.kill()
    }
}
