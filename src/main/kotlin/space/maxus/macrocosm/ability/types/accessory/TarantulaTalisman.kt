package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.damage.DamageKind
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.data.MutableContainer

object TarantulaTalisman: AccessoryAbility("tarantula_talisman", "Every <green>10th<gray> melee hit on the same enemy deals <red>+25% ${Statistic.DAMAGE.display}<gray>.") {
    private val hits = MutableContainer.empty<Int>()

    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if(!hasAccs(e.player) || e.kind != DamageKind.MELEE)
                return@listen
            hits.setOrTakeMut(e.damaged.uniqueId) { v ->
                val value = (v ?: 0) + 1
                if(value >= 10) {
                    e.damage *= 1.25f
                    0
                } else {
                    value
                }
            }
        }
    }
}

object GlacierTarantulaTalisman: AccessoryAbility("glacier_tarantula_talisman", "Every <green>8th<gray> melee hit on the same enemy deals <red>+40% ${Statistic.DAMAGE.display}<gray>.") {
    private val hits = MutableContainer.empty<Int>()

    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if(!hasAccs(e.player) || e.kind != DamageKind.MELEE)
                return@listen
            hits.setOrTakeMut(e.damaged.uniqueId) { v ->
                val value = (v ?: 0) + 1
                if(value >= 8) {
                    e.damage *= 1.40f
                    0
                } else {
                    value
                }
            }
        }
    }
}
