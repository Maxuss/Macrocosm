package space.maxus.macrocosm.util.game

import com.google.common.collect.ArrayListMultimap
import net.kyori.adventure.sound.Sound
import org.bukkit.Location
import kotlin.random.Random

open class SoundBank<S> private constructor(private val sounds: ArrayListMultimap<S, Sound>) {
    companion object {
        fun <S> from(vararg sounds: Pair<S, Pair<org.bukkit.Sound, Float>>): SoundBank<S> {
            val mm = ArrayListMultimap.create<S, Sound>()
            for ((type, pair) in sounds) {
                mm.put(type, Sound.sound(pair.first.key.key(), Sound.Source.NEUTRAL, 1f, pair.second))
            }
            return SoundBank(mm)
        }

        fun <S> fromAdventure(vararg sounds: Pair<S, Sound>): SoundBank<S> {
            val mm = ArrayListMultimap.create<S, Sound>()
            for ((type, sound) in sounds) {
                mm.put(type, sound)
            }
            return SoundBank(mm)
        }
    }

    fun playRandom(at: Location, type: S, deltaPitch: Float = .1f) {
        val sound = sounds[type].randomOrNull() ?: return
        val newSound = Sound.sound(sound)
            .pitch(sound.pitch().let { Random.nextFloat().coerceIn(it - deltaPitch, it + deltaPitch) }).build()
        at.world.playSound(newSound, at.x, at.y, at.z)
    }

    fun playRandom(at: Location, type: S, volume: Float, deltaPitch: Float = .1f) {
        val sound = sounds[type].randomOrNull() ?: return
        val newSound = Sound.sound(sound)
            .volume(volume)
            .pitch(sound.pitch().let { Random.nextFloat().coerceIn(it - deltaPitch, it + deltaPitch) }).build()
        at.world.playSound(newSound, at.x, at.y, at.z)
    }
}
