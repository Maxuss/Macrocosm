package space.maxus.macrocosm.item.buffs

import space.maxus.macrocosm.item.runes.ApplicableRune
import space.maxus.macrocosm.util.Identifier

object BuffRegistry {
    private val runes: HashMap<Identifier, ApplicableRune> = hashMapOf()

    fun registerRune(id: Identifier, rune: ApplicableRune): ApplicableRune {
        runes[id] = rune
        return rune
    }

    fun findRune(id: Identifier) = runes[id]!!
}
