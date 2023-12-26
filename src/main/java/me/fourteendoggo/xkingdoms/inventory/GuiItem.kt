package me.fourteendoggo.xkingdoms.inventory

import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class GuiItem(val item: ItemStack, private val clickEventConsumer: (InventoryClickEvent) -> Unit) {
    fun onClick(event: InventoryClickEvent) = clickEventConsumer(event)
}
