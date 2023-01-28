package space.maxus.macrocosm.data.level

import net.minecraft.nbt.CompoundTag

abstract class LevelDbAdapter(val name: String) {
    abstract fun save(to: CompoundTag)
    abstract fun load(from: CompoundTag)
}
