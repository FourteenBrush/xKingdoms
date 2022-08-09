package me.fourteendoggo.xkingdoms.storage.persistence;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.storage.persistence.impl.H2PersistenceHandler;
import me.fourteendoggo.xkingdoms.storage.persistence.impl.JsonPersistenceHandler;
import me.fourteendoggo.xkingdoms.storage.persistence.impl.MySQLPersistenceHandler;
import me.fourteendoggo.xkingdoms.storage.persistence.impl.SqlitePersistenceHandler;

import java.util.function.Function;

public enum StorageType {
    H2("H2 embedded database", H2PersistenceHandler::new),
    MYSQL("MySQL database", MySQLPersistenceHandler::new),
    SQLITE("Sqlite database", SqlitePersistenceHandler::new),
    JSON("JSON file based storage", JsonPersistenceHandler::new);
    private final String description;
    private final Function<XKingdoms, PersistenceHandler> persistenceFunction;

    StorageType(String description, Function<XKingdoms, PersistenceHandler> persistenceFunction) {
        this.description = description;
        this.persistenceFunction = persistenceFunction;
    }

    public String getDescription() {
        return description;
    }

    public PersistenceHandler getPersistenceHandler(XKingdoms plugin) {
        return persistenceFunction.apply(plugin);
    }

    public static StorageType parse(XKingdoms plugin) {
        String wantedType = plugin.getConfig().getString("storage.type");
        for (StorageType type : values()) {
            if (type.name().equalsIgnoreCase(wantedType)) {
                return type;
            }
        }
        return SQLITE; // most stable and fast option in my eyes
    }
}