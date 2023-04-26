package space.maxus.macrocosm.ui.animation

import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.ComponentSpace
import space.maxus.macrocosm.util.unreachable

/**
 * A helper class for rendering UI animations.
 */
object UIRenderHelper {
    /**
     * Returns a dummy itemstack for the given material.
     */
    fun dummy(mat: Material): ItemStack = ItemValue.placeholder(mat, "")

    /**
     * Instantly sets the given itemstacks in the given space.
     */
    fun instant(canvas: Inventory, item: ItemStack, space: ComponentSpace) {
        for (slot in space.enumerate(UIDimensions.fromRaw(canvas.size) ?: UIDimensions.SIX_X_NINE)) {
            canvas.setItem(slot, item)
        }
    }

    /**
     * Returns a task that will draw the given itemstack in the given space.
     *
     * @param canvas The inventory to draw in.
     * @param item The itemstack to draw.
     * @param space The space to draw in.
     * @param perTick The number of slots to draw per tick.
     * @param frequency The number of ticks per draw.
     */
    fun draw(
        canvas: Inventory,
        item: ItemStack,
        space: ComponentSpace,
        perTick: Int = 1,
        frequency: Int = 1
    ): RenderTask {
        val affected = space.enumerate(UIDimensions.fromRaw(canvas.size) ?: UIDimensions.SIX_X_NINE).toMutableList()
        val task = {
            if (affected.isEmpty())
                listOf()
            else {
                if (perTick <= 1) {
                    val single = affected.removeFirst()
                    canvas.setItem(single, item)
                    listOf(single)
                } else {
                    val all = mutableListOf<Int>()
                    for (amount in 0..perTick) {
                        val removed = affected.removeFirst()
                        all.add(removed)
                        canvas.setItem(removed, item)
                    }
                    all
                }
            }
        }
        return RenderTask(frequency, task) {
            affected.isEmpty()
        }
    }

    /**
     * Returns a task that will slowly replace space with certain itemstack.
     * The logic of the task is as following:
     * 1. Replace space with [item].
     * 2. Wait for [frequency] ticks.
     * 3. Replace space with [replacement] slot by slot.
     *
     * @param canvas The inventory to draw in.
     * @param item The itemstack to draw.
     * @param replacement The replacement itemstack.
     * @param space The space to draw in.
     * @param perTick The number of slots to draw per tick.
     * @param frequency  The number of ticks per draw.
     */
    fun instantDissolve(
        canvas: Inventory,
        item: ItemStack,
        replacement: ItemStack,
        space: ComponentSpace,
        perTick: Int = 1,
        frequency: Int = 1
    ): RenderTask {
        val affected = space.enumerate(UIDimensions.fromRaw(canvas.size) ?: UIDimensions.SIX_X_NINE).toMutableList()
        for (slot in affected) {
            canvas.setItem(slot, item)
        }
        return draw(canvas, replacement, space, perTick, frequency)
    }

    /**
     * Returns a task that will slowly replace space with certain itemstack.
     * The logic of the task is as following:
     * 1. Replace space with [item] slot by slot.
     * 2. Wait for [frequency] ticks.
     * 3. Replace space with [replacement] slot by slot.
     *
     * @param canvas The inventory to draw in.
     * @param item The itemstack to draw.
     * @param replacement The replacement itemstack.
     * @param space The space to draw in.
     * @param perTick The number of slots to draw per tick.
     * @param frequency  The number of ticks per draw.
     */
    fun drawDissolve(
        canvas: Inventory,
        item: ItemStack,
        replacement: ItemStack,
        space: ComponentSpace,
        perTick: Int = 1,
        frequency: Int = 1,
        delay: Int = frequency
    ): RenderTask {
        return draw(canvas, item, space, perTick, frequency).join(
            draw(
                canvas,
                replacement,
                space,
                perTick,
                frequency
            ).delay(delay)
        )
    }

    /**
     * Returns a task that will slowly replace space with certain itemstack.
     *
     * @param base The itemstack to draw as the base of the fire.
     * @param flame The itemstack to draw as the tip of the fire.
     * @param empty The itemstack to draw as the empty space.
     */
    fun burn(
        canvas: Inventory,
        base: ItemStack,
        flame: ItemStack,
        empty: ItemStack,
        space: ComponentSpace,
        frequency: Int = 1,
        delay: Int = frequency + 1
    ): RenderTask {
        return draw(canvas, base, space, 1, frequency)
            .join(
                draw(canvas, flame, space, 1, frequency).delay(delay)
            )
            .join(
                draw(canvas, empty, space, 1, frequency).delay(delay + 1)
            )
    }
}

/**
 * A task that can be run to draw an itemstack in a given space.
 *
 * @param processor The function to run to draw the itemstack.
 * @param isComplete The function to check if the task is complete.
 * @param frequency The number of ticks required to draw.
 */
open class RenderTask(
    protected val frequency: Int,
    private val processor: () -> List<Int>,
    val isComplete: () -> Boolean
) {
    private val processedSlots: MutableList<Int> = mutableListOf()
    protected var delay: Int = 0
    protected var frequencyTick: Int = frequency
    protected var sound: () -> Unit = { }
    open fun tick() {
        if (frequencyTick - 1 > 0) {
            frequencyTick -= 1
            return
        } else frequencyTick = frequency
        if (delay > 0) {
            delay -= 1
            return
        }
        sound()
        val processed = this.processor()
        processedSlots.addAll(processed)
    }

    /**
     * Adds ticks to the delay.
     */
    fun delay(ticks: Int): RenderTask {
        delay += ticks
        return this
    }

    /**
     * Returns a task that will run this task and the other task.
     */
    fun join(other: RenderTask): RenderTask {
        return RenderTaskPair(this, other)
    }

    /**
     * Plays a sound while rendering.
     *
     * @param player The player to play the sound for.
     * @param snd The sound to play.
     * @param volume The volume of the sound.
     * @param pitch The pitch of the sound.
     *
     * @return The current render task.
     */
    open fun sound(player: Player, snd: Sound, volume: Float = .5f, pitch: Float = 1f): RenderTask {
        this.sound = {
            sound(snd) {
                this.pitch = pitch
                this.volume = volume
                playFor(player)
            }
        }
        return this
    }
}

/**
 * A render task that runs two tasks one after the other.
 */
data class RenderTaskPair(
    val first: RenderTask,
    val second: RenderTask
) : RenderTask(1, { unreachable() }, { first.isComplete() && second.isComplete() }) {
    override fun tick() {
        if (frequencyTick - 1 > 0) {
            frequencyTick -= 1
            return
        } else frequencyTick = frequency
        if (delay > 0) {
            delay -= 1
            return
        }
        first.tick()
        second.tick()
    }

    override fun sound(player: Player, snd: Sound, volume: Float, pitch: Float): RenderTask {
        this.first.sound(player, snd, volume, pitch)
        return this
    }
}
