@file:Suppress("UnstableApiUsage")

package space.maxus.macrocosm.generators

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
    val variant: String
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
    private val noteBlockPowered = AtomicBoolean(false)
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

    fun blockData(id: Identifier): String {
        return enqueued.first { it.model.replace("macrocosm:block/", "") == id.path }.variant
    }

    fun parseBlockData(bd: String): Triple<Instrument, Int, Boolean> {
        val (sInstrument, sNote, sPowered) = bd.replace("instrument=", "").replace("note=", "").replace("powered=", "")
            .split(",")
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
        val note = sNote.toInt()
        val powered = sPowered.toBooleanStrict()
        return Triple(instrument, note, powered)
    }

    private fun nextNoteBlockInfo(): String {
        val powered: Boolean
        var note: Int
        var instrument: String
        val first = firstIndex.getAndSet(false)
        // reversing powered status
        if (noteBlockPowered.getAndSet(!noteBlockPowered.get()).also { powered = it } && !first) {
            // if we have reached false again, update note
            if (noteBlockNote.updateAndGet { if (it >= 24) 1 else it + 1 }.also { note = it } == 1) {
                // looks like we have done a loop on notes as well
                // updating the instrument
                if (noteBlockInstrument.updateAndGet { if (it >= instruments.size) 0 else it + 1 }
                        .also { instrument = instruments[it] } == 0) {
                    // looks like we have done a full loop even on instruments
                    // normally it should not happen
                    throw IllegalStateException("Done a full loop on note block instruments")
                }
            } else {
                // haven't done a loop on notes, just get the instrument
                instrument = instruments[noteBlockInstrument.get()]
            }
        } else {
            // haven't done a full loop on powered status
            note = noteBlockNote.get()
            instrument = instruments[noteBlockInstrument.get()]
        }
        return "instrument=$instrument,note=$note,powered=$powered"
    }

    fun enqueue(block: MacrocosmBlock) {
        CMDGenerator.enqueue(
            Model(
                latestBlockTexture.getAndIncrement(),
                "item/paper",
                block.texture.toString()
            )
        )
        enqueued.add(
            BlockModel(
                "assets/macrocosm/models/block/${block.id.path}.json",
                "block/cube_all",
                "macrocosm:block/${block.id.path}",
                block.texture.toString(),
                nextNoteBlockInfo()
            )
        )
    }

    override fun yieldGenerate(): Map<String, String> {
        val variants = hashMapOf<String, BlockStateVariant>()
        val associated = enqueued.associate {
            variants[it.variant] = BlockStateVariant(it.model)
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
