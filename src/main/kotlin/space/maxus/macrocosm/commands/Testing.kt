package space.maxus.macrocosm.commands

import com.mojang.brigadier.arguments.StringArgumentType
import net.axay.kspigot.commands.argument
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.runs
import net.axay.kspigot.commands.suggestList
import space.maxus.macrocosm.collections.CollectionType
import space.maxus.macrocosm.entity.EntityValue
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.pets.PetValue
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.recipes.RecipeMenu
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.util.id

fun testStatsCommand() = command("stats") {
    runs {
        for (comp in player.macrocosm?.stats()?.formatFancy()!!) {
            player.sendMessage(comp)
        }
    }
}

fun testPetsCommand() = command("spawnpet") {
    runs {
        val key = player.macrocosm!!.addPet(id("pickle_pet"), Rarity.LEGENDARY, 100)
        PetValue.PICKLE_PET.pet.spawn(player.macrocosm!!, key)
    }
}

fun testGivePetCommand() = command("givepet") {
    runs {
        val key = player.macrocosm!!.addPet(id("pickle_pet"), Rarity.LEGENDARY, 100)
        val item = PetValue.PICKLE_PET.pet.buildItem(player.macrocosm!!, player.macrocosm!!.ownedPets[key]!!)
        player.inventory.addItem(item)
    }
}

fun testLevelUp() = command("skillup") {
    argument("skill", StringArgumentType.word()) {
        suggestList {
            SkillType.values().filter { sk -> sk.name.contains(it.getArgumentOrNull<String>("skill") ?: "") }
        }
        runs {
            val sk = SkillType.valueOf(getArgument("skill"))
            player.macrocosm?.sendSkillLevelUp(sk)
        }
    }
}

fun testCollUp() = command("collup") {
    argument("coll", StringArgumentType.word()) {
        suggestList {
            CollectionType.values().filter { sk -> sk.name.contains(it.getArgumentOrNull<String>("coll") ?: "") }
        }
        runs {
            val sk = CollectionType.valueOf(getArgument("coll"))
            player.macrocosm?.sendCollectionLevelUp(sk)
        }
    }
}

fun testCraftingTable() = command("crafting_test") {
    runs {
        this.player.openInventory(RecipeMenu.craftingTable(this.player))
    }
}

fun testEntityCommand() = command("testsummon") {
    runs {
        val e = EntityValue.TEST_ENTITY.entity
        e.spawn(player.location)
    }
}
