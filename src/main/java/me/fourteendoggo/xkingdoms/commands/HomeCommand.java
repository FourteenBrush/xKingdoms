package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.inventory.GuiItem;
import me.fourteendoggo.xkingdoms.inventory.InventoryGui;
import me.fourteendoggo.xkingdoms.inventory.ItemBuilder;
import me.fourteendoggo.xkingdoms.lang.LangKey;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.player.PlayerData;
import me.fourteendoggo.xkingdoms.utils.Constants;
import me.fourteendoggo.xkingdoms.utils.Home;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class HomeCommand extends BaseCommand {
    private final XKingdoms plugin;

    public HomeCommand(XKingdoms plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("sethome")
    @Description("Creates a home at your current location")
    private void onSetHome(Player player, KingdomPlayer kPlayer, String name) {
        int homesLimit = player.hasPermission(Constants.MODERATOR_PERMISSION_STRING) ? 5 : 2;
        PlayerData data = kPlayer.getData();

        if (data.getHomes().size() >= homesLimit) {
            player.sendMessage(plugin.getLang(LangKey.HOME_LIMIT_REACHED));
            return;
        }
        if (!data.addHome(new Home(name, player.getUniqueId(), player.getLocation()))) {
            player.sendMessage(plugin.getLang(LangKey.HOME_ALREADY_EXISTS));
            return;
        }

        player.sendMessage(plugin.getLang(LangKey.HOME_CREATED, name));
    }

    @CommandAlias("delhome")
    @Description("Deletes the given home")
    private void onDeleteHome(Player player, Home home) {
        if (home == null) {
            player.sendMessage(plugin.getLang(LangKey.HOME_NOT_FOUND));
        } else {
            CompletableFuture<Void> future = plugin.getStorage().deleteHome(home);
            future.thenRun(() -> player.sendMessage(plugin.getLang(LangKey.HOME_REMOVED)));
        }
    }

    @CommandAlias("homes")
    @Description("Shows all the homes that you have")
    private void onHomesList(Player sender) {
        KingdomPlayer kPlayer = plugin.getUserManager().getUser(sender.getUniqueId());
        PlayerData playerdata = kPlayer.getData();

        if (playerdata.getHomes().isEmpty()) {
            sender.sendMessage(plugin.getLang(LangKey.HOME_NO_HOMES_CREATED));
            return;
        }

        InventoryGui gui = new InventoryGui("#F28C28Homes", 5);
        plugin.getInventoryManager().registerInventory(gui);

        int slot = 0;
        for (Home home : playerdata.getHomes().values()) {
            Material material = Material.DIAMOND;
            Consumer<InventoryClickEvent> clickEventConsumer = null;
            List<String> lore = new ArrayList<>(Arrays.asList(
                    "&6x: %s, y: %s, z: %s".formatted(
                            home.location().getBlockX(),
                            home.location().getBlockY(),
                            home.location().getBlockZ()),
                    ""));

            if (home.location().isWorldLoaded()) {
                lore.add("&7Click to teleport to this home");
                clickEventConsumer = event -> {
                    sender.teleport(home.location());
                    sender.sendMessage(plugin.getLang(LangKey.HOME_TELEPORTED, home.name()));
                };
            } else {
                material = Material.BARRIER;
                lore.add("&cCannot teleport to a home in an unloaded world");
            }

            ItemStack item = new ItemBuilder(material)
                    .setDisplayName(ChatColor.GOLD + home.name())
                    .setLore(lore)
                    .build();

            gui.setItem(slot++, new GuiItem(item, clickEventConsumer));
        }

        sender.openInventory(gui.getInventory());
    }
}
