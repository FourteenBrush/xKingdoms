package me.fourteendoggo.xkingdoms.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import me.fourteendoggo.xkingdoms.XKingdoms
import me.fourteendoggo.xkingdoms.inventory.GuiItem
import me.fourteendoggo.xkingdoms.inventory.InventoryGui
import me.fourteendoggo.xkingdoms.inventory.ItemBuilder
import me.fourteendoggo.xkingdoms.lang.LangKey
import me.fourteendoggo.xkingdoms.player.KingdomPlayer
import me.fourteendoggo.xkingdoms.utils.Constants
import me.fourteendoggo.xkingdoms.utils.Home
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

@Suppress("UNUSED")
class HomeCommand(private val plugin: XKingdoms) : BaseCommand() {
    @CommandAlias("sethome")
    @Description("Creates a home at your current location")
    private fun onSetHome(player: Player, kPlayer: KingdomPlayer, name: String) {
        // TODO: don't hardcode
        val homesLimit = if (player.hasPermission(Constants.MODERATOR_PERMISSION_STRING)) 5 else 2
        val data = kPlayer.playerData
        if (data.homes.size >= homesLimit) {
            player.sendMessage(plugin.getLang(LangKey.HOME_LIMIT_REACHED))
            return
        }
        if (!data.addHome(Home(name, player.uniqueId, player.location))) {
            player.sendMessage(plugin.getLang(LangKey.HOME_ALREADY_EXISTS))
            return
        }
        player.sendMessage(plugin.getLang(LangKey.HOME_CREATED, name))
    }

    @CommandAlias("delhome")
    @Description("Deletes the given home")
    private fun onDeleteHome(player: Player, home: Home?) {
        if (home == null) {
            player.sendMessage(plugin.getLang(LangKey.HOME_NOT_FOUND))
        } else {
            val future = plugin.storage.deleteHome(home)
            future.thenRun { player.sendMessage(plugin.getLang(LangKey.HOME_REMOVED)) }
        }
    }

    @CommandAlias("homes")
    @Description("Shows all the homes that you have")
    private fun onHomesList(sender: Player) {
        val kPlayer = plugin.userManager.getUser(sender.uniqueId)
        val playerData = kPlayer!!.playerData
        if (playerData.homes.isEmpty()) {
            sender.sendMessage(plugin.getLang(LangKey.HOME_NO_HOMES_CREATED))
            return
        }
        val gui = InventoryGui("#F28C28Homes", 5)
        plugin.inventoryManager.registerInventory(gui)

        for ((slot, home) in playerData.homes.values.withIndex()) {
            var material = Material.DIAMOND
            var clickEventConsumer = { _: InventoryClickEvent -> }
            val lore = home.location.run {
                mutableListOf("&6x: $blockX, y: $blockY, z: $blockZ")
            }

            if (home.location.isWorldLoaded) {
                lore.add("&7Click to teleport to this home")
                clickEventConsumer = {
                    sender.teleport(home.location)
                    sender.sendMessage(plugin.getLang(LangKey.HOME_TELEPORTED, home.name))
                }
            } else {
                material = Material.BARRIER
                lore.add("&cCannot teleport to a home in an unloaded world")
            }

            val item = ItemBuilder(material)
                    .setDisplayName("${ChatColor.GOLD}${home.name}")
                    .setLore(lore)
                    .build()

            gui.setItem(slot, GuiItem(item, clickEventConsumer))
        }
        sender.openInventory(gui.inventory)
    }
}
