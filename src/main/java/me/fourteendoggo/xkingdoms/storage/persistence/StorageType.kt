package me.fourteendoggo.xkingdoms.storage.persistence

import me.fourteendoggo.xkingdoms.XKingdoms
import me.fourteendoggo.xkingdoms.storage.persistence.impl.JsonPersistenceHandler
import me.fourteendoggo.xkingdoms.storage.persistence.impl.SqlitePersistenceHandler
import java.util.function.Function

enum class StorageType(val description: String, private val persistenceFunction: (XKingdoms) -> PersistenceHandler) {
    SQLITE("Sqlite database", ::SqlitePersistenceHandler),
    JSON("JSON file based storage", ::JsonPersistenceHandler);

    fun getPersistenceHandler(plugin: XKingdoms) = persistenceFunction(plugin)

    companion object {
        fun parse(plugin: XKingdoms, fallback: StorageType): StorageType {
            val wantedType = plugin.config.getString("storage.type")
            return entries.find { it.name.equals(wantedType, ignoreCase = true) } ?: fallback
        }
    }
}