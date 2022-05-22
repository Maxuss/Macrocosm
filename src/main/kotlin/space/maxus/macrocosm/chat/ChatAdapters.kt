package space.maxus.macrocosm.chat

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

object ComponentTypeAdapter: TypeAdapter<Component>() {
    override fun write(o: JsonWriter, value: Component?) {
        if(value == null)
            o.nullValue()
        else
            o.value(MiniMessage.miniMessage().serialize(value))
    }

    override fun read(i: JsonReader): Component {
        return MiniMessage.miniMessage().deserialize(i.nextString())
    }
}
