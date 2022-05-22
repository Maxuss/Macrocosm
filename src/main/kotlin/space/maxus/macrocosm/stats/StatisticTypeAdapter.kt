package space.maxus.macrocosm.stats

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

object StatisticTypeAdapter : TypeAdapter<Statistics>() {
    override fun write(o: JsonWriter, value: Statistics?) {
        if (value == null)
            o.nullValue()
        else {
            o.beginObject()
            for ((n, v) in value.iter()) {
                if (v == 0f)
                    continue
                o.name(n.name)
                o.value(v)
            }
            o.endObject()
        }
    }

    override fun read(i: JsonReader): Statistics {
        i.beginObject()
        val zero = Statistics.zero()
        while (i.hasNext()) {
            val name = Statistic.valueOf(i.nextName())
            zero[name] = i.nextDouble().toFloat()
        }
        i.endObject()
        return zero
    }

}

object SpecialStatisticTypeAdapter : TypeAdapter<SpecialStatistics>() {
    override fun write(o: JsonWriter, value: SpecialStatistics?) {
        if (value == null)
            o.nullValue()
        else {
            o.beginObject()
            for ((n, v) in value.iter()) {
                if (v == 0f)
                    continue
                o.name(n.name)
                o.value(v)
            }
            o.endObject()
        }
    }

    override fun read(i: JsonReader): SpecialStatistics {
        i.beginObject()
        val zero = SpecialStatistics()
        while (i.hasNext()) {
            val name = SpecialStatistic.valueOf(i.nextName())
            zero[name] = i.nextDouble().toFloat()
        }
        i.endObject()
        return zero
    }
}
