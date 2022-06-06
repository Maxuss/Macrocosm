package space.maxus.macrocosm.slayer.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.damage.truncateBigNumber
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.slayer.Slayer
import space.maxus.macrocosm.slayer.SlayerLevel
import space.maxus.macrocosm.slayer.SlayerTable
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.renderBar
import space.maxus.macrocosm.util.stripTags


fun rewardsMenu(player: MacrocosmPlayer, ty: SlayerType): GUI<ForInventoryFourByNine> = kSpigotGUI(GUIType.FOUR_BY_NINE) {
    val slayer = ty.slayer
    val playerLevel = player.slayers[ty]!!
    title = comp("${slayer.name.stripTags()} Rewards")
    defaultPage = 0

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        val cmp = createCompound<Int>(
            iconGenerator = { lvl ->
                if(lvl >= 10) {
                    ItemValue.placeholderDescripted(Material.CLOCK, "<yellow>Coming Soon!", "More level rewards with more", "new items are coming soon!")
                } else {
                    if(playerLevel.level + 2 < lvl) {
                        // displaying "unknown rewards"
                        ItemValue.placeholderDescripted(Material.COAL_BLOCK, "<red>???", "Reach higher level to view", "these rewards!")
                    } else {
                        val reward = slayer.rewards[lvl - 1]
                        val buffer = mutableListOf<String>()
                        buffer.add("<dark_gray>${if (lvl >= 6) slayer.professionNames[lvl - 6] else Slayer.defaultProfessionNames[lvl - 1]}")
                        buffer.add("")
                        buffer.add("Rewards:")

                        for (rw in reward.rewards) {
                            buffer.add(rw.display(lvl).str())
                        }

                        if (playerLevel.level == lvl - 1) {
                            // displaying progress to this level
                            buffer.add("")
                            buffer.add("Progress:")
                            val required = SlayerTable.expForLevel(lvl)
                            val ratio = playerLevel.overflow / required
                            val startColor = if (lvl < 7) NamedTextColor.DARK_PURPLE else NamedTextColor.RED
                            buffer.add(
                                renderBar(
                                    ratio.toFloat(),
                                    21,
                                    startColor,
                                    if (lvl < 7) NamedTextColor.LIGHT_PURPLE else NamedTextColor.GOLD
                                ) + " <${startColor.asHexString()}>${
                                    truncateBigNumber(
                                        playerLevel.overflow.toFloat(),
                                        true
                                    )
                                }/${truncateBigNumber(required.toFloat(), true)}"
                            )
                        } else if (playerLevel.level >= lvl) {
                            if (!playerLevel.collectedRewards.contains(lvl)) {
                                // displaying that player can collect rewards
                                buffer.add("")
                                buffer.add("<yellow>Click to claim rewards!")
                            } else {
                                buffer.add("")
                                buffer.add("<green>Rewards claimed!")
                            }
                        }

                        reward.display.display(
                            "<gold>${
                                slayer.validEntities[0].name.replace("_", " ").capitalized()
                            } Slayer LVL $lvl", *buffer.toTypedArray()
                        )
                    }
                }
            },
            onClick = { event, lvl ->
                event.bukkitEvent.isCancelled = true
                if(!playerLevel.collectedRewards.contains(lvl) && playerLevel.level >= lvl) {
                    // giving player the rewards
                    val rewards = slayer.rewards[lvl - 1]
                    for(reward in rewards.rewards) {
                        reward.reward(player, lvl)
                    }
                    player.slayers[ty] = SlayerLevel(playerLevel.level, playerLevel.overflow, listOf(*playerLevel.collectedRewards.toTypedArray(), lvl), playerLevel.rngMeter)
                    event.player.closeInventory()
                    player.sendMessage("<yellow>You have claimed rewards for ${slayer.name}<yellow> LVL $lvl")
                    sound(Sound.ENTITY_FIREWORK_ROCKET_BLAST) {
                        playFor(event.player)
                    }
                }
            })
        compoundSpace(LinearInventorySlots(listOf(
            *(2..8).slots(3), InventorySlot(2, 4), InventorySlot(2, 5), InventorySlot(2, 6)
        )), cmp)
        cmp.addContent(1..10)

        button(Slots.RowOneSlotOne, ItemValue.placeholder(Material.ARROW, "<red>Back")) { ev ->
            ev.bukkitEvent.isCancelled = true
            ev.player.openGUI(specificSlayerMenu(player, ty))
        }
    }
}
