package space.maxus.macrocosm.slayer

import net.axay.kspigot.extensions.pluginKey
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDeathEvent
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.slayer.ui.slayerLevelBuff
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.id
import java.util.*
import kotlin.math.roundToInt

object SlayerHandlers : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onKill(e: PlayerKillEntityEvent) {
        val quest = e.player.slayerQuest ?: return
        val type = e.killed.type

        if (!quest.type.slayer.validEntities.contains(type))
            return
        val newExp = quest.collectedExp + e.experience
        if (newExp >= quest.type.slayer.requiredExp[quest.tier - 1] && quest.status == SlayerStatus.COLLECT_EXPERIENCE) {
            // spawning boss and changing status to kill the boss
            val newQuest = SlayerQuest(quest.type, quest.tier, newExp.toFloat(), SlayerStatus.SLAY_BOSS)
            e.player.updateSlayerQuest(newQuest)
            newQuest.summonBoss(e.player.paper!!)
            return
        } else if (quest.status == SlayerStatus.COLLECT_EXPERIENCE) {
            // otherwise giving combat experience and updating display
            val newQuest = SlayerQuest(quest.type, quest.tier, newExp.toFloat(), quest.status)
            e.player.updateSlayerQuest(newQuest)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDeath(e: PlayerDeathEvent) {
        if (e.player.boundSlayerBoss == null || e.player.slayerQuest == null || e.isCancelled)
            return
        val quest = e.player.slayerQuest!!
        if (quest.status != SlayerStatus.SLAY_BOSS)
            return
        Bukkit.getEntity(e.player.boundSlayerBoss!!)?.remove()

        e.player.sendMessage("<red><bold>SLAYER QUEST FAILED! YOU DIED!")
        e.player.sendMessage("<gray>Good luck next time!")

        e.player.updateSlayerQuest(SlayerQuest(quest.type, quest.tier, quest.collectedExp, SlayerStatus.FAIL))
    }

    @EventHandler
    fun onKillBoss(e: PlayerKillEntityEvent) {
        val nbt = e.killed.persistentDataContainer
        if (!nbt.has(pluginKey("SUMMONER")))
            return
        val spawnedPlayer = UUID.fromString(nbt.get(pluginKey("SUMMONER"), PersistentDataType.STRING))
        val player = Bukkit.getPlayer(spawnedPlayer)?.macrocosm ?: return
        val quest = player.slayerQuest ?: return
        if (quest.status != SlayerStatus.SLAY_BOSS)
            return

        val mc = e.killed.macrocosm!!
        val id = mc.getId(e.killed)
        if (id.path != "${quest.type.slayer.id}_${quest.tier}")
            return

        // player has killed the boss, update status and give experience
        val newQuest = SlayerQuest(quest.type, quest.tier, quest.collectedExp, SlayerStatus.SUCCESS)
        player.updateSlayerQuest(newQuest)
        player.boundSlayerBoss = null

        val slayerId = id(quest.type.name.lowercase())
        if (quest.tier == 6 && !player.memory.tier6Slayers.contains(slayerId)) {
            player.memory.tier6Slayers.add(slayerId)
        }

        player.sendMessage("<gold><bold>NICE! SLAYER BOSS SLAIN!")
        val currentLevel = player.slayers[quest.type]!!
        val expForTier = rewardExperienceForTier(quest.tier)
        val newExp = currentLevel.overflow + expForTier

        // RNG Meter handlers
        val rngStatus = currentLevel.rng[quest.type]!!
        val selectedDrop = quest.type.slayer.drops[rngStatus.selectedRngDrop]
        var accumulated = rngStatus.expAccumulated + expForTier
        val oldChance = selectedDrop.drop.chance
        val expToDrop = (((1 / oldChance) * 900) * slayerLevelBuff[currentLevel.level]).roundToInt()
        if (accumulated >= expToDrop) {
            selectedDrop.drop.dropRngesusReward(player)
            accumulated = expForTier
        }
        if (SlayerTable.shouldLevelUp(currentLevel.level, newExp, .0)) {
            player.slayers[quest.type] =
                SlayerLevel(currentLevel.level + 1, .0, currentLevel.collectedRewards, currentLevel.rng)
            player.sendMessage(
                "<green><bold>LVL UP! <dark_purple>▶ </bold><yellow>${
                    quest.type.name.replace("_", " ").capitalized()
                } LVL ${currentLevel.level + 1}!"
            )
            player.sendMessage("<green><bold>REWARDS AVAILABLE")
            val rewards = quest.type.slayer.rewards[currentLevel.level]
            for (reward in rewards.rewards) {
                player.paper!!.sendMessage(
                    Component.text("+").color(NamedTextColor.DARK_GRAY).append(reward.display(currentLevel.level))
                )
            }
            player.paper!!.sendMessage(
                text("<gold><bold>CLICK TO COLLECT")
                    .hoverEvent(
                        HoverEvent
                            .showText(
                                text("<gold>Rewards for ${quest.type.slayer.name}<gold> LVL ${currentLevel.level + 1}")
                            )
                    )
                    .clickEvent(
                        ClickEvent.runCommand("/slayerrewards ${quest.type.name}")
                    )
            )
        } else {
            player.slayers[quest.type] =
                SlayerLevel(currentLevel.level, newExp, currentLevel.collectedRewards, currentLevel.rng.apply {
                    this[quest.type]!!.expAccumulated = accumulated
                })
            if (currentLevel.level == 9)
                player.sendMessage(
                    "<yellow>${
                        quest.type.name.replace("_", " ").capitalized()
                    } LVL 9<dark_purple> - <green><bold>LVL MAXED OUT!"
                )
            else {
                val requiredExp = SlayerTable.expForLevel(currentLevel.level + 1)

                val needed = requiredExp - currentLevel.overflow
                player.sendMessage(
                    "<yellow>${
                        quest.type.name.replace("_", " ").capitalized()
                    } LVL ${currentLevel.level}<dark_purple> - <gray>Next LVL in <light_purple>${
                        Formatting.stats(
                            needed.toBigDecimal(),
                            true
                        )
                    } XP<gray>!"
                )
            }
        }
        for (i in 0..5) {
            task(delay = i * 2L) {
                sound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP) {
                    pitch = .5f + (i / 10f)
                    playFor(player.paper!!)
                }
            }
        }
    }
}
