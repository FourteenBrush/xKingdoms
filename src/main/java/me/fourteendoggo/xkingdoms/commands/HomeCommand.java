package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Values;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.player.PlayerData;
import me.fourteendoggo.xkingdoms.utils.Constants;
import me.fourteendoggo.xkingdoms.utils.Home;
import me.fourteendoggo.xkingdoms.lang.LangKey;
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
        int homesLimit = player.hasPermission(Constants.MODERATOR_PERMISSION_STRING) ? 5 : 2;
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
    }

    @CommandAlias("delhome")
    @CommandCompletion("@homes")
    @Description("Deletes the home with the entered name")
    private void onDeleteHome(Player player, @Values("@homes") String name) {
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
        data.getHomes().values().forEach(home -> builder.append("\n&6  %s: [x: %s, y: %s, z: %s]".formatted(
                home.name(),
                home.location().getBlockX(),
                home.location().getBlockY(),
                home.location().getBlockZ()
        )));
        player.sendMessage(Utils.colorize(builder.toString()));
    }
}
