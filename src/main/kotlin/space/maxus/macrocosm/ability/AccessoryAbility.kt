package space.maxus.macrocosm.ability

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.text

abstract class AccessoryAbility(accs: String, description: String): AbilityBase(AbilityType.PASSIVE, "null", description) {
    private val acc: Identifier = Identifier.parse(accs)
    fun hasAccs(player: MacrocosmPlayer): Boolean {
        @Suppress("SENSELESS_COMPARISON") // NPEs are possible for accessory bag (for some reason)
        return player.accessoryBag != null && player.accessoryBag.accessories.any { it.item == acc }
    }

    override fun buildLore(lore: MutableList<Component>, player: MacrocosmPlayer?) {
        val tmp = mutableListOf<Component>()
        for (part in MacrocosmAbility.formatDamageNumbers(description, player).split("<br>")) {
            for (desc in part.reduceToList()) {
                tmp.add(text("<gray>$desc</gray>").noitalic())
            }
        }
        tmp.removeIf {
            ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlank()
        }
        lore.addAll(tmp)
    }
}
