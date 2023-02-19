package space.maxus.macrocosm.block

import net.axay.kspigot.extensions.events.clickedBlockExceptAir
import net.axay.kspigot.runnables.KSpigotRunnable
import net.axay.kspigot.runnables.task
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.NoteBlock
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.GenericGameEvent
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.events.MineTickEvent
import space.maxus.macrocosm.events.PlayerBreakBlockEvent
import space.maxus.macrocosm.events.StopBreakingBlockEvent
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Registry
import java.util.*
import java.util.concurrent.ConcurrentHashMap


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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInteract(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_BLOCK && e.item?.macrocosm is PlaceableItem && Macrocosm.isSandbox) {
            val item = e.item!!.macrocosm!! as PlaceableItem
            if (e.player.gameMode != GameMode.CREATIVE) {
                e.item!!.amount -= 1
            }
            val placedBlock = e.clickedBlockExceptAir!!.getRelative(e.blockFace)
            val block = Registry.BLOCK.find(item.blockId)
            block.place(e.player, e.player.macrocosm!!, placedBlock.location)
            block.soundBank.playRandom(placedBlock.location, BlockSoundType.PLACE, 1.0f, .2f)
            e.isCancelled = true
        } else if (e.action == Action.RIGHT_CLICK_BLOCK && e.hasBlock() && e.clickedBlockExceptAir?.type == Material.NOTE_BLOCK) {
            e.isCancelled = true
        }
    }

    private fun updateAndCheck(loc: Location) {
        val b: Block = loc.block.getRelative(BlockFace.UP)
        if (b.type === Material.NOTE_BLOCK) b.state.update(true, true)
        val nextBlock: Block = b.getRelative(BlockFace.UP)
        if (nextBlock.type === Material.NOTE_BLOCK) updateAndCheck(b.location)
    }

    object WoodHandlers : Listener {
        val breakingSound = ConcurrentHashMap<UUID, KSpigotRunnable>()

        @EventHandler
        fun onBreakWood(e: PlayerBreakBlockEvent) {
            breakingSound.remove(e.player.ref)?.cancel()
            if (e.block.type == Material.NOTE_BLOCK) {
                val mc = MacrocosmBlock.fromBlockData(e.block.blockData as NoteBlock) ?: return
                mc.soundBank.playRandom(e.block.location, BlockSoundType.BREAK, 0f)
            } else if (e.block.blockData.soundGroup.breakSound == Sound.BLOCK_WOOD_BREAK) {
                // intercept the sound, playing our custom wood break sound
                e.block.world.playSound(e.block.location, "minecraft:custom.block.wood.break", .8f, 1.0f)
            }
        }

        @EventHandler
        fun onHitWood(e: MineTickEvent) {
            if (breakingSound.containsKey(e.player.ref))
                return
            if (e.block.type == Material.NOTE_BLOCK) {
                val mc = MacrocosmBlock.fromBlockData(e.block.blockData as NoteBlock) ?: return
                breakingSound[e.player.ref] = task(false, delay = 2L, period = 4L) {
                    mc.soundBank.playRandom(e.block.location, BlockSoundType.BREAK, .25f, .5f)
                }!!
            } else if (e.block.blockData.soundGroup.hitSound == Sound.BLOCK_WOOD_HIT) {
                breakingSound[e.player.ref] = task(false, delay = 2L, period = 4L) {
                    e.block.world.playSound(e.block.location, "minecraft:custom.block.wood.hit", .25f, .5f)
                }!!
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        fun onStopHittingWood(e: StopBreakingBlockEvent) {
            breakingSound.remove(e.player.ref)?.cancel()
        }


        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        fun onStepFall(e: GenericGameEvent) {
            val entity: Entity = e.entity ?: return
            if (entity !is Player)
                return
            if (e.event != GameEvent.HIT_GROUND && e.event != GameEvent.STEP)
                return
            val eLoc: Location = entity.location
            if (!e.location.chunk.isLoaded || !eLoc.chunk.isLoaded) return

            val gameEvent: GameEvent = e.event
            val currentBlock: Block = entity.location.block
            val blockBelow = currentBlock.getRelative(BlockFace.DOWN)

            if (blockBelow.type == Material.NOTE_BLOCK) {
                val mc = MacrocosmBlock.fromBlockData(blockBelow.blockData as NoteBlock) ?: return
                if (gameEvent == GameEvent.HIT_GROUND) {
                    mc.soundBank.playRandom(entity.location, BlockSoundType.FALL, .5f, 0f)
                } else if (gameEvent == GameEvent.STEP) {
                    mc.soundBank.playRandom(entity.location, BlockSoundType.STEP, .15f, 0f)
                }
            } else if (gameEvent == GameEvent.HIT_GROUND && blockBelow.blockData.soundGroup.fallSound == Sound.BLOCK_WOOD_FALL) {
                blockBelow.world.playSound(entity.location, "minecraft:custom.block.wood.fall", .5f, .75f)
            } else if (gameEvent == GameEvent.STEP && blockBelow.blockData.soundGroup.stepSound == Sound.BLOCK_WOOD_STEP) {
                blockBelow.world.playSound(entity.location, "minecraft:custom.block.wood.step", .15f, .1f)
            }
        }
    }
}
