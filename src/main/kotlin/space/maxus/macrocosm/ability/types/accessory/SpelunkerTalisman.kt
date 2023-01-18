package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.task
import org.bukkit.block.data.type.NoteBlock
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.block.MacrocosmBlock
import space.maxus.macrocosm.events.BlockDropItemsEvent
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.Statistic

class SpelunkerTalisman(applicable: String, private val amount: Int) : AccessoryAbility(
    applicable,
    "Increases your ${Statistic.MINING_FORTUNE.display} by <gold>+$amount<gray> when mining rare minerals."
) {
    private val rareMinerals = arrayOf(
        "blackstone_adamantine_ore",
        "deepslate_adamantine_ore",
        "titanium_ore",
        "geyserite_ore",
        "lumium_ore"
    ).map(Identifier::macro)

    override fun registerListeners() {
        listen<BlockDropItemsEvent> { e ->
            if (!hasAccs(e.player) || e.block.blockData !is NoteBlock)
                return@listen
            val block = MacrocosmBlock.fromBlockData(e.block.blockData as NoteBlock) ?: return@listen
            if (!rareMinerals.contains(block.id))
                return@listen
            e.player.tempStats.miningFortune += amount
            task(delay = 5L) {
                e.player.tempStats.miningFortune -= amount
            }
        }
    }
}
