package space.maxus.macrocosm.block

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Note
import org.bukkit.Sound
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.entity.Player
import space.maxus.macrocosm.generators.HybridBlockModelGenerator
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType

/**
 * An interface for all custom macrocosm blocks
 */
interface MacrocosmBlock {
    /**
     * The identifier of this block.
     *
     * It is recommended to not include `block` in here, as in most cases it will be appended later
     */
    val id: Identifier

    /**
     * The identifier pointing to resource pack location of the texture for this block. Defaults to `$id.namespace:block/$id.path`
     */
    val texture: Identifier get() = id.let { Identifier(it.namespace, "block/${it.path}") }

    /**
     * The hardness of this block. Breaking speed depends on this value.
     */
    val hardness: Int

    /**
     * The steadiness of this block. Breaking power required to break this block depends on this value.
     */
    val steadiness: Int

    /**
     * Tools, suitable for breaking this block.
     */
    val suitableTools: List<ItemType>

    /**
     * Base skill experience amount that is given after breaking this block
     */
    val baseExperience: Pair<Float, SkillType>

    /**
     * The sounds that this block produces at certain conditions
     */
    val soundBank: BlockSoundBank

    /**
     * Builds a loot pool for this block, to drop for player
     */
    fun pool(player: Player, mc: MacrocosmPlayer): LootPool

    /**
     * Places this block at provided location
     */
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

    companion object {
        internal val blockReferences = hashMapOf<String, Identifier>()

        /**
         * Minimal hardness recommended for blocks
         */
        const val HARDNESS_MIN: Int = 50

        /**
         * Hardness of a vanilla stone block
         */
        const val HARDNESS_STONE: Int = 300

        /**
         * Hardness of a vanilla deepslate block
         */
        const val HARDNESS_DEEPSLATE: Int = 600

        /**
         * Hardness of most types of wood
         */
        const val HARDNESS_WOOD: Int = 250

        /**
         * Minimal steadiness for blocks. Steadiness does not support values below zero, that's why this is `0`
         */
        const val STEADINESS_MIN: Int = 0

        /**
         * Steadiness of a vanilla stone block
         */
        const val STEADINESS_STONE: Int = 1

        /**
         * Steadiness of most vanilla types of wood
         */
        const val STEADINESS_WOOD: Int = STEADINESS_MIN

        /**
         * Steadiness of a vanilla deepslate block
         */
        const val STEADINESS_DEEPSLATE: Int = 3

        /**
         * Converts a masked vanilla note block to a macrocosm block if possible, returns null otherwise
         */
        @Suppress("DEPRECATION")
        fun fromBlockData(nb: NoteBlock): MacrocosmBlock? {
            return Registry.BLOCK.findOrNull(blockReferences["${nb.instrument.name}${nb.note.id}"] ?: return null)
        }
    }

    /**
     * Various pre-made sound banks that can be used for blocks
     */
    object Sounds {
        /**
         * Vanilla deepslate sound bank
         */
        val DEEPSLATE by lazy { newSoundBank("BLOCK_DEEPSLATE") }

        /**
         * Vanilla stone sound bank
         */
        val STONE by lazy { newSoundBank("BLOCK_SOUND") }

        /**
         * Vanilla deepslate sound bank
         */
        val AMETHYST by lazy { newSoundBank("BLOCK_AMETHYST_BLOCK") }

        /**
         * Vanilla netherrack sound bank
         */
        val NETHERRACK by lazy { newSoundBank("BLOCK_NETHERRACK") }

        /**
         * Vanilla grass sound bank
         */
        val GRASS by lazy { newSoundBank("BLOCK_GRASS") }

        /**
         * Vanilla wet grass sound bank
         */
        val WET_GRASS by lazy { newSoundBank("BLOCK_WET_GRASS") }

        /**
         * Vanilla rooted dirt sound bank
         */
        val ROOTED_DIRT by lazy { newSoundBank("BLOCK_ROOTED_DIRT") }

        /**
         * Vanilla basalt sound bank
         */
        val BASALT by lazy { newSoundBank("BLOCK_BASALT") }

        /**
         * Vanilla nether bricks sound bank
         */
        val NETHER_BRICKS by lazy { newSoundBank("BLOCK_NETHER_BRICKS") }

        /**
         * Vanilla metal block sound bank
         */
        val METAL by lazy { newSoundBank("BLOCK_METAL") }

        /**
         * Constructs a new sound pack using a base `org.bukkit.Sound` block prefix
         */
        fun newSoundBank(base: String): BlockSoundBank {
            return BlockSoundBank.from(*BlockSoundType.values()
                .associateWith { (Sound.valueOf("${base}_${it.name}") to 1f) }
                .toList().toTypedArray())
        }
    }
}
