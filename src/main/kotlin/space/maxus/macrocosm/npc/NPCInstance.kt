package space.maxus.macrocosm.npc

import net.minecraft.nbt.CompoundTag
import org.bukkit.Bukkit
import org.bukkit.Location
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.getId
import space.maxus.macrocosm.util.general.putId
import java.util.*

/**
 * An instance of a summoned NPC, also stored in the LevelDB file
 */
data class NPCInstance(val location: Location, val kind: Identifier) {
    companion object {
        fun read(from: CompoundTag): NPCInstance {
            val x = from.getInt("X")
            val y = from.getInt("Y")
            val z = from.getInt("Z")
            val kind = from.getId("Kind")
            return NPCInstance(Location(Bukkit.getWorlds().first(), x.toDouble(), y.toDouble(), z.toDouble()), kind)
        }
    }

    fun save(): CompoundTag {
        val cmp = CompoundTag()
        cmp.putInt("X", location.x.toInt())
        cmp.putInt("Y", location.y.toInt())
        cmp.putInt("Z", location.z.toInt())
        cmp.putId("Kind", kind)
        return cmp
    }

    fun summon(): UUID {
        val model = Registry.NPC.find(kind)
        val instance = model.summon(location)
        return instance.uuid
    }
}
