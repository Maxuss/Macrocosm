package space.maxus.macrocosm.pack

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import space.maxus.macrocosm.util.GSON_PRETTY

@JsonAdapter(PackDescription.Serializer::class)
object PackDescription {
    const val PACK_FORMAT = 8
    const val DESCRIPTION = "Macrocosm Resources"

    fun descript(): String {
        return GSON_PRETTY.toJson(this)
    }

    object Serializer: TypeAdapter<PackDescription>() {
        override fun write(out: JsonWriter, value: PackDescription?) {
            if(value == null)
                out.nullValue()
            else {
                out.beginObject()
                out.name("pack")
                out.beginObject()
                out.name("pack_format")
                out.value(value.PACK_FORMAT)
                out.name("description")
                out.value(value.DESCRIPTION)
                out.endObject()
                out.endObject()
            }
        }

        override fun read(`in`: JsonReader?): PackDescription {
            throw java.lang.UnsupportedOperationException("Pack Description can not be deserialized!")
        }

    }
}
