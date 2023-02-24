package space.maxus.macrocosm.collections.ui

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.bukkit.Material
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.collections.CollectionSection
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.progressBar
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import java.time.Duration
import java.util.*

private val sectionSlots = arrayOf(
    Slot.RowThreeSlotThree,
    Slot.RowThreeSlotFour,
    Slot.RowThreeSlotFive,
    Slot.RowThreeSlotSix,
    Slot.RowThreeSlotSeven,
    Slot.RowFourSlotThree
)

fun collUi(player: MacrocosmPlayer): MacrocosmUI = macrocosmUi("collection_main", UIDimensions.SIX_X_NINE) {
    title = "Collection"

    val allCollections = player.collections.colls.keys
    val unlockedCollections = allCollections.filter { player.collections[it] > 0 }
    val maxedCollections = allCollections.filter { player.collections.isMaxLevel(it) }

    background()

    switchUi(Slot.RowOneSlotFive, { collRankings(player) }, ItemValue.placeholderDescripted(
        Material.PAINTING,
        "<green>Collection",
        "View all of the items available",
        "in Macrocosm. Collect more of an",
        "item to unlock rewards on your",
        "way to becoming a master of",
        "Macroocsm!",
        "",
        *(if (unlockedCollections.size == allCollections.size)
            buildBar(allCollections.size, maxedCollections.size).toList().toTypedArray()
        else
            buildBar(allCollections.size, unlockedCollections.size, "Collection Unlocked").toList()
                .toTypedArray()),
        "",
        "<yellow>Click to show rankings!"
    ))

    CollectionSection.values().forEachIndexed { i, section ->
        val allRelated = allCollections.filter { it.inst.section == section }
        val unlocked = allRelated.filter { player.collections[it] > 0 }

        switchUi(
            sectionSlots[i],
            { sectionUi(player, section, allRelated, unlocked) },
            ItemValue.placeholderDescripted(
                section.mat,
                "<aqua>${section.name.capitalized()} Collection",
                "View your ${section.name.capitalized()} Collection!",
                "",
                *(if (unlocked.size == allRelated.size)
                    buildBar(allRelated.size, allRelated.count { player.collections.isMaxLevel(it) }).toList()
                        .toTypedArray()
                else
                    buildBar(allRelated.size, unlocked.size, "Collection Unlocked").toList().toTypedArray()),
                "",
                "<yellow>Click to view!"
            )
        )
    }

    close()
}

fun collRankings(player: MacrocosmPlayer) = macrocosmUi("collection_rankings", UIDimensions.SIX_X_NINE) {
    title = "Collection Rankings"

    val allCollections = player.collections.colls.keys
    val unlockedCollections = allCollections.filter { player.collections[it] > 0 }
    val maxedCollections = allCollections.filter { player.collections.isMaxLevel(it) }

    background()

    switchUi(
        Slot.RowOneSlotFive,
        { collUi(player) },
        ItemValue.placeholderDescripted(
            Material.PAINTING,
            "<green>Collection",
            "View all of the items available",
            "in Macrocosm. Collect more of an",
            "item to unlock rewards on your",
            "way to becoming a master of",
            "Macrocosm!",
            "",
            *(if (unlockedCollections.size == allCollections.size)
                buildBar(allCollections.size, maxedCollections.size).toList().toTypedArray()
            else
                buildBar(allCollections.size, unlockedCollections.size, "Collection Unlocked").toList()
                    .toTypedArray()),
            "",
            "<yellow>Click to hide rankings!"
        )
    )

    CollectionSection.values().forEachIndexed { i, section ->
        val allRelated = allCollections.filter { it.inst.section == section }
        val unlocked = allRelated.filter { player.collections[it] > 0 }

        switchUi(
            sectionSlots[i],
            { sectionUi(player, section, allRelated, unlocked) },
            ItemValue.placeholderDescriptedGlow(
                section.mat,
                "<aqua>${section.name.capitalized()} Collection",
                "View your ${section.name.capitalized()} Collection!",
                "",
                *(if (unlocked.size == allRelated.size)
                    buildBar(allRelated.size, allRelated.count { player.collections.isMaxLevel(it) }).toList()
                        .toTypedArray()
                else
                    buildBar(allRelated.size, unlocked.size, "Collection Unlocked").toList().toTypedArray()),
                "",
                *aggregateRankings(player, allRelated),
                "",
                "<dark_gray>Rankings take ~10 minutes to refresh.",
                "<dark_gray>Requires 25k in collection to be ranked.",
                "",
                "<yellow>Click to view!"
            )
        )
    }

    close()
}

private val rankingsCached: Cache<CollectionType, List<UUID>> =
    CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build()

private fun aggregateRankings(player: MacrocosmPlayer, types: List<CollectionType>): Array<String> {
    return types.map {
        if (player.collections.colls[it]!!.total < 25_000)
        // locked collections display N/A
            return@map "${it.inst.name}: <dark_gray>N/A"
        val amount = player.collections.colls[it]!!.total
        val globalRankings = rankingsCached.get(it) {
            Macrocosm.playersLazy.map { playerId ->
                MacrocosmPlayer.loadPlayer(playerId)!!
            }.filter { each -> each.collections.colls[it]!!.total >= 25_000 }
                .sortedByDescending { each -> each.collections.colls[it]!!.total }.map { each -> each.ref }
        }
        val position = globalRankings.indexOf(player.ref)
        if (globalRankings.size > 100) {
            // calculate percentile
            val percentile = (position.toDouble() / globalRankings.size) * 100
            "${it.inst.name}: <green>${
                Formatting.withCommas(
                    amount.toBigDecimal(),
                    true
                )
            } <gray>(Top ${if (percentile > 1) "<yellow>" else "<green>"}${
                Formatting.withCommas(
                    percentile.toBigDecimal(),
                    scale = 2
                )
            }%<gray>)"
        } else {
            "${it.inst.name}: <green>${
                Formatting.withCommas(
                    amount.toBigDecimal(),
                    true
                )
            } <gray>(<green>#${position + 1}<gray>)"
        }
    }.toTypedArray()
}

internal fun buildBar(total: Int, passed: Int, description: String = "Collection Maxed Out"): Pair<String, String> {
    val ratio = passed.toDouble() / total
    return Pair(
        "$description: <yellow>${Formatting.withCommas((ratio * 100.0).toBigDecimal())}<gold>%",
        progressBar(passed, total, scale = 18, showCount = true).toString()
    )
}
