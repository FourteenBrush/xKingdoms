package me.fourteendoggo.xkingdoms.storage.persistence;

import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.storage.persistence.impl.H2PersistenceHandler;
import me.fourteendoggo.xkingdoms.storage.persistence.impl.JsonPersistenceHandler;
import me.fourteendoggo.xkingdoms.storage.persistence.impl.MySQLPersistenceHandler;

import java.util.function.Function;

public enum StorageType {
    H2("H2 embedded database", H2PersistenceHandler::new),
    MySQL("MySQL database", MySQLPersistenceHandler::new),
    JSON("JSON file based storage", JsonPersistenceHandler::new);

    private static XKingdoms plugin; // initialized when calling ::parse
    private final String description;
    private final Function<XKingdoms, PersistenceHandler> persistenceFunction;

    StorageType(String description, Function<XKingdoms, PersistenceHandler> persistenceFunction) {
        this.description = description;
        this.persistenceFunction = persistenceFunction;
    }

    public String getDescription() {
        return description;
    }

    public PersistenceHandler getPersistenceHandler() {
        return persistenceFunction.apply(plugin);
    }

    public static StorageType parse(XKingdoms plugin) {
        StorageType.plugin = plugin; // hacky way to avoid taking a XKingdoms parameter in the getPersistenceHandler method
        String wantedType = plugin.getConfig().getString("storage.type");
        for (StorageType type : values()) {
            if (type.name().equalsIgnoreCase(wantedType)) {
                return type;
            }
        }
        return JSON; // most stable and fast option in my eyes
    }
}