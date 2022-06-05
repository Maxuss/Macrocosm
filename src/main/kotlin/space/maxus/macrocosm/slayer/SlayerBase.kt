package space.maxus.macrocosm.slayer

import net.axay.kspigot.extensions.pluginKey
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.entity.EntityBase
import space.maxus.macrocosm.entity.EntitySoundBank
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.Statistics

open class SlayerBase(
    name: Component,
    type: EntityType,
    pool: LootPool,
    experience: Double,
    stats: Statistics,
    override var mainHand: MacrocosmItem? = null,
    override var offHand: MacrocosmItem? = null,
    override var helmet: MacrocosmItem? = null,
    override var chestplate: MacrocosmItem? = null,
    override var leggings: MacrocosmItem? = null,
    override var boots: MacrocosmItem? = null,
    disguiseSkin: String? = null,
    sounds: EntitySoundBank? = null,
    override val rewardingSkill: SkillType = SkillType.COMBAT,
) : EntityBase(
    name,
    type,
    pool,
    experience,
    stats,
    disguiseSkin = disguiseSkin,
    sounds = sounds
) {
    fun summonBy(at: Location, summoner: Player) {
        val entity = spawn(at)
        entity.persistentDataContainer.set(pluginKey("SUMMONER"), PersistentDataType.STRING, summoner.uniqueId.toString())
        summoner.macrocosm!!.summonedBoss = entity.uniqueId
    }
}
