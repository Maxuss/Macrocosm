package space.maxus.macrocosm.item

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.kyori.adventure.text.Component
import org.bukkit.Material
import space.maxus.macrocosm.cosmetic.Dye
import space.maxus.macrocosm.cosmetic.SkullSkin
import space.maxus.macrocosm.item.buffs.MinorItemBuff
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.item.runes.RuneState
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.RegistryPointer
import space.maxus.macrocosm.registry.registryPointer
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.stats

abstract class AbstractMacrocosmItem(override val id: Identifier, override val type: ItemType) : MacrocosmItem {
    override var stats = stats { }
    override var specialStats = SpecialStatistics()
    override var amount = 1
    override var stars = 0
    override var rarityUpgraded = false
    override val abilities: MutableList<RegistryPointer> = mutableListOf()
    override var enchantments: HashMap<Identifier, Int> = hashMapOf()
    override val runes: Multimap<RuneSlot, RuneState> = HashMultimap.create()
    override val buffs: HashMap<MinorItemBuff, Int> = hashMapOf()
    override var breakingPower: Int = 0
    override var dye: Dye? by registryPointer(Identifier.macro("cosmetic"))
    override var skin: SkullSkin? by registryPointer(Identifier.macro("cosmetic"))
    override var reforge: Reforge? by registryPointer(Identifier.macro("reforge"))

    abstract override val base: Material
    abstract override var name: Component
    abstract override var rarity: Rarity
}
