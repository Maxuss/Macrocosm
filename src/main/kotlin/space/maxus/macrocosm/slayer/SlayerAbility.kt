package space.maxus.macrocosm.slayer

import com.google.common.collect.Multimap
import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.multimap
import java.util.*

class SlayerAbility(
    val abilityId: String,
    val slayerType: SlayerType,
    val name: String,
    val description: String,
    val listenerRegister: SlayerAbility.() -> Unit
) {
    companion object {
        val bosses: Multimap<SlayerType, UUID> = multimap()
    }

    fun descript(tier: Int): List<Component> {
        val regexed = "\\[[\\d./]*]".toRegex().replace(description) {
            it.value.replace("[", "").replace("]", "").split("/")[tier - 1]
        }
        val reduced = regexed.reduceToList(30).map { text("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        reduced.add(0, text(name))
        return reduced
    }

    inline fun applyToBosses(handler: (MacrocosmEntity, LivingEntity, Int) -> Unit) {
        for((ty, boss) in bosses.entries().parallelStream()) {
            if(ty == slayerType) {
                val entity = Bukkit.getEntity(boss) as? LivingEntity
                if(entity == null) {
                    bosses.remove(ty, boss)
                    continue
                }
                val slayer = entity.macrocosm!!
                val id = slayer.getId(entity).path
                val tier = Integer.valueOf(id.replace("${slayerType.name.lowercase()}_", ""))
                handler(slayer, entity, tier)
            }
        }
    }
}
