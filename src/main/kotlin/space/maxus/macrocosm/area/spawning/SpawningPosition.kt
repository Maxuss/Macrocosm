package space.maxus.macrocosm.area.spawning

import net.axay.kspigot.extensions.worlds
import net.minecraft.nbt.CompoundTag
import org.bukkit.Location
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.getId
import space.maxus.macrocosm.util.general.putId

data class SpawningPosition(val location: Location, val entity: Identifier) {
    var counter: Int = 0

    companion object {
        fun read(from: CompoundTag): SpawningPosition {
            val x = from.getInt("X")
            val y = from.getInt("Y")
            val z = from.getInt("Z")
            val entity = from.getId("Entity")
            return SpawningPosition(Location(worlds[0], x.toDouble(), y.toDouble(), z.toDouble()), entity)
        }
    }

    fun spawn() {
        if(location.getNearbyEntities(7.0, 7.0, 7.0).size >= 5 || counter >= 2) {
            // Extra check not to spawn too many entities
            return
        }
        counter += 1
        Registry.ENTITY.find(entity).spawn(location)
    }

    fun killed() {
        if(this.counter > 0)
            this.counter -= 1
    }

    fun save(): CompoundTag {
        val tag = CompoundTag()
        tag.putInt("X", location.x.toInt())
        tag.putInt("Y", location.y.toInt())
        tag.putInt("Z", location.z.toInt())
        tag.putId("Entity", entity)
        return tag
    }
}
