package space.maxus.macrocosm.util.game

import com.google.common.collect.ArrayListMultimap
import net.axay.kspigot.sound.sound
import org.bukkit.Location
import org.bukkit.Sound
import kotlin.random.Random

open class SoundBank<S> private constructor(private val sounds: ArrayListMultimap<S, Pair<Sound, Float>>) {
    companion object {
        fun <S> from(vararg sounds: Pair<S, Pair<Sound, Float>>): SoundBank<S> {
            val mm = ArrayListMultimap.create<S, Pair<Sound, Float>>()
            for ((type, pair) in sounds) {
                mm.put(type, pair)
            }
            return SoundBank(mm)
        }
    }

    fun playRandom(at: Location, type: S, deltaPitch: Float = .1f) {
        val (sound, pitch) = sounds[type].randomOrNull() ?: return
        sound(sound) {
            this.pitch = pitch + if (Random.nextBoolean()) deltaPitch else -deltaPitch
            playAt(at)
        }
    }

    fun playRandom(at: Location, type: S, volume: Float, deltaPitch: Float = .1f) {
        val (sound, pitch) = sounds[type].randomOrNull() ?: return
        sound(sound) {
            this.volume = volume
            this.pitch = pitch + if (Random.nextBoolean()) deltaPitch else -deltaPitch
            playAt(at)
        }
    }
}
