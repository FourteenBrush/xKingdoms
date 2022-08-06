package me.fourteendoggo.xkingdoms.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.entity.Player;

@CommandAlias("skill|skills")
public class SkillCommand extends BaseCommand {

    @Subcommand("status")
    private void onStatus(Player player) {

    }
}
