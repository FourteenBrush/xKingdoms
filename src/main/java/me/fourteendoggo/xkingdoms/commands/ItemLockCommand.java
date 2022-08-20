package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.lang.LangKey;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@CommandAlias("itemlock|lockitem")
public class ItemLockCommand extends BaseCommand implements Listener {
    private final XKingdoms plugin;
    private final NamespacedKey itemLockKey;

    public ItemLockCommand(XKingdoms plugin) {
        this.plugin = plugin;
        this.itemLockKey = new NamespacedKey(plugin, "item-lock");
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Default
    @Description("Adds a lock to the item in your main hand so you can't longer drop it by accident")
    private void onItemLock(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType().isAir()) return;

        ItemMeta itemMeta = itemInHand.getItemMeta();
        if (itemMeta == null) return;

        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();
        if (pdc.has(itemLockKey, PersistentDataType.BYTE)) {
            pdc.remove(itemLockKey);
            player.sendMessage(plugin.getLang(LangKey.ITEM_LOCK_REMOVED));
        } else {
            pdc.set(itemLockKey, PersistentDataType.BYTE, (byte) 1); // byte value doesn't really matter
            player.sendMessage(plugin.getLang(LangKey.ITEM_LOCK_PLACED));
        }
        itemInHand.setItemMeta(itemMeta);
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        ItemStack itemStack = event.getItemDrop().getItemStack();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(itemLockKey, PersistentDataType.BYTE)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getLang(LangKey.ITEM_LOCK_CANNOT_DROP_ITEM));
        }
    }
}
