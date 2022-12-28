package space.maxus.macrocosm.pets.ui

import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.gui.*
import org.bukkit.Material
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.pets.StoredPet
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.giveOrDrop

fun petsMenu(player: MacrocosmPlayer, petToItem: Boolean = false): GUI<ForInventorySixByNine> =
    kSpigotGUI(GUIType.SIX_BY_NINE) {
        defaultPage = 0
        title = "Pets".toComponent()

        page(0) {
            placeholder(Slots.Border, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))
            placeholder(
                Slots.RowSixSlotFive,
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
            button(Slots.RowOneSlotFive, ItemValue.placeholder(Material.BARRIER, "<red>Close")) { e ->
                e.bukkitEvent.isCancelled = true
                e.player.closeInventory()
            }
            button(
                Slots.RowOneSlotSix,
                ItemValue.placeholderDescripted(
                    if (petToItem) Material.LIME_DYE else Material.GRAY_DYE,
                    "<green>Convert Pet to an Item",
                    "Enable this setting and click",
                    "any pet to convert it to an",
                    "item.",
                    "",
                    if (petToItem) "<green>Enabled" else "<red>Disabled"
                )
            ) { e ->
                e.bukkitEvent.isCancelled = true
                e.player.closeInventory()
                e.player.openGUI(petsMenu(player, !petToItem))
            }

            val petsCompound =
                createRectCompound<Pair<String, StoredPet>>(
                    Slots.RowTwoSlotTwo,
                    Slots.RowFiveSlotEight,
                    iconGenerator = { it.second.menuItem(player) },
                    onClick = { e, pet ->
                        e.bukkitEvent.isCancelled = true
                        if (petToItem) {
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
                        e.player.closeInventory()
                        e.player.openGUI(petsMenu(player, petToItem))

                    })
            petsCompound.addContent(player.ownedPets.toList().sortedBy { it.second.rarity.ordinal })

            compoundScroll(Slots.RowOneSlotNine, ItemValue.placeholder(Material.ARROW, "<green>Forward"), petsCompound)
            compoundScroll(
                Slots.RowOneSlotOne,
                ItemValue.placeholder(Material.ARROW, "<red>Backward"),
                petsCompound,
                reverse = true
            )
        }
    }
