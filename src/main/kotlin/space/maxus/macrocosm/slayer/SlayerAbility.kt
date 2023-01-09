package space.maxus.macrocosm.slayer

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.text.text
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class SlayerAbility(
    val abilityId: String,
    val slayerType: SlayerType,
    val name: String,
    val description: String,
    val listenerRegister: SlayerAbility.() -> Unit
) {
    companion object {
        val bosses: ConcurrentHashMap<SlayerType, ConcurrentLinkedQueue<UUID>> = ConcurrentHashMap(EnumMap(SlayerType::class.java))
    }

    fun descript(tier: Int): List<Component> {
        val regexed = "\\[[\\d./]*]".toRegex().replace(description) {
            it.value.replace("[", "").replace("]", "").split("/")[tier - 1]
        }
        val output = mutableListOf<Component>()
        output.add(text(name))
        for (part in regexed.split("<br>")) {
            val reduced = part.reduceToList(30).map { text("<gray>$it").noitalic() }.toMutableList()
            reduced.removeIf { it.toLegacyString().isBlank() }
            output.addAll(reduced)
        }
        return output
    }

    fun applyToBosses(handler: (MacrocosmEntity, LivingEntity, Int) -> Unit) {
        bosses[slayerType]?.forEach { boss ->
            val entity = Bukkit.getEntity(boss) as? LivingEntity
            if (entity == null || entity.isDead) {
                bosses[slayerType]!!.remove(boss)
                return@forEach
            }
            val slayer = entity.macrocosm!!
            val id = slayer.getId(entity).path
            val tier = Integer.valueOf(id.replace("${slayerType.name.lowercase()}_", ""))
            handler(slayer, entity, tier)
        }
    }

    fun isSlayerBoss(entity: LivingEntity): Pair<Boolean, Int> {
        val mc = entity.macrocosm ?: return Pair(false, -1)
        val id = mc.getId(entity).path
        if (!id.contains(slayerType.name.lowercase()))
            return Pair(false, -1)
        val tier = try {
            Integer.valueOf(id.replace("${slayerType.name.lowercase()}_", ""))
        } catch (e: NumberFormatException) {
            return Pair(false, -1)
        }
        return Pair(true, tier)
    }
}
