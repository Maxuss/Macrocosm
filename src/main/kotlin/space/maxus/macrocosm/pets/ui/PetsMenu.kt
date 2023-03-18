package space.maxus.macrocosm.pets.ui

import org.bukkit.Material
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.giveOrDrop

fun petsMenu(player: MacrocosmPlayer, petToItem: Boolean = false): MacrocosmUI =
    macrocosmUi("pets", UIDimensions.SIX_X_NINE) {
        title = "Pets"
        var petToItemMut = petToItem

        pageLazy {
            background()
            placeholder(
                Slot.RowOneSlotFive,
                ItemValue.placeholderDescripted(
                    Material.BONE,
                    "<green>Pets",
                    "View and manage all of your",
                    "pets.",
                    "",
                    "Level up your pets faster by",
                    "gaining xp in their favorite",
                    "skill!",
                    "",
                    "Selected pet: ${player.activePet?.let { inst -> "<${inst.rarity(player).color.asHexString()}>${inst.prototype.name}" }}"
                )
            )
            close()
            button(
                Slot.RowSixSlotSix,
                ItemValue.placeholderDescripted(
                    if (petToItemMut) Material.LIME_DYE else Material.GRAY_DYE,
                    "<green>Convert Pet to an Item",
                    "Enable this setting and click",
                    "any pet to convert it to an",
                    "item.",
                    "",
                    if (petToItemMut) "<green>Enabled" else "<red>Disabled"
                )
            ) { e ->
                petToItemMut = !petToItemMut
                e.instance.reload()
            }

            val cmp =
                compound(
                    Slot.RowTwoSlotTwo rect Slot.RowFiveSlotEight,
                    { player.ownedPets.toList().sortedBy { it.second.rarity.ordinal } },
                    { it.second.menuItem(player) },
                    { e, pet ->
                        if (petToItemMut) {
                            if (pet.first == player.activePet?.hashKey) {
                                player.activePet?.despawn(player)
                            }
                            val instance = player.ownedPets.remove(pet.first)!!
                            player.paper?.giveOrDrop(Registry.PET.find(instance.id).buildItem(player, instance))
                        } else if (player.activePet?.hashKey == pet.first) {
                            player.activePet?.despawn(player)
                        } else {
                            player.activePet?.despawn(player)
                            Registry.PET.find(pet.second.id)
                                .spawn(player, pet.first)
                        }
                        e.instance.reload()
                    })
            compoundWidthScroll(Slot.RowSixSlotNine, cmp)
            compoundWidthScroll(Slot.RowSixSlotOne, cmp, reverse = true)
        }
    }
