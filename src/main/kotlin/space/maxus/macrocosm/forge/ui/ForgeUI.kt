package space.maxus.macrocosm.forge.ui

import net.axay.kspigot.items.meta
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.forge.ActiveForgeRecipe
import space.maxus.macrocosm.forge.ForgeType
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.giveOrDrop
import space.maxus.macrocosm.util.padForward
import space.maxus.macrocosm.util.toFancyString
import java.util.concurrent.atomic.AtomicInteger

fun displayForge(player: MacrocosmPlayer, forge: ForgeType): MacrocosmUI =
    macrocosmUi("forge_display", UIDimensions.SIX_X_NINE) {
        title = forge.displayName

        pageLazy {
            val activeRecipes = player.activeForgeRecipes
            background()

            val slot = AtomicInteger(0)
            compound(Slot.RowTwoSlotThree rect Slot.RowTwoSlotSeven,
                { activeRecipes.padForward(5, ActiveForgeRecipe(Identifier.NULL, -1)).toList() },
                { recipe ->
                    val slotId = slot.incrementAndGet()
                    if (recipe.startTime != -1L) {
                        val actualRecipe = Registry.FORGE_RECIPE.find(recipe.id)
                        val (resId, resAmount) = actualRecipe.result
                        val result = Registry.ITEM.find(resId)
                        val name = (if (resAmount > 1) result.buildName()
                            .append(text(" ${resAmount}x")) else result.buildName()).str()
                        val endItem = result.build(player)!!
                        endItem.meta {
                            if (recipe.isDoneByNow()) {
                                displayName(text("<green>Slot #$slotId").noitalic())
                                lore(listOf(
                                    "<gray>Making $name",
                                    "<gray>Time left: <green>DONE",
                                    "",
                                    "<yellow>Click to collect!"
                                ).map { text(it).noitalic() })
                            } else {
                                displayName(text("<yellow>Slot #$slotId").noitalic())
                                lore(listOf(
                                    "<gray>Making $name",
                                    "<gray>Time left: <green>${recipe.leftTime().toFancyString()}"
                                ).map { text(it).noitalic() })
                            }
                        }
                        endItem
                    } else {
                        ItemValue.placeholderDescripted(
                            Material.BLAST_FURNACE,
                            "<red>Slot #$slotId",
                            "View and start forge processes",
                            "using the materials that you",
                            "have.",
                            " ",
                            "<yellow>Click to start a process!"
                        )
                    }
                },
                { e, recipe ->
                    if (recipe.startTime == -1L) {
                        e.instance.switch(recipeChoose(player, forge))
                    } else if (recipe.isDoneByNow()) {
                        player.activeForgeRecipes.remove(recipe)
                        val actualRecipe = Registry.FORGE_RECIPE.find(recipe.id)
                        val (resId, resAmount) = actualRecipe.result
                        val result = Registry.ITEM.find(resId)
                        val reward = result.build(player)!!
                        reward.amount = resAmount
                        e.paper.giveOrDrop(reward)
                        sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                            pitch = 2f
                            volume = 2f

                            playFor(e.paper)
                        }
                        e.instance.reload()
                    }
                }
            )

            progressBarSlots.forEachIndexed { index, (from, to) ->
                val recipe = activeRecipes.getOrNull(index)
                placeholder(
                    from rect to,
                    if (recipe == null) ItemValue.placeholder(
                        Material.RED_STAINED_GLASS_PANE,
                        ""
                    ) else if (recipe.isDoneByNow()) ItemValue.placeholder(
                        Material.LIME_STAINED_GLASS_PANE,
                        "Recipe done!"
                    ) else ItemValue.placeholder(
                        Material.YELLOW_STAINED_GLASS_PANE,
                        "<yellow>${recipe.leftTime().toFancyString()}"
                    )
                )
            }
        }
    }

private val progressBarSlots = listOf(
    Slot.RowThreeSlotThree to Slot.RowFiveSlotThree,
    Slot.RowThreeSlotFour to Slot.RowFiveSlotFour,
    Slot.RowThreeSlotFive to Slot.RowFiveSlotFive,
    Slot.RowThreeSlotSix to Slot.RowFiveSlotSix,
    Slot.RowThreeSlotSeven to Slot.RowFiveSlotSeven
)
