package space.maxus.macrocosm.slayer.ui

import net.axay.kspigot.items.meta
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.slayer.colorFromTier
import space.maxus.macrocosm.slayer.costFromTier
import space.maxus.macrocosm.slayer.rewardExperienceForTier
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.LinearComponentSpace
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.stripTags
import kotlin.math.roundToInt

fun specificSlayerMenu(player: MacrocosmPlayer, ty: SlayerType): MacrocosmUI =
    macrocosmUi("specific_slayer", UIDimensions.SIX_X_NINE) {
        title = ty.slayer.name.stripTags()
        val slayer = ty.slayer
        val slayerId = id(slayer.id)

        page(0) {
            background()

            compound(
                LinearComponentSpace(
                    listOf(
                        Slot(1, 2),
                        Slot(1, 3),
                        Slot(1, 4),
                        Slot(1, 5),
                        Slot(1, 6),
                        Slot(2, 4),
                    ).map(Slot::value)
                ),
                slayer.tiers,
                {
                    if (it >= 6)
                        if (!player.memory.tier6Slayers.contains(slayerId))
                            ItemValue.placeholderDescripted(
                                Material.COAL,
                                "<dark_aqua>${slayer.name.stripTags()} VI",
                                "<dark_gray>${slayer.difficulties[it - 1]}",
                                "",
                                *"Maddox doesn't seem to know how to summon <red>this<gray> boss!".reduceToList(20)
                                    .toTypedArray(),
                                "<yellow>Oh... Okay."
                            )
                        else {
                            val buffer = mutableListOf<String>()
                            buffer.add("<dark_gray>" + slayer.difficulties[it - 1])

                            val model = slayer.bossModelForTier(it)
                            buffer.add("")
                            val stats = model.calculateStats()
                            val dmg = stats.damage * (1 + (stats.strength / 100f))
                            buffer.add(" Health: <red>${Statistic.HEALTH.specialChar} ${Formatting.withCommas(stats.health.toBigDecimal())}")
                            buffer.add(
                                " Damage: <red>${Statistic.DAMAGE.specialChar} ${
                                    Formatting.withCommas(
                                        dmg.roundToInt().toBigDecimal()
                                    )
                                }"
                            )
                            buffer.add("")
                            slayer.abilitiesForTier(it).map { abil -> abil.descript(it).map { e -> e.str() } }
                                .forEach { l -> l.forEach { v -> buffer.add(v) }; buffer.add("") }
                            buffer.add("Slayer EXP: <red>${Formatting.stats(rewardExperienceForTier(it).toBigDecimal())} EXP")
                            buffer.add("  <green>And <gradient:gold:yellow>extra fancy<green> drops!")
                            ItemValue.placeholderDescripted(
                                slayer.secondaryItem,
                                "<dark_aqua>${slayer.name.stripTags()} VI",
                                *buffer.toTypedArray()
                            )
                        }
                    else {
                        val buffer = mutableListOf<String>()
                        buffer.add("<dark_gray>" + slayer.difficulties[it - 1])
                        val model = slayer.bossModelForTier(it)
                        buffer.add("")
                        val stats = model.calculateStats()
                        val dmg = stats.damage * (1 + (stats.strength / 100f))
                        buffer.add(" Health: <red>${Formatting.withCommas(stats.health.toBigDecimal())} ${Statistic.HEALTH.specialChar} ")
                        buffer.add(
                            "Damage: <red>${
                                Formatting.withCommas(
                                    dmg.roundToInt().toBigDecimal()
                                )
                            } ${Statistic.DAMAGE.specialChar}"
                        )
                        buffer.add("")
                        slayer.abilitiesForTier(it).map { abil -> abil.descript(it).map { e -> e.str() } }
                            .forEach { l -> l.forEach { v -> buffer.add(v) }; buffer.add("") }
                        buffer.add("")
                        buffer.add("Reward: <light_purple>${Formatting.stats(rewardExperienceForTier(it).toBigDecimal())} ${slayer.entityKind} Slayer EXP")
                        buffer.add("  <dark_gray> + Boss drops")
                        buffer.add("")
                        buffer.add("Cost to start: <gold>${Formatting.stats(costFromTier(it).toBigDecimal())} coins")
                        buffer.add("")
                        if (it >= 5) {
                            if (player.slayers[ty]!!.level < it + 2)
                                buffer.add("<red>Requires ${slayer.name.stripTags()} LVL ${it + 2}")
                        } else
                            buffer.add("<yellow>Click to slay!")
                        ItemValue.placeholderDescripted(
                            if (it < 5) slayer.item else slayer.secondaryItem,
                            "<${colorFromTier(it).asHexString()}>${slayer.name.stripTags()} ${roman(it)}",
                            *buffer.toTypedArray()
                        )
                    }
                },
                { e, tier ->
                    if (tier < 6) {
                        if (tier == 5 && player.slayers[ty]!!.level < 7) {
                            player.sendMessage("<red>You do not meet requirements to start this quest!")
                            e.paper.closeInventory()
                        } else {
                            val cost = costFromTier(tier)
                            if (player.purse < cost.toBigDecimal()) {
                                e.paper.closeInventory()
                                e.player.sendMessage("<red>You don't have enough coins to start this quest!")
                                sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                                    pitch = 0f
                                    playFor(e.paper)
                                }
                            } else {
                                e.instance.switch(confirmationRedirect { p ->
                                    p.purse -= cost.toFloat().toBigDecimal()
                                    p.startSlayerQuest(ty, tier)
                                    p.paper!!.closeInventory()
                                })
                            }
                        }
                    }
                })

            // rewards
            switchUi(
                Slot.RowFiveSlotThree,
                { rewardsMenu(player, ty) },
                ItemValue.placeholderDescripted(
                    Material.GOLD_BLOCK,
                    "<gold>Boss Rewards",
                    "Rewards per level that you",
                    "can get from this slayer.",
                    "",
                    "Your Level: <green>${player.slayers[ty]!!.level}"
                )
            )

            // drops
            switchUi(
                Slot.RowFiveSlotFive,
                { dropsMenu(player, ty) },
                ItemValue.placeholderDescripted(
                    Material.NETHERITE_SCRAP,
                    "<red>Boss Drops",
                    "Drops that you can get",
                    "from this slayer bosses"
                )
            )

            val item = rngMeterButton(player.slayers[ty]!!, ty)
            item.meta {
                val lore = lore()!!
                lore.add(Component.empty())
                lore.add(text("<yellow>Click to select!").noitalic())
                lore(lore)
            }

            switchUi(Slot.RowFiveSlotSeven, { rngMeter(player, player.slayers[ty]!!, ty) }, item)

            goBack(Slot.RowSixSlotOne, { slayerChooseMenu(player) })
        }
    }
