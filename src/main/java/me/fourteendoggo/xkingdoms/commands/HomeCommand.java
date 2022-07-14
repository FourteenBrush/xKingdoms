package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.player.PlayerData;
import me.fourteendoggo.xkingdoms.utils.Home;
import me.fourteendoggo.xkingdoms.utils.LangKey;
import me.fourteendoggo.xkingdoms.utils.Permissions;
import me.fourteendoggo.xkingdoms.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HomeCommand extends BaseCommand {
    private final XKingdoms plugin;

    public HomeCommand(XKingdoms plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("sethome")
    @Description("Creates a home at your current location")
    private void onSetHome(Player player, String name) {
        int homesLimit = player.hasPermission(Permissions.MODERATOR) ? 5 : 2;
        KingdomPlayer kPlayer = plugin.getUserRepository().get(player.getUniqueId());
        PlayerData data = kPlayer.getData();

        if (data.getHomes().size() >= homesLimit) {
            player.sendMessage(plugin.getLang(LangKey.HOME_LIMIT_REACHED));
            return;
        }
        if (data.hasHome(name)) {
            player.sendMessage(plugin.getLang(LangKey.HOME_ALREADY_EXISTS));
            return;
        }
        Home home = new Home(name, player.getUniqueId(), player.getLocation());
        data.addHome(home);
        player.sendMessage(plugin.getLang(LangKey.HOME_CREATED, name));
        // save the homes when saving the whole user object periodically
    }

    @CommandAlias("delhome")
    @CommandCompletion("@homes")
    @Description("Deletes the home with the entered name")
    private void onDeleteHome(Player player, String name) {
        Bukkit.broadcastMessage("deleted home " + name);
    }

    // TODO maybe a gui mode, maybe toggleable?
    @CommandAlias("homes")
    @Description("Shows all the homes that you have")
    private void onHomesList(Player player) {
        KingdomPlayer kPlayer = plugin.getUserRepository().get(player.getUniqueId());
        PlayerData data = kPlayer.getData();

        if (data.getHomes().isEmpty()) {
            player.sendMessage(plugin.getLang(LangKey.HOME_NO_HOMES_CREATED));
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("&e------------ &7[&eHomes&7] &e------------\n&7Below is a list of all your homes:");
        data.getHomes().forEach((name, home) -> {
            String formatted = "\\n&6  %s: [x: %s, y: %s, z: %s]".formatted(
                    name,
                    home.location().getBlockX(),
                    home.location().getBlockY(),
                    home.location().getBlockZ()
            );
            builder.append(formatted);
        });
        player.sendMessage(Utils.colorize(builder.toString()));
    }
}
