package space.maxus.macrocosm.collections.ui

import net.axay.kspigot.items.meta
import org.bukkit.Material
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.collections.CollectionSection
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.damage.truncateBigNumber
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.progressBar
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi

fun sectionUi(
    player: MacrocosmPlayer,
    section: CollectionSection,
    allRelated: List<CollectionType>,
    unlocked: List<CollectionType>
): MacrocosmUI = macrocosmUi("collection_section", UIDimensions.SIX_X_NINE) {
    val sectionName: String = section.name.capitalized()
    title = "Collections ► $sectionName"

    page {

        background()

        placeholder(
            Slot.RowOneSlotFive,
            ItemValue.placeholderDescripted(
                section.mat,
                "<green>$sectionName Collection",
                "Your $sectionName Collection!",
                "",
                *(if (unlocked.size == allRelated.size)
                    buildBar(allRelated.size, allRelated.count { player.collections.isMaxLevel(it) }).toList()
                        .toTypedArray()
                else
                    buildBar(allRelated.size, unlocked.size, "Collection Unlocked").toList().toTypedArray()),
            )
        )

        close()

        goBack(Slot.RowSixSlotFour, { collUi(player) })

        transparentCompound(Slot.RowTwoSlotTwo rect Slot.RowFourSlotEight, allRelated, { ty ->
            val (lvl, amount) = player.collections.colls[ty]!!
            if (lvl == 0 && amount == 0)
                return@transparentCompound ItemValue.placeholderDescripted(
                    Material.GRAY_DYE,
                    "<red>${ty.inst.name}",
                    "Find this item to add it to your",
                    "collection and unlock collection",
                    "rewards!"
                )

            val itemName = "<yellow>${ty.inst.name}${if (lvl == 0) "" else " ${roman(lvl)}"}"
            val lore = mutableListOf(
                "View all your ${ty.inst.name}",
                "Collection progress and rewards!",
                ""
            )

            if (!player.collections.isMaxLevel(ty)) {
                val nextLevel = lvl + 1
                val requiredItems = ty.inst.table.itemsForLevel(nextLevel)
                val progress = amount.toFloat() / requiredItems
                val roman = roman(nextLevel)
                lore.add("Progress to ${ty.inst.name} $roman: <yellow>${Formatting.withCommas((progress * 100f).toBigDecimal())}<gold>%")
                lore.add(
                    "${
                        progressBar(
                            amount,
                            requiredItems,
                            18
                        )
                    } <yellow>${Formatting.withCommas(amount.toBigDecimal(), true)}<gold>/<yellow>${
                        truncateBigNumber(
                            requiredItems.toFloat()
                        )
                    }"
                )
                lore.add("")
                lore.add("${ty.inst.name} $roman Rewards:")
                val rewardsForNextLevel = ty.inst.rewards[lvl]
                val rewardStr = rewardsForNextLevel.display(nextLevel).str()
                lore.addAll(rewardStr.split("\n").map { " $it" })
                lore.add("")
            }
            lore.add("<yellow>Click to view!")

            val base = ty.displayItem.build(player)!!
            base.meta {
                displayName(text(itemName).noitalic())
                lore(lore.map { text("<gray>$it").noitalic() })
            }
            base
        }) { data, ty ->
            if (player.collections.colls[ty]!!.total > 0)
                data.instance.switch(specificCollectionUi(player, ty))
        }
    }
}
