package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.entity.EntityType
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.events.ItemCalculateStatsEvent
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.item.KillStorageItem
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text
import java.util.*

private fun EntityType.pretty(): String {
    return when (this) {
        EntityType.ENDERMAN -> "Endermen"
        EntityType.SILVERFISH -> "Silverfish"
        EntityType.DROWNED -> "Drowned"
        else -> "${name.replace("_", " ").capitalized(" <blue>")}s"
    }
}

private fun descriptAbility(family: MutableList<EntityType>): String {
    val str = StringBuilder()
    if (family.size > 1) {
        val last = family.removeLast()
        for (mob in family) {
            str.append("<blue>${mob.pretty()}")
            if (family.last() != mob)
                str.append("<gray>, ")
        }
        str.append("<gray> and <blue>${last.pretty()}<gray>")
        family.add(last)
    } else
        str.append("<blue>${family.last().pretty()}<gray>")
    return str.toString()
}

open class EntityKillCounterBonus(
    id: String,
    name: String,
    private val entities: List<EntityType>,
    private val stat: Statistic,
    private val rewardTable: List<Int> = listOf(
        0,
        10,
        25,
        50,
        100,
        120,
        130,
        150,
        220,
        250,
        300
    ),
    private val table: TreeMap<Int, Int> = TreeMap(
        hashMapOf(
            0 to 0,
            10 to 1,
            100 to 2,
            250 to 3,
            500 to 4,
            1000 to 5,
            2500 to 6,
            5000 to 7,
            8000 to 8,
            12000 to 9,
            15000 to 10
        )
    )
) : AbilityBase(AbilityType.PASSIVE, name, "") {
    override val id: Identifier = Identifier.parse(id)

    open fun addLore(item: MacrocosmItem): List<Component> {
        val (index, kills) = counterBuff(item)
        if (index == 8)
            return listOf(
                text("<gray>Current Bonus: <${stat.color.asHexString()}>${rewardTable[index]} ${stat.display}").noitalic(),
                text("<gray>Next Upgrade: <green><bold>MAXED OUT! NICE!").noitalic()
            )
        val requiredKills = table.keys.toList()[index + 1]
        val nextBonus = rewardTable[index + 1]
        val color = stat.color.asHexString()
        return listOf(
            text("<gray>Current Bonus: <$color>${rewardTable[index]} ${stat.display}").noitalic(),
            text(
                "<gray>Next Upgrade: <$color>${Formatting.stats(nextBonus.toBigDecimal())} ${stat.display} <dark_gray>(<$color><gray>${
                    Formatting.stats(
                        kills.toBigDecimal()
                    )
                }/<red>${Formatting.stats(requiredKills.toBigDecimal())}<dark_gray>)"
            ).noitalic()
        )
    }

    override fun buildLore(lore: MutableList<Component>, player: MacrocosmPlayer?) {
        val str = "Kill ${descriptAbility(entities.toMutableList())} to accumulate ${stat.display}"

        val tmp = mutableListOf<Component>()
        tmp.add(text("<gold>Item Ability: $name").noitalic())
        for (desc in str.reduceToList()) {
            tmp.add(text("<gray>$desc</gray>").noitalic())
        }
        tmp.removeIf {
            ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlank()
        }
        lore.addAll(tmp)
    }

    // index, kills
    protected fun counterBuff(item: MacrocosmItem): Pair<Int, Int> {
        if (item !is KillStorageItem)
            return Pair(0, 0)
        val i = item.kills
        val entry = table.floorEntry(i) ?: return Pair(0, 0)
        return Pair(entry.value, i)
    }

    override fun registerListeners() {
        listen<ItemCalculateStatsEvent> { e ->
            if (!e.item.abilities.any { it.pointer == this.id })
                return@listen
            val (index, _) = counterBuff(e.item)
            e.stats[stat] = e.stats[stat] + rewardTable[index]
        }
        listen<PlayerKillEntityEvent> { e ->
            val p = e.player.paper!!
            if (!entities.contains(e.killed.type))
                return@listen
            for (slot in listOf(
                EquipmentSlot.HAND,
                EquipmentSlot.OFF_HAND,
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
            ).parallelStream()) {
                if (!ensureRequirements(e.player, slot))
                    continue
                val item = p.inventory.getItem(slot).macrocosm!!
                if (item !is KillStorageItem)
                    continue
                item.kills++
                p.inventory.setItem(slot, item.build(e.player)!!)
            }
        }
    }
}
