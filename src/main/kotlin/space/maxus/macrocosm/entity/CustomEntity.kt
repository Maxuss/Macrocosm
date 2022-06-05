package space.maxus.macrocosm.entity

import net.axay.kspigot.extensions.bukkit.kill
import net.axay.kspigot.extensions.server
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.bukkit.entity.*
import space.maxus.macrocosm.events.EntityDropItemsEvent
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.item.MACROCOSM_TAG
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.loot.GlobalLootPool
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.SpecialStatistic
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.util.getId
import java.util.*

class CustomEntity(private val paperId: UUID) : MacrocosmEntity {
    override var mainHand: MacrocosmItem? = paper?.equipment?.itemInMainHand?.macrocosm
    override var offHand: MacrocosmItem? = paper?.equipment?.itemInOffHand?.macrocosm
    override var helmet: MacrocosmItem? = paper?.equipment?.helmet?.macrocosm
    override var chestplate: MacrocosmItem? = paper?.equipment?.chestplate?.macrocosm
    override var leggings: MacrocosmItem? = paper?.equipment?.leggings?.macrocosm
    override var boots: MacrocosmItem? = paper?.equipment?.boots?.macrocosm

    override val name: Component
    override val type: EntityType
    override var baseStats: Statistics = Statistics.zero()
    override var baseSpecials: SpecialStatistics = SpecialStatistics()
    override val rewardingSkill: SkillType
    override val experience: Double
    override var currentHealth: Float = baseStats.health

    private val lootPool: Identifier
    private val id: Identifier

    private val paper: LivingEntity? get() = server.getEntity(paperId) as? LivingEntity

    init {
        val paper = paper!!
        val tag = paper.readNbt().getCompound(MACROCOSM_TAG)
        name = GsonComponentSerializer.gson().deserialize(tag.getString("BaseName"))
        type = paper.type

        val stats = Statistics.zero()
        val statCmp = tag.getCompound("Stats")
        for (stat in statCmp.allKeys) {
            val value = statCmp.getFloat(stat)
            if (value == 0f)
                continue
            stats[Statistic.valueOf(stat)] = value
        }
        baseStats = stats

        val specials = SpecialStatistics()
        val specs = tag.getCompound("Specials")
        for (stat in specs.allKeys) {
            val value = specs.getFloat(stat)
            if (value == 0f)
                continue
            specials[SpecialStatistic.valueOf(stat)] = value
        }
        baseSpecials = specials
        currentHealth = tag.getFloat("CurrentHealth")
        lootPool = tag.getId("LootID")
        id = tag.getId("ID")
        rewardingSkill = SkillType.valueOf(tag.getString("Skill"))
        experience = tag.getDouble("Experience")
    }

    override fun getId(entity: LivingEntity): Identifier {
        return id
    }

    override fun lootPool(player: MacrocosmPlayer?): LootPool {
        return Registry.LOOT_POOL.findOrNull(lootPool) ?: LootPool.of()
    }

    override fun damage(amount: Float, damager: Entity?) {
        if (paper == null || paper!!.isDead)
            return

        val entity = paper!!

        if (Registry.SOUND.has(id)) {
            val soundBank = Registry.SOUND.find(id)
            soundBank.playRandom(entity.location, SoundType.DAMAGED)
        }

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

        if (Registry.SOUND.has(id)) {
            val soundBank = Registry.SOUND.find(id)
            soundBank.playRandom(entity.location, SoundType.DEATH)
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
