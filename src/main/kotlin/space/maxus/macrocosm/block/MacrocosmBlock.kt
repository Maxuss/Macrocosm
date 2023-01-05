package space.maxus.macrocosm.block

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.entity.Player
import space.maxus.macrocosm.generators.HybridBlockModelGenerator
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Clone
import space.maxus.macrocosm.registry.Identifier

interface MacrocosmBlock : Clone {
    val id: Identifier
    val texture: Identifier get() = id.let { Identifier(it.namespace, "block/${it.path}") }
    val durability: Long

    fun pool(player: Player, mc: MacrocosmPlayer): LootPool

    fun place(placer: Player, mc: MacrocosmPlayer, at: Location) {
        val (instrument, note) = HybridBlockModelGenerator.blockData(
            id
        )
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
}
