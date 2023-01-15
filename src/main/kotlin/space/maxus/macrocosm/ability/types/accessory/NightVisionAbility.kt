package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.runnables.task
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.AccessoryAbility

object NightVisionAbility: AccessoryAbility("night_vision_charm", "Grants you Night Vision I while in your Accessory Bag.") {
    override fun registerListeners() {
        task(period = 100L) {
            for(player in Macrocosm.loadedPlayers.values) {
                if(hasAccs(player))
                    player.paper?.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 200, 0, true, false, false))
            }
        }
    }
}
