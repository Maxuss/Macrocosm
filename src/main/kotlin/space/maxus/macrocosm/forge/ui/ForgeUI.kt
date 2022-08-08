package space.maxus.macrocosm.forge.ui

import net.axay.kspigot.gui.*
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
import space.maxus.macrocosm.util.giveOrDrop
import space.maxus.macrocosm.util.pad
import space.maxus.macrocosm.util.toFancyString
import java.util.concurrent.atomic.AtomicInteger

fun displayForge(player: MacrocosmPlayer, forge: ForgeType): GUI<*> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    defaultPage = 0
    title = text(forge.displayName)

    val activeRecipes = player.activeForgeRecipes

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        val slot = AtomicInteger(0)
        val furnaceCompound = createRectCompound<ActiveForgeRecipe>(Slots.RowFiveSlotThree, Slots.RowFiveSlotSeven,
            iconGenerator = { recipe ->
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
            onClick = { e, recipe ->
                e.bukkitEvent.isCancelled = true
                if (recipe.startTime == -1L) {
                    e.player.openGUI(recipeChoose(player, forge))
                } else if (recipe.isDoneByNow()) {
                    player.activeForgeRecipes.remove(recipe)
                    val actualRecipe = Registry.FORGE_RECIPE.find(recipe.id)
                    val (resId, resAmount) = actualRecipe.result
                    val result = Registry.ITEM.find(resId)
                    val reward = result.build(player)!!
                    reward.amount = resAmount
                    e.player.giveOrDrop(reward)
                    sound(Sound.BLOCK_NOTE_BLOCK_PLING) {
                        pitch = 2f
                        volume = 2f

                        playFor(e.player)
                    }
                    e.player.closeInventory()
                    e.player.openGUI(displayForge(player, forge))
                }
            }
        )
        furnaceCompound.addContent(activeRecipes.pad(5, ActiveForgeRecipe(Identifier.NULL, -1)))

        progressBarSlots.forEachIndexed { index, (from, to) ->
            val recipe = activeRecipes.getOrNull(index)
            placeholder(
                from rectTo to,
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
    Slots.RowTwoSlotThree to Slots.RowFourSlotThree,
    Slots.RowTwoSlotFour to Slots.RowFourSlotFour,
    Slots.RowTwoSlotFive to Slots.RowFourSlotFive,
    Slots.RowTwoSlotSix to Slots.RowFourSlotSix,
    Slots.RowTwoSlotSeven to Slots.RowFourSlotSeven
)
