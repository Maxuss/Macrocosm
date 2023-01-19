package space.maxus.macrocosm.db.mongo

interface MongoConvert<O> {
    val mongo: O
}

interface MongoRepr<A> {
    val actual: A
}
