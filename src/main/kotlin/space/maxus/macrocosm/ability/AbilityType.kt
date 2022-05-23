package space.maxus.macrocosm.ability

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.text.comp

/**
 * Represents type of the ability, used for lore manipulations *only*
 */
@Suppress("UNUSED")
enum class AbilityType {
    /**
     * Rendered as **LEFT CLICK**
     *
     */
    LEFT_CLICK,

    /**
     * Rendered as **RIGHT CLICK**
     *
     */
    RIGHT_CLICK,

    /**
     * Rendered as **SNEAK**
     *
     */
    SNEAK,

    /**
     * Rendered as an empty component
     *
     */
    PASSIVE

    ;

    /**
     * Formats the provided [abilityName] to include the ability type, rendered as described in each type.
     *
     * @param abilityName Name of the ability to be rendered. May contain MiniMessage tags
     * @return Formatted and colored component **with** italic formatting. Make sure to call [space.maxus.macrocosm.chat.noitalic] on it, if you plan to add it to lore.
     */
    fun format(abilityName: String): Component {
        val type = comp(if (this == PASSIVE) "" else " <bold><yellow>${name.replace("_", " ")}</yellow></bold>")
        return comp("<gold>Ability: $abilityName").append(type)
    }
}
