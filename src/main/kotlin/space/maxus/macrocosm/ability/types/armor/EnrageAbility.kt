package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.data.nbtData
import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack
import org.bukkit.entity.EntityType
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.events.PlayerSneakEvent
import space.maxus.macrocosm.players.isAirOrNull
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.annotations.UnsafeFeature
import space.maxus.macrocosm.util.data.MutableContainer

object EnrageAbility : FullSetBonus(
    "Enrage",
    "Enrage, boosting your stats:<br> ▶ <white>+100 ${Statistic.SPEED.display}<br> ▶ <red>3% ${Statistic.HEALTH.display}/s<br> ▶ <red>-15% ${Statistic.DAMAGE.display}<gray> from <blue>Wither Skeletons<br> ▶ <red>+50% ${Statistic.DAMAGE.display}<gray> against <blue>Wither Skeletons<br>for <green>6 seconds<gray>."
) {
    override val type: AbilityType = AbilityType.SNEAK
    override val cost: AbilityCost = AbilityCost(500, 100, 15)

    private val enabled = MutableContainer.empty<Boolean>()

    override fun registerListeners() {
        listen<PlayerDealDamageEvent>(priority = EventPriority.LOW) { e ->
            enabled.take(e.player.ref) {
                if (e.damaged.type == EntityType.WITHER_SKELETON)
                    e.damage *= 1.5f
            }
        }
        listen<PlayerReceiveDamageEvent>(priority = EventPriority.LOW) { e ->
            enabled.take(e.player.ref) {
                if (e.damager.type == EntityType.WITHER_SKELETON) {
                    e.damage *= .85f
                }
            }
        }
        @OptIn(UnsafeFeature::class)
        listen<PlayerSneakEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen

            if (!cost.ensureRequirements(e.player, id))
                return@listen

            if (e.player.ref in enabled)
                return@listen

            val p = e.player.paper ?: return@listen

            enabled[e.player.ref] = true

            val boots = p.equipment.boots
            val legs = p.equipment.leggings
            val chest = p.equipment.chestplate
            val head = p.equipment.helmet

            color(boots, 0x83D2DA)
            color(legs, 0x50C5D2)
            color(chest, 0x27C3D4)
            var headTag = head.nbtData
            headTag.putString(
                "__TempSkin",
                "ewogICJ0aW1lc3RhbXAiIDogMTY1ODQ3MDg1OTcwMywKICAicHJvZmlsZUlkIiA6ICIxM2U3NjczMGRlNTI0MTk3OTA5YTZkNTBlMGEyMjAzYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJtX3h1cyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82Y2RkZTljYWNkZDFhZWYwODM2ZjRiMjllYWI3NzdlMDI5YTk3YjRjMjRhOTgxY2JjODFjZjEyMjlmMjY3ZWYyIgogICAgfQogIH0KfQ=="
            )
            var nms = (head as CraftItemStack).handle
            nms.tag = headTag

            p.equipment.setBoots(Macrocosm.unsafe.reloadItem(boots, e.player), true)
            p.equipment.setLeggings(Macrocosm.unsafe.reloadItem(legs, e.player), true)
            p.equipment.setChestplate(Macrocosm.unsafe.reloadItem(chest, e.player), true)
            p.equipment.setHelmet(Macrocosm.unsafe.reloadItem(head, e.player), true)

            sound(Sound.ENTITY_WOLF_GROWL) {
                pitch = 0f
                volume = 5f

                playAt(p.location)
            }

            val regen = e.player.stats()!!.health * .03f
            e.player.tempSpecs.extraRegen += regen
            e.player.tempStats.speed += 100

            taskRunLater(20 * 6) {
                enabled.remove(e.player.ref)

                for (slot in listOf(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)) {
                    val item = p.equipment.getItem(slot)
                    if (item.isAirOrNull())
                        continue
                    if (item.itemMeta is LeatherArmorMeta) {
                        removeColor(item)
                    } else {
                        headTag = item.nbtData
                        headTag.remove("__TempSkin")
                        nms = (item as CraftItemStack).handle
                        if (nms == null)
                            continue
                        nms.tag = headTag
                    }
                    p.equipment.setItem(slot, Macrocosm.unsafe.reloadItem(item, e.player) ?: continue)
                }

                e.player.tempSpecs.extraRegen -= regen
                e.player.tempStats.speed -= 100

                sound(Sound.ENTITY_WOLF_DEATH) {
                    pitch = 0f
                    volume = 5f

                    playAt(p.location)
                }
            }
        }
    }

    private fun removeColor(item: ItemStack) {
        val tag = item.nbtData
        tag.remove("__TempColor")
        val craft = (item as CraftItemStack)
        craft.handle.tag = tag
    }

    private fun color(item: ItemStack, color: Int) {
        val tag = item.nbtData
        tag.putInt("__TempColor", color)
        val craft = (item as CraftItemStack)
        craft.handle.tag = tag
    }
}
