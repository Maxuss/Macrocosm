package space.maxus.macrocosm.npc.shop

import com.google.gson.JsonObject
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.GSON
import space.maxus.macrocosm.util.typetoken
import space.maxus.macrocosm.util.walkDataResources
import kotlin.io.path.readText

object ShopParser {
    fun init() {
        val pool = Threading.newFixedPool(2)

        walkDataResources("data", "shops") { file ->
            val data = GSON.fromJson(file.readText(), JsonObject::class.java)
            for ((key, obj) in data.entrySet()) {
                pool.execute {
                    val id = Identifier.parse(key)
                    Registry.SHOP.register(id, parseSingleModel(obj.asJsonObject))
                }
            }
        }
    }

    private fun parseSingleModel(obj: JsonObject): ShopModel {
        val name = obj["name"].asString
        val items = obj["items"].asJsonArray.map {
            val each = it.asJsonObject
            val item = Identifier.parse(each["item"].asString)
            val price = each["price"].asNumber
            val amount = if(each.has("amount")) each["amount"].asInt else 1
            val onlyOne = each.has("onlyOne") && each["onlyOne"].asBoolean
            val additionalItems = if(each.has("extra")) GSON.fromJson<HashMap<Identifier, Int>>(each["extra"].asJsonObject, typetoken<HashMap<Identifier, Int>>()) else hashMapOf()
            Purchasable(item, price, amount, additionalItems, onlyOne)
        }
        return ShopModel(name, items)
    }
}
