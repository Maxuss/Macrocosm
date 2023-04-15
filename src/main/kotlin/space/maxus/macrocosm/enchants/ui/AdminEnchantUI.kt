package space.maxus.macrocosm.enchants.ui

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
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.pad
import java.util.*

fun adminEnchantUi(item: MacrocosmItem, search: String = ""): MacrocosmUI =
    macrocosmUi("admin_enchanting", UIDimensions.SIX_X_NINE) {
        title = "Enchanting"

        page {
            background()

            val cmp = compound(Slot.RowTwoSlotTwo rect Slot.RowFiveSlotEight, Registry.ENCHANT.iter().values.filter {
                it.applicable.contains(item.type) && it.name.lowercase().contains(search.lowercase())
            }, { e ->
                val name = e.name
                val desc = e.description(1).map { it.str() }.toTypedArray()

                val conflicts = e.conflicts.mapNotNull {
                    Registry.ENCHANT.findOrNull(it)?.name?.let { i ->
                        "<blue>${
                            i.replace(
                                " ",
                                " <blue>"
                            )
                        }"
                    }
                }.joinToString(separator = ", ", prefix = "Conflicts with: ").reduceToList()

                ItemValue.placeholderDescripted(
                    Material.ENCHANTED_BOOK,
                    if (e is UltimateEnchantment) "<light_purple><bold>$name" else "<blue>$name",
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
                e.instance.switch(specificAdminEnchUi(e.player, item, ench))
            })

            compoundWidthScroll(
                Slot.RowSixSlotNine,
                cmp
            )
            compoundWidthScroll(
                Slot.RowSixSlotEight,
                cmp,
                reverse = true
            )

            button(
                Slot.RowSixSlotOne,
                ItemValue.placeholderDescripted(
                    Material.OAK_SIGN,
                    "<yellow>Search",
                    *(if (search.isNotBlank()) arrayOf("Current filter: <green>$search") else arrayOf())
                )
            ) { e ->
                e.paper.closeInventory()
                val inputFilterPrompt = object : ValidatingPrompt() {
                    override fun getPromptText(context: ConversationContext): String {
                        return "§eInput enchantments to search:"
                    }

                    override fun isInputValid(context: ConversationContext, input: String): Boolean {
                        return true
                    }

                    override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt? {
                        adminEnchantUi(item, input).open(e.paper)
                        return Prompt.END_OF_CONVERSATION
                    }

                }

                val conv = ConversationFactory(Macrocosm).withLocalEcho(false).withFirstPrompt(inputFilterPrompt)
                    .buildConversation(e.paper)
                conv.begin()
            }
        }
    }

private fun specificAdminEnchUi(
    player: MacrocosmPlayer,
    item: MacrocosmItem,
    ench: Enchantment
): MacrocosmUI = macrocosmUi("admin_enchant_specific", UIDimensions.FOUR_X_NINE) {
    title = "Enchanting -> ${ench.name}"

    page(0) {
        background()

        compound(
            Slot.RowTwoSlotTwo rect Slot.RowTwoSlotEight,
            ench.levels.toList().map { Optional.of(it) }.pad(7, Optional.empty()),
            { oLevel ->
                if (oLevel.isEmpty)
                    return@compound ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, "")
                val level = oLevel.get()
                val name = ench.name
                val desc = ench.description(level).map { it.str() }.toTypedArray()

                ItemValue.placeholderDescripted(
                    Material.ENCHANTED_BOOK,
                    if (ench is UltimateEnchantment) "<light_purple><bold>$name ${roman(level)}" else "<blue>$name ${
                        roman(
                            level
                        )
                    }",
                    *desc,
                    "",
                    "<yellow>Click to enchant!"
                )
            },
            { e, oLevel ->
                if (oLevel.isEmpty)
                    return@compound
                item.enchant(ench, oLevel.get())
                val built = item.build(player)
                e.paper.inventory.setItemInMainHand(built)
                e.instance.switch(adminEnchantUi(item))
            })

        goBack(Slot.RowFourSlotFive, { adminEnchantUi(item) })
    }
}
