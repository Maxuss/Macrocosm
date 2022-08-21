package space.maxus.macrocosm.bazaar

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import space.maxus.macrocosm.registry.Identifier
import java.util.*

object BazaarDataCompoundTypeAdapter: TypeAdapter<BazaarDataCompound>() {
    override fun write(out: JsonWriter, value: BazaarDataCompound?) {
        if(value == null)
            out.nullValue()
        else {
            out.beginArray()
            for(order in value.orders) {
                BazaarOrderTypeAdapter.write(out, order)
            }
            out.endArray()
        }
    }

    override fun read(r: JsonReader): BazaarDataCompound {
        r.beginArray()
        val out = mutableListOf<BazaarOrder>()
        while(r.peek() != JsonToken.END_ARRAY) {
            out.add(BazaarOrderTypeAdapter.read(r))
        }
        r.endArray()
        return BazaarDataCompound(out)
    }

}

object BazaarOrderTypeAdapter: TypeAdapter<BazaarOrder>() {
    override fun write(out: JsonWriter, value: BazaarOrder?) {
        if(value == null)
            out.nullValue()
        else {
            out.beginObject()
            if(value is BazaarBuyOrder) {
                out.name("\$t")
                out.value(0)
                out.name("qty")
                out.value(value.qty)
                out.name("pricePer")
                out.value(value.pricePer)
                out.name("bought")
                out.value(value.bought)
                out.name("sellers")
                out.beginArray()
                for(seller in value.sellers) {
                    out.value(seller.toString())
                }
                out.endArray()
            } else if(value is BazaarSellOrder) {
                out.name("\$t")
                out.value(1)
                out.name("qty")
                out.value(value.qty)
                out.name("pricePer")
                out.value(value.pricePer)
                out.name("sold")
                out.value(value.sold)
                out.name("buyers")
                out.beginArray()
                for(seller in value.buyers) {
                    out.value(seller.toString())
                }
                out.endArray()
            }
            out.name("item")
            out.value(value.item.toString())
            out.name("by")
            out.value(value.createdBy.toString())
            out.name("at")
            out.value(value.createdAt)

            out.endObject()
        }
    }

    override fun read(r: JsonReader): BazaarOrder {
        r.beginObject()
        r.nextName()
        if(r.nextInt() == 0) {
            // buy order
            r.nextName()
            val qty = r.nextInt()
            r.nextName()
            val pricePer = r.nextDouble()
            r.nextName()
            val bought = r.nextInt()
            r.nextName()
            r.beginArray()
            val sellers = mutableListOf<UUID>()
            while(r.peek() != JsonToken.END_ARRAY) {
                sellers.add(UUID.fromString(r.nextString()))
            }
            r.endArray()
            val (item, by, at) = readCommonData(r)
            r.endObject()
            return BazaarBuyOrder(item, qty, pricePer, bought, sellers, by, at)
        } else {
            // sell order
            r.nextName()
            val qty = r.nextInt()
            r.nextName()
            val pricePer = r.nextDouble()
            r.nextName()
            val sold = r.nextInt()
            r.nextName()
            r.beginArray()
            val buyers = mutableListOf<UUID>()
            while(r.peek() != JsonToken.END_ARRAY) {
                buyers.add(UUID.fromString(r.nextString()))
            }
            r.endArray()
            val (item, by, at) = readCommonData(r)
            r.endObject()
            return BazaarSellOrder(item, qty, pricePer, sold, buyers, by, at)
        }
    }

    private fun readCommonData(r: JsonReader): Triple<Identifier, UUID, Long> {
        r.nextName()
        val item = Identifier.parse(r.nextString())
        r.nextName()
        val by = UUID.fromString(r.nextString())
        r.nextName()
        val at = r.nextLong()
        return Triple(item, by, at)
    }
}
