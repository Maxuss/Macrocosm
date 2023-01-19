package space.maxus.macrocosm.mongo

interface MongoConvert<O> {
    val mongo: O
}

interface MongoRepr<A> {
    val actual: A
}
