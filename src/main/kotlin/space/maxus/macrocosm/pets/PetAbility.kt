package space.maxus.macrocosm.pets

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.text.comp

open class PetAbility(val name: String, val description: String) {
    companion object {
        private val PLACEHOLDER_REGEX = "\\[[\\d.]+]".toRegex()
    }

    fun description(pet: StoredPet): List<Component> {
        val tmp = mutableListOf<Component>()
        tmp.add(comp("<gold>$name").noitalic())
        for (desc in description.reduceToList()) {
            tmp.add(comp("<gray>${parseLine(desc, pet.level)}</gray>").noitalic())
        }
        tmp.removeIf {
            ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlankOrEmpty()
        }
        return tmp
    }

    private fun parseLine(line: String, level: Int) = PLACEHOLDER_REGEX.replace(line) {
        val trimmedValue = it.value.trim('[', ']')
        Formatting.withCommas((java.lang.Double.parseDouble(trimmedValue) * level).toBigDecimal())
    }
}
