package space.maxus.macrocosm.item.buffs

import space.maxus.macrocosm.item.runes.RuneType
import space.maxus.macrocosm.registry.Identifier

object BuffRegistry {
    val runes: HashMap<Identifier, RuneType> = hashMapOf()
    private val buffs: HashMap<Identifier, MinorItemBuff> = hashMapOf()

    fun registerRune(id: Identifier, rune: RuneType): RuneType {
        runes[id] = rune
        return rune
    }

    fun registerBuff(id: Identifier, buff: MinorItemBuff): MinorItemBuff {
        buffs[id] = buff
        return buff
    }

    fun findRune(id: Identifier) = runes[id]!!
    fun findBuff(id: Identifier) = buffs[id]!!
}
