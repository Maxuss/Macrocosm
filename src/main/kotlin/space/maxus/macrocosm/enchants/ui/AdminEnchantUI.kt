package space.maxus.macrocosm.enchants.ui

import net.axay.kspigot.gui.*
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.ValidatingPrompt
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.Enchantment
import space.maxus.macrocosm.enchants.UltimateEnchantment
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.pad
import java.util.*

fun adminEnchantUi(item: MacrocosmItem, search: String = ""): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    defaultPage = 0
    title = text("Enchanting")

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        val compound = createCompound<Enchantment>({ e ->
            val name = e.name
            val desc = e.description(1).map { it.str() }.toTypedArray()

            val conflicts = e.conflicts.mapNotNull { Registry.ENCHANT.findOrNull(it)?.name?.let { i -> "<blue>${i.replace(" ", " <blue>")}" } }.joinToString(separator = ", ", prefix = "Conflicts with: ").reduceToList()

            ItemValue.placeholderDescripted(
                Material.ENCHANTED_BOOK,
                if(e is UltimateEnchantment) "<light_purple><bold>$name" else "<blue>$name",
                *desc,
                "",
                "Levels: <blue>${e.levels.let { "${it.first}<gray>-<blue>${it.last}" }}",
                *conflicts.toTypedArray(),
                "Applicable to:",
                *e.applicable.map { " - <blue>${it.name.replace("_", " ").capitalized()}" }.toTypedArray(),
                "",
                "<yellow>Click to select level!"
            )
        }, { e, ench ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(specificAdminEnchUi(e.player.macrocosm!!, item, ench))
        })

        compoundSpace(Slots.RowTwoSlotTwo rectTo Slots.RowFiveSlotEight, compound)

        compound.addContent(Registry.ENCHANT.iter().values.filter { it.applicable.contains(item.type) && it.name.lowercase().contains(search.lowercase()) })

        compoundScroll(Slots.RowOneSlotNine, ItemValue.placeholder(Material.ARROW, "<green>Forward 1 Row"), compound, scrollTimes = 1)
        compoundScroll(Slots.RowOneSlotEight, ItemValue.placeholder(Material.ARROW, "<red>Back 1 Row"), compound, reverse = true, scrollTimes = 1)

        compoundScroll(Slots.RowSixSlotNine, ItemValue.placeholder(Material.ARROW, "<green>Forward 4 Rows"), compound, scrollTimes = 4)
        compoundScroll(Slots.RowSixSlotEight, ItemValue.placeholder(Material.ARROW, "<red>Back 4 Rows"), compound, reverse = true, scrollTimes = 4)

        button(Slots.RowOneSlotOne, ItemValue.placeholder(Material.OAK_SIGN, "<yellow>Search")) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.closeInventory()
            val inputFilterPrompt = object: ValidatingPrompt() {
                override fun getPromptText(context: ConversationContext): String {
                    return ChatColor.YELLOW.toString() + "Input enchantments to search:"
                }

                override fun isInputValid(context: ConversationContext, input: String): Boolean {
                    return true
                }

                override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt? {
                    e.player.openGUI(
                        adminEnchantUi(item, input)
                    )
                    return Prompt.END_OF_CONVERSATION
                }

            }

            val conv = ConversationFactory(Macrocosm).withLocalEcho(false).withFirstPrompt(inputFilterPrompt).buildConversation(e.player)
            conv.begin()
        }
    }
}

private fun specificAdminEnchUi(player: MacrocosmPlayer, item: MacrocosmItem, ench: Enchantment): GUI<ForInventoryFourByNine> = kSpigotGUI(GUIType.FOUR_BY_NINE) {
    defaultPage = 0
    title = text("Enchantming -> ${ench.name}")

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        val levelsCompound = createCompound<Optional<Int>>({ oLevel ->
            if(oLevel.isEmpty)
                return@createCompound ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, "")
            val level = oLevel.get()
            val name = ench.name
            val desc = ench.description(level).map { it.str() }.toTypedArray()

            ItemValue.placeholderDescripted(
                Material.ENCHANTED_BOOK,
                if(ench is UltimateEnchantment) "<light_purple><bold>$name ${roman(level)}" else "<blue>$name ${roman(level)}",
                *desc,
                "",
                "<yellow>Click to enchant!"
            )
        }, { e, oLevel ->
            e.bukkitEvent.isCancelled = true
            if(oLevel.isEmpty)
                return@createCompound
            item.enchant(ench, oLevel.get())
            val built = item.build(player)
            e.player.inventory.setItemInMainHand(built)
            e.player.openGUI(adminEnchantUi(item))
        })
        compoundSpace(Slots.RowThreeSlotTwo rectTo  Slots.RowThreeSlotEight, levelsCompound)

        levelsCompound.addContent(ench.levels.toList().map { Optional.of(it) }.pad(7, Optional.empty()))

        button(Slots.RowOneSlotFive, ItemValue.placeholder(Material.ARROW, "<green>Go Back")) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(adminEnchantUi(item))
        }
    }
}
