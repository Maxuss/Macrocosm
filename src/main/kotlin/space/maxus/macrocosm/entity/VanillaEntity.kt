package space.maxus.macrocosm.entity

import net.axay.kspigot.extensions.bukkit.kill
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.extensions.server
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.attribute.Attribute
import org.bukkit.entity.*
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.damage.DamageKind
import space.maxus.macrocosm.events.EntityDropItemsEvent
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.MACROCOSM_TAG
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.loot.*
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.*
import java.util.*
import kotlin.math.max

internal fun specialsFromEntity(entity: LivingEntity?) = specialStats {
    if (entity == null)
        return@specialStats
    knockbackResistance = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE)?.value?.toFloat() ?: 0f
    knockbackBoost = entity.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK)?.value?.toFloat() ?: 0f

    when (entity.type) {
        EntityType.BLAZE, EntityType.GHAST, EntityType.WITHER_SKELETON, EntityType.WITHER, EntityType.ZOGLIN, EntityType.ZOMBIFIED_PIGLIN -> {
            fireResistance = 1f
        }

        else -> {
            // do not modify stats
        }
    }
}

internal fun isEntityFriendly(entity: Entity) = entity is Tameable && entity.isTamed

internal fun statsFromEntity(entity: LivingEntity?) = defaultStats {
    if (entity == null)
        return@defaultStats
    health = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value.toFloat() * 5f
    defense = entity.getAttribute(Attribute.GENERIC_ARMOR)?.value?.toFloat()?.times(5f) ?: 0f
    damage = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value?.toFloat()?.times(5f) ?: 15f
    when (entity.type) {
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

        else -> {
            // do not modify stats
        }
    }
}

fun dropsFromType(ty: EntityType): List<Drop> {
    return when (ty) {
        EntityType.ELDER_GUARDIAN -> listOf(
            vanilla(Material.PRISMARINE_SHARD, 1.0, amount = 5..10),
            vanilla(Material.PRISMARINE_CRYSTALS, 1.0, amount = 6..10),
            vanilla(Material.SPONGE, 1.0, DropRarity.RARE, amount = 1..2),
        )

        EntityType.WITHER_SKELETON -> listOf(
            vanilla(Material.BONE, .6, amount = 1..2),
            vanilla(Material.COAL, .6, amount = 1..4),
        )

        EntityType.STRAY -> listOf(
            vanilla(Material.BONE, .8, amount = 1..2),
            vanilla(Material.SNOWBALL, .5),
            vanilla(Material.SKELETON_SKULL, .001, DropRarity.SUPER_RARE),
            custom(ItemValue.enchanted(Material.BONE), DropRarity.RARE, .01, 1..2)
        )

        EntityType.HUSK -> listOf(
            vanilla(Material.ROTTEN_FLESH, .8, amount = 1..2),
            vanilla(Material.LEATHER, .2),
            vanilla(Material.POTATO, .01, DropRarity.RARE),
            vanilla(Material.CARROT, .01, DropRarity.RARE),
            vanilla(Material.ZOMBIE_HEAD, .0012, DropRarity.SUPER_RARE),
            custom(ItemValue.enchanted(Material.ROTTEN_FLESH), DropRarity.RARE, .01, 1..2),
            custom(ItemValue.enchanted(Material.GOLD_INGOT), DropRarity.VERY_RARE, .001, 1..2),
            custom(ItemValue.enchanted(Material.DIAMOND), DropRarity.SUPER_RARE, .0006),
        )

        EntityType.ZOMBIE_VILLAGER -> listOf(
            vanilla(Material.ROTTEN_FLESH, .8, amount = 1..2),
            vanilla(Material.EMERALD, .001, DropRarity.CRAZY_RARE),
            vanilla(Material.POTATO, .021, DropRarity.RARE),
            vanilla(Material.CARROT, .021, DropRarity.RARE),
            vanilla(Material.ZOMBIE_HEAD, .0012, DropRarity.SUPER_RARE),
            custom(ItemValue.enchanted(Material.ROTTEN_FLESH), DropRarity.RARE, .01, 1..2),
            custom(ItemValue.enchanted(Material.GOLD_INGOT), DropRarity.VERY_RARE, .001, 1..2),
            custom(ItemValue.enchanted(Material.DIAMOND), DropRarity.SUPER_RARE, .0006),
        )

        EntityType.SKELETON_HORSE -> listOf(
            vanilla(Material.BONE, .8, amount = 1..2),
        )

        EntityType.ZOMBIE_HORSE -> listOf(
            vanilla(Material.ROTTEN_FLESH, .8, amount = 1..2),
        )

        EntityType.DONKEY, EntityType.MULE, EntityType.HORSE, EntityType.LLAMA -> listOf(
            vanilla(Material.LEATHER, .9, amount = 1..3),
        )

        EntityType.EVOKER -> listOf(
            vanilla(Material.EMERALD, .8, amount = 1..2),
            vanilla(Material.TOTEM_OF_UNDYING, .4, DropRarity.RARE)
        )

        EntityType.VINDICATOR -> listOf(
            vanilla(Material.EMERALD, .8),
            vanilla(Material.TOTEM_OF_UNDYING, .2, DropRarity.VERY_RARE)
        )

        EntityType.ILLUSIONER -> listOf(
            vanilla(Material.EMERALD, 1.0, amount = 1..4),
            vanilla(Material.TOTEM_OF_UNDYING, .7, DropRarity.RARE)
        )

        EntityType.CREEPER -> listOf(
            vanilla(Material.GUNPOWDER, 1.0, amount = 1..2)
        )

        EntityType.SKELETON -> listOf(
            vanilla(Material.BONE, 0.9, amount = 1..2),
            vanilla(Material.SKELETON_SKULL, .001, DropRarity.SUPER_RARE),
            custom(ItemValue.enchanted(Material.BONE), DropRarity.RARE, .01, 1..2)
        )

        EntityType.SPIDER -> listOf(
            vanilla(Material.STRING, 0.9, amount = 1..2),
            vanilla(Material.SPIDER_EYE, .5),
            custom(ItemValue.enchanted(Material.STRING), DropRarity.RARE, .01, 1..2)
        )

        EntityType.ZOMBIE -> listOf(
            vanilla(Material.ROTTEN_FLESH, .8, amount = 1..2),
            vanilla(Material.POTATO, .01, DropRarity.RARE),
            vanilla(Material.CARROT, .01, DropRarity.RARE),
            vanilla(Material.ZOMBIE_HEAD, .0012, DropRarity.SUPER_RARE),
            custom(ItemValue.enchanted(Material.ROTTEN_FLESH), DropRarity.RARE, .01, 1..2),
            custom(ItemValue.enchanted(Material.GOLD_INGOT), DropRarity.VERY_RARE, .001, 1..2),
            custom(ItemValue.enchanted(Material.DIAMOND), DropRarity.SUPER_RARE, .0006),
        )

        EntityType.SLIME -> listOf(
            vanilla(Material.SLIME_BALL, 1.0),
            custom(ItemValue.enchanted(Material.SLIME_BALL), DropRarity.RARE, .01)
        )

        EntityType.GHAST -> listOf(
            vanilla(Material.GHAST_TEAR, .9, amount = 1..2)
        )

        EntityType.ZOMBIFIED_PIGLIN -> listOf(
            vanilla(Material.ROTTEN_FLESH, .8, amount = 1..2),
            vanilla(Material.WARPED_FUNGUS, .01, DropRarity.RARE),
            vanilla(Material.CRIMSON_FUNGUS, .01, DropRarity.RARE),
            vanilla(Material.PORKCHOP, .01),
            custom(ItemValue.enchanted(Material.ROTTEN_FLESH), DropRarity.RARE, .01, 1..2),
            custom(ItemValue.enchanted(Material.GOLD_INGOT), DropRarity.VERY_RARE, .001, 1..2),
            custom(ItemValue.enchanted(Material.DIAMOND), DropRarity.SUPER_RARE, .0006),
        )

        EntityType.ENDERMAN -> listOf(
            vanilla(Material.ENDER_PEARL, .5, amount = 1..2),
            vanilla(Material.WARPED_FUNGUS, .001, DropRarity.VERY_RARE),
            vanilla(Material.CRIMSON_FUNGUS, .01, DropRarity.RARE),
            custom(ItemValue.enchanted(Material.ENDER_PEARL), DropRarity.RARE, .01),
        )

        EntityType.CAVE_SPIDER -> listOf(
            vanilla(Material.STRING, .9, amount = 1..2),
            vanilla(Material.SPIDER_EYE, .5),
            custom(ItemValue.enchanted(Material.STRING), DropRarity.RARE, .01, 1..2)
        )

        EntityType.SILVERFISH -> listOf(
            custom(ItemValue.enchanted(Material.STRING), DropRarity.RARE, .01, 1..2)
        )

        EntityType.BLAZE -> listOf(
            vanilla(Material.BLAZE_ROD, .9, amount = 1..2),
        )

        EntityType.MAGMA_CUBE -> listOf(
            vanilla(Material.MAGMA_CREAM, .9, amount = 1..3),
            custom(ItemValue.enchanted(Material.MAGMA_CREAM), DropRarity.RARE, .01)
        )

        EntityType.WITCH -> listOf(
            vanilla(Material.SPIDER_EYE, .5),
            vanilla(Material.GLOWSTONE_DUST, .5),
            vanilla(Material.REDSTONE, .5),
            vanilla(Material.DIAMOND, .01, DropRarity.RARE),
        )

        EntityType.ENDERMITE -> listOf(
            vanilla(Material.ENDER_PEARL, .5),
        )

        EntityType.GUARDIAN -> listOf(
            vanilla(Material.PRISMARINE_SHARD, .7, amount = 1..5),
            vanilla(Material.PRISMARINE_CRYSTALS, .7, amount = 1..6),
            vanilla(Material.SPONGE, .01, DropRarity.RARE, amount = 1..1),
        )

        EntityType.SHULKER -> listOf(
            vanilla(Material.ENDER_PEARL, .5),
            vanilla(Material.SHULKER_SHELL, .4),
        )

        EntityType.PIG -> listOf(
            vanilla(Material.PORKCHOP, 1.0, amount = 1..2),
        )

        EntityType.SHEEP -> listOf(
            vanilla(Material.MUTTON, 1.0, amount = 1..2),
            vanilla(Material.WHITE_WOOL, 1.0),
        )

        EntityType.COW -> listOf(
            vanilla(Material.BEEF, 1.0, amount = 1..2),
            vanilla(Material.LEATHER, .7, amount = 1..2),
        )

        EntityType.CHICKEN -> listOf(
            vanilla(Material.CHICKEN, 1.0, amount = 1..2),
            vanilla(Material.FEATHER, .7, amount = 1..2),
        )

        EntityType.SQUID -> listOf(
            vanilla(Material.INK_SAC, .7, amount = 1..2),
            custom(ItemValue.enchanted(Material.INK_SAC), DropRarity.RARE, .01, 1..2)
        )

        EntityType.MUSHROOM_COW -> listOf(
            vanilla(Material.BEEF, 1.0, amount = 1..2),
            vanilla(Material.LEATHER, .7, amount = 1..2),
            vanilla(Material.BROWN_MUSHROOM, .5, amount = 1..2),
            vanilla(Material.RED_MUSHROOM, .5, amount = 1..2),
            vanilla(Material.WARPED_FUNGUS, .01, DropRarity.VERY_RARE, amount = 1..2),
            vanilla(Material.CRIMSON_FUNGUS, .01, DropRarity.VERY_RARE, amount = 1..2),
        )

        EntityType.IRON_GOLEM -> listOf(
            vanilla(Material.IRON_INGOT, 1.0, amount = 1..4),
            vanilla(Material.POPPY, 1.0),
        )

        EntityType.RABBIT -> listOf(
            vanilla(Material.RABBIT_HIDE, .7),
            vanilla(Material.RABBIT, 1.0, amount = 1..2),
            vanilla(Material.RABBIT_FOOT, .6, amount = 1..2),
        )

        EntityType.PHANTOM -> listOf(
            vanilla(Material.PHANTOM_MEMBRANE, 1.0, amount = 1..2),
            custom(ItemValue.enchanted(Material.PHANTOM_MEMBRANE), DropRarity.RARE, .01)
        )

        EntityType.COD -> listOf(vanilla(Material.COD, 1.0))
        EntityType.SALMON -> listOf(vanilla(Material.SALMON, 1.0))
        EntityType.PUFFERFISH -> listOf(vanilla(Material.PUFFERFISH, 1.0))
        EntityType.TROPICAL_FISH -> listOf(vanilla(Material.TROPICAL_FISH, 1.0))
        EntityType.DROWNED -> listOf(
            vanilla(Material.ROTTEN_FLESH, .8, amount = 1..2),
            vanilla(Material.SEAGRASS, .01, DropRarity.RARE),
            vanilla(Material.CARROT, .01, DropRarity.RARE),
            vanilla(Material.ZOMBIE_HEAD, .0012, DropRarity.SUPER_RARE),
            vanilla(Material.TRIDENT, .0453, DropRarity.VERY_RARE),
            vanilla(Material.NAUTILUS_SHELL, .0354, DropRarity.VERY_RARE),
            custom(ItemValue.enchanted(Material.ROTTEN_FLESH), DropRarity.RARE, .01, 1..2),
            custom(ItemValue.enchanted(Material.GOLD_INGOT), DropRarity.VERY_RARE, .001, 1..2),
            custom(ItemValue.enchanted(Material.DIAMOND), DropRarity.SUPER_RARE, .0006),
        )

        EntityType.PILLAGER -> listOf(
            vanilla(Material.EMERALD, .8, amount = 1..2),
        )

        EntityType.RAVAGER -> listOf(
            vanilla(Material.EMERALD, .8, amount = 1..5),
            vanilla(Material.SADDLE, 1.0, DropRarity.RARE),
            vanilla(Material.LEATHER, .8, amount = 1..2),
        )

        EntityType.TRADER_LLAMA -> listOf(
            vanilla(Material.LEATHER, .8, amount = 1..2),
        )

        EntityType.HOGLIN -> listOf(
            vanilla(Material.PORKCHOP, 1.0, amount = 1..2),
            vanilla(Material.COOKED_PORKCHOP, .3, amount = 1..2),
        )

        EntityType.ZOGLIN -> listOf(
            vanilla(Material.PORKCHOP, 1.0, amount = 1..2),
            vanilla(Material.ROTTEN_FLESH, .3, amount = 1..2),
        )

        EntityType.GLOW_SQUID -> listOf(
            vanilla(Material.GLOW_INK_SAC, 1.0, amount = 1..2),
        )

        else -> listOf()
    }
}

private fun skillFromType(type: EntityType, level: Int): Pair<Double, SkillType> {
    return when (type) {
        EntityType.SQUID, EntityType.GLOW_SQUID -> Pair(12.0, SkillType.FISHING)
        EntityType.COD, EntityType.SALMON -> Pair(4.0, SkillType.FISHING)
        EntityType.TROPICAL_FISH, EntityType.PUFFERFISH -> Pair(5.0, SkillType.FISHING)
        EntityType.GUARDIAN -> Pair(30.0, SkillType.FISHING)
        EntityType.ELDER_GUARDIAN -> Pair(120.0, SkillType.FISHING)
        else -> Pair(level * 5.0, SkillType.COMBAT)
    }
}

class VanillaEntity(val id: UUID) : MacrocosmEntity {
    companion object {
        fun from(entity: LivingEntity): VanillaEntity {
            if (entity.readNbt().contains(MACROCOSM_TAG)) {
                val vanilla = VanillaEntity(entity.uniqueId)
                val tag = entity.readNbt().getCompound(MACROCOSM_TAG)
                vanilla.paper = entity
                vanilla.currentHealth = tag.getFloat("CurrentHealth")
                val stats = Statistics.zero()
                val statCmp = tag.getCompound("Stats")
                for (stat in statCmp.allKeys) {
                    val value = statCmp.getFloat(stat)
                    if (value == 0f)
                        continue
                    stats[Statistic.valueOf(stat)] = value
                }
                vanilla.baseStats = stats
                vanilla.loadChanges(entity)
                return vanilla
            } else {
                val vanilla = VanillaEntity(entity.uniqueId)
                vanilla.paper = entity
                vanilla.baseStats = statsFromEntity(entity)
                vanilla.currentHealth = vanilla.baseStats.health
                vanilla.loadChanges(entity)
                return vanilla
            }
        }

        fun from(type: EntityType, loc: Location) =
            from(loc.world.spawnEntity(loc, type) as LivingEntity)
    }

    var paper: LivingEntity? = server.getEntity(id) as? LivingEntity

    override var mainHand: MacrocosmItem? by equipment(id, EquipmentSlot.HAND)
    override var offHand: MacrocosmItem? by equipment(id, EquipmentSlot.OFF_HAND)
    override var helmet: MacrocosmItem? by equipment(id, EquipmentSlot.HEAD)
    override var chestplate: MacrocosmItem? by equipment(id, EquipmentSlot.CHEST)
    override var leggings: MacrocosmItem? by equipment(id, EquipmentSlot.LEGS)
    override var boots: MacrocosmItem? by equipment(id, EquipmentSlot.FEET)

    override var baseStats: Statistics = statsFromEntity(paper)
    override var baseSpecials: SpecialStatistics = specialsFromEntity(paper)
    override val rewardingSkill: SkillType
    override val experience: Double
    override val playerFriendly: Boolean get() = paper != null && isEntityFriendly(paper!!)

    init {
        val (exp, skill) = skillFromType(type, level)
        rewardingSkill = skill
        experience = exp
    }

    override fun tryRetrieveUuid(): UUID {
        return this.id
    }

    override fun lootPool(player: MacrocosmPlayer?): LootPool {
        return LootPool.of(*dropsFromType(paper!!.type).toTypedArray())
    }

    override var currentHealth: Float = baseStats.health

    override val name: Component
        get() = type.name.lowercase().split("_").joinToString(separator = " ") { str ->
            str.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }.toComponent()


    override val type: EntityType
        get() = paper?.type ?: EntityType.UNKNOWN

    override fun damage(amount: Float, damager: Entity?, kind: DamageKind) {
        if (paper == null || paper!!.isDead)
            return

        val entity = paper!!

        if (playerFriendly && damager is Player)
            return

        currentHealth -= amount
        if (currentHealth <= 0) {
            kill(damager)
            return
        }

        if (entity is Creature) {
            entity.target = damager as? LivingEntity
        }
        entity.damage(0.0)


        loadChanges(paper!!)
    }

    override fun kill(damager: Entity?) {
        if (paper == null)
            return

        val entity = paper!!
        val loc = paper!!.location

        currentHealth = 0f
        val killer = (damager as? Player)?.macrocosm

        loadChanges(entity)
        entity.kill()

        if (killer != null) {
            val killEvent = PlayerKillEntityEvent(damager.macrocosm!!, entity, experience)
            killEvent.callEvent()
            val universal = GlobalLootPool.of(damager.macrocosm!!, this)
            for (item in universal.roll(damager.macrocosm)) {
                loc.world.dropItemNaturally(loc, item ?: continue)
            }
            killer.addSkillExperience(rewardingSkill, killEvent.experience)
        }

        var pool = lootPool(killer)
        val event = EntityDropItemsEvent(damager, entity, pool)
        val cancelled = !event.callEvent()
        pool = event.pool

        if (cancelled)
            return

        val items = pool.roll(killer)
        for (item in items) {
            loc.world.dropItemNaturally(loc, item ?: continue)
        }
    }
}
