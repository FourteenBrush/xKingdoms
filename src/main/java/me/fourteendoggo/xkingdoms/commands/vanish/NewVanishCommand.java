package me.fourteendoggo.xkingdoms.commands.vanish;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import me.fourteendoggo.xkingdoms.utils.Constants;
import org.bukkit.entity.Player;

@CommandAlias("vanish|v")
@CommandPermission(Constants.MODERATOR_PERMISSION_STRING)
public class NewVanishCommand extends BaseCommand {
    private final VanishManager vanishManager;

    public NewVanishCommand(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    /**
     * /vanish
     * /vanish on
     * /vanish off
     * /vanish on <player>
     * /vanish off <player>
     * /vanish fakejoin
     * /vanish fakequit
     */

    @Default
    private void onToggleVanish(Player player) {
        vanishManager.toggle(player);
    }
}
