package space.maxus.macrocosm.ability.types.accessory

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.players.macrocosm
import kotlin.math.roundToInt

object ExperienceAbility: AccessoryAbility("experience_artifact", "Increases the experience orbs you gain by <green>25%<gray> while in your <blue>Accessory Bag<gray>.") {
    override fun registerListeners() {
        listen<PlayerPickupExperienceEvent> { e ->
            if(!hasAccs(e.player.macrocosm ?: return@listen))
                return@listen
            e.experienceOrb.experience = (e.experienceOrb.experience * 1.25f).roundToInt()
        }
    }
}
