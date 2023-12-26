package me.fourteendoggo.xkingdoms.inventory

import me.fourteendoggo.xkingdoms.utils.Utils
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class InventoryGui(title: String, rows: Int, private val closeEventConsumer: (InventoryCloseEvent) -> Unit = {}) {
    val inventory: Inventory
    private val items = HashMap<Int, GuiItem>()

    init {
        inventory = Bukkit.createInventory(null, rows * 9, Utils.colorizeWithHex(title))
    }

    fun setItem(slot: Int, item: GuiItem) {
        inventory.setItem(slot, item.item)
        items[slot] = item
    }

    fun getItem(slot: Int) = items[slot]

    fun onClose(event: InventoryCloseEvent) = closeEventConsumer(event)
}
