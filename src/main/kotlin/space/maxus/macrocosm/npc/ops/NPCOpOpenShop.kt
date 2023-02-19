package space.maxus.macrocosm.npc.ops

import net.axay.kspigot.gui.openGUI
import net.axay.kspigot.runnables.task
import space.maxus.macrocosm.npc.shop.shopUi
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import java.util.concurrent.CompletableFuture

/**
 * An operation that opens a shop to player
 */
class NPCOpOpenShop(val id: Identifier) : NPCOp {
    override fun operate(data: NPCOperationData): CompletableFuture<Unit> {
        task(sync = true) {
            data.playerPaper.openGUI(shopUi(data.player, Registry.SHOP.find(id)))
        }
        return CompletableFuture.completedFuture(Unit)
    }
}
