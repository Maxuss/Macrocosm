package space.maxus.macrocosm.item.json

import com.google.gson.JsonObject
import org.bukkit.Material
import space.maxus.macrocosm.accessory.AccessoryItem
import space.maxus.macrocosm.accessory.TexturedAccessoryItem
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.block.Blocks
import space.maxus.macrocosm.chat.capitalized
import space.maxus.macrocosm.generators.*
import space.maxus.macrocosm.item.*
import space.maxus.macrocosm.item.runes.RuneSlot
import space.maxus.macrocosm.recipes.RecipeParser
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.registry.RegistryPointer
import space.maxus.macrocosm.stats.SpecialStatistic
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.util.GSON
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.walkDataResources
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.readText

object ItemParser {
    fun init() {
        Threading.contextBoundedRunAsync(name = "Item Parser") {
            val pool = Threading.newFixedPool(5)
            info("Starting recipe parser...")

            val amount = AtomicInteger(0)
            walkDataResources("data", "items") { file ->
                info("Converting items from ${file.fileName}...")
                val data = GSON.fromJson(file.readText(), JsonObject::class.java)
                for ((key, obj) in data.entrySet()) {
                    pool.execute {
                        val id = Identifier.parse(key)
                        Registry.ITEM.register(id, parseAndPrepare(id, obj.asJsonObject))
                        amount.incrementAndGet()
                    }
                }
            }

            pool.shutdown()
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
            info("Registered ${amount.get()} items from .json definitions!")

            // only starting recipe parser after all items are registered to prevent data racing
            RecipeParser.init()

            // same for blocks
            Blocks.init()
        }
    }

    private fun parseAndPrepare(id: Identifier, obj: JsonObject): MacrocosmItem {
        val type =
            if (obj.has("material")) Material.valueOf(obj["material"].asString.uppercase()) else if(obj.has("model")) Material.PAPER else Material.PLAYER_HEAD
        val headSkin = if (type == Material.PLAYER_HEAD) obj["head_skin"].asString else null
        val rarity = Rarity.valueOf(obj["rarity"].asString.uppercase())
        val name = if (obj.has("name")) obj["name"].asString else id.path.replace("_", " ").capitalized()
        val desc = if (obj.has("description")) obj["description"].asString else null
        val itemType = if (obj.has("type")) ItemType.valueOf(obj["type"].asString.uppercase()) else null

        val base: MacrocosmItem = if (obj.has("reforge")) {
            val reforge = Identifier.parse(obj["reforge"].asString)
            ReforgeStone(
                Registry.REFORGE.find(reforge),
                name,
                rarity,
                headSkin ?: throw AssertionError("Schema expected head skin for reforge item!")
            )
        } else if (!obj.has("abilities")) {
            // parsing recipe item
            RecipeItem(type, rarity, name, headSkin, desc, obj.has("glow") && obj["glow"].asBoolean)
        } else {
            // parsing ability item
            val stats = if (obj.has("stats")) parseStats(obj.get("stats").asJsonObject) else Statistics.zero()
            val abils =
                obj["abilities"].asJsonArray.mapNotNull { ele -> Identifier.parse(ele.asString) }
                    .toMutableList()
            val specialStats =
                if (obj.has("special_stats")) parseSpecialStats(obj.get("special_stats").asJsonObject) else SpecialStatistics()
            val bp = if (obj.has("breaking_power")) obj["breaking_power"].asInt else 0
            val runes = if (obj.has("runes")) obj["runes"].asJsonArray.map { ele ->
                if (ele.isJsonObject) Identifier.parse(ele.asJsonObject["specific"].asString) else Identifier.parse(ele.asString)
            } else listOf()
            if(itemType == ItemType.ACCESSORY) {
                if(obj.has("model")) {
                    TexturedAccessoryItem(
                        id.path,
                        name,
                        rarity,
                        stats,
                        abils.map { RegistryPointer(Identifier.macro("ability"), it) }.toMutableList()
                    )
                } else {
                    AccessoryItem(
                        id.path,
                        name,
                        rarity,
                        stats,
                        abils.map { RegistryPointer(Identifier.macro("ability"), it) }.toMutableList(),
                        headSkin,
                        if(headSkin == null) type else Material.PLAYER_HEAD
                    )

                }
            } else if (headSkin != null)
                SkullAbilityItem(
                    itemType!!,
                    name,
                    rarity,
                    headSkin,
                    stats,
                    abils.map { RegistryPointer(id("ability"), it) }.toMutableList(),
                    specialStats,
                    bp,
                    runes,
                    desc,
                    id = id
                )
            else
                AbilityItem(
                    itemType!!,
                    name,
                    rarity,
                    type,
                    stats,
                    abils.mapNotNull { Registry.ABILITY.findOrNull(it) }.toMutableList(),
                    specialStats,
                    bp,
                    runes.map { RuneSlot.fromId(it) },
                    desc,
                    id = id
                )
        }
        val model = if (obj.has("model")) parseModel(obj["model"].asJsonObject) else null
        val animation = if (obj.has("animation")) parseAnimation(obj["animation"].asJsonObject) else null

        if (model != null) {
            Registry.MODEL_PREDICATES.register(id, model)
            if (animation != null) {
                MetaGenerator.enqueue(
                    "assets/macrocosm/textures/${model.to.replace("macrocosm:", "")}.png",
                    AnimationData(animation)
                )
            }
        }

        return base
    }

    private fun parseModel(obj: JsonObject): Model {
        return if (obj.has("raw") && obj["raw"].asBoolean)
            RawModel(
                obj["id"].asInt,
                if(obj.has("from")) obj["from"].asString else "item/paper",
                obj["to"].asString,
            )
        else
            Model(
                obj["id"].asInt,
                if(obj.has("from")) obj["from"].asString else "item/paper",
                obj["to"].asString,
                if (obj.has("parent"))
                    obj["parent"].asString
                else
                    "item/generated"
            )
    }

    private fun parseAnimation(obj: JsonObject): Animation {
        return if (obj.has("frames")) Animation(
            obj["frames"].asInt,
            if (obj.has("time")) obj["time"].asInt else 2,
            obj.has("interpolate") && obj["interpolate"].asBoolean
        ) else RawAnimation(
            obj["raw_frames"].asJsonArray.map { ele -> ele.asInt },
            if (obj.has("time")) obj["time"].asInt else 2,
            obj.has("interpolate") && obj["interpolate"].asBoolean
        )
    }

    private fun parseSpecialStats(obj: JsonObject): SpecialStatistics {
        val zero = SpecialStatistics()

        for ((key, value) in obj.entrySet()) {
            zero[SpecialStatistic.valueOf(key.uppercase())] = value.asNumber.toFloat()
        }

        return zero

    }

    private fun parseStats(obj: JsonObject): Statistics {
        val zero = Statistics.zero()

        for ((key, value) in obj.entrySet()) {
            val stat = Statistic.valueOf(key.uppercase())
            val v = value.asNumber.toFloat()
            zero[stat] = v
        }

        return zero
    }
}
