package space.maxus.macrocosm.npc.ops

import org.bukkit.Sound
import java.util.concurrent.CompletableFuture

/**
 * Plays a sound
 */
data class NPCOpPlaySound(val sound: Sound, val pitch: Number, val volume: Number): NPCOp {
    override fun operate(data: NPCOperationData): CompletableFuture<Unit> {
        data.playerPaper.location.world.playSound(data.selfPaper.location, sound, pitch.toFloat(), volume.toFloat())
        return CompletableFuture.completedFuture(Unit)
    }
}
