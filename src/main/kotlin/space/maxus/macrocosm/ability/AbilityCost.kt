package space.maxus.macrocosm.ability

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.events.AbilityCostApplyEvent
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.text.comp
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * Cost of the ability in mana, health and cooldown
 *
 * @property mana Required mana
 * @property health Required health
 * @property cooldown Cooldown to wait **in seconds**
 */
data class AbilityCost(val mana: Int = 0, val health: Int = 0, val cooldown: Int = 0) {
    /**
     * Builds this cost lore and inserts it into the provided [lore] list
     *
     * @param lore List to be used for lore insertion
     */
    fun buildLore(lore: MutableList<Component>) {
        if (mana > 0) {
            lore.add(comp("<dark_gray>Mana Cost: <dark_aqua>$mana").noitalic())
        }
        if (health > 0) {
            lore.add(comp("<dark_gray>Health Cost: <red>$health").noitalic())
        }
        if (cooldown > 0) {
            lore.add(comp("<dark_gray>Cooldown: <green>${cooldown}s").noitalic())
        }
    }

    /**
     * Ensures that the provided [player] has requirements to use the ability.
     *
     * @param player Player against which the checks will be done
     * @param ability ID of the ability. This is stored internally to ensure that the cooldown's time elapsed
     * @param silent Whether to loudly send player an action bar message that they do not have enough mana/health
     * @return True if all checks passed, false otherwise
     */
    fun ensureRequirements(player: MacrocosmPlayer, ability: Identifier, silent: Boolean = false): Boolean {
        val event = AbilityCostApplyEvent(player, mana, health, cooldown)
        event.callEvent()

        if (event.mana > 0 && player.currentMana < event.mana) {
            if (!silent)
                player.paper!!.sendActionBar(comp("<red><bold>NOT ENOUGH MANA"))
            return false
        }
        if (event.health > 0 && player.currentHealth < event.health) {
            if (!silent)
                player.paper!!.sendActionBar(comp("<red><bold>NOT ENOUGH HEALTH"))
            return false
        }
        val lastUse = player.lastAbilityUse[ability]

        val cdMillis = TimeUnit.SECONDS.toMillis(event.cooldown.toLong())
        val now = Instant.now().toEpochMilli()
        if (event.cooldown > 0 && player.lastAbilityUse.contains(ability) && lastUse!! + cdMillis > now) {
            if (!silent)
                player.paper!!.sendMessage(
                    comp(
                        "<red>This ability is current on cooldown for ${
                            TimeUnit.MILLISECONDS.toSeconds(
                                (lastUse + cdMillis) - now
                            )
                        }s!"
                    )
                )
            return false
        }

        if (event.mana > 0) {
            player.decreaseMana(event.mana.toFloat())
        }
        if (event.health > 0) {
            player.damage(event.health.toFloat(), comp("Cruel Ability"))
        }
        if (event.cooldown > 0) {
            player.lastAbilityUse[ability] = now
        }

        return true
    }
}
