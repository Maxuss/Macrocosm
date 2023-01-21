package space.maxus.macrocosm.mongo

/**
 * Represents an object that can be converted to MongoDB data representation
 */
interface MongoConvert<O> {
    /**
     * Gets MongoDB representation of this object
     */
    val mongo: O
}

/**
 * Represents an object that can be converted from MongoDB representation to actual data representation
 */
interface MongoRepr<A> {
    /**
     * Gets actual representation of this object
     */
    val actual: A
}
