package space.maxus.macrocosm.ability.types

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.entity.Arrow
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerRightClickEvent
import kotlin.math.roundToLong

object ShortbowAbility: AbilityBase(AbilityType.RIGHT_CLICK, "Shortbow", "Instantly shoots!") {
    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if(e.player.onAtsCooldown)
                return@listen
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen
            val proj = e.player.paper!!.launchProjectile(Arrow::class.java, e.player.paper!!.eyeLocation.direction.multiply(2.0))
            e.player.onAtsCooldown = true
            // 0.45s is default attack speed, becomes 0.2s at 100 attack speed
            taskRunLater((((1.2 - (e.player.stats()!!.attackSpeed / 100f))) * 9f).roundToLong()) {
                e.player.onAtsCooldown = false
            }

            proj.persistentDataContainer.set(pluginKey("despawn_me"), PersistentDataType.BYTE, 1)
        }
    }
}
