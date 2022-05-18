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
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.loot.LootRegistry
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.stats.SpecialStatistic
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.util.Identifier
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
    }

    override fun getId(entity: LivingEntity): Identifier {
        return id
    }

    override fun lootPool(player: MacrocosmPlayer?): LootPool {
        return LootRegistry.findOrNull(lootPool) ?: LootPool.of()
    }

    override fun damage(amount: Float, damager: Entity?) {
        if (paper == null)
            return

        val entity = paper!!

        if (EntityRegistry.hasSounds(id)) {
            val soundBank = EntityRegistry.findSounds(id)
            soundBank.playRandom(entity.location, SoundType.DAMAGED)
        }

        currentHealth -= amount
        if (currentHealth <= 0) {
            if (damager != null && damager is Player) {
                val event = PlayerKillEntityEvent(damager.macrocosm!!, this.paper!!)
                event.callEvent()
            }
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

        if (EntityRegistry.hasSounds(id)) {
            val soundBank = EntityRegistry.findSounds(id)
            soundBank.playRandom(entity.location, SoundType.DEATH)
        }

        currentHealth = 0f
        val killer = (damager as? Player)?.macrocosm
        var pool = lootPool(killer)
        val event = EntityDropItemsEvent(damager, entity, pool)
        val cancelled = !event.callEvent()
        pool = event.pool
        val loc = entity.location
        loadChanges(entity)
        entity.kill()
        if (cancelled)
            return

        val items = pool.roll(killer)
        for (item in items) {
            loc.world.dropItemNaturally(loc, item ?: continue)
        }
    }
}
