package space.maxus.macrocosm.recipes

import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.VanillaItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.stripTags

fun recipeBrowser(player: MacrocosmPlayer) = macrocosmUi("recipe_browser", UIDimensions.SIX_X_NINE) {
    title = "Recipe Browser"
    page {
        background()
        val cmp = compound(
            Slot.RowTwoSlotTwo rect Slot.RowFiveSlotEight,
            { Registry.RECIPE.iter().keys().toList() },
            {
                if (player.isRecipeLocked(it))
                    ItemValue.placeholderDescripted(
                        Material.GRAY_DYE, "<red>???", "" // TODO: 03.07.2022 Recipe obtaining types
                    )
                else
                    Registry.RECIPE.findOrNull(it)?.resultItem() ?: ItemStack(Material.AIR)
            },
            { e, it ->
                if (player.isRecipeLocked(it)) {
                    player.sendMessage("<red>You haven't unlocked that recipe!")
                    sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                        pitch = 0f
                        playFor(e.paper)
                    }
                } else {
                    e.instance.switch(recipeViewer(it, player))
                }
            }
        )
        compoundWidthScroll(
            Slot.RowSixSlotOne,
            cmp,
            reverse = true
        )
        compoundWidthScroll(
            Slot.RowSixSlotNine,
            cmp
        )
    }
}

fun recipesUsing(item: Identifier, player: MacrocosmPlayer): MacrocosmUI = macrocosmUi("recipe_browser_using", UIDimensions.FIVE_X_NINE) {
    title = "Recipe Browser"
    val recipes = Recipes.using(item)
    val indices = recipes.filter { !player.isRecipeLocked(it.id) }.indices
    for (i in indices) {
        val recipe = recipes[i]
        page(i) {
            background()

            // crafting grid
            val items = mutableListOf<ItemStack>()
            recipe.ingredients().map {
                it.map { v ->
                    val its = Registry.ITEM.findOrNull(v.first)?.build(player)
                        ?: VanillaItem(Material.valueOf(v.first.path.uppercase())).build(player)!!
                    its.amount = v.second
                    its
                }
            }.forEach {
                items.addAll(it)
            }
            compound(
                Slot.RowTwoSlotTwo rect Slot.RowFourSlotFour,
                items,
                { it },
            ) { e, it ->
                if (!it.type.isAir) {
                    val id = it.macrocosm?.id ?: Identifier.NULL
                    val rec = Registry.RECIPE.findOrNull(id)
                    if (rec != null)
                        e.instance.switch(recipeViewer(id, player))
                }
            }

            // result
            placeholder(Slot.RowThreeSlotEight, recipe.resultItem())

            if(i != 0)
                changePage(Slot.RowSixSlotOne, i - 1)
            if(i != indices.last)
                changePage(Slot.RowSixSlotNine, i + 1)
        }
    }
}

fun recipeViewer(item: Identifier, player: MacrocosmPlayer): MacrocosmUI =
    macrocosmUi("single_recipe_viewer", UIDimensions.FIVE_X_NINE) {
        title = "${Registry.ITEM.find(item).name.str().stripTags()} Recipe"

        page {
            // # # # # # # # # #
            // #      # # # # #
            // #      # # #   #
            // #      # # # # #
            // # # # # # # # # #
            // # # # # # # # # #

            background()

            // crafting grid
            val recipe = Registry.RECIPE.findOrNull(item) ?: kotlin.run {
                player.paper!!.closeInventory()
                return@page
            }
            val items = mutableListOf<ItemStack>()
            recipe.ingredients().map {
                it.map { v ->
                    if (v.first.namespace == "minecraft") {
                        VanillaItem(Material.valueOf(v.first.path.uppercase()), v.second).build(player)!!
                    } else {
                        val its = Registry.ITEM.findOrNull(v.first)?.build() ?: ItemStack(Material.AIR)
                        its.amount = v.second
                        its
                    }
                }
            }.forEach {
                items.addAll(it)
            }
            compound(
                Slot.RowTwoSlotTwo rect Slot.RowFourSlotFour,
                items,
                { it },
            ) { e, it: ItemStack ->
                if (!it.type.isAir) {
                    val id = it.macrocosm?.id ?: Identifier.NULL
                    val newRecipe = Registry.RECIPE.findOrNull(id)
                    if (newRecipe != null && !player.isRecipeLocked(id))
                        e.instance.switch(recipeViewer(id, player))
                }
            }

            // result
            placeholder(Slot.RowThreeSlotEight, recipe.resultItem())
            placeholder(
                Slot.RowThreeSlotSix,
                ItemValue.placeholderDescripted(
                    Material.CRAFTING_TABLE,
                    "<green>Crafting Table",
                    "Craft this recipe by using crafting table"
                )
            )

            // TODO: 03.07.2022 Proper recipe book
            goBack(
                Slot.RowOneSlotOne,
                { recipeBrowser(player) },
                "Recipe Browser",
            )
        }
    }
