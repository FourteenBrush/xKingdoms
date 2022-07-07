package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import me.fourteendoggo.xkingdoms.XKingdoms;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.player.PlayerData;
import me.fourteendoggo.xkingdoms.utils.Home;
import me.fourteendoggo.xkingdoms.utils.LangKey;
import me.fourteendoggo.xkingdoms.utils.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class HomeCommand extends BaseCommand {
    private final XKingdoms plugin;

    public HomeCommand(XKingdoms plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("sethome")
    @Description("Creates a home at your current location if you haven't exceeded the limit yet")
    private void onSetHome(Player player, String name) {
        int homesLimit = player.hasPermission(Permissions.MODERATOR) ? 5 : 2;
        KingdomPlayer user = plugin.getUserRepository().get(player.getUniqueId());
        PlayerData userdata = user.getData();

        if (userdata.getHomes().size() >= homesLimit) {
            player.sendMessage(plugin.getLang(LangKey.HOME_LIMIT_REACHED));
            return;
        }
        if (userdata.hasHome(name)) {
            player.sendMessage(plugin.getLang(LangKey.HOME_ALREADY_EXISTS));
            return;
        }
        Home home = new Home(name, player.getUniqueId(), player.getLocation());
        userdata.addHome(home);
        // save the homes when saving the whole user object
        player.sendMessage();
    }

    @CommandAlias("delhome")
    @Description("Deletes the home with the entered name")
    private void onDeleteHome(Player player, String name) {
        Bukkit.broadcastMessage("deleted home " + name);
    }
}
