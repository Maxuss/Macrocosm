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
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.util.Identifier

class EntityBase(
    override val name: Component,
    override val type: EntityType,
    private val pool: LootPool,
    override var baseStats: Statistics = Statistics.zero(),
    override var baseSpecials: SpecialStatistics = SpecialStatistics(),
    override var mainHand: MacrocosmItem? = null,
    override var offHand: MacrocosmItem? = null,
    override var helmet: MacrocosmItem? = null,
    override var chestplate: MacrocosmItem? = null,
    override var leggings: MacrocosmItem? = null,
    override var boots: MacrocosmItem? = null,
    private val disguiseSkin: String? = null,
    private val sounds: EntitySoundBank? = null
) : MacrocosmEntity {
    override var currentHealth: Float = baseStats.health

    fun register(id: Identifier) {
        EntityRegistry.register(id, this)
        if (disguiseSkin != null) {
            EntityRegistry.registerDisguise(id, disguiseSkin)
        }
        if (sounds != null) {
            EntityRegistry.registerSounds(id, sounds)
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
        return EntityRegistry.nameOf(this) ?: Identifier.NULL
    }

    override fun loadChanges(entity: LivingEntity) {
        super.loadChanges(entity)

        val id = EntityRegistry.nameOf(this)!!
        if (EntityRegistry.shouldDisguise(id)) {
            val skin = EntityRegistry.findDisguise(id)
            val disguise = PlayerDisguise(nameMm(buildName()), skin)
            DisguiseAPI.disguiseEntity(entity, disguise)
        }
    }
}

fun nameMm(comp: Component): String {
    return MiniMessage.miniMessage().serialize(comp).replace("<!italic>", "")
}
