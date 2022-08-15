package space.maxus.macrocosm.spell.essence

import net.kyori.adventure.text.Component
import org.bukkit.Material
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.text.text

enum class EssenceType(val display: String, val description: String, val displayItem: Material) {
    FIRE("<gold>Ardens", "Gathered straight out of <gold>blaze's<gray> core.", Material.YELLOW_GLAZED_TERRACOTTA),
    FROST("<aqua>Gelu", "This chilly essence burns your hands with frost.", Material.LIGHT_BLUE_GLAZED_TERRACOTTA),
    WATER("<blue>Amnis", "It smells like rotten seaweed.", Material.BLUE_GLAZED_TERRACOTTA),
    SHADE("<dark_purple>Umbra", "Gathered from the darkness of Abyss.", Material.PURPLE_GLAZED_TERRACOTTA),
    LIFE("<green>Vita", "This essence gives life to the world around you.", Material.GREEN_GLAZED_TERRACOTTA),
    DEATH("<red>Obitus", "This essence takes life from the world around you.", Material.BLACK_GLAZED_TERRACOTTA),
    CONNECTION("<rainbow>Confluxus", "This essence was deemed <light_purple>mythical<gray> and <red>nonexistent<gray> for a long time.", Material.WHITE_GLAZED_TERRACOTTA)
    ;

    fun descript(list: MutableList<Component>) {
        for(part in this.description.reduceToList(21)) {
            if(part.isBlankOrEmpty())
                continue
            list.add(text("<gray>$part").noitalic())
        }
    }
}
