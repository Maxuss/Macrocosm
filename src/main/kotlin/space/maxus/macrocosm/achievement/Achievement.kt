package space.maxus.macrocosm.achievement

data class Achievement(
    val name: String,
    val expAwarded: Int = 10,
    val rarity: AchievementRarity = AchievementRarity.BASIC
) {

}
