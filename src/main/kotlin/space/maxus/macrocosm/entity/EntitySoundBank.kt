package space.maxus.macrocosm.entity

import com.google.common.collect.HashMultimap
import net.axay.kspigot.sound.sound
import org.bukkit.Location
import org.bukkit.Sound
import kotlin.random.Random

enum class SoundType {
    DAMAGED,
    DEATH
}

class EntitySoundBank private constructor(private val sounds: HashMultimap<SoundType, Pair<Sound, Float>>) {
    companion object {
        fun from(vararg sounds: Pair<SoundType, Pair<Sound, Float>>): EntitySoundBank {
            val mm = HashMultimap.create<SoundType, Pair<Sound, Float>>()
            for ((type, pair) in sounds) {
                mm.put(type, pair)
            }
            return EntitySoundBank(mm)
        }
    }

    fun playRandom(at: Location, type: SoundType, deltaPitch: Float = .1f) {
        val (sound, pitch) = sounds[type].random()
        sound(sound) {
            this.pitch = pitch + if (Random.nextBoolean()) deltaPitch else -deltaPitch
            playAt(at)
        }
    }

}
