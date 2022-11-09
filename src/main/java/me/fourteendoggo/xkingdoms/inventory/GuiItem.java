package me.fourteendoggo.xkingdoms.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class GuiItem {
    private final ItemStack itemStack;
    private final Consumer<InventoryClickEvent> clickEventConsumer;

    public GuiItem(ItemStack itemStack, Consumer<InventoryClickEvent> clickEventConsumer) {
        this.itemStack = itemStack;
        this.clickEventConsumer = clickEventConsumer;
    }

    public ItemStack getItem() {
        return itemStack;
    }

    public void onClick(InventoryClickEvent event) {
        if (clickEventConsumer != null) {
            clickEventConsumer.accept(event);
        }
    }
}
