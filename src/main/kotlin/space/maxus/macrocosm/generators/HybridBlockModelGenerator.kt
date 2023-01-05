@file:Suppress("UnstableApiUsage")

package space.maxus.macrocosm.generators

import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import org.bukkit.Instrument
import space.maxus.macrocosm.block.MacrocosmBlock
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.GSON
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

const val BEGIN_BLOCK_TEXTURES = 3500

private data class BlockModel(
    val path: String,
    val parent: String,
    val model: String,
    val textures: String,
    val variant: Pair<Instrument, Int>
)

private class BlockStateVariants(
    val variants: HashMap<String, BlockStateVariant>
)

private class BlockStateVariant(val model: String)

private class SingleBlockModel(val parent: String, val textures: HashMap<String, String>)

object HybridBlockModelGenerator : ResGenerator {
    private val enqueued: ConcurrentLinkedQueue<BlockModel> = ConcurrentLinkedQueue()
    private val latestBlockTexture = AtomicInteger(BEGIN_BLOCK_TEXTURES)
    private val firstIndex = AtomicBoolean(true)
    private val noteBlockNote = AtomicInteger(1)
    private val noteBlockInstrument = AtomicInteger(0)
    private val instruments =
        arrayOf(
            "banjo",
            "basedrum",
            "bass",
            "bell",
            "bit",
            "chime",
            "cow_bell",
            "creeper",
            "custom_head",
            "didgeridoo",
            "dragon",
            "flute",
            "guitar",
            "harp",
            "hat",
            "iron_xylophone",
            "piglin",
            "pling",
            "skeleton",
            "snare",
            "wither_skeleton",
            "xylophone",
            "zombie"
        )

    fun blockData(id: Identifier): Pair<Instrument, Int> {
        return enqueued.first { it.model.replace("macrocosm:block/", "") == id.path }.variant
    }

    private fun nextNoteBlockInfo(): Pair<Instrument, Int> {
        val note: Int
        val sInstrument: String
        val first = firstIndex.getAndSet(false)
        // if we have reached false again, update note
        if (noteBlockNote.getAndUpdate { if (it >= 24) 1 else it + 1 }.also { note = it } == 1 && !first) {
            // looks like we have done a loop on notes as well
            // updating the instrument
            if (noteBlockInstrument.updateAndGet { if (it >= instruments.size) 0 else it + 1 }
                    .also { sInstrument = instruments[it] } == 0) {
                // looks like we have done a full loop even on instruments
                // normally it should not happen
                throw IllegalStateException("Done a full loop on note block instruments")
            }
        } else {
            // haven't done a loop on notes, just get the instrument
            sInstrument = instruments[noteBlockInstrument.get()]
        }
        val instrument = when (sInstrument) {
            "banjo" -> Instrument.BANJO
            "basedrum" -> Instrument.BASS_DRUM
            "bass" -> Instrument.BASS_GUITAR
            "bell" -> Instrument.BELL
            "bit" -> Instrument.BIT
            "chime" -> Instrument.CHIME
            "cow_bell" -> Instrument.COW_BELL
            "creeper" -> Instrument.CREEPER
            "custom_head" -> Instrument.CUSTOM_HEAD
            "didgeridoo" -> Instrument.DIDGERIDOO
            "dragon" -> Instrument.DRAGON
            "flute" -> Instrument.FLUTE
            "guitar" -> Instrument.GUITAR
            "harp" -> Instrument.PIANO
            "hat" -> Instrument.STICKS
            "iron_xylophone" -> Instrument.IRON_XYLOPHONE
            "piglin" -> Instrument.PIGLIN
            "pling" -> Instrument.PLING
            "skeleton" -> Instrument.SKELETON
            "snare" -> Instrument.SNARE_DRUM
            "wither_skeleton" -> Instrument.WITHER_SKELETON
            "xylophone" -> Instrument.XYLOPHONE
            "zombie" -> Instrument.ZOMBIE
            else -> Instrument.PIANO
        }
        return Pair(instrument, note)
    }

    fun enqueue(block: MacrocosmBlock) {
        CMDGenerator.enqueue(
            Model(
                latestBlockTexture.getAndIncrement(),
                "item/paper",
                block.texture.toString()
            )
        )
        val nextInfo = nextNoteBlockInfo()
        MacrocosmBlock.blockReferences["${nextInfo.first.name}${nextInfo.second}"] = block.id
        enqueued.add(
            BlockModel(
                "assets/macrocosm/models/block/${block.id.path}.json",
                "block/cube_all",
                "macrocosm:block/${block.id.path}",
                block.texture.toString(),
                nextInfo
            )
        )
    }

    override fun yieldGenerate(): Map<String, String> {
        val variants = hashMapOf<String, BlockStateVariant>()
        val associated = enqueued.associate {
            val base =
                "instrument=${NoteBlockInstrument.values()[it.variant.first.ordinal].name.lowercase()},note=${it.variant.second},"
            variants["${base}powered=false"] = BlockStateVariant(it.model)
            variants["${base}powered=true"] = BlockStateVariant(it.model)
            it.path to GSON.toJson(SingleBlockModel(it.parent, hashMapOf("all" to it.textures)))
        }.toMutableMap()
        associated["assets/minecraft/blockstates/note_block.json"] =
            GSON.toJson(BlockStateVariants(variants.apply {
                this["instrument=banjo,note=0,powered=false"] = BlockStateVariant("block/note_block")
                this["instrument=banjo,note=0,powered=true"] = BlockStateVariant("block/note_block")
            }))
        return associated
    }
}
