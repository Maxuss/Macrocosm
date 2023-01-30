package space.maxus.macrocosm.npc.ops

import net.axay.kspigot.gui.ForInventory
import net.axay.kspigot.gui.GUI
import net.axay.kspigot.gui.openGUI
import java.util.concurrent.CompletableFuture

/**
 * Opens a KSpigot UI for player
 */
data class NPCOpOpenUI(val open: (NPCOperationData) -> GUI<out ForInventory>): NPCOp {
    override fun operate(data: NPCOperationData): CompletableFuture<Unit> {
        data.playerPaper.openGUI(open(data))
        return CompletableFuture.completedFuture(Unit)
    }
}
