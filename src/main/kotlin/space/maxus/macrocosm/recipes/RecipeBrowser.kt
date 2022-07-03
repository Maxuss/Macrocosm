package space.maxus.macrocosm.recipes

import net.axay.kspigot.gui.*
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
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.text.str

fun recipeBrowser(player: MacrocosmPlayer) = kSpigotGUI(GUIType.SIX_BY_NINE) {
    title = text("<dark_gray>Recipe Browser")
    page(1) {
        placeholder(Slots.Border, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE))
        val compound = createRectCompound<Identifier>(
            Slots.RowTwoSlotTwo, Slots.RowFiveSlotEight,
            iconGenerator = {
                if (player.isRecipeLocked(it))
                    ItemValue.placeholderDescripted(Material.GRAY_DYE, "<red>???", "" // TODO: 03.07.2022 Recipe obtaining types
                    )
                else
                    Registry.RECIPE.findOrNull(it)?.resultItem() ?: ItemStack(Material.AIR)
            },
            onClick = { e, it ->
                e.bukkitEvent.isCancelled = true
                if(player.isRecipeLocked(it)) {
                    player.sendMessage("<red>You haven't unlocked that recipe!")
                    sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                        pitch = 0f
                        playFor(e.player)
                    }
                } else {
                    player.paper?.openGUI(recipeViewer(it, player))
                }
            }
        )
        compound.addContent(Registry.RECIPE.iter().keys().toList())
        compoundScroll(
            Slots.RowOneSlotOne,
            ItemValue.placeholder(Material.ARROW, "<red><!italic>Previous Page"), compound, scrollTimes = 4
        )
        compoundScroll(
            Slots.RowOneSlotNine,
            ItemValue.placeholder(Material.ARROW, "<green><!italic>Next Page"),
            compound,
            scrollTimes = 4,
            reverse = true
        )

    }
}

fun recipesUsing(item: Identifier, player: MacrocosmPlayer) = kSpigotGUI(GUIType.FIVE_BY_NINE) {
    title = text("<dark_gray>Recipe Browser")
    defaultPage = 0
    val recipes = Recipes.using(item)
    for (i in recipes.filter { !player.isRecipeLocked(it.id) }.indices) {
        val recipe = recipes[i]
        page(i) {
            placeholder(
                Slots.RowOneSlotOne rectTo Slots.RowFiveSlotNine,
                ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE)
            )
            placeholder(Slots.RowTwoSlotTwo rectTo Slots.RowFourSlotFour, ItemStack(Material.AIR))

            // crafting grid
            val compound = createRectCompound(
                Slots.RowTwoSlotTwo, Slots.RowFourSlotFour,
                iconGenerator = {
                    it
                }
            ) { e, it: ItemStack ->
                e.bukkitEvent.isCancelled = true
                if (!it.type.isAir) {
                    val id = it.macrocosm?.id ?: Identifier.NULL
                    val rec = Registry.RECIPE.findOrNull(id)
                    if (rec != null)
                        player.paper?.openGUI(recipeViewer(id, player))
                }
            }
            val items = mutableListOf<ItemStack>()
            recipe.ingredients().map {
                it.map { v ->
                    val its = Registry.ITEM.findOrNull(v.first)?.build() ?: ItemStack(Material.AIR)
                    its.amount = v.second
                    its
                }
            }.forEach {
                items.addAll(it)
            }
            compound.addContent(items)

            // result
            placeholder(Slots.RowThreeSlotEight, recipe.resultItem())

            previousPage(
                Slots.RowOneSlotOne,
                ItemValue.placeholder(Material.ARROW, "<red><!italic>Previous"),
                null,
                null
            )
            nextPage(Slots.RowOneSlotNine, ItemValue.placeholder(Material.ARROW, "<green><!italic>Next"), null, null)
        }
    }
}

fun recipeViewer(item: Identifier, player: MacrocosmPlayer): GUI<ForInventorySixByNine> =
    kSpigotGUI(GUIType.SIX_BY_NINE) {
        title = text("<dark_gray>${Registry.ITEM.find(item).name.str()}<dark_gray> Recipe")
        page(1) {
            // # # # # # # # # #
            // #      # # # # #
            // #      # # #   #
            // #      # # # # #
            // # # # # # # # # #
            // # # # # # # # # #

            placeholder(
                Slots.RowOneSlotOne rectTo Slots.RowSixSlotNine,
                ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE)
            )
            placeholder(Slots.RowThreeSlotTwo rectTo Slots.RowFiveSlotFour, ItemStack(Material.AIR))
            // crafting grid
            val compound = createRectCompound(
                Slots.RowThreeSlotTwo, Slots.RowFiveSlotFour,
                iconGenerator = {
                    it
                },
            ) { e, it: ItemStack ->
                e.bukkitEvent.isCancelled = true
                if (!it.type.isAir) {
                    val id = it.macrocosm?.id ?: Identifier.NULL
                    val recipe = Registry.RECIPE.findOrNull(id)
                    if (recipe != null)
                        player.paper?.openGUI(recipeViewer(id, player))
                }
            }
            val recipe = Registry.RECIPE.findOrNull(item) ?: kotlin.run {
                player.paper!!.closeInventory()
                return@page
            }
            val items = mutableListOf<ItemStack>()
            recipe.ingredients().map {
                it.map { v ->
                    if(v.first.namespace == "minecraft") {
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
            compound.addContent(items)

            // result
            placeholder(Slots.RowFourSlotEight, recipe.resultItem())
            placeholder(Slots.RowFourSlotSix, ItemValue.placeholderDescripted(Material.CRAFTING_TABLE, "<green>Crafting Table", "Craft this recipe by using crafting table"))

            // TODO: 03.07.2022 Proper recipe book
            button(
                Slots.RowFiveSlotOne, ItemValue.placeholderHead(
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzFkMDlhYTA3MTNjYzlhM2YzYzY4MDkyM2IxZjdiMjE2N2Y2NzMyMmZiN2U1ZjczMDE4ODEyZmVmNWZlMWEzYSJ9fX0=",
                    "<green><!italic>Back to Recipe Browser", "<gray><!italic>Return to browser"
                )
            ) { e ->
                e.bukkitEvent.isCancelled = true
                player.paper?.openGUI(recipeBrowser(player))
            }
        }
    }
