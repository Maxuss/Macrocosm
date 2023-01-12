package space.maxus.macrocosm.slayer

import java.io.Serializable

class SlayerLevel(val level: Int, val overflow: Double, val collectedRewards: List<Int>, val rng: HashMap<SlayerType, RngStatus>) :
    Serializable

class RngStatus(var expAccumulated: Double, var selectedRngDrop: Int) : Serializable
