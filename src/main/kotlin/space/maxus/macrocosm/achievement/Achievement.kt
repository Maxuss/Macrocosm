package space.maxus.macrocosm.achievement

import net.axay.kspigot.particles.particle
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identified
import space.maxus.macrocosm.registry.Identifier

/**
 * A single achievement that can be awarded to players
 */
class Achievement(
    /**
     * ID of this achievement
     */
    id: String,
    /**
     * Display name of this achievement
     */
    val name: String,
    /**
     * Description of this achievement
     */
    val description: String,
    /**
     * Amount of EXP that will be awarded for this achievement
     */
    val expAwarded: Int = 10,
    /**
     * Rarity of this achievement
     */
    val rarity: AchievementRarity = AchievementRarity.BASIC
) : Identified {
    override val id: Identifier = Identifier.parse(id)

    /**
     * Builds a display item for this achievement
     */
    fun buildItem(player: MacrocosmPlayer): ItemStack {
        val isUnlocked = player.achievements.contains(id)
        val mat = if (isUnlocked) rarity.openDisplay else rarity.closedDisplay
        val description = arrayOf(
            "<dark_gray>${rarity.name.capitalized()} Achievement",
            "",
            *(if (!isUnlocked && rarity.ordinal >= 2) arrayOf("<yellow>???") else description.reduceToList()
                .toTypedArray()),
            "",
            *(if (isUnlocked) arrayOf("<aqua>+${expAwarded} Achievement EXP") else arrayOf()),
            if (isUnlocked) "<green><bold>ACHIEVED!" else "<red>LOCKED"
        )
        val name = if (isUnlocked) "<green>$name" else "<yellow>$name"
        return if (rarity.hasGlint && isUnlocked) ItemValue.placeholderDescriptedGlow(mat, name, *description)
        else ItemValue.placeholderDescripted(mat, name, *description)
    }

    /**
     * Sends an award message to player
     */
    fun award(to: MacrocosmPlayer) {
        val message = when (rarity) {
            AchievementRarity.BASIC -> "<gold><obfuscated><bold>a</bold></obfuscated> >>><green> Achievement unlocked! <yellow>$name</yellow> <gold><<< <obfuscated><bold>a</bold></obfuscated>"
            AchievementRarity.RARE -> "<gradient:gold:yellow:white><obfuscated><bold>a</bold></obfuscated> >>><green> Achievement unlocked! <gold>$name</gold> <gradient:white:yellow:gold><<< <obfuscated><bold>a</bold></obfuscated>"
            AchievementRarity.EPIC -> "<gradient:red:gold:yellow:white><obfuscated><bold>a</bold></obfuscated> >>><yellow> Epic achievement unlocked! <red>$name</red> <gradient:white:yellow:gold:red><<< <obfuscated><bold>a</bold></obfuscated>"
            AchievementRarity.MACROCOSMIC -> "<gradient:dark_purple:blue:light_purple><obfuscated><bold>a</bold></obfuscated> >>><#8834ef> Macrocosmic goal: <gradient:#a96bf4:#c9a6f4>$name</gradient> <gradient:light_purple:blue:dark_purple><<< <obfuscated><bold>a</bold></obfuscated>"
        }
        to.sendMessage(message)
        val p = to.paper!!
        sound(if (rarity.ordinal >= 2) Sound.UI_TOAST_CHALLENGE_COMPLETE else Sound.ENTITY_PLAYER_LEVELUP) {
            volume = 10f
            pitch = if (rarity.ordinal >= 2) 1f else 0f
            playAt(p.location)
        }
        when (rarity) {
            AchievementRarity.EPIC -> {
                particle(Particle.END_ROD) {
                    amount = 50
                    offset = Vector.getRandom()
                    extra = 0.3
                    spawnAt(p.eyeLocation)
                }
                particle(Particle.TOTEM) {
                    amount = 50
                    offset = Vector.getRandom()
                    extra = 0.3
                    spawnAt(p.eyeLocation)
                }
            }

            AchievementRarity.MACROCOSMIC -> {
                particle(Particle.FIREWORKS_SPARK) {
                    amount = 50
                    offset = Vector.getRandom()
                    extra = 0.3
                    spawnAt(p.eyeLocation)
                }
                particle(Particle.END_ROD) {
                    amount = 50
                    offset = Vector.getRandom()
                    extra = 0.3
                    spawnAt(p.eyeLocation)
                }
                particle(Particle.REDSTONE) {
                    amount = 50
                    offset = Vector.getRandom()
                    data = DustOptions(Color.fromRGB(0x974bf4), 1.9f)
                    extra = 0.3
                    spawnAt(p.eyeLocation)
                }
            }

            else -> {
                particle(Particle.TOTEM) {
                    amount = 50
                    offset = Vector.getRandom()
                    extra = 0.3
                    spawnAt(p.eyeLocation)
                }
            }
        }
    }
}
