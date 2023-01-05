package space.maxus.macrocosm.block

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.entity.Player
import space.maxus.macrocosm.generators.HybridBlockModelGenerator
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Clone
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType

interface MacrocosmBlock : Clone {
    val id: Identifier
    val texture: Identifier get() = id.let { Identifier(it.namespace, "block/${it.path}") }
    val hardness: Int
    val steadiness: Int
    val suitableTools: List<ItemType>
    val baseExperience: Pair<Float, SkillType>

    fun pool(player: Player, mc: MacrocosmPlayer): LootPool

    fun place(placer: Player, mc: MacrocosmPlayer, at: Location) {
        val (instrument, note) = HybridBlockModelGenerator.model(
            id
        )?.variant ?: return
        at.block.setType(Material.NOTE_BLOCK, false)
        val nb = at.block.blockData as NoteBlock
        nb.instrument = instrument
        nb.note = Note(note)
        nb.isPowered = false
        at.block.blockData = nb
    }

    override fun clone(): Clone {
        throw IllegalStateException("Override the clone method for MacrocosmBlock!")
    }

    companion object {
        val blockReferences = hashMapOf<String, Identifier>()

        const val HARDNESS_MIN: Int = 50
        const val HARDNESS_STONE: Int = 300
        const val HARDNESS_DEEPSLATE: Int = 600
        const val HARDNESS_WOOD: Int = 250

        const val STEADINESS_MIN: Int = 0
        const val STEADINESS_STONE: Int = 1
        const val STEADINESS_WOOD: Int = STEADINESS_MIN
        const val STEADINESS_DEEPSLATE: Int = 3

        @Suppress("DEPRECATION")
        fun fromBlockData(nb: NoteBlock): MacrocosmBlock? {
            return Registry.BLOCK.findOrNull(blockReferences["${nb.instrument.name}${nb.note.id}"]!!)
        }
    }
}
