package space.maxus.macrocosm.ability

import net.kyori.adventure.text.Component
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.comp
import java.time.Instant
import java.util.concurrent.TimeUnit

data class AbilityCost(private val mana: Int = 0, private val health: Int = 0, private val cooldown: Int = 0) {
    fun buildLore(lore: MutableList<Component>) {
        if (mana > 0) {
            lore.add(comp("<dark_gray>Mana Cost: <dark_aqua>$mana").noitalic())
        }
        if (health > 0) {
            lore.add(comp("<dark_gray>Health Cost: <dark_red>$health").noitalic())
        }
        if (cooldown > 0) {
            lore.add(comp("<dark_gray>Cooldown: <green>${cooldown}s").noitalic())
        }
    }

    fun ensureRequirements(player: MacrocosmPlayer, ability: String): Boolean {
        if (mana > 0 && player.currentMana < mana) {
            player.paper!!.sendActionBar(comp("<red><bold>NOT ENOUGH MANA"))

            return false
        } else if (mana > 0) {
            player.decreaseMana(mana.toFloat())
        }
        if (health > 0 && player.currentHealth < health) {
            player.paper!!.sendActionBar(comp("<red><bold>NOT ENOUGH HEALTH"))
            return false
        } else if (health > 0) {
            player.damage(health.toFloat(), comp("Cruel Ability"))
        }
        val lastUse = player.lastAbilityUse[ability]
        val cdMillis = TimeUnit.SECONDS.toMillis(cooldown.toLong())
        val now = Instant.now().toEpochMilli()
        if (cooldown > 0 && player.lastAbilityUse.contains(ability) && lastUse!! + cdMillis > now) {
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
        } else if (cooldown > 0) {
            player.lastAbilityUse[ability] = now
        }
        return true
    }
}
