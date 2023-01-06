package space.maxus.macrocosm.entity

import space.maxus.macrocosm.util.game.SoundBank

enum class EntitySoundType {
    DAMAGED,
    DEATH
}

typealias EntitySoundBank = SoundBank<EntitySoundType>
