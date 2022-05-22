package space.maxus.macrocosm.item.buffs

import space.maxus.macrocosm.item.runes.ApplicableRune
import space.maxus.macrocosm.registry.Identifier

object BuffRegistry {
    private val runes: HashMap<Identifier, ApplicableRune> = hashMapOf()
    private val buffs: HashMap<Identifier, MinorItemBuff> = hashMapOf()

    fun registerRune(id: Identifier, rune: ApplicableRune): ApplicableRune {
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
