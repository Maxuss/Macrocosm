package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.comp

object EmeraldPickaxeBonus : AbilityBase(
    AbilityType.PASSIVE,
    "Emerald Affection",
    "Grants <gold>+10 ${Statistic.MINING_FORTUNE.display}<gray> per <green>5.000 Emerald Collection<gray>."
) {
    override fun buildLore(lore: MutableList<Component>, player: MacrocosmPlayer?) {
        val tmp = mutableListOf<Component>()
        tmp.add(comp("<gold>Item Ability: $name").noitalic())
        for (desc in description.reduceToList()) {
            tmp.add(comp("<gray>$desc</gray>").noitalic())
        }
        tmp.add("".toComponent())
        if (player == null) {
            tmp.add(comp("<gray>Emerald Collection: <green>0").noitalic())
            tmp.add(comp("${Statistic.MINING_FORTUNE.display}<gray> Bonus: <green>0").noitalic())
        } else {
            val coll = player.collections[CollectionType.EMERALD]
            tmp.add(comp("<gray>Emerald Collection: <green>$coll").noitalic())
            val bonus = coll.div(5000) * 10
            tmp.add(comp("${Statistic.MINING_FORTUNE.display}<gray> Bonus: <green>$bonus").noitalic())
        }
        tmp.removeIf {
            ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlankOrEmpty()
        }
        lore.addAll(tmp)
    }

    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen
            val stats = e.stats
            val coll = e.player.collections[CollectionType.EMERALD]
            val bonus = coll.div(5000) * 10
            stats.miningFortune += bonus
            e.stats = stats
        }
    }
}
