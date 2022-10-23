package space.maxus.macrocosm.pets

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.text.text

open class PetAbility(val name: String, val description: String) {
    companion object {
        private val PLACEHOLDER_REGEX = "\\[[\\d.]+]".toRegex()
    }

    fun description(pet: StoredPet): List<Component> {
        val tmp = mutableListOf<Component>()
        tmp.add(text("<gold>$name").noitalic())
        for (desc in description.split("<br>")) {
            val moreSplit = desc.reduceToList()
            for (it in moreSplit) {
                tmp.add(text("<gray>${parseLine(it, pet.level)}</gray>").noitalic())
            }
        }
        tmp.removeIf {
            ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlank()
        }
        return tmp
    }

    private fun parseLine(line: String, level: Int) = PLACEHOLDER_REGEX.replace(line) {
        val trimmedValue = it.value.trim('[', ']')
        Formatting.withCommas((java.lang.Double.parseDouble(trimmedValue) * level).toBigDecimal())
    }
}
