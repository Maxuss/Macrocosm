package space.maxus.macrocosm.exceptions

class DatabaseException(case: String) : Exception("Error when updating player DB: $case")
