package space.maxus.macrocosm.item

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.text.comp

@Suppress("UNUSED")
enum class AbilityType {
    LEFT_CLICK,
    RIGHT_CLICK,
    SNEAK,
    PASSIVE

    ;

    fun format(abilityName: String): Component {
        val type = comp(if(this == PASSIVE) "" else "<bold><yellow>${name.replace("_", " ")}</yellow></bold>")
        return comp("<gold>Ability: $abilityName").append(type)
    }
}
