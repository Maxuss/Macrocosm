package space.maxus.macrocosm.collections.ui

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.openGUI
import org.bukkit.Material
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.collections.CollectionSection
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.progressBar
import space.maxus.macrocosm.text.text
import java.time.Duration
import java.util.*

private val sectionSlots = arrayOf(
    Slots.RowFourSlotThree,
    Slots.RowFourSlotFour,
    Slots.RowFourSlotFive,
    Slots.RowFourSlotSix,
    Slots.RowFourSlotSeven,
    Slots.RowThreeSlotThree
)

fun collectionUi(player: MacrocosmPlayer) = kSpigotGUI(GUIType.SIX_BY_NINE) {
    defaultPage = 0
    title = text("Collection")

    val allCollections = player.collections.colls.keys
    val unlockedCollections = allCollections.filter { player.collections[it] > 0 }
    val maxedCollections = allCollections.filter { player.collections.isMaxLevel(it) }

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        pageChanger(Slots.RowSixSlotFive, ItemValue.placeholderDescripted(
            Material.PAINTING,
            "<green>Collection",
            "View all of the items available",
            "in Macrocosm. Collect more of an",
            "item to unlock rewards on your",
            "way to becoming a master of",
            "Macroocsm!",
            "",
            *(if(unlockedCollections.size == allCollections.size)
                buildBar(allCollections.size, maxedCollections.size).toList().toTypedArray()
            else
                buildBar(allCollections.size, unlockedCollections.size, "Collection Unlocked").toList().toTypedArray()),
            "",
            "<yellow>Click to show rankings!"
        ), 1, null, null)

        CollectionSection.values().forEachIndexed { i, section ->
            val allRelated = allCollections.filter { it.inst.section == section }
            val unlocked = allRelated.filter { player.collections[it] > 0 }

            button(sectionSlots[i], ItemValue.placeholderDescripted(
                section.mat,
                "<aqua>${section.name.capitalized()} Collection",
                "View your ${section.name.capitalized()} Collection!",
                "",
                *(if (unlocked.size == allRelated.size)
                    buildBar(allRelated.size, allRelated.count { player.collections.isMaxLevel(it) }).toList().toTypedArray()
                else
                    buildBar(allRelated.size, unlocked.size, "Collection Unlocked").toList().toTypedArray()),
                "",
                "<yellow>Click to view!"
            )) {
                it.bukkitEvent.isCancelled = true
                it.player.openGUI(sectionUi(player, section, allRelated, unlocked))
            }
        }

        button(Slots.RowOneSlotFive, ItemValue.placeholder(Material.BARRIER, "<red>Close")) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.closeInventory()
        }
    }

    page(1) {
        // rankings
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        pageChanger(Slots.RowSixSlotFive, ItemValue.placeholderDescripted(
            Material.PAINTING,
            "<green>Collection",
            "View all of the items available",
            "in Macrocosm. Collect more of an",
            "item to unlock rewards on your",
            "way to becoming a master of",
            "Macrocosm!",
            "",
            *(if(unlockedCollections.size == allCollections.size)
                buildBar(allCollections.size, maxedCollections.size).toList().toTypedArray()
            else
                buildBar(allCollections.size, unlockedCollections.size, "Collection Unlocked").toList().toTypedArray()),
            "",
            "<yellow>Click to hide rankings!"
        ), 0, null, null)

        CollectionSection.values().forEachIndexed { i, section ->
            val allRelated = allCollections.filter { it.inst.section == section }
            val unlocked = allRelated.filter { player.collections[it] > 0 }

            button(sectionSlots[i], ItemValue.placeholderDescriptedGlow(
                section.mat,
                "<aqua>${section.name.capitalized()} Collection",
                "View your ${section.name.capitalized()} Collection!",
                "",
                *(if (unlocked.size == allRelated.size)
                    buildBar(allRelated.size, allRelated.count { player.collections.isMaxLevel(it) }).toList().toTypedArray()
                else
                    buildBar(allRelated.size, unlocked.size, "Collection Unlocked").toList().toTypedArray()),
                "",
                *aggregateRankings(player, allRelated),
                "",
                "<dark_gray>Rankings take ~10 minutes to refresh.",
                "<dark_gray>Requires 25k in collection to be ranked.",
                "",
                "<yellow>Click to view!"
            )) {
                it.bukkitEvent.isCancelled = true
                it.player.openGUI(sectionUi(player, section, allRelated, unlocked))
            }
        }

        button(Slots.RowOneSlotFive, ItemValue.placeholder(Material.BARRIER, "<red>Close")) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.closeInventory()
        }
    }
}

private val rankingsCached: Cache<CollectionType, List<UUID>> = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build()

private fun aggregateRankings(player: MacrocosmPlayer, types: List<CollectionType>): Array<String> {
    return types.map {
        if(player.collections.colls[it]!!.total < 25_000)
            // locked collections display N/A
            return@map "${it.inst.name}: <dark_gray>N/A"
        val amount = player.collections.colls[it]!!.total
        val globalRankings = rankingsCached.get(it) {
            Macrocosm.playersLazy.map { playerId ->
                MacrocosmPlayer.loadPlayer(playerId)!!
            }.filter { each -> each.collections.colls[it]!!.total >= 25_000 }.sortedByDescending { each -> each.collections.colls[it]!!.total }.map { each -> each.ref }
        }
        val position = globalRankings.indexOf(player.ref)
        if(globalRankings.size > 100) {
            // calculate percentile
            val percentile = (position.toDouble() / globalRankings.size) * 100
            "${it.inst.name}: <green>${Formatting.withCommas(amount.toBigDecimal(), true)} <gray>(Top ${if(percentile > 1) "<yellow>" else "<green>"}${Formatting.withCommas(percentile.toBigDecimal(), scale = 2)}%<gray>)"
        } else {
            "${it.inst.name}: <green>${Formatting.withCommas(amount.toBigDecimal(), true)} <gray>(<green>#${position + 1}<gray>)"
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
