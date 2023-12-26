package me.fourteendoggo.xkingdoms.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import me.fourteendoggo.xkingdoms.XKingdoms
import me.fourteendoggo.xkingdoms.lang.LangKey
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.persistence.PersistentDataType

@CommandAlias("itemlock|lockitem")
class ItemLockCommand(private val plugin: XKingdoms) : BaseCommand(), Listener {
    private val itemLockKey = NamespacedKey(plugin, "item-lock")

    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)
    }

    @Default
    @Description("Adds a lock to the item in your main hand so you can't longer drop it by accident")
    private fun onItemLock(player: Player) {
        val itemInHand = player.inventory.itemInMainHand
        if (itemInHand.type.isAir) return
        val itemMeta = itemInHand.itemMeta ?: return

        val pdc = itemMeta.persistentDataContainer
        if (pdc.has(itemLockKey, PersistentDataType.BYTE)) {
            pdc.remove(itemLockKey)
            player.sendMessage(plugin.getLang(LangKey.ITEM_LOCK_REMOVED))
        } else {
            pdc.set(itemLockKey, PersistentDataType.BYTE, 1) // byte value doesn't really matter
            player.sendMessage(plugin.getLang(LangKey.ITEM_LOCK_PLACED))
        }
        itemInHand.setItemMeta(itemMeta)
    }

    @EventHandler(ignoreCancelled = true)
    fun onItemDrop(event: PlayerDropItemEvent) {
        val itemStack = event.itemDrop.itemStack
        val meta = itemStack.itemMeta ?: return
        val pdc = meta.persistentDataContainer

        if (pdc.has(itemLockKey, PersistentDataType.BYTE)) {
            event.isCancelled = true
            event.player.sendMessage(plugin.getLang(LangKey.ITEM_LOCK_CANNOT_DROP_ITEM))
        }
    }
}
