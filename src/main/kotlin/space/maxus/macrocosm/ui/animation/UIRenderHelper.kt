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

object UIRenderHelper {
    fun dummy(mat: Material): ItemStack = ItemValue.placeholder(mat, "")

    fun instant(canvas: Inventory, item: ItemStack, space: ComponentSpace) {
        for (slot in space.enumerate(UIDimensions.fromRaw(canvas.size) ?: UIDimensions.SIX_X_NINE)) {
            canvas.setItem(slot, item)
        }
    }

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

    fun delay(ticks: Int): RenderTask {
        delay += ticks
        return this
    }

    fun join(other: RenderTask): RenderTask {
        return RenderTaskPair(this, other)
    }

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
