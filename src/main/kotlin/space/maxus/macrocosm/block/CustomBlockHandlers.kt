package space.maxus.macrocosm.block

import net.axay.kspigot.extensions.events.clickedBlockExceptAir
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Door
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent


object CustomBlockHandlers : Listener {
    @EventHandler(ignoreCancelled = true)
    fun onBlockPhysics(e: BlockPhysicsEvent) {
        val b: Block = e.block
        val topBlock: Block = b.getRelative(BlockFace.UP) // Block (y + 1)
        val bottomBlock: Block = b.getRelative(BlockFace.DOWN) // Block (y - 1)


        if (topBlock.type === Material.NOTE_BLOCK) {
            updateAndCheck(b.location)
            if (Tag.DOORS.isTagged(b.type) && b.blockData is Door) {
                val data: Door = b.blockData as Door
                if (data.half != Bisected.Half.TOP) return
                val d: Door = bottomBlock.blockData as Door
                d.isOpen = data.isOpen
                bottomBlock.blockData = d
                bottomBlock.state.update(true, false)
            }
            e.isCancelled = true
            if (!Tag.SIGNS.isTagged(b.type) && b.type != Material.LECTERN && !b.type.toString()
                    .contains("BEE")
            ) b.state.update(true, false)
        }
    }

    @EventHandler
    fun onPistonExtends(event: BlockPistonExtendEvent) {
        if (event.blocks.stream().anyMatch { b: Block -> b.type == Material.NOTE_BLOCK }) event.isCancelled =
            true
    }

    @EventHandler
    fun onPistonRetract(event: BlockPistonRetractEvent) {
        if (event.blocks.stream().anyMatch { b: Block -> b.type == Material.NOTE_BLOCK }) event.isCancelled =
            true
    }

    @EventHandler
    fun onNotePlay(event: NotePlayEvent) {
        event.isCancelled = true
    }

    @EventHandler
    fun onNoteInteract(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.hasBlock() && e.clickedBlockExceptAir?.type == Material.NOTE_BLOCK) {
            e.isCancelled = true
        }
    }

    private fun updateAndCheck(loc: Location) {
        val b: Block = loc.block.getRelative(BlockFace.UP)
        if (b.type === Material.NOTE_BLOCK) b.state.update(true, true)
        val nextBlock: Block = b.getRelative(BlockFace.UP)
        if (nextBlock.type === Material.NOTE_BLOCK) updateAndCheck(b.location)
    }
}
