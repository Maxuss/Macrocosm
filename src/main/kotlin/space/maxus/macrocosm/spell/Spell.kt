package space.maxus.macrocosm.spell

import net.axay.kspigot.event.listen
import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.SpellScroll
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType

abstract class Spell(val name: String, val description: String, val cost: AbilityCost, val rarity: Rarity, val requiredKnowledge: Int) {
    @Suppress("SameParameterValue")
    protected open fun ensureRequirements(
        player: MacrocosmPlayer
    ): Boolean {
        val item = player.paper!!.inventory.getItem(EquipmentSlot.HAND)
        val mc = item.macrocosm
        if (mc !is SpellScroll || mc.spell != this)
            return false
        if(player.skills.level(SkillType.MYSTICISM) < requiredKnowledge) {
            player.sendMessage("<red>Insufficient Mysticism level! Spell '$name<red>' requires Mysticism $requiredKnowledge!")
            sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                pitch = 0f
                playFor(player.paper ?: return@sound)
            }
            return false
        }
        val success = cost.ensureRequirements(player, Registry.SPELL.byValue(this)!!)
        if (!success) {
            sound(Sound.ENTITY_ENDERMAN_TELEPORT) {
                pitch = 0f
                playFor(player.paper!!)
            }
            return false
        }
        return true
    }

    open fun rightClick(mc: MacrocosmPlayer, p: Player) {
        /* no-op */
    }

    open fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if(!ensureRequirements(e.player))
                return@listen
            rightClick(e.player, e.player.paper ?: return@listen)
        }
    }
}
