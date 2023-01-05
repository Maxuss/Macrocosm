package space.maxus.macrocosm.block

import space.maxus.macrocosm.util.game.SoundBank

enum class BlockSoundType {
    PLACE,
    HIT,
    STEP,
    FALL,
    BREAK,
}

typealias BlockSoundBank = SoundBank<BlockSoundType>
