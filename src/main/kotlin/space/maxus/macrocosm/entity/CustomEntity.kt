package space.maxus.macrocosm.entity

import net.axay.kspigot.extensions.bukkit.kill
import net.axay.kspigot.extensions.server
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.world.entity.OwnableEntity
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity
import org.bukkit.entity.*
import space.maxus.macrocosm.damage.DamageKind
import space.maxus.macrocosm.events.EntityDropItemsEvent
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.item.MACROCOSM_TAG
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.loot.GlobalLootPool
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.nms.NativeMacrocosmEntity
import space.maxus.macrocosm.npc.nms.NPCEntity
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.SpecialStatistic
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.util.general.getId
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
    override val playerFriendly: Boolean
    override var currentHealth: Float = baseStats.health

    private val lootPool: Identifier
    private val id: Identifier

    private val paper: LivingEntity? get() = server.getEntity(paperId) as? LivingEntity

    override fun tryRetrieveUuid(): UUID {
        return paperId
    }

    init {
        val handle = (paper as? CraftEntity)?.handle
        if (handle is NativeMacrocosmEntity) {
            id = handle.id
            val delegate = Registry.ENTITY.find(id)
            name = delegate.name
            type = delegate.type
            baseStats = delegate.baseStats
            baseSpecials = delegate.baseSpecials
            lootPool = Registry.LOOT_POOL.byValue((delegate as? EntityBase)?.pool ?: throw AssertionError())!!
            rewardingSkill = delegate.rewardingSkill
            playerFriendly = delegate.playerFriendly
            experience = delegate.experience
            val p = paper
            currentHealth = if (p == null)
                delegate.currentHealth
            else {
                val tag = p.readNbt().getCompound(MACROCOSM_TAG)
                tag.getFloat("CurrentHealth")
            }
        } else {
            val paper = paper
            if(paper != null) {
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
                playerFriendly = tag.getBoolean("PlayerFriendly")
            } else {
                baseSpecials = SpecialStatistics()
                currentHealth = 0f
                lootPool = Identifier.NULL
                id = Identifier.NULL
                rewardingSkill = SkillType.COMBAT
                experience = 0.0
                playerFriendly = false
                name = Component.empty()
                type = EntityType.ARMOR_STAND
            }
        }
    }

    override fun getId(entity: LivingEntity): Identifier {
        return id
    }

    override fun lootPool(player: MacrocosmPlayer?): LootPool {
        return Registry.LOOT_POOL.findOrNull(lootPool) ?: LootPool.of()
    }

    override fun damage(amount: Float, damager: Entity?, kind: DamageKind) {
        if (paper == null || paper!!.isDead)
            return

        val entity = paper!!
        val handle = (damager as? CraftEntity)?.handle
        if (playerFriendly && (damager is Player || handle is OwnableEntity))
            return

        if (Registry.SOUND.has(id)) {
            val soundBank = Registry.SOUND.find(id)
            soundBank.playRandom(entity.location, EntitySoundType.DAMAGED)
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
        val nmsEntity = (entity as? CraftEntity)?.handle
        if (nmsEntity is NPCEntity)
            return

        val loc = paper!!.location

        currentHealth = 0f
        val handle = (damager as? CraftEntity)?.handle
        var killer = (damager as? Player)?.macrocosm

        loadChanges(entity)
        entity.kill()

        if (killer != null || handle is OwnableEntity) {
            killer = if (handle is OwnableEntity) {
                (handle.owner as? Player)?.macrocosm ?: return
            } else {
                killer!!
            }
            val killEvent = PlayerKillEntityEvent(killer, entity, experience)
            killEvent.callEvent()
            val universal = GlobalLootPool.of(killer, this)
            for (item in universal.roll(killer)) {
                loc.world.dropItemNaturally(loc, item ?: continue)
            }
            killer.addSkillExperience(rewardingSkill, killEvent.experience)
        }

        if (Registry.SOUND.has(id)) {
            val soundBank = Registry.SOUND.find(id)
            soundBank.playRandom(entity.location, EntitySoundType.DEATH)
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
