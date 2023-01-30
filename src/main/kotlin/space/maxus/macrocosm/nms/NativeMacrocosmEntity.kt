package space.maxus.macrocosm.nms

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.registry.Identified
import space.maxus.macrocosm.registry.Identifier

interface NativeMacrocosmEntity: Identified {
    override val id: Identifier

    companion object {
        fun summon(entity: LivingEntity, at: Location, mc: MacrocosmEntity) {
            entity.setPos(Vec3(at.x, at.y, at.z))
            mc.loadChanges(entity.bukkitLivingEntity)
            (at.world as CraftWorld).handle.addFreshEntity(entity)
        }
    }
}
