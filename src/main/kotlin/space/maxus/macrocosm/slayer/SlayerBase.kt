package space.maxus.macrocosm.slayer

import net.axay.kspigot.extensions.pluginKey
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.entity.EntityBase
import space.maxus.macrocosm.entity.EntitySoundBank
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.loot.MacrocosmDrop
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp

open class SlayerBase(
    type: EntityType,
    slayer: SlayerType,
    tier: Int,
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
    actualName: String? = null
) : EntityBase(
    comp(actualName ?: "${slayer.slayer.name} ${roman(tier)}"),
    type,
    LootPool.of(*slayer.slayer.drops.filter { it.minTier <= tier }.map { MacrocosmDrop(it.drop.item, it.drop.rarity, it.drop.chance, it.amounts[tier] ?: 0..0) }.toTypedArray()),
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
