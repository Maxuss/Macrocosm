package space.maxus.macrocosm.entity

import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics

open class EntityBase(
    override val name: Component,
    override val type: EntityType,
    private val pool: LootPool,
    override val experience: Double,
    final override var baseStats: Statistics = Statistics.zero(),
    override var baseSpecials: SpecialStatistics = SpecialStatistics(),
    override var mainHand: MacrocosmItem? = null,
    override var offHand: MacrocosmItem? = null,
    override var helmet: MacrocosmItem? = null,
    override var chestplate: MacrocosmItem? = null,
    override var leggings: MacrocosmItem? = null,
    override var boots: MacrocosmItem? = null,
    private val disguiseSkin: String? = null,
    private val sounds: EntitySoundBank? = null,
    override val rewardingSkill: SkillType = SkillType.COMBAT,
) : MacrocosmEntity {
    override var currentHealth: Float = baseStats.health

    fun register(id: Identifier) {
        Registry.ENTITY.register(id, this)
        if (disguiseSkin != null) {
            Registry.DISGUISE.register(id, disguiseSkin)
        }
        if (sounds != null) {
            Registry.SOUND.register(id, sounds)
        }
    }

    override fun lootPool(player: MacrocosmPlayer?): LootPool {
        return pool
    }

    override fun damage(amount: Float, damager: Entity?) {
        // we cant damage entity template, continue
    }

    override fun kill(damager: Entity?) {
        // see above
    }

    override fun getId(entity: LivingEntity): Identifier {
        return Registry.ENTITY.byValue(this) ?: Identifier.NULL
    }

    override fun loadChanges(entity: LivingEntity) {
        super.loadChanges(entity)

        val id = Registry.ENTITY.byValue(this)!!
        if (Registry.DISGUISE.has(id)) {
            val skin = Registry.DISGUISE.find(id)
            val disguise = PlayerDisguise(nameMm(buildName()), skin)
            DisguiseAPI.disguiseEntity(entity, disguise)
        }
    }
}

fun nameMm(comp: Component): String {
    return MiniMessage.miniMessage().serialize(comp).replace("<!italic>", "")
}
