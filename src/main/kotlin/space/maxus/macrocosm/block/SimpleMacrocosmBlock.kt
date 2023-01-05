package space.maxus.macrocosm.block

import org.bukkit.entity.Player
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.AutoRegister
import space.maxus.macrocosm.registry.Clone
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.text.text

open class SimpleMacrocosmBlock(
    id: String,
    val name: String,
    private val itemRarity: Rarity,
    hardness: Number,
    override val steadiness: Int,
    baseExperience: Pair<Number, SkillType>,
    override val suitableTools: List<ItemType>,
    private val pool: LootPool,
    override val soundBank: BlockSoundBank,
    texture: String = id,
) : MacrocosmBlock, AutoRegister<MacrocosmItem> {
    override val hardness: Int = hardness.toInt()
    override val baseExperience: Pair<Float, SkillType> = Pair(baseExperience.first.toFloat(), baseExperience.second)
    override fun pool(player: Player, mc: MacrocosmPlayer): LootPool {
        return pool
    }

    override val id: Identifier = Identifier.parse(id)
    override val texture: Identifier = Identifier.parse(texture).let { Identifier(it.namespace, "block/${it.path}") }

    override fun clone(): Clone {
        return SimpleMacrocosmBlock(
            id.toString(),
            name,
            itemRarity,
            hardness,
            steadiness,
            baseExperience,
            suitableTools,
            pool,
            soundBank,
            texture.toString(),
        )
    }

    override fun register(registry: Registry<MacrocosmItem>) {
        val itemId = Identifier(id.namespace, "${id.path}_block")
        val item = PlaceableItem(itemId, id, text(name), itemRarity)
        registry.register(itemId, item)
    }
}
