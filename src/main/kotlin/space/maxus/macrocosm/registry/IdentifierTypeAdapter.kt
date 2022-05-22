package space.maxus.macrocosm.registry

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

object IdentifierTypeAdapter: TypeAdapter<Identifier>() {
    override fun write(out: JsonWriter, value: Identifier?) {
        if(value == null)
            out.nullValue()
        else
            out.value(value.toString())
    }

    override fun read(`in`: JsonReader): Identifier {
        return Identifier.parse(`in`.nextString())
    }
}
