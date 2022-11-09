package me.fourteendoggo.xkingdoms.inventory;

import me.fourteendoggo.xkingdoms.XKingdoms;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class InventoryManager implements Listener {
    private final Map<Inventory, InventoryGui> inventories = new HashMap<>();

    public InventoryManager(XKingdoms plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void registerInventory(InventoryGui gui) {
        inventories.put(gui.getInventory(), gui);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryGui gui = inventories.get(event.getView().getTopInventory()); // do not handle clicks in player inv
        if (gui == null) return;

        GuiItem item = gui.getItem(event.getSlot());
        if (item == null) return;

        item.onClick(event);
        event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        InventoryGui gui = inventories.remove(event.getView().getTopInventory());
        if (gui != null) {
            gui.onClose(event);
        }
    }
}
