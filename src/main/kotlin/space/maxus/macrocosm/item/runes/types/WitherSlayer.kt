package space.maxus.macrocosm.item.runes.types

import net.axay.kspigot.event.listen
import net.kyori.adventure.text.format.TextColor
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.runes.RuneSpec
import space.maxus.macrocosm.item.runes.RuneType
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.runNTimes
import java.util.*
import kotlin.math.pow

object FlameboundRune : RuneType {
    override val id: Identifier = id("flamebound_rune")
    override val spec: RuneSpec = RuneSpec.OFFENSIVE
    override val display: String = "\uD83D\uDD25"
    override val color: TextColor = TextColor.color(0xD27000)
    override val headSkin: String =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTkwY2JkNzJlNDFhOWJkNDExYmU5MjliNzNmZDI2OTIwNjM2OGIyODEwZDZjNjgxOTkxOGNiOGViNjYyMjRmNCJ9fX0="

    private val entities: MutableList<UUID> = mutableListOf()

    override fun register() {
        listen<PlayerDealDamageEvent> { e ->
            val tier = runeTier(e.player.mainHand ?: return@listen)
            if (tier <= 0)
                return@listen

            if (entities.contains(e.damaged.uniqueId))
                return@listen

            entities.add(e.damaged.uniqueId)

            val mc = e.damaged.macrocosm
            e.damaged.isVisualFire = true
            val dmg = 45f.pow(tier) / (5f.pow(tier + 1))
            // 2sec
            runNTimes(4, 10, {
                entities.remove(e.damaged.uniqueId)
                e.damaged.isVisualFire = false
            }) {
                mc?.damage(dmg)
                DamageHandlers.summonDamageIndicator(e.damaged.location, dmg, DamageType.FIRE)
            }
        }
    }

    override fun descript(): String {
        return "It is said, that when <yellow>prepared<gray> and <yellow>applied<gray> properly, this rune will allow user to <gold>Burn Enemies<gray>."
    }
}
