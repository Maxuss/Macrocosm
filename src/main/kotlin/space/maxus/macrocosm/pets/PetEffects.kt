package space.maxus.macrocosm.pets

import com.google.common.collect.HashMultimap
import net.axay.kspigot.particles.particle
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.util.Vector
import space.maxus.macrocosm.item.Rarity

interface PetEffects {
    fun spawnParticles(at: Location, tier: Rarity)
}

interface PetParticles {
    fun spawn(at: Location)
}

data class DefaultPetParticle(private val particle: Particle, private val amount: Int = 1, private val offset: Vector? = null): PetParticles {
    override fun spawn(at: Location) {
        particle(this.particle) {
            this.amount = this@DefaultPetParticle.amount
            this.offset = this@DefaultPetParticle.offset ?: Vector.getRandom()
            spawnAt(at)
        }
    }
}

data class BlockPetParticle(private val block: Material, private val amount: Int = 1, private val offset: Vector? = null): PetParticles {
    private val cachedData = block.createBlockData()

    override fun spawn(at: Location) {
        particle(Particle.BLOCK_CRACK) {
            this.amount = this@BlockPetParticle.amount
            this.data = cachedData
            this.offset = this@BlockPetParticle.offset ?: Vector.getRandom()
            spawnAt(at)
        }
    }
}

data class DustPetParticle(private val color: Int, private val size: Float = 1f, private val amount: Int = 1, private val offset: Vector? = null): PetParticles {
    private val cachedData = DustOptions(Color.fromRGB(color), size)

    override fun spawn(at: Location) {
        particle(Particle.REDSTONE) {
            this.amount = this@DustPetParticle.amount
            this.data = cachedData
            this.offset = this@DustPetParticle.offset ?: Vector.getRandom()
            spawnAt(at)
        }
    }
}

@JvmInline
value class FixedPetEffects(val particles: List<PetParticles>): PetEffects {
    override fun spawnParticles(at: Location, tier: Rarity) {
        for(particle in particles) {
            particle.spawn(at)
        }
    }
}

class TieredPetEffects(vararg particles: Pair<Rarity, List<PetParticles>>): PetEffects {
    private val particleMap = HashMultimap.create<Rarity, PetParticles>()

    init {
        for((rarity, effects) in particles) {
            particleMap.putAll(rarity, effects)
        }
    }

    override fun spawnParticles(at: Location, tier: Rarity) {
        particleMap[tier].forEach {
            it.spawn(at)
        }
    }
}
