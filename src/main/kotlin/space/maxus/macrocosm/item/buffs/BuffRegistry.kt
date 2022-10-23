package space.maxus.macrocosm.item.buffs

import org.jetbrains.annotations.ApiStatus
import space.maxus.macrocosm.item.runes.RuneType
import space.maxus.macrocosm.registry.Identifier

@Suppress("DEPRECATION")
@ApiStatus.ScheduledForRemoval(inVersion = "0.3.0")
@Deprecated("BuffRegistry is deprecated. Use the new registries instead.", ReplaceWith("Registry", "space.maxus.macrocosm.registry"))
object BuffRegistry {
    @Deprecated("Use the new registries inside the global registry instead")
    val runes: HashMap<Identifier, RuneType> = hashMapOf()
    private val buffs: HashMap<Identifier, MinorItemBuff> = hashMapOf()

    @Deprecated("Use the new registries inside the global registry instead")
    fun registerRune(id: Identifier, rune: RuneType): RuneType {
        runes[id] = rune
        return rune
    }

    @Deprecated("Use the new registries inside the global registry instead")
    fun registerBuff(id: Identifier, buff: MinorItemBuff): MinorItemBuff {
        buffs[id] = buff
        return buff
    }

    @Deprecated("Use the new registries inside the global registry instead", ReplaceWith("Registry.RUNE.find"))
    fun findRune(id: Identifier) = runes[id]!!
    @Deprecated("Use the new registries inside the global registry instead", ReplaceWith("Registry.ITEM_BUFF.find"))
    fun findBuff(id: Identifier) = buffs[id]!!
}
