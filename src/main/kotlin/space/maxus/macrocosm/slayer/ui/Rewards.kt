package space.maxus.macrocosm.slayer.ui

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
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.LinearComponentSpace
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.renderBar
import space.maxus.macrocosm.util.stripTags


fun rewardsMenu(player: MacrocosmPlayer, ty: SlayerType): MacrocosmUI =
    macrocosmUi("slayer_rewards", UIDimensions.FOUR_X_NINE) {
        val slayer = ty.slayer
        val playerLevel = player.slayers[ty]!!
        title = "${slayer.name.stripTags()} Rewards"

        page(0) {
            background()

            compound(
                LinearComponentSpace(
                    listOf(
                        *(1..7).slots(1), Slot(2, 3), Slot(2, 4), Slot(2, 5)
                    ).map(Slot::value),
                ),
                (1..10).toList(),
                { lvl ->
                    if (lvl >= 10) {
                        ItemValue.placeholderDescripted(
                            Material.CLOCK,
                            "<yellow>Coming Soon!",
                            "More level rewards with more",
                            "new items are coming soon!"
                        )
                    } else {
                        if (playerLevel.level + 2 < lvl) {
                            // displaying "unknown rewards"
                            ItemValue.placeholderDescripted(
                                Material.COAL_BLOCK,
                                "<red>???",
                                "Reach higher level to view",
                                "these rewards!"
                            )
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
                { e, lvl ->
                    if (!playerLevel.collectedRewards.contains(lvl) && playerLevel.level >= lvl) {
                        // giving player the rewards
                        val rewards = slayer.rewards[lvl - 1]
                        for (reward in rewards.rewards) {
                            reward.reward(player, lvl)
                        }
                        player.slayers[ty] = SlayerLevel(
                            playerLevel.level,
                            playerLevel.overflow,
                            listOf(*playerLevel.collectedRewards.toTypedArray(), lvl),
                            playerLevel.rng
                        )
                        e.paper.closeInventory()
                        player.sendMessage("<yellow>You have claimed rewards for ${slayer.name}<yellow> LVL $lvl")
                        sound(Sound.ENTITY_FIREWORK_ROCKET_BLAST) {
                            playFor(e.paper)
                        }
                    }
                })

            goBack(Slot.RowFourSlotOne, { specificSlayerMenu(player, ty) })
        }
    }
