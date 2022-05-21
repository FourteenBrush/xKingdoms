package me.fourteendoggo.xkingdoms.storage;

import me.fourteendoggo.xkingdoms.Xkingdoms;
import me.fourteendoggo.xkingdoms.storage.storage.H2Storage;

public enum StorageType {
    H2("H2 database in embedded mode", plugin -> new H2Storage());

    private final String description;
    private final StorageSupplier storageSupplier;

    StorageType(String description, StorageSupplier storageSupplier) {
        this.description = description;
        this.storageSupplier = storageSupplier;
    }

    public String getDescription() {
        return this.description;
    }

    public Storage getStorage(Xkingdoms plugin) {
        return this.storageSupplier.get(plugin);
    }

    public static StorageType fromString(String str, StorageType def) {
        for (StorageType t : values()) {
            if (t.name().equalsIgnoreCase(str)) {
                return t;
            }
        }
        return def;
    }

    @FunctionalInterface
    private interface StorageSupplier {

        Storage get(Xkingdoms plugin);
    }
}
