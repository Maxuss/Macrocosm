package space.maxus.macrocosm.entity

import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise
import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.nbt.CompoundTag
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.damage.healthColor
import space.maxus.macrocosm.damage.truncateBigNumber
import space.maxus.macrocosm.item.MACROCOSM_TAG
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.generic.getId
import space.maxus.macrocosm.util.generic.putId
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

fun levelFromStats(stats: Statistics, extraWeight: Float = 0f): Int {
    val weight = stats.damage + stats.strength * 10 + stats.critDamage * 10 + stats.defense * 10 + stats.health / 10f + stats.critChance + extraWeight
    return max(ceil(weight / 100f).roundToInt(), 1)
}

interface MacrocosmEntity : Listener {
    var mainHand: MacrocosmItem?
    var offHand: MacrocosmItem?
    var helmet: MacrocosmItem?
    var chestplate: MacrocosmItem?
    var leggings: MacrocosmItem?
    var boots: MacrocosmItem?

    var currentHealth: Float

    val name: Component
    val type: EntityType
    var baseStats: Statistics
    var baseSpecials: SpecialStatistics
    val rewardingSkill: SkillType
    val experience: Double
    val playerFriendly: Boolean

    val level get() = levelFromStats(calculateStats(), extraWeight())

    fun lootPool(player: MacrocosmPlayer?): LootPool

    fun extraWeight(): Float {
        return 0f
    }

    fun addExtraNbt(nbt: CompoundTag) {

    }

    fun rewardExperience(player: MacrocosmPlayer) {
        player.addSkillExperience(rewardingSkill, experience)
    }

    fun preModify(entity: LivingEntity) {

    }

    fun getId(entity: LivingEntity): Identifier {
        val nbt = entity.readNbt()
        return if (nbt.contains(MACROCOSM_TAG))
            nbt.getCompound(MACROCOSM_TAG).getId("ID")
        else
            Registry.ENTITY.byValue(this) ?: Identifier("minecraft", type.name.lowercase())
    }

    fun calculateStats(): Statistics {
        val base = baseStats.clone()
        val specials = specialStats()
        base.multiply(1 + specials.statBoost)
        base.increase(mainHand?.stats())
        base.increase(offHand?.stats())
        base.increase(helmet?.stats())
        base.increase(chestplate?.stats())
        base.increase(leggings?.stats())
        base.increase(boots?.stats())
        return base
    }

    fun specialStats(): SpecialStatistics {
        val base = baseSpecials.clone()
        base.increase(mainHand?.specialStats() ?: SpecialStatistics())
        base.increase(offHand?.specialStats() ?: SpecialStatistics())
        base.increase(helmet?.specialStats() ?: SpecialStatistics())
        base.increase(chestplate?.specialStats() ?: SpecialStatistics())
        base.increase(leggings?.specialStats() ?: SpecialStatistics())
        base.increase(boots?.specialStats() ?: SpecialStatistics())
        return base
    }

    fun buildName(): Component {
        val stats = calculateStats()
        val curHealth = truncateBigNumber(currentHealth)
        val fullHealth = truncateBigNumber(stats.health)
        val level = levelFromStats(stats, extraWeight())
        return text("<dark_gray>[<gray>Lv $level<dark_gray>] ").append(name.colorIfAbsent(NamedTextColor.RED))
            .append(" ".toComponent())
            .append(curHealth.toComponent().color(healthColor(currentHealth, stats.health)))
            .append(text("<white>/<green>$fullHealth <red> ❤"))
            .noitalic()
    }

    fun damage(amount: Float, damager: Entity? = null)
    fun kill(damager: Entity? = null)
    fun isDamageFatal(damage: Float): Boolean {
        return currentHealth <= damage
    }

    fun spawn(at: Location): LivingEntity {
        val craftWorld = at.world as CraftWorld
        val level = craftWorld.handle
        val types = net.minecraft.world.entity.EntityType.byString(type.key.asString())
        val nmsEntity = (if (types.isPresent) {
            val entityTypes = types.get()
            entityTypes.create(level)
        } else null) ?: throw RuntimeException("Can not spawn an entity of type $type!")
        val bukkit = nmsEntity.bukkitEntity as LivingEntity
        loadChanges(bukkit)

        bukkit.teleport(at)
        level.addFreshEntity(nmsEntity, CreatureSpawnEvent.SpawnReason.CUSTOM)
        return bukkit
    }

    fun loadChanges(entity: LivingEntity) {
        if (entity.type != type)
            return

        preModify(entity)

        // equipment
        entity.equipment?.setItemInMainHand(this.mainHand?.build(), true)
        entity.equipment?.setItemInOffHand(this.offHand?.build(), true)
        entity.equipment?.helmet = this.helmet?.build()
        entity.equipment?.chestplate = this.chestplate?.build()
        entity.equipment?.leggings = this.leggings?.build()
        entity.equipment?.boots = this.boots?.build()

        EquipmentSlot.values().forEach {
            try {
                entity.equipment?.setDropChance(it, 0.0f)
            } catch (e: java.lang.IllegalArgumentException) {
                // pass
            }
        }
        entity.isCustomNameVisible = true
        // name
        val builtName = buildName()
        entity.customName(builtName)

        // actual data
        val nbt = entity.readNbt()

        val tag = CompoundTag()
        tag.put("Stats", baseStats.compound())
        tag.put("Specials", baseSpecials.compound())
        tag.putFloat("CurrentHealth", currentHealth)
        tag.putString("BaseName", GsonComponentSerializer.gson().serialize(name))
        val lootId = Registry.LOOT_POOL.byValue(lootPool(null)) ?: Identifier.NULL
        tag.putId("LootID", lootId)
        val id = getId(entity).toString()
        tag.putString("ID", id)
        tag.putDouble("Experience", experience)
        tag.putString("Skill", rewardingSkill.name)
        tag.putBoolean("PlayerFriendly", playerFriendly)

        addExtraNbt(tag)

        nbt.put(MACROCOSM_TAG, tag)
        entity.loadNbt(nbt)

        // disguises
        if (DisguiseAPI.isDisguised(entity)) {
            val disguise = DisguiseAPI.getDisguise(entity) as? PlayerDisguise ?: return
            disguise.name = nameMm(builtName)
        }

        // movement speed
        val speed = calculateStats().speed
        val attr = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) ?: return
        val modifier = .2 * (speed / 100.0)
        attr.baseValue = modifier
    }
}

fun Entity.loadNbt(nbt: CompoundTag) {
    this.persistentDataContainer.set(NamespacedKey(Macrocosm, "NBT_WRAPPER"), CompoundDataType, nbt)
}

fun Entity.readNbt(): CompoundTag {
    return this.persistentDataContainer.get(NamespacedKey(Macrocosm, "NBT_WRAPPER"), CompoundDataType) ?: CompoundTag()
}
