package space.maxus.macrocosm.slayer

import com.comphenix.protocol.wrappers.WrappedGameProfile
import net.axay.kspigot.extensions.pluginKey
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
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
import space.maxus.macrocosm.text.text
import java.util.concurrent.ConcurrentLinkedQueue

open class SlayerBase(
    type: EntityType,
    private val slayer: SlayerType,
    tier: Int,
    experience: Double,
    stats: Statistics,
    @Transient
    override var mainHand: MacrocosmItem? = null,
    @Transient
    override var offHand: MacrocosmItem? = null,
    @Transient
    override var helmet: MacrocosmItem? = null,
    @Transient
    override var chestplate: MacrocosmItem? = null,
    @Transient
    override var leggings: MacrocosmItem? = null,
    @Transient
    override var boots: MacrocosmItem? = null,
    disguiseSkin: String? = null,
    sounds: EntitySoundBank? = null,
    @Transient
    override val rewardingSkill: SkillType = SkillType.COMBAT,
    actualName: String? = null,
    disguiseProfile: WrappedGameProfile? = null
) : EntityBase(
    text(actualName ?: "${slayer.slayer.name} ${roman(tier)}"),
    type,
    LootPool.of(*slayer.slayer.drops.filter { it.minTier <= tier }
        .map { MacrocosmDrop(it.drop.item, it.drop.rarity, it.drop.chance, it.amounts[tier] ?: 0..0) }.toTypedArray()),
    experience,
    stats,
    disguiseSkin = disguiseSkin,
    sounds = sounds,
    disguiseProfile = disguiseProfile
) {
    fun summonBy(at: Location, summoner: Player) {
        val entity = spawn(at)
        entity.persistentDataContainer.set(
            pluginKey("SUMMONER"),
            PersistentDataType.STRING,
            summoner.uniqueId.toString()
        )
        summoner.macrocosm!!.boundSlayerBoss = entity.uniqueId
    }

    override fun spawn(at: Location): LivingEntity {
        val e = super.spawn(at)
        if(SlayerAbility.bosses[slayer] == null)
            SlayerAbility.bosses[slayer] = ConcurrentLinkedQueue()
        SlayerAbility.bosses[slayer]!!.add(e.uniqueId)
        return e
    }
}
