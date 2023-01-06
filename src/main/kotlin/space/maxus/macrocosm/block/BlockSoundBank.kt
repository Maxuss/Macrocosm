package space.maxus.macrocosm.block

import space.maxus.macrocosm.util.game.SoundBank

/**
 * Sound type to interact with custom blocks
 */
enum class BlockSoundType {
    /**
     * Played when a custom block is placed
     */
    PLACE,

    /**
     * Played when a custom block is mined for one tick
     */
    HIT,

    /**
     * Played when a custom block is stepped on
     */
    STEP,

    /**
     * Played when an entity falls on a custom block
     */
    FALL,

    /**
     * Played when a custom block is broken
     */
    BREAK,
}

/**
 * A type alias to easier access block-related sound bank
 */
typealias BlockSoundBank = SoundBank<BlockSoundType>
