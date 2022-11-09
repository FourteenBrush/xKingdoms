package me.fourteendoggo.xkingdoms.inventory;

import me.fourteendoggo.xkingdoms.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class InventoryGui {
    private final Inventory inventory;
    private final Map<Integer, GuiItem> items;
    private final Consumer<InventoryCloseEvent> closeEventConsumer;

    public InventoryGui(String title, int rows) {
        this(title, rows, null);
    }

    public InventoryGui(String title, int rows, Consumer<InventoryCloseEvent> closeEventConsumer) {
        this.inventory = Bukkit.createInventory(null, rows * 9, Utils.colorizeWithHex(title));
        this.items = new HashMap<>();
        this.closeEventConsumer = closeEventConsumer;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setItem(int slot, GuiItem item) {
        items.put(slot, item);
        inventory.setItem(slot, item.getItem());
    }

    public GuiItem getItem(int slot) {
        return items.get(slot);
    }

    public void onClose(InventoryCloseEvent event) {
        if (closeEventConsumer != null) {
            closeEventConsumer.accept(event);
        }
    }
}
