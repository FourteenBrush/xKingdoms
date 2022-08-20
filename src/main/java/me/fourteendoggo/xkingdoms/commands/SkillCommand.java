package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import me.fourteendoggo.xkingdoms.player.KingdomPlayer;
import me.fourteendoggo.xkingdoms.player.PlayerData;
import me.fourteendoggo.xkingdoms.utils.Utils;
import org.bukkit.entity.Player;

@CommandAlias("skill|skills")
public class SkillCommand extends BaseCommand {

    @Default
    @Description("Shows an overview of your skills progress")
    private void onListSkills(Player player, KingdomPlayer kPlayer) {
        PlayerData playerData = kPlayer.getData();
        StringBuilder builder = new StringBuilder();
        builder.append("&e------------ &7[&eSkills&7] &e------------\n&7Below is a list of all your skills:");

        playerData.getSkills().forEach((type, progress) ->
                builder.append("\n&6  %s: level: %s, xp: %s"
                        .formatted(type.getDisplayName(), progress.getLevel(), progress.getXp())));

        player.sendMessage(Utils.colorize(builder.toString()));
    }
}
