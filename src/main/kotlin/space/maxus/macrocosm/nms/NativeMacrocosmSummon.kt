package space.maxus.macrocosm.nms

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.OwnableEntity
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.registry.Identifier
import java.util.*

interface NativeMacrocosmSummon : OwnableEntity, NativeMacrocosmEntity {
    override val id: Identifier
    val owner: UUID

    override fun getOwner(): LivingEntity? {
        return (Bukkit.getPlayer(owner) as? CraftPlayer)?.handle
    }

    override fun getOwnerUUID(): UUID? {
        return owner
    }

    companion object {
        fun summon(entity: LivingEntity, at: Location, mc: MacrocosmEntity) {
            entity.setPos(Vec3(at.x, at.y, at.z))
            mc.loadChanges(entity.bukkitLivingEntity)
            (at.world as CraftWorld).handle.addFreshEntity(entity)
        }
    }
}
