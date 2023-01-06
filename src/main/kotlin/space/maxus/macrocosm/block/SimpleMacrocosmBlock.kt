package space.maxus.macrocosm.block

import org.bukkit.entity.Player
import space.maxus.macrocosm.generators.HybridBlockModelGenerator
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.AutoRegister
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.text.text

/**
 * A wrapper over a [MacrocosmBlock] that allows to generate and register a [PlaceableItem], as well
 * as a model both for the block and the item using the [HybridBlockModelGenerator].
 *
 * This is an open class, so you may override it for further extending
 *
 * @param id Unparsed ID of the block and item. It will further be parsed using [Identifier.parse], so make sure
 * to include your own namespace if needed!
 * @param name Name of the block item. Supports MiniMessage formatting
 * @param hardness The hardness of this block. Breaking speed depends on this value.
 * Even though it supports a [Number], the value will be rounded to an [Int]
 * @param steadiness The steadiness of this block. Breaking power required to break this block depends on this value.
 * Values below `0` are not supported
 * @param baseExperience Base skill experience amount that is given after breaking this block
 * @param suitableTools Tools, suitable for breaking this block.
 * @param pool The static loot pool which will be used to determine the items to drop from this block. To generate a dynamic loot pool, override the [pool] method
 * @param soundBank The sound bank containing events for this block, e.g. when it is broken/placed.
 * @param texture Unparsed ID pointing to the texture of this block. By default, it is equal to [id]
 */
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

    override fun register(registry: Registry<MacrocosmItem>) {
        val itemId = Identifier(id.namespace, "${id.path}_block")
        val item = PlaceableItem(itemId, id, text(name), itemRarity)
        registry.register(itemId, item)
    }
}
