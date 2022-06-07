package space.maxus.macrocosm.slayer

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.axay.kspigot.extensions.worlds
import net.kyori.adventure.text.Component
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.entity.readNbt
import space.maxus.macrocosm.item.MACROCOSM_TAG
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.getId

class SlayerAbility(
    val abilityId: String,
    val slayerId: String,
    val name: String,
    val description: String,
    val listenerRegister: SlayerAbility.() -> Unit
) {
    fun descript(tier: Int): List<Component> {
        val regexed = "\\[[\\d./]*]".toRegex().replace(description) {
            it.value.replace("[", "").replace("]", "").split("/")[tier - 1]
        }
        val reduced = regexed.reduceToList(30).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        reduced.add(0, comp(name))
        return reduced
    }

    inline fun applyToBosses(handler: (MacrocosmEntity, LivingEntity, Int) -> Unit) {
        for(world in worlds) {
            for (entity in world.entities.parallelStream()) {
                val (ok, level) = ensureBoss(entity)
                if (!ok)
                    continue
                val living = entity as LivingEntity
                val mc = living.macrocosm!!
                if(!living.isDead && mc.currentHealth > 0f)
                    handler(living.macrocosm!!, living, level)
            }
        }
    }

    fun ensureBoss(entity: Entity): Pair<Boolean, Int> {
        if(entity !is LivingEntity || entity is ArmorStand || entity is Player)
            return Pair(false, -1)
        val mc = entity.readNbt()
        if(mc.contains(MACROCOSM_TAG)) {
            val id = mc.getCompound(MACROCOSM_TAG).getId("ID")
            if(!id.path.contains(this.slayerId))
                return Pair(false, -1)
            return Pair(true, Integer.parseInt(id.path.replace("${this.slayerId}_", "")))
        }
        return Pair(false, -1)
    }
}
