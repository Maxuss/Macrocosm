package space.maxus.macrocosm.pets

import net.axay.kspigot.particles.particle
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.util.Vector
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.util.multimap

interface LazyEffects {
    fun spawnParticles(at: Location, tier: Rarity)
}

interface LazyParticle {
    fun spawn(at: Location)
}

data class DefaultLazyParticle(
    private val particle: Particle,
    private val amount: Int = 1,
    private val offset: Vector? = null
) : LazyParticle {
    override fun spawn(at: Location) {
        particle(this.particle) {
            this.amount = this@DefaultLazyParticle.amount
            this.offset = this@DefaultLazyParticle.offset ?: Vector.getRandom()
            spawnAt(at)
        }
    }
}

data class BlockLazyParticle(
    private val block: Material,
    private val amount: Int = 1,
    private val offset: Vector? = null
) : LazyParticle {
    private val cachedData = block.createBlockData()

    override fun spawn(at: Location) {
        particle(Particle.BLOCK_CRACK) {
            this.amount = this@BlockLazyParticle.amount
            this.data = cachedData
            this.offset = this@BlockLazyParticle.offset ?: Vector.getRandom()
            spawnAt(at)
        }
    }
}

data class DustLazyParticle(
    private val color: Int,
    private val size: Float = 1f,
    private val amount: Int = 1,
    private val offset: Vector? = null
) : LazyParticle {
    private val cachedData = DustOptions(Color.fromRGB(color), size)

    override fun spawn(at: Location) {
        particle(Particle.REDSTONE) {
            this.amount = this@DustLazyParticle.amount
            this.data = cachedData
            this.offset = this@DustLazyParticle.offset ?: Vector.getRandom()
            spawnAt(at)
        }
    }
}

@JvmInline
value class FixedLazyEffects(val particles: List<LazyParticle>) : LazyEffects {
    override fun spawnParticles(at: Location, tier: Rarity) {
        for (particle in particles) {
            particle.spawn(at)
        }
    }
}

class TieredLazyEffects(vararg particles: Pair<Rarity, List<LazyParticle>>) : LazyEffects {
    private val particleMap = multimap<Rarity, LazyParticle>()

    init {
        for ((rarity, effects) in particles) {
            particleMap.putAll(rarity, effects)
        }
    }

    override fun spawnParticles(at: Location, tier: Rarity) {
        particleMap[tier].forEach {
            it.spawn(at)
        }
    }
}
