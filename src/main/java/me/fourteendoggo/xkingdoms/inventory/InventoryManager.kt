package me.fourteendoggo.xkingdoms.inventory

import me.fourteendoggo.xkingdoms.XKingdoms
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class InventoryManager(plugin: XKingdoms) : Listener {
    private val inventories = HashMap<Inventory, InventoryGui>()

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    fun registerInventory(gui: InventoryGui) {
        inventories[gui.inventory] = gui
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val gui = inventories[event.view.topInventory] ?: return // do not handle clicks in player inv
        val item = gui.getItem(event.slot) ?: return
        event.isCancelled = true
        item.onClick(event)
    }

    @EventHandler
    fun onClose(event: InventoryCloseEvent) {
        val gui = inventories.remove(event.view.topInventory)
        gui?.onClose(event)
    }
}
