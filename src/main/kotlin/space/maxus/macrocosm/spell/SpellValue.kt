package space.maxus.macrocosm.spell

import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.spell.types.SparklesSpell
import space.maxus.macrocosm.util.generic.id

enum class SpellValue(val spell: Spell) {
    SPARKLES(SparklesSpell)
    ;
    companion object {
        fun initSpells() {
            for(value in values()) {
                Registry.SPELL.register(id(value.name.lowercase()), value.spell)
                value.spell.registerListeners()
            }
        }
    }
}
