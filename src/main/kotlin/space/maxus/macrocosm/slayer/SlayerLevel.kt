package space.maxus.macrocosm.slayer

import java.io.Serializable

class SlayerLevel(val level: Int, val overflow: Double, val collectedRewards: List<Int>, val rngMeter: Double): Serializable
