package space.maxus.macrocosm.item

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ability.MacrocosmAbility
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.events.CostCompileEvent
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.spell.Spell
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.getId
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.general.putId

class SpellScroll : AbstractMacrocosmItem(id("spell_scroll"), ItemType.SCROLL) {
    override val base: Material = Material.PAPER
    var spell: Spell? = null
        set(value) {
            field = value
            name = text(value?.name ?: "Empty Spell Scroll")
            rarity = value?.rarity ?: rarity
        }
    override var rarity: Rarity = spell?.rarity ?: Rarity.COMMON
    override var name: Component = text(spell?.name ?: "Empty Spell Scroll")

    override fun addExtraNbt(cmp: CompoundTag) {
        cmp.putId("Spell", if (spell == null) Identifier.NULL else Registry.SPELL.byValue(spell!!)!!)
    }

    override fun reforge(ref: Reforge) {
        // this item should not be reforged
    }

    override fun buildLore(player: MacrocosmPlayer?, lore: MutableList<Component>) {
        val sp = spell
        if (sp == null) {
            lore.addAll(
                listOf(
                    "This is an empty Spell Scroll, apply",
                    "a spell to it using the Fusion Table!"
                ).map { text("<dark_gray>$it").noitalic() }
            )
        } else {
            lore.add(text("<gold>Spell: ${sp.name} <yellow><bold>RIGHT CLICK").noitalic())
            val tmp = mutableListOf<Component>()
            for (part in MacrocosmAbility.formatDamageNumbers(sp.description, player).split("<br>")) {
                tmp.addAll(part.reduceToList(31).filter { t -> !t.isBlank() }
                    .map { t -> text("<gray>$t").noitalic() })
            }
            lore.addAll(tmp)
            lore.add(text("<dark_gray>Spell Level: <dark_purple>${sp.requiredKnowledge}").noitalic())
            val event = CostCompileEvent(player, this, sp.cost.copy())
            if (player != null)
                event.callEvent()
            event.cost?.buildLore(lore)
            lore.add("".toComponent())
        }
    }

    override fun clone(): MacrocosmItem {
        val clone = SpellScroll()
        clone.rarity = spell?.rarity ?: rarity
        clone.rarityUpgraded = rarityUpgraded
        clone.spell = spell
        clone.name = text(spell?.name ?: "Empty Spell Scroll")
        return clone
    }

    override fun convert(from: ItemStack, nbt: CompoundTag): MacrocosmItem {
        val base = super.convert(from, nbt) as SpellScroll
        val spId = nbt.getId("Spell")
        if (spId.isNotNull())
            base.spell = Registry.SPELL.find(spId)
        return base
    }
}
